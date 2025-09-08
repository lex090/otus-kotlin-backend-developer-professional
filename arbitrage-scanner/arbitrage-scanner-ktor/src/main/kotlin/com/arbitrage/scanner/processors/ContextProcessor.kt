package com.arbitrage.scanner.processors

import com.arbitrage.scanner.BusinessLogicProcessor
import com.arbitrage.scanner.asError
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.base.Timestamp
import com.arbitrage.scanner.context.Context
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
suspend inline fun processContext(
    businessLogicProcessor: BusinessLogicProcessor,
    crossinline prepareContextFromRequest: suspend Context.() -> Unit,
    crossinline resolveContextToResponse: suspend Context.() -> Unit,
) {
    val context = Context(startTimestamp = Timestamp(value = Clock.System.now().epochSeconds))
    try {
        context.prepareContextFromRequest()
        businessLogicProcessor.exec(context)
        context.resolveContextToResponse()
    } catch (throwable: Throwable) {
        context.state = State.FAILING
        context.internalErrors.add(throwable.asError())
        context.resolveContextToResponse()
    }
}
