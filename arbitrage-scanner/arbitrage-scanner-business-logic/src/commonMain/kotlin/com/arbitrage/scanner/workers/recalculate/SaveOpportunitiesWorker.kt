package com.arbitrage.scanner.workers.recalculate

import com.arbitrage.scanner.BusinessLogicProcessorImplDeps
import com.arbitrage.scanner.base.InternalError
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.base.Timestamp
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.libs.logging.LogLevel
import com.arbitrage.scanner.models.CexToCexArbitrageOpportunity
import com.crowdproj.kotlin.cor.ICorAddExecDsl
import com.crowdproj.kotlin.cor.handlers.worker
import kotlin.reflect.KFunction
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private val kFun: KFunction<Unit> =
    ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>::saveOpportunitiesWorker

/**
 * Проверяет, соответствуют ли две арбитражные возможности одной и той же ситуации
 *
 * Две возможности считаются идентичными, если у них совпадают:
 * - Токен (cexTokenId)
 * - Биржа покупки (buyCexExchangeId)
 * - Биржа продажи (sellCexExchangeId)
 *
 * Спред может незначительно меняться, но это всё ещё та же арбитражная возможность.
 * Например, BTC: купить на Binance $100 → продать на OKX $105 остаётся той же
 * возможностью, даже если через минуту спред изменился с 5% до 4.8%.
 *
 * @param opp1 Первая возможность для сравнения
 * @param opp2 Вторая возможность для сравнения
 * @return true если возможности представляют одну и ту же арбитражную ситуацию
 */
private fun opportunitiesMatch(
    opp1: CexToCexArbitrageOpportunity,
    opp2: CexToCexArbitrageOpportunity
): Boolean {
    return opp1.cexTokenId == opp2.cexTokenId &&
            opp1.buyCexExchangeId == opp2.buyCexExchangeId &&
            opp1.sellCexExchangeId == opp2.sellCexExchangeId
}

/**
 * Worker для сохранения найденных арбитражных возможностей в репозиторий
 *
 * Этот worker сохраняет найденные арбитражные возможности с автоматической генерацией
 * startTimestamp для каждой возможности. Использует интеллектуальное сравнение для определения,
 * какие возможности являются новыми, а какие продолжают существовать.
 *
 * ## Логика сравнения:
 * Две возможности считаются идентичными, если совпадают:
 * - cexTokenId (тот же токен)
 * - buyCexExchangeId (та же биржа покупки)
 * - sellCexExchangeId (та же биржа продажи)
 *
 * ## Предусловия:
 * - state == State.RUNNING
 * - Context.foundOpportunities содержит найденные возможности (может быть пустым)
 *
 * ## Постусловия (успех):
 * - Исчезнувшие возможности помечены как завершенные (endTimestamp установлен)
 * - Существующие возможности остаются без изменений
 * - Только новые возможности сохранены в ArbitrageOpportunityRepository с сгенерированными ID
 * - Каждая новая возможность имеет startTimestamp = текущее время, endTimestamp = null
 * - state остаётся State.RUNNING
 *
 * ## Постусловия (пустой список найденных):
 * - Все активные возможности помечаются как завершенные
 * - state остаётся State.RUNNING (нормальная ситуация)
 *
 * ## Постусловия (ошибка):
 * - state == State.FAILING
 * - Context.errors содержит информацию об ошибке
 *
 * @param title Название worker'а для логирования и отладки
 */
@OptIn(ExperimentalTime::class)
fun ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>.saveOpportunitiesWorker(
    title: String,
) = worker {
    this.title = title
    on { state == State.RUNNING }
    val logger = config.loggerProvider.logger(kFun)
    handle {
        logger.doWithLogging(id = requestId.toString(), level = LogLevel.INFO) {
            try {
                // Генерация timestamp для всех возможностей (T017a)
                val currentTimestamp = Timestamp(Clock.System.now().epochSeconds)

                // Загрузить текущие активные возможности
                val activeOpportunities = config.arbitrageOpportunityRepository.findActive()

                // Определить, какие возможности исчезли (есть в активных, но нет в найденных)
                val disappeared = activeOpportunities.filter { active ->
                    foundOpportunities.none { found ->
                        opportunitiesMatch(active, found)
                    }
                }

                // Пометить исчезнувшие возможности как завершенные
                if (disappeared.isNotEmpty()) {
                    logger.info(
                        msg = "Marking ${disappeared.size} disappeared opportunities as ended",
                        data = mapOf(
                            "requestId" to requestId.toString(),
                            "disappearedCount" to disappeared.size,
                            "endTimestamp" to currentTimestamp.value
                        )
                    )

                    disappeared.forEach { opportunity ->
                        config.arbitrageOpportunityRepository.markAsEnded(opportunity.id, currentTimestamp)
                    }
                }

                // Определить, какие возможности являются действительно новыми
                val reallyNew = foundOpportunities.filter { found ->
                    activeOpportunities.none { active ->
                        opportunitiesMatch(active, found)
                    }
                }

                // Если нет новых возможностей для сохранения
                if (reallyNew.isEmpty()) {
                    val stillActive = foundOpportunities.size - reallyNew.size
                    logger.info(
                        msg = "No new opportunities to save (disappeared: ${disappeared.size}, still active: $stillActive)",
                        data = mapOf(
                            "requestId" to requestId.toString(),
                            "disappearedCount" to disappeared.size,
                            "stillActiveCount" to stillActive
                        )
                    )
                    return@doWithLogging
                }

                // Обновить startTimestamp для всех новых возможностей
                val opportunitiesWithTimestamp = reallyNew.map { opportunity ->
                    opportunity.copy(startTimestamp = currentTimestamp, endTimestamp = null)
                }

                // Сохранить в репозиторий только новые возможности
                val savedIds = config.arbitrageOpportunityRepository.saveAll(opportunitiesWithTimestamp)

                val stillActive = foundOpportunities.size - reallyNew.size
                logger.info(
                    msg = "Saved ${savedIds.size} new arbitrage opportunities (disappeared: ${disappeared.size}, still active: $stillActive)",
                    data = mapOf(
                        "requestId" to requestId.toString(),
                        "newCount" to savedIds.size,
                        "disappearedCount" to disappeared.size,
                        "stillActiveCount" to stillActive,
                        "timestamp" to currentTimestamp.value
                    )
                )

                // Debug: логируем первые несколько ID
                if (savedIds.isNotEmpty()) {
                    val sampleIds = savedIds.take(3).joinToString(", ") { it.value }
                    logger.debug(
                        msg = "Sample saved IDs: $sampleIds",
                        data = mapOf("requestId" to requestId.toString())
                    )
                }
            } catch (e: Exception) {
                logger.error(
                    msg = "Failed to save arbitrage opportunities: ${e.message}",
                    data = mapOf(
                        "requestId" to requestId.toString(),
                        "error" to (e.message ?: "Unknown error"),
                        "opportunitiesCount" to foundOpportunities.size
                    )
                )
                state = State.FAILING
                errors.add(
                    InternalError(
                        code = "save-opportunities-failed",
                        field = "arbitrageOpportunityRepository",
                        message = "Failed to save opportunities: ${e.message}",
                        exception = e
                    )
                )
            }
        }
    }
}
