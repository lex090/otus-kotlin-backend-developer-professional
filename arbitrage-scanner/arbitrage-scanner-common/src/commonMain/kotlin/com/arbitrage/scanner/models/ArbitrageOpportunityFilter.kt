package com.arbitrage.scanner.models

data class ArbitrageOpportunityFilter(
    val dexCurrencyId: DexCurrencyId,
    val dexExchangeId: DexExchangeId,
    val dexChainId: DexChainId,
    val cexCurrencyId: CexCurrencyId,
    val cexExchangeId: CexExchangeId,
    val spread: ArbitrageOpportunitySpread,
)