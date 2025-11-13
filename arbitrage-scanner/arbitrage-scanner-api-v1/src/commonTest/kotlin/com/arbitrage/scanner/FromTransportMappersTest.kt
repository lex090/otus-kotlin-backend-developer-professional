package com.arbitrage.scanner

import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityDebugApi
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityReadRequest
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityRequestDebugModeApi
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityRequestDebugStubsApi
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunitySearchFilterApi
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunitySearchRequest
import com.arbitrage.scanner.base.Command
import com.arbitrage.scanner.base.StubCase
import com.arbitrage.scanner.base.WorkMode
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.mappers.fromTransport
import com.arbitrage.scanner.models.ArbitrageOpportunityFilter
import com.arbitrage.scanner.models.ArbitrageOpportunityId
import com.arbitrage.scanner.models.ArbitrageOpportunitySpread
import com.arbitrage.scanner.models.CexExchangeId
import com.arbitrage.scanner.models.CexTokenId
import com.arbitrage.scanner.models.CexTokenIds
import kotlin.test.Test
import kotlin.test.assertEquals

class FromTransportMappersTest {

    @Test
    fun test1() {
        val givenTransport = ArbitrageOpportunityReadRequest(
            id = "120",
            debug = ArbitrageOpportunityDebugApi(
                mode = ArbitrageOpportunityRequestDebugModeApi.TEST,
                stub = null
            ),
        )

        val context = Context()

        val expectedContext = Context(
            command = Command.READ,
            workMode = WorkMode.TEST,
            stubCase = StubCase.NONE,
            arbitrageOpportunityReadRequest = ArbitrageOpportunityId("120"),
        )

        context.fromTransport(givenTransport)

        assertEquals(expectedContext, context, "expected: $expectedContext, found: $context")
    }

    @Test
    fun test2() {
        val givenTransport = ArbitrageOpportunityReadRequest(
            id = "120",
            debug = ArbitrageOpportunityDebugApi(
                mode = ArbitrageOpportunityRequestDebugModeApi.STUB,
                stub = ArbitrageOpportunityRequestDebugStubsApi.NOT_FOUND
            ),
        )

        val context = Context()

        val expectedContext = Context(
            command = Command.READ,
            workMode = WorkMode.STUB,
            stubCase = StubCase.NOT_FOUND,
            arbitrageOpportunityReadRequest = ArbitrageOpportunityId(value = "120"),
        )

        context.fromTransport(givenTransport)

        assertEquals(expectedContext, context, "expected: $expectedContext, found: $context")
    }

    @Test
    fun test3() {
        val givenTransport = ArbitrageOpportunityReadRequest(
            id = "120",
            debug = ArbitrageOpportunityDebugApi(
                mode = ArbitrageOpportunityRequestDebugModeApi.STUB,
                stub = ArbitrageOpportunityRequestDebugStubsApi.SUCCESS
            ),
        )

        val context = Context()

        val expectedContext = Context(
            command = Command.READ,
            workMode = WorkMode.STUB,
            stubCase = StubCase.SUCCESS,
            arbitrageOpportunityReadRequest = ArbitrageOpportunityId(value = "120"),
        )

        context.fromTransport(givenTransport)

        assertEquals(expectedContext, context, "expected: $expectedContext, found: $context")
    }

    @Test
    fun test4() {
        val givenTransport = ArbitrageOpportunityReadRequest(
            id = "120",
            debug = ArbitrageOpportunityDebugApi(
                mode = ArbitrageOpportunityRequestDebugModeApi.STUB,
                stub = ArbitrageOpportunityRequestDebugStubsApi.NOT_FOUND
            ),
        )

        val context = Context()

        val expectedContext = Context(
            command = Command.READ,
            workMode = WorkMode.STUB,
            stubCase = StubCase.NOT_FOUND,
            arbitrageOpportunityReadRequest = ArbitrageOpportunityId(value = "120"),
        )

        context.fromTransport(givenTransport)

        assertEquals(expectedContext, context, "expected: $expectedContext, found: $context")
    }

    @Test
    fun test5() {
        val givenTransport = ArbitrageOpportunityReadRequest(
            id = "120",
            debug = ArbitrageOpportunityDebugApi(
                mode = ArbitrageOpportunityRequestDebugModeApi.PROD,
                stub = null
            ),
        )

        val context = Context()

        val expectedContext = Context(
            command = Command.READ,
            workMode = WorkMode.PROD,
            stubCase = StubCase.NONE,
            arbitrageOpportunityReadRequest = ArbitrageOpportunityId(value = "120"),
        )

        context.fromTransport(givenTransport)

        assertEquals(expectedContext, context, "expected: $expectedContext, found: $context")
    }

    @Test
    fun test6() {
        val givenTransport = ArbitrageOpportunitySearchRequest(
            debug = ArbitrageOpportunityDebugApi(
                mode = ArbitrageOpportunityRequestDebugModeApi.PROD,
                stub = null
            ),
            filter = ArbitrageOpportunitySearchFilterApi(
                cexTokenIds = emptySet(),
                buyExchangeIds = emptySet(),
                sellExchangeIds = emptySet(),
                minSpread = null,
                maxSpread = null,
                status = null,
                startTimestampFrom = null,
                startTimestampTo = null,
                endTimestampFrom = null,
                endTimestampTo = null
            )
        )

        val context = Context()

        val expectedContext = Context(
            command = Command.SEARCH,
            workMode = WorkMode.PROD,
            stubCase = StubCase.NONE,
            arbitrageOpportunitySearchRequest = ArbitrageOpportunityFilter(
                cexTokenIds = CexTokenIds(emptySet())
            ),
        )

        context.fromTransport(givenTransport)

        assertEquals(
            expectedContext.command,
            context.command,
            "expected: ${expectedContext.command}, found: ${context.command}"
        )
        assertEquals(
            expectedContext.state,
            context.state,
            "expected: ${expectedContext.state}, found: ${context.state}"
        )
        assertEquals(
            expectedContext.internalErrors,
            context.internalErrors,
            "expected: ${expectedContext.internalErrors}, found: ${context.internalErrors}"
        )
        assertEquals(
            expectedContext.workMode,
            context.workMode,
            "expected: ${expectedContext.workMode}, found: ${context.workMode}"
        )
        assertEquals(
            expectedContext.stubCase,
            context.stubCase,
            "expected: ${expectedContext.stubCase}, found: ${context.stubCase}"
        )
        assertEquals(
            expectedContext.requestId,
            context.requestId,
            "expected: ${expectedContext.requestId}, found: ${context.requestId}"
        )
        assertEquals(
            expectedContext.startTimestamp,
            context.startTimestamp,
            "expected: ${expectedContext.startTimestamp}, found: ${context.startTimestamp}"
        )
        assertEquals(
            expectedContext.arbitrageOpportunityReadRequest,
            context.arbitrageOpportunityReadRequest,
            "expected: ${expectedContext.arbitrageOpportunityReadRequest}, found: ${context.arbitrageOpportunityReadRequest}"
        )
        assertEquals(
            expectedContext.arbitrageOpportunitySearchRequest,
            context.arbitrageOpportunitySearchRequest,
            "expected: ${expectedContext.arbitrageOpportunitySearchRequest}, found: ${context.arbitrageOpportunitySearchRequest}"
        )
        assertEquals(
            expectedContext.arbitrageOpportunityReadResponse,
            context.arbitrageOpportunityReadResponse,
            "expected: ${expectedContext.arbitrageOpportunityReadResponse}, found: ${context.arbitrageOpportunityReadResponse}"
        )
        assertEquals(
            expectedContext.arbitrageOpportunitySearchResponse,
            context.arbitrageOpportunitySearchResponse,
            "expected: ${expectedContext.arbitrageOpportunitySearchResponse}, found: ${context.arbitrageOpportunitySearchResponse}"
        )
    }

    @Test
    fun test7() {
        val givenTransport = ArbitrageOpportunitySearchRequest(
            debug = ArbitrageOpportunityDebugApi(
                mode = ArbitrageOpportunityRequestDebugModeApi.TEST,
                stub = null
            ),
            filter = ArbitrageOpportunitySearchFilterApi(
                cexTokenIds = setOf("1234567"),
                buyExchangeIds = setOf("binance"),
                sellExchangeIds = setOf("okx"),
                minSpread = 10.0,
                maxSpread = 30.0,
                status = null,
                startTimestampFrom = null,
                startTimestampTo = null,
                endTimestampFrom = null,
                endTimestampTo = null
            )
        )

        val context = Context()

        val expectedContext = Context(
            command = Command.SEARCH,
            workMode = WorkMode.TEST,
            stubCase = StubCase.NONE,
            arbitrageOpportunitySearchRequest = ArbitrageOpportunityFilter(
                cexTokenIds = CexTokenIds(setOf(CexTokenId("1234567"))),
                buyExchangeIds = setOf(CexExchangeId("binance")),
                sellExchangeIds = setOf(CexExchangeId("okx")),
                minSpread = ArbitrageOpportunitySpread(10.0),
                maxSpread = ArbitrageOpportunitySpread(30.0)
            ),
        )

        context.fromTransport(givenTransport)

        assertEquals(
            expectedContext.command,
            context.command,
            "expected: ${expectedContext.command}, found: ${context.command}"
        )
        assertEquals(
            expectedContext.state,
            context.state,
            "expected: ${expectedContext.state}, found: ${context.state}"
        )
        assertEquals(
            expectedContext.internalErrors,
            context.internalErrors,
            "expected: ${expectedContext.internalErrors}, found: ${context.internalErrors}"
        )
        assertEquals(
            expectedContext.workMode,
            context.workMode,
            "expected: ${expectedContext.workMode}, found: ${context.workMode}"
        )
        assertEquals(
            expectedContext.stubCase,
            context.stubCase,
            "expected: ${expectedContext.stubCase}, found: ${context.stubCase}"
        )
        assertEquals(
            expectedContext.requestId,
            context.requestId,
            "expected: ${expectedContext.requestId}, found: ${context.requestId}"
        )
        assertEquals(
            expectedContext.startTimestamp,
            context.startTimestamp,
            "expected: ${expectedContext.startTimestamp}, found: ${context.startTimestamp}"
        )
        assertEquals(
            expectedContext.arbitrageOpportunityReadRequest,
            context.arbitrageOpportunityReadRequest,
            "expected: ${expectedContext.arbitrageOpportunityReadRequest}, found: ${context.arbitrageOpportunityReadRequest}"
        )
        assertEquals(
            expectedContext.arbitrageOpportunitySearchRequest,
            context.arbitrageOpportunitySearchRequest,
            "expected: ${expectedContext.arbitrageOpportunitySearchRequest}, found: ${context.arbitrageOpportunitySearchRequest}"
        )
        assertEquals(
            expectedContext.arbitrageOpportunityReadResponse,
            context.arbitrageOpportunityReadResponse,
            "expected: ${expectedContext.arbitrageOpportunityReadResponse}, found: ${context.arbitrageOpportunityReadResponse}"
        )
        assertEquals(
            expectedContext.arbitrageOpportunitySearchResponse,
            context.arbitrageOpportunitySearchResponse,
            "expected: ${expectedContext.arbitrageOpportunitySearchResponse}, found: ${context.arbitrageOpportunitySearchResponse}"
        )
    }
}