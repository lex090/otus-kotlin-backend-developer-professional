package com.arbitrage.scanner.models

import com.arbitrage.scanner.base.Timestamp
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlin.jvm.JvmInline

data class CexPrice(
    val tokenId: CexTokenId = CexTokenId.NONE,
    val exchangeId: CexExchangeId = CexExchangeId.NONE,
    val priceRaw: CexPriceRaw = CexPriceRaw.NONE,
    val timeStamp: Timestamp = Timestamp.NONE,
) {

    @JvmInline
    value class CexPriceRaw(val value: BigDecimal) {

        fun isNone(): Boolean = this == NONE

        fun isNotNone(): Boolean = !isNone()

        companion object {
            val NONE = CexPriceRaw(value = BigDecimal.fromInt(-1))
        }
    }

    companion object {
        val DEFAULT = CexPrice()
    }
}
