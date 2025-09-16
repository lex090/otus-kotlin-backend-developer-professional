package com.arbitrage.scanner.processors

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
            msg = "Request $logId started",
            marker = "BIZ",
            data = null,//ctx.toLog(logId) // TODO
        )
        businessLogicProcessor.exec(context)
        logger.info(
            msg = "Request $logId processed",
            marker = "BIZ",
            data = null, //ctx.toLog(logId)
        )
        context.resolveContextToResponse()
    } catch (throwable: Throwable) {
        logger.error(
            msg = "Request $logId failed",
            marker = "BIZ",
            data = null,//ctx.toLog(logId),
            e = throwable,
        )
        context.state = State.FAILING
        context.internalErrors.add(throwable.asError())
        context.resolveContextToResponse()
    }
}
