package com.arbitrage.scanner.models

import com.arbitrage.scanner.base.Timestamp
import kotlin.jvm.JvmInline

data class CexPrice(
    val currencyId: CexTokenId,
    val exchangeId: CexExchangeId,
    val priceRaw: CexPriceRaw,
    val timeStamp: Timestamp,
) {

    @JvmInline
    value class CexPriceRaw(val value: Double) { // TODO Тут надо что типо BigDecimal использовать
        companion object {
            val DEFAULT = CexPriceRaw(0.0)
        }
    }

    companion object {
        val DEFAULT = CexPrice(
            currencyId = CexTokenId.DEFAULT,
            exchangeId = CexExchangeId.DEFAULT,
            priceRaw = CexPriceRaw.DEFAULT,
            timeStamp = Timestamp.DEFAULT
        )
    }
}
