package com.arbitrage.scanner.models

import kotlin.jvm.JvmInline

data class CexPrice(
    val currencyId: CexCurrencyId,
    val exchangeId: CexExchangeId,
    val priceRaw: CexPriceRaw,
    val timeStamp: TimeStamp,
) {

    @JvmInline
    value class CexPriceRaw(val value: Double) { // TODO Тут надо что типо BigDecimal использовать
        companion object {
            val DEFAULT = CexPriceRaw(0.0)
        }
    }

    companion object {
        val DEFAULT = CexPrice(
            currencyId = CexCurrencyId.DEFAULT,
            exchangeId = CexExchangeId.DEFAULT,
            priceRaw = CexPriceRaw.DEFAULT,
            timeStamp = TimeStamp.DEFAULT
        )
    }
}
