package com.arbitrage.scanner

import com.arbitrage.scanner.algorithm.CexToCexArbitrageFinder
import com.arbitrage.scanner.libs.logging.ArbScanLoggerProvider
import com.arbitrage.scanner.repository.IArbOpRepository
import com.arbitrage.scanner.service.CexPriceClientService

interface BusinessLogicProcessorImplDeps {
    val loggerProvider: ArbScanLoggerProvider
    val cexToCexArbitrageFinder: CexToCexArbitrageFinder
    val testCexPriceClientService: CexPriceClientService
    val testArbOpRepository: IArbOpRepository
}
