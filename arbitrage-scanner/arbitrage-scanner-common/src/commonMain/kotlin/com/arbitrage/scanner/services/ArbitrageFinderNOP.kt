package com.arbitrage.scanner.services

import com.arbitrage.scanner.models.CexPrice
import com.arbitrage.scanner.models.CexToCexArbitrageOpportunity

class ArbitrageFinderNOP : ArbitrageFinder() {
    override suspend fun findOpportunities(
        prices: List<CexPrice>,
        minSpreadPercent: Double
    ): List<CexToCexArbitrageOpportunity> = throw NotImplementedError("Not used in READ tests")
}
