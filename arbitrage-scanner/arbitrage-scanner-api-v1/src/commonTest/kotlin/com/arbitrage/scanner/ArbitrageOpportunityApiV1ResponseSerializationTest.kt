package com.arbitrage.scanner

import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityReadResponse
import com.arbitrage.scanner.api.v1.models.CexToCexArbitrageOpportunityApi
import com.arbitrage.scanner.api.v1.models.ResponseResult
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class ArbitrageOpportunityApiV1ResponseSerializationTest {

    private val givenResponse = ArbitrageOpportunityReadResponse(
        result = ResponseResult.SUCCESS,
        errors = emptyList(),
        arbitrageOpportunity = CexToCexArbitrageOpportunityApi(
            id = "test-id",
            cexTokenId = "BTC",
            buyCexExchangeId = "binance",
            sellCexExchangeId = "okx",
            buyCexPriceRaw = 50000.0,
            sellCexPriceRaw = 51000.0,
            spread = 2.0,
            timestampStart = 1640995200000,
            timestampEnd = 1640995260000
        )
    )

    private val givenJsonString =
        """{"responseType":"read","result":"success","errors":[],"arbitrageOpportunity":{"id":"test-id","cexTokenId":"BTC","buyCexExchangeId":"binance","sellCexExchangeId":"okx","buyCexPriceRaw":50000.0,"sellCexPriceRaw":51000.0,"spread":2.0,"timestampStart":1640995200000,"timestampEnd":1640995260000}}"""

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