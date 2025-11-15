package com.arbitrage.scanner.models

import com.arbitrage.scanner.base.Timestamp
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlin.jvm.JvmInline

data class CexPrice(
    val tokenId: CexTokenId,
    val exchangeId: CexExchangeId,
    val priceRaw: CexPriceRaw,
    val timeStamp: Timestamp,
) {

    @JvmInline
    value class CexPriceRaw(val value: BigDecimal) {

        fun isNone(): Boolean = this == NONE

        fun isNotNone(): Boolean = !isNone()

        companion object {
            val NONE = CexPriceRaw(value = BigDecimal.fromInt(-1))
        }
    }

    fun isNone(): Boolean = this == NONE

    fun isNotNone(): Boolean = !isNone()

    companion object {
        val NONE = CexPrice(
            tokenId = CexTokenId.NONE,
            exchangeId = CexExchangeId.NONE,
            priceRaw = CexPriceRaw.NONE,
            timeStamp = Timestamp.NONE,
        )
    }
}
