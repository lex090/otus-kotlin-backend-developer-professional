package com.arbitrage.scanner.workers.stubs

import com.arbitrage.scanner.ArbOpStubs
import com.arbitrage.scanner.BusinessLogicProcessorImplDeps
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.stubs.Stubs
import com.crowdproj.kotlin.cor.ICorAddExecDsl
import com.crowdproj.kotlin.cor.handlers.worker

fun ICorAddExecDsl<Context, BusinessLogicProcessorImplDeps>.readSuccessStubWorker(
    title: String,
) = worker {
    this.title = title
    on { this.stubCase == Stubs.SUCCESS && state == State.RUNNING }
    handle {
        arbitrageOpportunityReadResponse = ArbOpStubs.arbitrageOpportunity
        state = State.FINISHING
    }
}