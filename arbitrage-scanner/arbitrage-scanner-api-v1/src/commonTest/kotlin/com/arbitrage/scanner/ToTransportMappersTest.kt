package com.arbitrage.scanner

import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityReadResponse
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunitySearchResponse
import com.arbitrage.scanner.api.v1.models.CexToCexArbitrageOpportunityApi
import com.arbitrage.scanner.api.v1.models.ResponseResult
import com.arbitrage.scanner.base.Command
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.base.StubCase
import com.arbitrage.scanner.base.Timestamp
import com.arbitrage.scanner.base.WorkMode
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.mappers.toTransport
import com.arbitrage.scanner.models.ArbitrageOpportunityId
import com.arbitrage.scanner.models.ArbitrageOpportunitySpread
import com.arbitrage.scanner.models.CexExchangeId
import com.arbitrage.scanner.models.CexPrice
import com.arbitrage.scanner.models.CexTokenId
import com.arbitrage.scanner.models.CexToCexArbitrageOpportunity
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

class ToTransportMappersTest {

    private val stub = CexToCexArbitrageOpportunity(
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

    private val transportStub = CexToCexArbitrageOpportunityApi(
        id = "123",
        cexTokenId = "BTC",
        buyCexExchangeId = "binance",
        sellCexExchangeId = "okx",
        buyCexPriceRaw = 50000.0,
        sellCexPriceRaw = 51000.0,
        spread = 2.0,
        timestampStart = 1640995200000,
        timestampEnd = 1640995260000
    )

    @Test
    fun test1() {
        val givenContext = Context(
            command = Command.READ,
            workMode = WorkMode.PROD,
            state = State.FINISHING,
            stubCase = StubCase.NONE,
            arbitrageOpportunityReadResponse = stub
        )

        val expectedTransport = ArbitrageOpportunityReadResponse(
            result = ResponseResult.SUCCESS,
            errors = null,
            arbitrageOpportunity = transportStub
        )

        val transport = givenContext.toTransport()

        assertEquals(expectedTransport, transport, "expected: $expectedTransport, found: $transport")
    }

    @Test
    fun test2() {
        val givenContext = Context(
            command = Command.SEARCH,
            workMode = WorkMode.PROD,
            state = State.FINISHING,
            stubCase = StubCase.NONE,
            arbitrageOpportunitySearchResponse = mutableSetOf(stub)
        )

        val expectedTransport = ArbitrageOpportunitySearchResponse(
            result = ResponseResult.SUCCESS,
            errors = null,
            arbitrageOpportunities = listOf(transportStub),
        )

        val transport = givenContext.toTransport()

        assertEquals(expectedTransport, transport, "expected: $expectedTransport, found: $transport")
    }
}