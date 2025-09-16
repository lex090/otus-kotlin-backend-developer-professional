package com.arbitrage.scanner.routing.v1

import com.arbitrage.scanner.BusinessLogicProcessor
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityReadRequest
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityReadResponse
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunitySearchRequest
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunitySearchResponse
import com.arbitrage.scanner.libs.logging.ArbScanLoggerProvider
import com.arbitrage.scanner.processors.processRequest
import io.ktor.server.application.ApplicationCall
import kotlin.reflect.KFunction

val kFunRead: KFunction<*> = ApplicationCall::readArbitrageOpportunity
suspend fun ApplicationCall.readArbitrageOpportunity(
    businessLogicProcessor: BusinessLogicProcessor,
    loggerProvider: ArbScanLoggerProvider,
) = processRequest<ArbitrageOpportunityReadRequest, ArbitrageOpportunityReadResponse>(
    businessLogicProcessor = businessLogicProcessor,
    loggerProvider = loggerProvider,
    kFun = kFunRead,
    logId = kFunRead.name,
)

val kFunSearch: KFunction<*> = ApplicationCall::readArbitrageOpportunity
suspend fun ApplicationCall.searchArbitrageOpportunity(
    businessLogicProcessor: BusinessLogicProcessor,
    loggerProvider: ArbScanLoggerProvider,
) = processRequest<ArbitrageOpportunitySearchRequest, ArbitrageOpportunitySearchResponse>(
    businessLogicProcessor,
    loggerProvider = loggerProvider,
    kFun = kFunSearch,
    logId = kFunSearch.name,
)
