package com.arbitrage.scanner.workers.stubs

import com.arbitrage.scanner.BusinessLogicProcessorImplDeps
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.base.StubCase
import com.crowdproj.kotlin.cor.ICorAddExecDsl
import com.crowdproj.kotlin.cor.handlers.worker

fun ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>.searchNotFoundStubWorker(
    title: String,
) = worker {
    this.title = title
    on { this.stubCase == StubCase.NOT_FOUND && state == State.RUNNING }
    handle {
        arbitrageOpportunitySearchResponse.clear()
        state = State.FINISHING
    }
}