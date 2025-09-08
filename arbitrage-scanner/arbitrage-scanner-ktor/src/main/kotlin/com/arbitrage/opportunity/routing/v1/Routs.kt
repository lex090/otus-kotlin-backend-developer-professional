package com.arbitrage.opportunity.routing.v1

import com.arbitrage.opportunity.BusinessLogicProcessor
import com.arbitrage.opportunity.processors.processRequest
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityReadRequest
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityReadResponse
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunitySearchRequest
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunitySearchResponse
import io.ktor.server.application.ApplicationCall

suspend fun ApplicationCall.readArbitrageOpportunity(
    businessLogicProcessor: BusinessLogicProcessor
) = processRequest<ArbitrageOpportunityReadRequest, ArbitrageOpportunityReadResponse>(businessLogicProcessor)

suspend fun ApplicationCall.searchArbitrageOpportunity(
    businessLogicProcessor: BusinessLogicProcessor
) = processRequest<ArbitrageOpportunitySearchRequest, ArbitrageOpportunitySearchResponse>(businessLogicProcessor)
