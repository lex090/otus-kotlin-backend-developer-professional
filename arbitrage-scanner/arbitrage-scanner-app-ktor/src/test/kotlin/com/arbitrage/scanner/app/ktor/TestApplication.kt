package com.arbitrage.scanner.app.ktor

import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityDebugApi
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityReadRequest
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityReadResponse
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityRecalculateRequest
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityRecalculateResponse
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityRequestDebugModeApi
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityRequestDebugStubsApi
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunitySearchFilterApi
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunitySearchRequest
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunitySearchResponse
import com.arbitrage.scanner.api.v1.models.CexToCexArbitrageOpportunityApi
import com.arbitrage.scanner.api.v1.models.ResponseResult

import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.assertInstanceOf
import kotlin.test.Test
import kotlin.test.assertEquals

class TestApplication {

    @Test
    fun `read request success stub test`() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        val arbitrageOpportunityReadRequest = ArbitrageOpportunityReadRequest(
            debug = ArbitrageOpportunityDebugApi(
                mode = ArbitrageOpportunityRequestDebugModeApi.STUB,
                stub = ArbitrageOpportunityRequestDebugStubsApi.SUCCESS
            ),
            id = "123"
        )
        val response = client.post("v1/arbitrage_opportunities/read") {
            contentType(ContentType.Application.Json)
            setBody(arbitrageOpportunityReadRequest)
        }

        val arbitrageOpportunityReadResponse = response.body<ArbitrageOpportunityReadResponse>()
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(ResponseResult.SUCCESS, arbitrageOpportunityReadResponse.result)
        assertInstanceOf<CexToCexArbitrageOpportunityApi>(arbitrageOpportunityReadResponse.arbitrageOpportunity)
        when (val arbitrageOpportunity = arbitrageOpportunityReadResponse.arbitrageOpportunity) {
            is CexToCexArbitrageOpportunityApi -> {
                assertEquals(
                    "123",
                    arbitrageOpportunity.id
                )
            }
        }
    }

    @Test
    fun `search request success stub test`() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        val arbitrageOpportunitySearchRequest = ArbitrageOpportunitySearchRequest(
            debug = ArbitrageOpportunityDebugApi(
                mode = ArbitrageOpportunityRequestDebugModeApi.STUB,
                stub = ArbitrageOpportunityRequestDebugStubsApi.SUCCESS
            ),
            filter = ArbitrageOpportunitySearchFilterApi(
                cexTokenIds = emptySet(),
                cexExchangeIds = emptySet(),
                spread = 1.0,
            ),
        )
        val response = client.post("v1/arbitrage_opportunities/search") {
            contentType(ContentType.Application.Json)
            setBody(arbitrageOpportunitySearchRequest)
        }

        val arbitrageOpportunitySearchResponse = response.body<ArbitrageOpportunitySearchResponse>()
        assertEquals(HttpStatusCode.Companion.OK, response.status)
        assertEquals(ResponseResult.SUCCESS, arbitrageOpportunitySearchResponse.result)
        assertEquals(1, arbitrageOpportunitySearchResponse.arbitrageOpportunities?.size)
        arbitrageOpportunitySearchResponse.arbitrageOpportunities?.forEach { arbitrageOpportunity ->
            when (arbitrageOpportunity) {
                is CexToCexArbitrageOpportunityApi -> {
                    assertEquals(
                        "123",
                        arbitrageOpportunity.id
                    )
                }
            }
        }
    }

    @Test
    fun `recalculate request success stub test`() = testApplication {
        application { module() }

        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        val arbitrageOpportunityRecalculateRequest = ArbitrageOpportunityRecalculateRequest(
            debug = ArbitrageOpportunityDebugApi(
                mode = ArbitrageOpportunityRequestDebugModeApi.STUB,
                stub = ArbitrageOpportunityRequestDebugStubsApi.SUCCESS
            ),
        )
        val response = client.post("v1/arbitrage_opportunities/recalculate") {
            contentType(ContentType.Application.Json)
            setBody(arbitrageOpportunityRecalculateRequest)
        }

        val arbitrageOpportunityRecalculateResponse = response.body<ArbitrageOpportunityRecalculateResponse>()
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(ResponseResult.SUCCESS, arbitrageOpportunityRecalculateResponse.result)
        assertEquals(1, arbitrageOpportunityRecalculateResponse.opportunitiesCount)
        assertEquals(100L, arbitrageOpportunityRecalculateResponse.processingTimeMs)
    }
}