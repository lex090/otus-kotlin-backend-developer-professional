package com.arbitrage.scanner.models

import kotlin.jvm.JvmInline

data class DexPrice(
    val currencyId: DexCurrencyId,
    val exchangeId: DexExchangeId,
    val chainId: DexChainId,
    val priceRaw: DexPriceRaw,
    val timeStamp: TimeStamp,
) {

    @JvmInline
    value class DexPriceRaw(val value: Double) { // TODO Тут надо что типо BigDecimal использовать
        companion object {
            val DEFAULT = DexPriceRaw(0.0)
        }
    }

    companion object {
        val DEFAULT = DexPrice(
            currencyId = DexCurrencyId.DEFAULT,
            exchangeId = DexExchangeId.DEFAULT,
            chainId = DexChainId.DEFAULT,
            priceRaw = DexPriceRaw.DEFAULT,
            timeStamp = TimeStamp.DEFAULT
        )
    }
}
