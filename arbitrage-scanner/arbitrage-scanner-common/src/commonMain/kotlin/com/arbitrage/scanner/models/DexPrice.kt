package com.arbitrage.scanner.models

import kotlin.jvm.JvmInline

data class DexPrice(
    val id: DexPriceId,
    val exchangeId: DexExchangeId,
    val chainId: DexChainId,
    val priceRaw: DexPriceRaw,
    val timeStamp: TimeStamp,
) {
    @JvmInline
    value class DexPriceId(val value: String) {
        companion object {
            val DEFAULT = DexPriceId("")
        }
    }

    @JvmInline
    value class DexPriceRaw(val value: Double) { // TODO Тут надо что типо BigDecimal использовать
        companion object {
            val DEFAULT = DexPriceRaw(0.0)
        }
    }

    companion object {
        val DEFAULT = DexPrice(
            id = DexPriceId.DEFAULT,
            exchangeId = DexExchangeId.DEFAULT,
            chainId = DexChainId.DEFAULT,
            priceRaw = DexPriceRaw.DEFAULT,
            timeStamp = TimeStamp.DEFAULT
        )
    }
}
