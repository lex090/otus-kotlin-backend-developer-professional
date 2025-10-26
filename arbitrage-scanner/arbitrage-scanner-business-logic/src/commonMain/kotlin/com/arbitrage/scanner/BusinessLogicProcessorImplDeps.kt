package com.arbitrage.scanner

import com.arbitrage.scanner.libs.logging.ArbScanLoggerProvider
import com.arbitrage.scanner.repository.CexPriceRepository
import com.arbitrage.scanner.repository.ArbitrageOpportunityRepository
import com.arbitrage.scanner.services.ArbitrageFinder

interface BusinessLogicProcessorImplDeps {
    val loggerProvider: ArbScanLoggerProvider
    val cexPriceRepository: CexPriceRepository
    val arbitrageOpportunityRepository: ArbitrageOpportunityRepository
    val arbitrageFinder: ArbitrageFinder
}
