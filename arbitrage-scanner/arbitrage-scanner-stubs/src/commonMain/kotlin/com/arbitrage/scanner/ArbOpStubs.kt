package com.arbitrage.scanner

import com.arbitrage.scanner.base.Timestamp
import com.arbitrage.scanner.models.ArbitrageOpportunity
import com.arbitrage.scanner.models.ArbitrageOpportunityId
import com.arbitrage.scanner.models.ArbitrageOpportunitySpread
import com.arbitrage.scanner.models.CexExchangeId
import com.arbitrage.scanner.models.CexPrice
import com.arbitrage.scanner.models.CexTokenId
import com.arbitrage.scanner.models.DexChainId
import com.arbitrage.scanner.models.DexExchangeId
import com.arbitrage.scanner.models.DexPrice
import com.arbitrage.scanner.models.DexTokenId
import com.ionspin.kotlin.bignum.decimal.BigDecimal

object ArbOpStubs {
    val arbitrageOpportunity = ArbitrageOpportunity.DexToCexSimpleArbitrageOpportunity(
        id = ArbitrageOpportunityId("123"),
        startTimestamp = Timestamp(12),
        endTimestamp = Timestamp(13),
        dexPrice = DexPrice(
            tokenId = DexTokenId("1234_1234"),
            exchangeId = DexExchangeId("12345_12345"),
            chainId = DexChainId("123456_123456"),
            priceRaw = DexPrice.DexPriceRaw(BigDecimal.fromDouble(1243.0)),
            timeStamp = Timestamp(12),
        ),
        cexPrice = CexPrice(
            tokenId = CexTokenId("12340_12340"),
            exchangeId = CexExchangeId("123450_123450"),
            priceRaw = CexPrice.CexPriceRaw(BigDecimal.fromDouble(1243.0)),
            timeStamp = Timestamp(12),
        ),
        spread = ArbitrageOpportunitySpread(12313.0)
    )
}