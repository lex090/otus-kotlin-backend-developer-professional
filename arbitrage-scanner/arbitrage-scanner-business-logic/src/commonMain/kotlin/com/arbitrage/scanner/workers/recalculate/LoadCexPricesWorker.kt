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
    ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>::loadCexPricesWorker

/**
 * Worker для загрузки ценовых данных CEX бирж из репозитория
 *
 * Этот worker является первым в цепочке обработки RECALCULATE события.
 * Загружает все доступные цены из CexPriceRepository и сохраняет их в Context.loadedPrices.
 *
 * ## Предусловия:
 * - state == State.RUNNING
 * - command == Command.RECALCULATE
 * - workMode == WorkMode.PROD
 *
 * ## Постусловия (успех):
 * - Context.loadedPrices содержит список цен из репозитория
 * - state остаётся State.RUNNING для продолжения цепочки
 *
 * ## Постусловия (ошибка):
 * - state == State.FAILING
 * - Context.errors содержит информацию об ошибке
 *
 * @param title Название worker'а для логирования и отладки
 */
fun ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>.loadCexPricesWorker(
    title: String,
) = worker {
    this.title = title
    on { state == State.RUNNING }
    val logger = config.loggerProvider.logger(kFun)
    handle {
        logger.doWithLogging(id = requestId.toString(), level = LogLevel.INFO) {
            try {
                // Загрузить все цены из репозитория
                val prices = config.cexPriceRepository.findAll()

                // Сохранить в контекст для следующих worker'ов
                loadedPrices = prices

                logger.info(
                    msg = "Loaded ${prices.size} price records from repository",
                    data = mapOf("requestId" to requestId.toString())
                )
            } catch (e: Exception) {
                logger.error(
                    msg = "Failed to load CEX prices: ${e.message}",
                    data = mapOf(
                        "requestId" to requestId.toString(),
                        "error" to (e.message ?: "Unknown error")
                    )
                )
                state = State.FAILING
                errors.add(
                    InternalError(
                        code = "price-load-failed",
                        field = "cexPriceRepository",
                        message = "Failed to load price data: ${e.message}",
                        exception = e
                    )
                )
            }
        }
    }
}
