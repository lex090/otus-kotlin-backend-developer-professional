package com.arbitrage.scanner

import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityDebugApi
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityReadRequest
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityRequestDebugModeApi
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityRequestDebugStubsApi
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class ArbitrageOpportunityApiV1RequestSerializationTest {

    private val givenRequest = ArbitrageOpportunityReadRequest(
        id = "test-id",
        debug = ArbitrageOpportunityDebugApi(
            mode = ArbitrageOpportunityRequestDebugModeApi.STUB,
            stub = ArbitrageOpportunityRequestDebugStubsApi.SUCCESS
        )
    )

    private val givenJsonString = """{"requestType":"read","debug":{"mode":"stub","stub":"success"},"id":"test-id"}"""

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testToRequestJsonString() {
        val jsonString = json.toRequestJsonString(givenRequest)
        assertEquals(jsonString, givenJsonString)
    }

    @Test
    fun testFromRequestJsonString() {
        val request = json.fromRequestJsonString<ArbitrageOpportunityReadRequest>(givenJsonString)
        assertEquals(request, givenRequest)
    }

    @Test
    fun testSerializationRoundTrip() {
        val jsonString = json.toRequestJsonString(givenRequest)
        val deserializedRequest = json.fromRequestJsonString<ArbitrageOpportunityReadRequest>(jsonString)

        assertEquals(givenRequest, deserializedRequest)
    }
}