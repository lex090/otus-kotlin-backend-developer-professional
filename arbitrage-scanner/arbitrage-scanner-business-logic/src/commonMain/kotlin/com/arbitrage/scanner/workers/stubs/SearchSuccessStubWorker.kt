package com.arbitrage.scanner.workers.stubs

import com.arbitrage.scanner.ArbOpStubs
import com.arbitrage.scanner.BusinessLogicProcessorImplDeps
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.libs.logging.LogLevel
import com.arbitrage.scanner.stubs.Stubs
import com.crowdproj.kotlin.cor.ICorAddExecDsl
import com.crowdproj.kotlin.cor.handlers.worker
import kotlin.reflect.KFunction

private val kFun: KFunction<Unit> = ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>::searchSuccessStubWorker
fun ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>.searchSuccessStubWorker(
    title: String,
) = worker {
    this.title = title
    on { this.stubCase == Stubs.SUCCESS && state == State.RUNNING }
    val logger = config.loggerProvider.logger(kFun)
    handle {
        logger.doWithLogging(id = requestId.toString(), level = LogLevel.DEBUG) {
            arbitrageOpportunitySearchResponse.add(ArbOpStubs.arbitrageOpportunity)
            state = State.FINISHING
        }
    }
}