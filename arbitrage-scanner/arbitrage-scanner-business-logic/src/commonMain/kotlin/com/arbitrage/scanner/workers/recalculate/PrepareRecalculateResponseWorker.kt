package com.arbitrage.scanner.workers.recalculate

import com.arbitrage.scanner.BusinessLogicProcessorImplDeps
import com.arbitrage.scanner.base.InternalError
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.libs.logging.LogLevel
import com.arbitrage.scanner.models.RecalculateResult
import com.crowdproj.kotlin.cor.ICorAddExecDsl
import com.crowdproj.kotlin.cor.handlers.worker
import kotlin.reflect.KFunction
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private val kFun: KFunction<Unit> =
    ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>::prepareRecalculateResponseWorker

/**
 * Worker для формирования ответа на RECALCULATE запрос
 *
 * Этот worker завершает цепочку обработки RECALCULATE события.
 * Формирует RecalculateResult с метриками: количество найденных возможностей
 * и время обработки.
 *
 * ## Предусловия:
 * - state == State.RUNNING
 * - Context.foundOpportunities содержит результат поиска (заполнен предыдущими workers)
 * - Context.startTimestamp содержит время начала обработки запроса
 *
 * ## Постусловия (успех):
 * - Context.recalculateResponse содержит RecalculateResult с метриками
 * - state = State.FINISHING (успешное завершение цепочки)
 *
 * ## Метрики в ответе:
 * - opportunitiesCount: количество найденных арбитражных возможностей
 * - processingTimeMs: время обработки в миллисекундах
 *
 * @param title Название worker'а для логирования и отладки
 */
@OptIn(ExperimentalTime::class)
fun ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>.prepareRecalculateResponseWorker(
    title: String,
) = worker {
    this.title = title
    on { state == State.RUNNING }
    val logger = config.loggerProvider.logger(kFun)
    handle {
        logger.doWithLogging(id = requestId.toString(), level = LogLevel.INFO) {
            try {
                // Вычислить время обработки
                val currentTime = Clock.System.now().epochSeconds
                val processingTimeMs = (currentTime - startTimestamp.value) * 1000

                // Сформировать результат
                val result = RecalculateResult(
                    opportunitiesCount = foundOpportunities.size,
                    processingTimeMs = processingTimeMs
                )

                // Установить в контекст
                recalculateResponse = result

                logger.info(
                    msg = "Prepared RECALCULATE response: ${result.opportunitiesCount} opportunities, ${result.processingTimeMs}ms",
                    data = mapOf(
                        "requestId" to requestId.toString(),
                        "opportunitiesCount" to result.opportunitiesCount,
                        "processingTimeMs" to result.processingTimeMs
                    )
                )

                // Успешное завершение обработки
                state = State.FINISHING
            } catch (e: Exception) {
                logger.error(
                    msg = "Failed to prepare RECALCULATE response: ${e.message}",
                    data = mapOf(
                        "requestId" to requestId.toString(),
                        "error" to (e.message ?: "Unknown error")
                    )
                )
                state = State.FAILING
                errors.add(
                    InternalError(
                        code = "response-preparation-failed",
                        field = "recalculateResponse",
                        message = "Failed to prepare response: ${e.message}",
                        exception = e
                    )
                )
            }
        }
    }
}
