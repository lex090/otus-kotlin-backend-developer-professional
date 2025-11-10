package com.arbitrage.scanner.app.ktor

import com.arbitrage.scanner.StubsDataFactory
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityDebugApi
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityReadRequest
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityReadResponse
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityRecalculateRequest
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityRecalculateResponse
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityRequestDebugModeApi
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunitySearchFilterApi
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunitySearchRequest
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunitySearchResponse
import com.arbitrage.scanner.api.v1.models.IRequest
import com.arbitrage.scanner.api.v1.models.ResponseResult
import com.arbitrage.scanner.mappers.toTransport
import com.arbitrage.scanner.mappers.toTransportId
import com.arbitrage.scanner.mappers.toTransportRawPrice
import com.arbitrage.scanner.models.CexPrice
import com.arbitrage.scanner.models.CexToCexArbitrageOpportunity
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
import org.koin.core.module.Module
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class KtorApplicationBaseTest {

    //  Либо test, либо prod
    protected abstract val workMode: ArbitrageOpportunityRequestDebugModeApi

    protected abstract val readModule: Module
    protected abstract val searchModule: Module
    protected abstract val recalculateModule: Module

    protected val initServiceObject: List<CexPrice> = StubsDataFactory.getDefaultCexPrices().toList()

    protected val initRepoObject: List<CexToCexArbitrageOpportunity> =
        listOf(
            StubsDataFactory.createArbitrageOpportunity("test-token-id1", "BTC", spread = 1.8),
            StubsDataFactory.createArbitrageOpportunity("test-token-id2", "ETH", spread = 2.8),
            StubsDataFactory.createArbitrageOpportunity("test-token-id3", "USDT", spread = 0.8),
        )

    protected val filter: ArbitrageOpportunitySearchFilterApi = ArbitrageOpportunitySearchFilterApi(
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
    )

    @Test
    fun `read request success stub test2`() = testApplicationV2(
        module = readModule,
        request = ArbitrageOpportunityReadRequest(
            debug = ArbitrageOpportunityDebugApi(mode = workMode),
            id = initRepoObject[0].id.toTransportId(),
        ),
        method = "read",
    ) { response ->
        val responseBody = response.body<ArbitrageOpportunityReadResponse>()
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(ResponseResult.SUCCESS, responseBody.result)

        val arbitrageOpportunity = responseBody.arbitrageOpportunity
        assertNotNull(arbitrageOpportunity)

        val expected = initRepoObject[0]
        assertEquals(expected.id.toTransportId(), arbitrageOpportunity.id)
        assertEquals(expected.cexTokenId.toTransportId(), arbitrageOpportunity.cexTokenId)
        assertEquals(expected.buyCexExchangeId.toTransportId(), arbitrageOpportunity.buyCexExchangeId)
        assertEquals(expected.sellCexExchangeId.toTransportId(), arbitrageOpportunity.sellCexExchangeId)
        assertEquals(expected.buyCexPriceRaw.toTransportRawPrice(), arbitrageOpportunity.buyCexPriceRaw)
        assertEquals(expected.sellCexPriceRaw.toTransportRawPrice(), arbitrageOpportunity.sellCexPriceRaw)
        assertEquals(expected.spread.toTransport(), arbitrageOpportunity.spread)
        assertEquals(expected.startTimestamp.toTransport(), arbitrageOpportunity.timestampStart)
        assertEquals(expected.endTimestamp.toTransport(), arbitrageOpportunity.timestampEnd)
    }

    @Test
    fun `search request success stub test2`() =
        testApplicationV2(
            module = searchModule,
            request = ArbitrageOpportunitySearchRequest(
                debug = ArbitrageOpportunityDebugApi(mode = workMode),
                filter = filter,
            ),
            method = "search",
        ) { response ->
            val responseBody = response.body<ArbitrageOpportunitySearchResponse>()
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(ResponseResult.SUCCESS, responseBody.result)

            val arbitrageOpportunities = responseBody.arbitrageOpportunities
            assertNotNull(arbitrageOpportunities)
            // Фильтр minSpread = 1.0, поэтому должны вернуться BTC (1.8) и ETH (2.8), но не USDT (0.8)
            assertEquals(2, arbitrageOpportunities.size)

            // Проверяем первый элемент (BTC)
            val firstOpportunity = arbitrageOpportunities[0]
            val expectedFirst = initRepoObject[0]
            assertEquals(expectedFirst.id.toTransportId(), firstOpportunity.id)
            assertEquals(expectedFirst.cexTokenId.toTransportId(), firstOpportunity.cexTokenId)
            assertEquals(expectedFirst.buyCexExchangeId.toTransportId(), firstOpportunity.buyCexExchangeId)
            assertEquals(expectedFirst.sellCexExchangeId.toTransportId(), firstOpportunity.sellCexExchangeId)
            assertEquals(expectedFirst.buyCexPriceRaw.toTransportRawPrice(), firstOpportunity.buyCexPriceRaw)
            assertEquals(expectedFirst.sellCexPriceRaw.toTransportRawPrice(), firstOpportunity.sellCexPriceRaw)
            assertEquals(expectedFirst.spread.toTransport(), firstOpportunity.spread)
            assertEquals(expectedFirst.startTimestamp.toTransport(), firstOpportunity.timestampStart)
            assertEquals(expectedFirst.endTimestamp.toTransport(), firstOpportunity.timestampEnd)

            // Проверяем второй элемент (ETH)
            val secondOpportunity = arbitrageOpportunities[1]
            val expectedSecond = initRepoObject[1]
            assertEquals(expectedSecond.id.toTransportId(), secondOpportunity.id)
            assertEquals(expectedSecond.cexTokenId.toTransportId(), secondOpportunity.cexTokenId)
            assertEquals(expectedSecond.buyCexExchangeId.toTransportId(), secondOpportunity.buyCexExchangeId)
            assertEquals(expectedSecond.sellCexExchangeId.toTransportId(), secondOpportunity.sellCexExchangeId)
            assertEquals(expectedSecond.buyCexPriceRaw.toTransportRawPrice(), secondOpportunity.buyCexPriceRaw)
            assertEquals(expectedSecond.sellCexPriceRaw.toTransportRawPrice(), secondOpportunity.sellCexPriceRaw)
            assertEquals(expectedSecond.spread.toTransport(), secondOpportunity.spread)
            assertEquals(expectedSecond.startTimestamp.toTransport(), secondOpportunity.timestampStart)
            assertEquals(expectedSecond.endTimestamp.toTransport(), secondOpportunity.timestampEnd)
        }

    @Test
    fun `recalculate request success stub test`() =
        testApplicationV2(
            module = recalculateModule,
            request = ArbitrageOpportunityRecalculateRequest(
                debug = ArbitrageOpportunityDebugApi(mode = workMode),
            ),
            method = "recalculate",
        ) { response ->
            val responseBody = response.body<ArbitrageOpportunityRecalculateResponse>()
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(ResponseResult.SUCCESS, responseBody.result)

            // Проверяем, что найдены арбитражные возможности
            // На основе initServiceObject ожидаем как минимум 2 возможности:
            // 1. BTC: binance (50000) -> okx (51000) = 2% spread
            // 2. ETH: binance (4000) -> bybit (4050) = 1.25% spread
            val opportunitiesCount = responseBody.opportunitiesCount
            assertNotNull(opportunitiesCount)
            assertEquals(2, opportunitiesCount)

            // Проверяем, что время обработки не null и неотрицательное
            val processingTime = responseBody.processingTimeMs
            assertNotNull(processingTime)
            assert(processingTime >= 0) { "Processing time should be greater than 0" }
        }

    private inline fun testApplicationV2(
        module: Module,
        request: IRequest,
        method: String,
        crossinline block: suspend (HttpResponse) -> Unit,
    ): Unit = testApplication {
        application { moduleTest(module) }

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