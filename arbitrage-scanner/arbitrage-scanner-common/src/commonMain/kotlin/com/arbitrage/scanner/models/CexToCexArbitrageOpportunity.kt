package com.arbitrage.scanner.models

import com.arbitrage.scanner.base.Timestamp
import com.arbitrage.scanner.models.CexPrice.CexPriceRaw

data class CexToCexArbitrageOpportunity(
    val id: ArbitrageOpportunityId,
    val cexTokenId: CexTokenId,
    val buyCexExchangeId: CexExchangeId,
    val buyCexPriceRaw: CexPriceRaw,
    val sellCexExchangeId: CexExchangeId,
    val sellCexPriceRaw: CexPriceRaw,
    val spread: ArbitrageOpportunitySpread,
    val startTimestamp: Timestamp,
    val endTimestamp: Timestamp?,
    val lockToken: LockToken,
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

    fun isNone(): Boolean = this == NONE

    fun isNotNone(): Boolean = !isNone()

    companion object {
        val NONE = CexToCexArbitrageOpportunity(
            id = ArbitrageOpportunityId.NONE,
            cexTokenId = CexTokenId.NONE,
            buyCexExchangeId = CexExchangeId.NONE,
            buyCexPriceRaw = CexPriceRaw.NONE,
            sellCexExchangeId = CexExchangeId.NONE,
            sellCexPriceRaw = CexPriceRaw.NONE,
            spread = ArbitrageOpportunitySpread.NONE,
            startTimestamp = Timestamp.NONE,
            endTimestamp = null,
            lockToken = LockToken.NONE,
        )
    }
}
