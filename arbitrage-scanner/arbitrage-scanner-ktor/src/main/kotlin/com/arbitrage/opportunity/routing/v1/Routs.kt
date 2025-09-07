package com.arbitrage.opportunity.routing.v1

import com.arbitrage.opportunity.LogicProcessor
import com.arbitrage.opportunity.processors.processRequest
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityReadRequest
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityReadResponse
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunitySearchRequest
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunitySearchResponse
import io.ktor.server.application.ApplicationCall

suspend fun ApplicationCall.readArbitrageOpportunity(
    logicProcessor: LogicProcessor
) = processRequest<ArbitrageOpportunityReadRequest, ArbitrageOpportunityReadResponse>(logicProcessor)

suspend fun ApplicationCall.searchArbitrageOpportunity(
    logicProcessor: LogicProcessor
) = processRequest<ArbitrageOpportunitySearchRequest, ArbitrageOpportunitySearchResponse>(logicProcessor)
