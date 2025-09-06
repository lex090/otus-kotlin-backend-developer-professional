package com.arbitrage.scanner

import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityReadResponse
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunitySearchResponse
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityStatusType
import com.arbitrage.scanner.api.v1.models.ResponseResult
import com.arbitrage.scanner.base.Command
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.base.Timestamp
import com.arbitrage.scanner.base.WorkMode
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.mappers.toTransport
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
import com.arbitrage.scanner.stubs.Stubs
import kotlin.test.Test
import kotlin.test.assertEquals
import com.arbitrage.scanner.api.v1.models.CexPrice as CexPriceApi
import com.arbitrage.scanner.api.v1.models.DexPrice as DexPriceApi
import com.arbitrage.scanner.api.v1.models.DexToCexSimpleArbitrageOpportunity as DexToCexSimpleArbitrageOpportunityApi

class ToTransportMappersTest {

    private val stub = ArbitrageOpportunity.DexToCexSimpleArbitrageOpportunity.create(
        id = ArbitrageOpportunityId("123"),
        startTimestamp = Timestamp(12),
        endTimestamp = Timestamp(13),
        dexPrice = DexPrice(
            tokenId = DexTokenId("1234_1234"),
            exchangeId = DexExchangeId("12345_12345"),
            chainId = DexChainId("123456_123456"),
            priceRaw = DexPrice.DexPriceRaw(1243.0),
            timeStamp = Timestamp(12),
        ),
        cexPrice = CexPrice(
            tokenId = CexTokenId("12340_12340"),
            exchangeId = CexExchangeId("123450_123450"),
            priceRaw = CexPrice.CexPriceRaw(1243.0),
            timeStamp = Timestamp(12),
        ),
        spread = ArbitrageOpportunitySpread(12313.0)
    )

    private val transportStub = DexToCexSimpleArbitrageOpportunityApi(
        id = "123",
        dexPrice = DexPriceApi(
            tokenId = "1234_1234",
            chainId = "123456_123456",
            exchangeId = "12345_12345",
            priceRaw = 1243.0
        ),
        cexPrice = CexPriceApi(
            tokenId = "12340_12340",
            exchangeId = "123450_123450",
            priceRaw = 1243.0
        ),
        spread = 12313.0,
        statusType = ArbitrageOpportunityStatusType.EXPIRED,
        timestampStart = 12,
        timestampEnd = 13
    )

    @Test
    fun test1() {
        val givenContext = Context(
            command = Command.READ,
            workMode = WorkMode.PROD,
            state = State.FINISHING,
            stubCase = Stubs.NONE,
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
            stubCase = Stubs.NONE,
            arbitrageOpportunitySearchResponse = setOf(stub)
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