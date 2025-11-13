package com.arbitrage.scanner.models

import com.arbitrage.scanner.base.Timestamp
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlin.jvm.JvmInline

data class CexPrice(
    val tokenId: CexTokenId = CexTokenId.NONE,
    val exchangeId: CexExchangeId = CexExchangeId.NONE,
    val priceRaw: CexPriceRaw = CexPriceRaw.DEFAULT,
    val timeStamp: Timestamp = Timestamp.DEFAULT,
) {

    @JvmInline
    value class CexPriceRaw(val value: BigDecimal = BigDecimal.ZERO) {

        fun isDefault(): Boolean = this == DEFAULT

        fun isNotDefault(): Boolean = !isDefault()

        companion object {
            val DEFAULT = CexPriceRaw()
        }
    }

    companion object {
        val DEFAULT = CexPrice()
    }
}
