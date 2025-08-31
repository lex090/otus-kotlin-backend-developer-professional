package com.arbitrage.scanner

import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityReadResponse
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityStatus
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityStatusType
import com.arbitrage.scanner.api.v1.models.ResponseResult
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class ArbitrageOpportunityApiV1ResponseSerializationTest {

    private val givenResponse = ArbitrageOpportunityReadResponse(
        result = ResponseResult.SUCCESS,
        errors = emptyList(),
        id = "test-id",
        tokenPair = "BTC/USDT",
        buyExchange = "Binance",
        sellExchange = "PancakeSwap",
        buyPrice = 50000.0,
        sellPrice = 51000.0,
        spread = 2.0,
        status = ArbitrageOpportunityStatus(statusType = ArbitrageOpportunityStatusType.ACTIVE)
    )

    private val givenJsonString =
        """{"responseType":"read","result":"success","errors":[],"id":"test-id","tokenPair":"BTC/USDT","buyExchange":"Binance","sellExchange":"PancakeSwap","buyPrice":50000.0,"sellPrice":51000.0,"spread":2.0,"status":{"statusType":"active"}}"""

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testToResponseJsonString() {
        val jsonString = json.toResponseJsonString(givenResponse)
        assertEquals(jsonString, givenJsonString)
    }

    @Test
    fun testFromResponseJsonString() {
        val response = json.fromResponseJsonString<ArbitrageOpportunityReadResponse>(givenJsonString)
        assertEquals(response, givenResponse)
    }

    @Test
    fun testSerializationRoundTrip() {
        val jsonString = json.toResponseJsonString(givenResponse)
        val deserializedResponse = json.fromResponseJsonString<ArbitrageOpportunityReadResponse>(jsonString)

        assertEquals(givenResponse, deserializedResponse)
    }
}