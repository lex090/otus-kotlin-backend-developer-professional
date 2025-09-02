package com.arbitrage.scanner.models

import com.arbitrage.scanner.base.Timestamp
import kotlin.jvm.JvmInline

data class CexPrice(
    val tokenId: CexTokenId,
    val exchangeId: CexExchangeId,
    val priceRaw: CexPriceRaw,
    val timeStamp: Timestamp,
) {

    @JvmInline
    value class CexPriceRaw(val value: Double) { // TODO Тут надо что типа BigDecimal использовать

        fun isDefault(): Boolean = this == DEFAULT

        fun isNotDefault(): Boolean = !isDefault()

        companion object {
            val DEFAULT = CexPriceRaw(0.0)
        }
    }

    companion object {
        val DEFAULT = CexPrice(
            tokenId = CexTokenId.DEFAULT,
            exchangeId = CexExchangeId.DEFAULT,
            priceRaw = CexPriceRaw.DEFAULT,
            timeStamp = Timestamp.DEFAULT
        )
    }
}
