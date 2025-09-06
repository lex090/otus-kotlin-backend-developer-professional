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

        fun isDefault(): Boolean = this == DEFAULT

        fun isNotDefault(): Boolean = !isDefault()

        companion object {
            val DEFAULT = CexPriceRaw(BigDecimal.ZERO)
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
