package com.arbitrage.scanner.models

import com.arbitrage.scanner.base.Timestamp
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlin.jvm.JvmInline

data class DexPrice(
    val tokenId: DexTokenId,
    val exchangeId: DexExchangeId,
    val chainId: DexChainId,
    val priceRaw: DexPriceRaw,
    val timeStamp: Timestamp,
) {

    @JvmInline
    value class DexPriceRaw(val value: BigDecimal) {

        fun isDefault(): Boolean = this == DEFAULT

        fun isNotDefault(): Boolean = !isDefault()

        companion object {
            val DEFAULT = DexPriceRaw(BigDecimal.ZERO)
        }
    }

    companion object {
        val DEFAULT = DexPrice(
            tokenId = DexTokenId.DEFAULT,
            exchangeId = DexExchangeId.DEFAULT,
            chainId = DexChainId.DEFAULT,
            priceRaw = DexPriceRaw.DEFAULT,
            timeStamp = Timestamp.DEFAULT
        )
    }
}
