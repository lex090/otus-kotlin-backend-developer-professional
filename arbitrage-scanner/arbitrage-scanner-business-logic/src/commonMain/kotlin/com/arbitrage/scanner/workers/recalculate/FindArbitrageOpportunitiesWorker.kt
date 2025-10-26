package com.arbitrage.scanner.workers.recalculate

import com.arbitrage.scanner.BusinessLogicProcessorImplDeps
import com.arbitrage.scanner.base.InternalError
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.libs.logging.LogLevel
import com.crowdproj.kotlin.cor.ICorAddExecDsl
import com.crowdproj.kotlin.cor.handlers.worker
import kotlin.reflect.KFunction

private val kFun: KFunction<Unit> =
    ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>::findArbitrageOpportunitiesWorker

/**
 * Worker для поиска арбитражных возможностей
 *
 * Этот worker применяет алгоритм поиска арбитража к загруженным ценам.
 * Использует ArbitrageFinder сервис для выполнения оптимизированного поиска.
 *
 * ## Предусловия:
 * - state == State.RUNNING
 * - Context.loadedPrices содержит ценовые данные (заполнен LoadCexPricesWorker)
 *
 * ## Постусловия (успех):
 * - Context.foundOpportunities содержит найденные арбитражные возможности
 * - state остаётся State.RUNNING
 *
 * ## Постусловия (пустой результат):
 * - Context.foundOpportunities = emptyList()
 * - state остаётся State.RUNNING (это нормальная ситуация, не ошибка)
 *
 * ## Постусловия (ошибка):
 * - state == State.FAILING
 * - Context.errors содержит информацию об ошибке
 *
 * @param title Название worker'а для логирования и отладки
 */
fun ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>.findArbitrageOpportunitiesWorker(
    title: String,
) = worker {
    this.title = title
    on { state == State.RUNNING }
    val logger = config.loggerProvider.logger(kFun)
    handle {
        logger.doWithLogging(id = requestId.toString(), level = LogLevel.INFO) {
            try {
                // Применить алгоритм поиска арбитража
                val opportunities = config.arbitrageFinder.findOpportunities(
                    prices = loadedPrices,
                    minSpreadPercent = 0.1 // Минимальный порог прибыльности
                )

                // Сохранить результат в контекст
                foundOpportunities = opportunities

                logger.info(
                    msg = "Found ${opportunities.size} arbitrage opportunities from ${loadedPrices.size} prices",
                    data = mapOf(
                        "requestId" to requestId.toString(),
                        "pricesCount" to loadedPrices.size,
                        "opportunitiesCount" to opportunities.size
                    )
                )
            } catch (e: Exception) {
                logger.error(
                    msg = "Failed to find arbitrage opportunities: ${e.message}",
                    data = mapOf(
                        "requestId" to requestId.toString(),
                        "error" to (e.message ?: "Unknown error"),
                        "pricesCount" to loadedPrices.size
                    )
                )
                state = State.FAILING
                errors.add(
                    InternalError(
                        code = "arbitrage-search-failed",
                        field = "arbitrageFinder",
                        message = "Failed to find arbitrage opportunities: ${e.message}",
                        exception = e
                    )
                )
            }
        }
    }
}
