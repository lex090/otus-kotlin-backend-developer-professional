package com.arbitrage.scanner.models

import com.arbitrage.scanner.base.Timestamp
import com.arbitrage.scanner.models.CexPrice.CexPriceRaw

data class CexToCexArbitrageOpportunity(
    val id: ArbitrageOpportunityId = ArbitrageOpportunityId.NONE,
    val cexTokenId: CexTokenId = CexTokenId.NONE,
    val buyCexExchangeId: CexExchangeId = CexExchangeId.NONE,
    val buyCexPriceRaw: CexPriceRaw = CexPriceRaw.NONE,
    val sellCexExchangeId: CexExchangeId = CexExchangeId.NONE,
    val sellCexPriceRaw: CexPriceRaw = CexPriceRaw.NONE,
    val spread: ArbitrageOpportunitySpread = ArbitrageOpportunitySpread.NONE,
    val startTimestamp: Timestamp = Timestamp.NONE,
    val endTimestamp: Timestamp? = null,
    val lockToken: LockToken = LockToken.DEFAULT,
) {
    val fastKey: ArbOpFastKey = ArbOpFastKey(
        cexTokenId = cexTokenId,
        buyCexExchangeId = buyCexExchangeId,
        sellCexExchangeId = sellCexExchangeId,
    )

    data class ArbOpFastKey(
        val cexTokenId: CexTokenId,
        val buyCexExchangeId: CexExchangeId,
        val sellCexExchangeId: CexExchangeId
    )

    companion object {
        val DEFAULT = CexToCexArbitrageOpportunity()
    }
}
