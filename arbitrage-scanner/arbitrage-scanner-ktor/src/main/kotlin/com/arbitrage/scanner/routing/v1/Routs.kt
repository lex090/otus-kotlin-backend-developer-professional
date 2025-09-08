package com.arbitrage.scanner.routing.v1

import com.arbitrage.scanner.BusinessLogicProcessor
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityReadRequest
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityReadResponse
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunitySearchRequest
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunitySearchResponse
import com.arbitrage.scanner.processors.processRequest
import io.ktor.server.application.ApplicationCall

suspend fun ApplicationCall.readArbitrageOpportunity(
    businessLogicProcessor: BusinessLogicProcessor
) = processRequest<ArbitrageOpportunityReadRequest, ArbitrageOpportunityReadResponse>(businessLogicProcessor)

suspend fun ApplicationCall.searchArbitrageOpportunity(
    businessLogicProcessor: BusinessLogicProcessor
) = processRequest<ArbitrageOpportunitySearchRequest, ArbitrageOpportunitySearchResponse>(businessLogicProcessor)
