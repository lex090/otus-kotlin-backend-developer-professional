package com.arbitrage.scanner.context

import com.arbitrage.scanner.base.Command
import com.arbitrage.scanner.base.InternalError
import com.arbitrage.scanner.base.RequestId
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.base.Timestamp
import com.arbitrage.scanner.base.WorkMode
import com.arbitrage.scanner.models.ArbitrageOpportunity
import com.arbitrage.scanner.models.ArbitrageOpportunityFilter
import com.arbitrage.scanner.models.ArbitrageOpportunityId
import com.arbitrage.scanner.models.RecalculateResult
import com.arbitrage.scanner.stubs.Stubs

data class Context(
    var command: Command = Command.NONE,
    var state: State = State.NONE,
    val internalErrors: MutableList<InternalError> = mutableListOf(),
    var workMode: WorkMode = WorkMode.PROD,
    var stubCase: Stubs = Stubs.NONE,

    var requestId: RequestId = RequestId.DEFAULT,
    var startTimestamp: Timestamp = Timestamp.DEFAULT,

    val errors: MutableSet<InternalError> = mutableSetOf(),

    // START READ
    var arbitrageOpportunityReadRequest: ArbitrageOpportunityId = ArbitrageOpportunityId.DEFAULT,
    var arbitrageOpportunityReadRequestValidating: ArbitrageOpportunityId = ArbitrageOpportunityId.DEFAULT,
    var arbitrageOpportunityReadRequestValidated: ArbitrageOpportunityId = ArbitrageOpportunityId.DEFAULT,

    var arbitrageOpportunityReadResponse: ArbitrageOpportunity =
        ArbitrageOpportunity.DexToCexSimpleArbitrageOpportunity.DEFAULT,
    // END READ

    // START SEARCH
    var arbitrageOpportunitySearchRequest: ArbitrageOpportunityFilter = ArbitrageOpportunityFilter.DEFAULT,
    var arbitrageOpportunitySearchRequestValidating: ArbitrageOpportunityFilter = ArbitrageOpportunityFilter.DEFAULT,
    var arbitrageOpportunitySearchRequestValidated: ArbitrageOpportunityFilter = ArbitrageOpportunityFilter.DEFAULT,

    val arbitrageOpportunitySearchResponse: MutableSet<ArbitrageOpportunity> = mutableSetOf(),
    // END READ

    // START RECALCULATE
    var recalculateResponse: RecalculateResult = RecalculateResult.DEFAULT,
    // END RECALCULATE
)
