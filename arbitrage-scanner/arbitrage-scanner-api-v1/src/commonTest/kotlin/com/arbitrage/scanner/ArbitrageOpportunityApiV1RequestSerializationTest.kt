package com.arbitrage.scanner

import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityReadRequest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class ArbitrageOpportunityApiV1RequestSerializationTest {

    val givenRequest = ArbitrageOpportunityReadRequest(id = "test-id")

    val givenJsonString = """{"requestType":"read","id":"test-id"}"""

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