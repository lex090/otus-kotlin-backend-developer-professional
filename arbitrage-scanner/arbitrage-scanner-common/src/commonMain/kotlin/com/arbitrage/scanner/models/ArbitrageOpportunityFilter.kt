package com.arbitrage.scanner.models

data class ArbitrageOpportunityFilter(
    val cexTokenIds: Set<CexTokenId>,
    val cexExchangeIds: Set<CexExchangeId>,
    val spread: ArbitrageOpportunitySpread,
) {
    companion object {
        val DEFAULT = ArbitrageOpportunityFilter(
            cexTokenIds = emptySet(),
            cexExchangeIds = emptySet(),
            spread = ArbitrageOpportunitySpread.DEFAULT,
        )
    }
}
