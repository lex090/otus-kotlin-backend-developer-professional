package com.arbitrage.scanner.workers.recalculate

import com.arbitrage.scanner.BusinessLogicProcessorImplDeps
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.base.Timestamp
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.libs.logging.LogLevel
import com.arbitrage.scanner.models.CexToCexArbitrageOpportunity
import com.crowdproj.kotlin.cor.ICorAddExecDsl
import com.crowdproj.kotlin.cor.handlers.worker
import kotlin.reflect.KFunction
import kotlin.time.Clock

private val kFun: KFunction<Unit> = ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>::analyzeArbOpChangesWorker

fun ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>.analyzeArbOpChangesWorker(
    title: String
) = worker {
    this.title = title
    this.description = """
        Анализ изменений арбитражных возможностей.
        Определяет какие возможности нужно создать, обновить или закрыть.

        Логика:
        - Новые (нет в БД) → создать
        - Существующие (есть в БД) → обновить
        - Отсутствующие в новых данных (есть в БД) → закрыть
    """.trimIndent()
    on { state == State.RUNNING && arbOps.isNotEmpty() }
    val logger = config.loggerProvider.logger(kFun)
    handle {
        logger.doWithLogging(id = requestId.toString(), level = LogLevel.INFO) {
            // Создаем карты для быстрого поиска по ключу
            val existingMap = existingActiveArbOps.associateBy { it.fastKey }
            val newMap = arbOps.associateBy { it.fastKey }

            // Проверка на дубликаты в новых данных
            val duplicateKeys = arbOps
                .groupBy { it.fastKey }
                .filter { it.value.size > 1 }

            if (duplicateKeys.isNotEmpty()) {
                logger.error("Обнаружены дубликаты ключей в новых данных: ${duplicateKeys.keys.size} дубликатов")
                duplicateKeys.forEach { (key, ops) ->
                    logger.error("Дубликат: $key -> ${ops.size} возможностей")
                }
                // Продолжаем работу, но берем первую из дубликатов (Обдумать как с этим работать)
            }

            // Счетчики для статистики
            var skippedCount = 0

            // Анализ: создать или обновить
            newMap.forEach { (key, newOp) ->
                val existing = existingMap[key]
                if (existing != null) {
                    // Проверяем, изменились ли значимые данные
                    val hasChanges = hasSignificantChanges(existing, newOp)

                    if (hasChanges) {
                        // Существующая возможность с изменениями - обновляем, сохраняя id, startTimestamp и lockToken
                        arbOpsToUpdate.add(
                            newOp.copy(
                                id = existing.id,
                                startTimestamp = existing.startTimestamp,
                                lockToken = existing.lockToken
                            )
                        )
                        logger.debug("Обновление: $key (изменения обнаружены)")
                    } else {
                        skippedCount++
                        logger.debug("Пропуск обновления: $key (нет изменений)")
                    }
                } else {
                    // Новая возможность - создаем
                    arbOpsToCreate.add(newOp)
                }
            }

            // Анализ: закрыть те, которых нет в новых данных
            val currentTimestamp = Timestamp(Clock.System.now().epochSeconds)
            existingMap.forEach { (key, existingOp) ->
                if (!newMap.containsKey(key)) {
                    // Возможность больше не актуальна - закрываем
                    arbOpsToClose.add(
                        existingOp.copy(endTimestamp = currentTimestamp)
                    )
                }
            }

            logger.info(
                "Анализ завершен: создать=${arbOpsToCreate.size}, " +
                "обновить=${arbOpsToUpdate.size}, закрыть=${arbOpsToClose.size}, " +
                "пропущено=${skippedCount} (нет изменений)"
            )
        }
    }
}

/**
 * Проверяет, изменились ли значимые данные арбитражной возможности.
 *
 * Сравниваются только поля, которые влияют на бизнес-логику:
 * - buyCexPriceRaw - цена покупки
 * - sellCexPriceRaw - цена продажи
 * - spread - спред
 *
 * Не сравниваются:
 * - id - идентификатор записи (служебное поле)
 * - cexTokenId, buyCexExchangeId, sellCexExchangeId - входят в fastKey (уже совпадают)
 * - startTimestamp - время начала (не меняется для существующей записи)
 * - lockToken - токен оптимистичной блокировки (служебное поле)
 */
private fun hasSignificantChanges(
    existing: CexToCexArbitrageOpportunity,
    new: CexToCexArbitrageOpportunity
): Boolean {
    return existing.buyCexPriceRaw != new.buyCexPriceRaw ||
            existing.sellCexPriceRaw != new.sellCexPriceRaw ||
            existing.spread != new.spread
}
