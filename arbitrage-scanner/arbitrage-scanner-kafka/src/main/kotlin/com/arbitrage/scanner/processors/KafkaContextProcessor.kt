package com.arbitrage.scanner.processors

import com.arbitrage.scanner.BusinessLogicProcessor
import com.arbitrage.scanner.asError
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.libs.logging.ArbScanLoggerProvider

/**
 * Обработка контекста для Kafka транспорта
 *
 * Адаптирует логику обработки контекста из Ktor модуля для работы
 * в Kafka окружении без зависимости от ApplicationCall.
 *
 * @param context контекст выполнения запроса
 * @param businessLogicProcessor процессор бизнес-логики
 * @param loggerProvider провайдер логгера
 */
suspend fun processKafkaContext(
    context: Context,
    businessLogicProcessor: BusinessLogicProcessor,
    loggerProvider: ArbScanLoggerProvider
) {
    val logger = loggerProvider.logger("KafkaContextProcessor")

    try {
        logger.info(
            msg = "Request processing started",
            marker = "BIZ",
            data = "requestId=${context.requestId}, command=${context.command}"
        )

        businessLogicProcessor.exec(context)

        logger.info(
            msg = "Request processing completed",
            marker = "BIZ",
            data = "requestId=${context.requestId}, state=${context.state}"
        )

    } catch (throwable: Throwable) {
        logger.error(
            msg = "Request processing failed",
            marker = "BIZ",
            data = "requestId=${context.requestId}",
            e = throwable
        )
        context.state = State.FAILING
        context.internalErrors.add(throwable.asError())
    }
}