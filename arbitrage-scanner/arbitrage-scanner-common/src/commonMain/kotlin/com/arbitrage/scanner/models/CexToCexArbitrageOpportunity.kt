package com.arbitrage.scanner.models

import com.arbitrage.scanner.base.Timestamp
import com.arbitrage.scanner.models.CexPrice.CexPriceRaw

data class CexToCexArbitrageOpportunity(
    val id: ArbitrageOpportunityId = ArbitrageOpportunityId.DEFAULT,
    val cexTokenId: CexTokenId = CexTokenId.DEFAULT,
    val buyCexExchangeId: CexExchangeId = CexExchangeId.DEFAULT,
    val buyCexPriceRaw: CexPriceRaw = CexPriceRaw.DEFAULT,
    val sellCexExchangeId: CexExchangeId = CexExchangeId.DEFAULT,
    val sellCexPriceRaw: CexPriceRaw = CexPriceRaw.DEFAULT,
    val spread: ArbitrageOpportunitySpread = ArbitrageOpportunitySpread.DEFAULT,
    val startTimestamp: Timestamp = Timestamp.DEFAULT,
    val endTimestamp: Timestamp? = null,
) {
    companion object {
        val DEFAULT = CexToCexArbitrageOpportunity()
    }
}
