package com.arbitrage.opportunity.processors

import com.arbitrage.opportunity.LogicProcessor
import com.arbitrage.scanner.asError
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.base.Timestamp
import com.arbitrage.scanner.context.Context
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
suspend inline fun processContext(
    logicProcessor: LogicProcessor,
    crossinline prepareContextFromRequest: suspend Context.() -> Unit,
    crossinline resolveContextToResponse: suspend Context.() -> Unit,
) {
    val context = Context(startTimestamp = Timestamp(value = Clock.System.now().epochSeconds))
    try {
        context.prepareContextFromRequest()
        logicProcessor.exec(context)
        context.resolveContextToResponse()
    } catch (throwable: Throwable) {
        context.state = State.FAILING
        context.internalErrors.add(throwable.asError())
        context.resolveContextToResponse()
    }
}
