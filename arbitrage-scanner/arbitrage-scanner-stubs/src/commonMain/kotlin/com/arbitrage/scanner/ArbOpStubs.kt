package com.arbitrage.scanner

import com.arbitrage.scanner.base.Timestamp
import com.arbitrage.scanner.models.ArbitrageOpportunityId
import com.arbitrage.scanner.models.ArbitrageOpportunitySpread
import com.arbitrage.scanner.models.CexExchangeId
import com.arbitrage.scanner.models.CexPrice
import com.arbitrage.scanner.models.CexToCexArbitrageOpportunity
import com.arbitrage.scanner.models.CexTokenId
import com.arbitrage.scanner.models.RecalculateResult
import com.ionspin.kotlin.bignum.decimal.BigDecimal

object ArbOpStubs {
    val arbitrageOpportunity = CexToCexArbitrageOpportunity(
        id = ArbitrageOpportunityId("123"),
        cexTokenId = CexTokenId("BTC"),
        buyCexExchangeId = CexExchangeId("binance"),
        sellCexExchangeId = CexExchangeId("okx"),
        buyCexPriceRaw = CexPrice.CexPriceRaw(BigDecimal.fromDouble(50000.0)),
        sellCexPriceRaw = CexPrice.CexPriceRaw(BigDecimal.fromDouble(51000.0)),
        spread = ArbitrageOpportunitySpread(2.0),
        startTimestamp = Timestamp(1640995200000),
        endTimestamp = Timestamp(1640995260000)
    )

    val recalculateResult = RecalculateResult(
        opportunitiesCount = 1,
        processingTimeMs = 100L,
    )
}