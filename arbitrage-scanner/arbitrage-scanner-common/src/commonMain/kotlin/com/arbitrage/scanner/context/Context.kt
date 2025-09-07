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
import com.arbitrage.scanner.stubs.Stubs

data class Context(
    var command: Command = Command.NONE,
    var state: State = State.NONE,
    val internalErrors: MutableList<InternalError> = mutableListOf(),
    var workMode: WorkMode = WorkMode.PROD,
    var stubCase: Stubs = Stubs.NONE,

    var requestId: RequestId = RequestId.DEFAULT,
    var startTimestamp: Timestamp = Timestamp.DEFAULT,

    var arbitrageOpportunityReadRequest: ArbitrageOpportunityId = ArbitrageOpportunityId.DEFAULT,
    var arbitrageOpportunitySearchRequest: ArbitrageOpportunityFilter = ArbitrageOpportunityFilter.DEFAULT,

    var arbitrageOpportunityReadResponse: ArbitrageOpportunity =
        ArbitrageOpportunity.DexToCexSimpleArbitrageOpportunity.DEFAULT,
    val arbitrageOpportunitySearchResponse: MutableSet<ArbitrageOpportunity> = mutableSetOf(),
)
