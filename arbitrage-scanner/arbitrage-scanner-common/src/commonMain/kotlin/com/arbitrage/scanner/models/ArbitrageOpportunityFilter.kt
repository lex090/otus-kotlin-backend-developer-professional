package com.arbitrage.scanner.models

data class ArbitrageOpportunityFilter(
    val dexCurrencyIds: Set<DexCurrencyId>,
    val dexExchangeIds: Set<DexExchangeId>,
    val dexChainIds: Set<DexChainId>,
    val cexCurrencyIds: Set<CexCurrencyId>,
    val cexExchangeIds: Set<CexExchangeId>,
    val spread: ArbitrageOpportunitySpread,
)
