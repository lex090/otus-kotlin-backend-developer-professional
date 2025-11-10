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
import com.arbitrage.scanner.api.v1.models.IRequest
import com.arbitrage.scanner.api.v1.models.ResponseResult
import com.arbitrage.scanner.app.ktor.koin.modules.blModuleTest
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.assertNotNull
import kotlin.test.Test
import kotlin.test.assertEquals

class KtorApplicationStubTest {

    @Test
    fun `read request success stub test2`() = testApplicationV2(
        request = ArbitrageOpportunityReadRequest(
            debug = ArbitrageOpportunityDebugApi(
                mode = ArbitrageOpportunityRequestDebugModeApi.STUB,
                stub = ArbitrageOpportunityRequestDebugStubsApi.SUCCESS
            ),
            id = "123"
        ),
        method = "read",
    ) { response ->
        val responseBody = response.body<ArbitrageOpportunityReadResponse>()
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(ResponseResult.SUCCESS, responseBody.result)

        val arbitrageOpportunity = responseBody.arbitrageOpportunity
        assertNotNull(arbitrageOpportunity)
        assertEquals("123", arbitrageOpportunity.id)
    }

    @Test
    fun `search request success stub test2`() =
        testApplicationV2(
            request = ArbitrageOpportunitySearchRequest(
                debug = ArbitrageOpportunityDebugApi(
                    mode = ArbitrageOpportunityRequestDebugModeApi.STUB,
                    stub = ArbitrageOpportunityRequestDebugStubsApi.SUCCESS
                ),
                filter = ArbitrageOpportunitySearchFilterApi(
                    cexTokenIds = emptySet(),
                    buyExchangeIds = emptySet(),
                    sellExchangeIds = emptySet(),
                    minSpread = 1.0,
                    maxSpread = null,
                    status = null,
                    startTimestampFrom = null,
                    startTimestampTo = null,
                    endTimestampFrom = null,
                    endTimestampTo = null
                ),
            ),
            method = "search",
        ) { response ->
            val responseBody = response.body<ArbitrageOpportunitySearchResponse>()
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(ResponseResult.SUCCESS, responseBody.result)

            val arbitrageOpportunities = responseBody.arbitrageOpportunities
            assertNotNull(arbitrageOpportunities)
            assertEquals(1, arbitrageOpportunities.size)

            val arbitrageOpportunity = arbitrageOpportunities.first()
            assertEquals("123", arbitrageOpportunity.id)
        }

    @Test
    fun `recalculate request success stub test`() =
        testApplicationV2(
            request = ArbitrageOpportunityRecalculateRequest(
                debug = ArbitrageOpportunityDebugApi(
                    mode = ArbitrageOpportunityRequestDebugModeApi.STUB,
                    stub = ArbitrageOpportunityRequestDebugStubsApi.SUCCESS
                ),
            ),
            method = "recalculate",
        ) { response ->
            val body = response.body<ArbitrageOpportunityRecalculateResponse>()
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(ResponseResult.SUCCESS, body.result)
            assertEquals(1, body.opportunitiesCount)
            assertEquals(100L, body.processingTimeMs)
        }

    private inline fun testApplicationV2(
        request: IRequest,
        method: String,
        crossinline block: suspend (HttpResponse) -> Unit,
    ): Unit = testApplication {
        application { moduleTest(blModuleTest(emptyList(), emptyList())) }

        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        val response = client.post("v1/arbitrage_opportunities/$method") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        block.invoke(response)
    }
}