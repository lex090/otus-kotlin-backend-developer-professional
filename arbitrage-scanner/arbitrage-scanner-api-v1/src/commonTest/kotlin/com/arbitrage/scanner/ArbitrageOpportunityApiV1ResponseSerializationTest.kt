package com.arbitrage.scanner

import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityReadResponse
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityStatusType
import com.arbitrage.scanner.api.v1.models.ResponseResult
import com.arbitrage.scanner.api.v1.models.DexToCexSimpleArbitrageOpportunity
import com.arbitrage.scanner.api.v1.models.DexPrice
import com.arbitrage.scanner.api.v1.models.CexPrice
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class ArbitrageOpportunityApiV1ResponseSerializationTest {

    private val givenResponse = ArbitrageOpportunityReadResponse(
        result = ResponseResult.SUCCESS,
        errors = emptyList(),
        arbitrageOpportunity = DexToCexSimpleArbitrageOpportunity(
            id = "test-id",
            dexPrice = DexPrice(
                tokenId = "BTC",
                chainId = "BSC",
                exchangeId = "pancakeswap",
                priceRaw = 50000.0
            ),
            cexPrice = CexPrice(
                tokenId = "BTC",
                exchangeId = "binance",
                priceRaw = 51000.0
            ),
            spread = 2.0,
            statusType = ArbitrageOpportunityStatusType.ACTIVE,
            timestampStart = 1640995200000,
            timestampEnd = 1640995260000
        )
    )

    private val givenJsonString =
        """{"responseType":"read","result":"success","errors":[],"arbitrageOpportunity":{"opportunityType":"DexToCexSimpleArbitrageOpportunity","id":"test-id","dexPrice":{"tokenId":"BTC","chainId":"BSC","exchangeId":"pancakeswap","priceRaw":50000.0},"cexPrice":{"tokenId":"BTC","exchangeId":"binance","priceRaw":51000.0},"spread":2.0,"statusType":"active","timestampStart":1640995200000,"timestampEnd":1640995260000}}"""

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