package com.arbitrage.scanner.context

import com.arbitrage.scanner.base.Command
import com.arbitrage.scanner.base.InternalError
import com.arbitrage.scanner.base.RequestId
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.base.StubCase
import com.arbitrage.scanner.base.Timestamp
import com.arbitrage.scanner.base.WorkMode
import com.arbitrage.scanner.models.CexToCexArbitrageOpportunityFilter
import com.arbitrage.scanner.models.ArbitrageOpportunityId
import com.arbitrage.scanner.models.CexPrice
import com.arbitrage.scanner.models.CexToCexArbitrageOpportunity
import com.arbitrage.scanner.models.RecalculateResult
import com.arbitrage.scanner.repository.IArbOpRepository
import com.arbitrage.scanner.service.CexPriceClientService
import kotlin.time.Duration

data class Context(
    var command: Command = Command.NONE,
    var state: State = State.NONE,
    var workMode: WorkMode = WorkMode.PROD,
    var stubCase: StubCase = StubCase.NONE,

    var requestId: RequestId = RequestId.DEFAULT,
    var startTimestamp: Timestamp = Timestamp.DEFAULT,

    val internalErrors: MutableList<InternalError> = mutableListOf(),

    var cexPriceClientService: CexPriceClientService = CexPriceClientService.NONE,
    var arbOpRepo: IArbOpRepository = IArbOpRepository.NONE,

    // START READ
    var arbitrageOpportunityReadRequest: ArbitrageOpportunityId = ArbitrageOpportunityId.NONE,
    var arbitrageOpportunityReadRequestValidating: ArbitrageOpportunityId = ArbitrageOpportunityId.NONE,
    var arbitrageOpportunityReadRequestValidated: ArbitrageOpportunityId = ArbitrageOpportunityId.NONE,

    var arbitrageOpportunityReadResponse: CexToCexArbitrageOpportunity = CexToCexArbitrageOpportunity.DEFAULT,
    // END READ

    // START SEARCH
    var arbitrageOpportunitySearchRequest: CexToCexArbitrageOpportunityFilter = CexToCexArbitrageOpportunityFilter.DEFAULT,
    var arbitrageOpportunitySearchRequestValidating: CexToCexArbitrageOpportunityFilter = CexToCexArbitrageOpportunityFilter.DEFAULT,
    var arbitrageOpportunitySearchRequestValidated: CexToCexArbitrageOpportunityFilter = CexToCexArbitrageOpportunityFilter.DEFAULT,

    val arbitrageOpportunitySearchResponse: MutableSet<CexToCexArbitrageOpportunity> = mutableSetOf(),
    // END READ

    // START RECALCULATE
    val cexPrices: MutableList<CexPrice> = mutableListOf(),
    val arbOps: MutableList<CexToCexArbitrageOpportunity> = mutableListOf(),
    val existingActiveArbOps: MutableList<CexToCexArbitrageOpportunity> = mutableListOf(),
    val arbOpsToCreate: MutableList<CexToCexArbitrageOpportunity> = mutableListOf(),
    val arbOpsToUpdate: MutableList<CexToCexArbitrageOpportunity> = mutableListOf(),
    val arbOpsToClose: MutableList<CexToCexArbitrageOpportunity> = mutableListOf(),
    var executionTimeOfFindArbOps: Duration = Duration.ZERO,
    var recalculateResponse: RecalculateResult = RecalculateResult.DEFAULT,
    // END RECALCULATE
)
