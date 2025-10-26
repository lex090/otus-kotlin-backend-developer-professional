package com.arbitrage.scanner.models

data class ArbitrageOpportunityFilter(
    val cexTokenIds: Set<CexTokenId> = emptySet(),
    val cexExchangeIds: Set<CexExchangeId> = emptySet(),
    val spread: ArbitrageOpportunitySpread = ArbitrageOpportunitySpread.DEFAULT,
) {
    companion object {
        val DEFAULT = ArbitrageOpportunityFilter()
    }
}
