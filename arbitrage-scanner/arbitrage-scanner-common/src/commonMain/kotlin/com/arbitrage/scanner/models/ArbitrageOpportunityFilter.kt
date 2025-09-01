package com.arbitrage.scanner.models

data class ArbitrageOpportunityFilter(
    val dexTokenIds: Set<DexTokenId>,
    val dexExchangeIds: Set<DexExchangeId>,
    val dexChainIds: Set<DexChainId>,
    val cexTokenIds: Set<CexTokenId>,
    val cexExchangeIds: Set<CexExchangeId>,
    val spread: ArbitrageOpportunitySpread,
) {
    companion object {
        val DEFAULT = ArbitrageOpportunityFilter(
            dexTokenIds = emptySet(),
            dexExchangeIds = emptySet(),
            dexChainIds = emptySet(),
            cexTokenIds = emptySet(),
            cexExchangeIds = emptySet(),
            spread = ArbitrageOpportunitySpread.DEFAULT,
        )
    }
}
