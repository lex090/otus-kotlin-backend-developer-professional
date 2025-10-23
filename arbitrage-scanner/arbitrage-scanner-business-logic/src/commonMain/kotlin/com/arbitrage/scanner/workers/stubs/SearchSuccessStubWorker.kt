package com.arbitrage.scanner.workers.stubs

import com.arbitrage.scanner.ArbOpStubs
import com.arbitrage.scanner.BusinessLogicProcessorStubsDeps
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.stubs.Stubs
import com.crowdproj.kotlin.cor.ICorAddExecDsl
import com.crowdproj.kotlin.cor.handlers.worker

fun ICorAddExecDsl<Context, BusinessLogicProcessorStubsDeps>.searchSuccessStubWorker(
    title: String,
) = worker {
    this.title = title
    on { this.stubCase == Stubs.SUCCESS && state == State.RUNNING }
    handle {
        arbitrageOpportunitySearchResponse.add(ArbOpStubs.arbitrageOpportunity)
        state = State.FINISHING
    }
}