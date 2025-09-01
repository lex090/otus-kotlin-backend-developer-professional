package com.arbitrage.scanner.models

data class ArbitrageOpportunityFilter(
    val dexCurrencyIds: Set<DexCurrencyId>,
    val dexExchangeIds: Set<DexExchangeId>,
    val dexChainIds: Set<DexChainId>,
    val cexCurrencyIds: Set<CexCurrencyId>,
    val cexExchangeIds: Set<CexExchangeId>,
    val spread: ArbitrageOpportunitySpread,
) {
    companion object {
        val DEFAULT = ArbitrageOpportunityFilter(
            dexCurrencyIds = emptySet(),
            dexExchangeIds = emptySet(),
            dexChainIds = emptySet(),
            cexCurrencyIds = emptySet(),
            cexExchangeIds = emptySet(),
            spread = ArbitrageOpportunitySpread.DEFAULT,
        )
    }
}
