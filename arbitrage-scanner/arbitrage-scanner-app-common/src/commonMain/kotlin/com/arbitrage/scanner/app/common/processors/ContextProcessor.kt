package com.arbitrage.scanner.app.common.processors

import com.arbitrage.scanner.BusinessLogicProcessor
import com.arbitrage.scanner.asError
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.base.Timestamp
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.libs.logging.ArbScanLoggerProvider
import kotlin.reflect.KFunction
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
suspend inline fun processContext(
    crossinline prepareContextFromRequest: suspend Context.() -> Unit,
    crossinline resolveContextToResponse: suspend Context.() -> Unit,
    businessLogicProcessor: BusinessLogicProcessor,
    loggerProvider: ArbScanLoggerProvider,
    kFun: KFunction<*>,
    logId: String,
) {
    val logger = loggerProvider.logger(kFun)
    val context = Context(startTimestamp = Timestamp(value = Clock.System.now().epochSeconds))
    try {
        context.prepareContextFromRequest()
        logger.info(
            msg = "Request started",
            marker = "BIZ",
            data = context.toString(),
        )
        businessLogicProcessor.exec(context)
        logger.info(
            msg = "Request processed",
            marker = "BIZ",
            data = context.toString(),
        )
        context.resolveContextToResponse()
    } catch (throwable: Throwable) {
        logger.error(
            msg = "Request failed",
            marker = "BIZ",
            data = context.toString(),
            e = throwable,
        )
        context.state = State.FAILING
        context.internalErrors.add(throwable.asError())
        context.resolveContextToResponse()
    }
}
