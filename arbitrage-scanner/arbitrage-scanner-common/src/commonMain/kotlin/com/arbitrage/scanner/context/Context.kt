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
    val command: Command = Command.NONE,
    val state: State = State.NONE,
    val internalErrors: List<InternalError> = emptyList(),
    val workMode: WorkMode = WorkMode.PROD,
    val stubCase: Stubs = Stubs.NONE,

    val requestId: RequestId = RequestId.DEFAULT,
    val startTimestamp: Timestamp = Timestamp.DEFAULT,

    val arbitrageOpportunityReadRequest: ArbitrageOpportunityId = ArbitrageOpportunityId.DEFAULT,
    val arbitrageOpportunitySearchRequest: ArbitrageOpportunityFilter = ArbitrageOpportunityFilter.DEFAULT,

    val arbitrageOpportunityReadResponse: ArbitrageOpportunity = ArbitrageOpportunity.DexToCexSimpleArbitrageOpportunity.DEFAULT,
    val arbitrageOpportunitySearchResponse: Set<ArbitrageOpportunity> = emptySet(),
)
