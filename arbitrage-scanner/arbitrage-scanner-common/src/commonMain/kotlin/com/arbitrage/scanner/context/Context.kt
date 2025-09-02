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
    val command: Command,
    val state: State,
    val internalErrors: List<InternalError>,
    val workMode: WorkMode,
    val stubCase: Stubs,

    val requestId: RequestId,
    val startTimestamp: Timestamp,

    val arbitrageOpportunityReadRequest: ArbitrageOpportunityId,
    val arbitrageOpportunitySearchRequest: ArbitrageOpportunityFilter,

    val arbitrageOpportunityReadResponse: ArbitrageOpportunity,
    val arbitrageOpportunitySearchResponse: Set<ArbitrageOpportunity>,
)
