package com.arbitrage.scanner

import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityDebug
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityReadRequest
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityRequestDebugMode
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityRequestDebugStubs
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class ArbitrageOpportunityApiV1RequestSerializationTest {

    private val givenRequest = ArbitrageOpportunityReadRequest(
        id = "test-id",
        debug = ArbitrageOpportunityDebug(
            mode = ArbitrageOpportunityRequestDebugMode.STUB,
            stub = ArbitrageOpportunityRequestDebugStubs.SUCCESS
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