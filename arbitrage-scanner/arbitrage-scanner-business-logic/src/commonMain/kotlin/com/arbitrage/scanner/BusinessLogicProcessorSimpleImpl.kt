package com.arbitrage.scanner

import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.context.Context

class BusinessLogicProcessorSimpleImpl : BusinessLogicProcessor {
    override suspend fun exec(ctx: Context) {
        ctx.state = State.RUNNING
        ctx.arbitrageOpportunityReadResponse = ArbOpStubs.arbitrageOpportunity
        ctx.arbitrageOpportunitySearchResponse.add(ArbOpStubs.arbitrageOpportunity)
        ctx.recalculateResponse = ArbOpStubs.recalculateResult
        ctx.state = State.FINISHING
    }
}
