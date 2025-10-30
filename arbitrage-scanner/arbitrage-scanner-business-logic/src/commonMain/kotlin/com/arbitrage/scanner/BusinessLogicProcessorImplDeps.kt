package com.arbitrage.scanner

import com.arbitrage.scanner.algorithm.CexToCexArbitrageFinder
import com.arbitrage.scanner.libs.logging.ArbScanLoggerProvider
import com.arbitrage.scanner.service.CexPriceClientService

interface BusinessLogicProcessorImplDeps {
    val loggerProvider: ArbScanLoggerProvider
    val stubCexPriceClientService: CexPriceClientService
    val cexToCexArbitrageFinder: CexToCexArbitrageFinder
}
