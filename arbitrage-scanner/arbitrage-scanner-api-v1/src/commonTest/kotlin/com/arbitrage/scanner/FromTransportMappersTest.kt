package com.arbitrage.scanner

import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityDebug
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityReadRequest
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityRequestDebugMode
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityRequestDebugStubs
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunitySearchFilter
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunitySearchRequest
import com.arbitrage.scanner.base.Command
import com.arbitrage.scanner.base.WorkMode
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.mappers.fromTransport
import com.arbitrage.scanner.models.ArbitrageOpportunityFilter
import com.arbitrage.scanner.models.ArbitrageOpportunityId
import com.arbitrage.scanner.models.ArbitrageOpportunitySpread
import com.arbitrage.scanner.models.CexExchangeId
import com.arbitrage.scanner.models.CexTokenId
import com.arbitrage.scanner.models.DexChainId
import com.arbitrage.scanner.models.DexExchangeId
import com.arbitrage.scanner.models.DexTokenId
import com.arbitrage.scanner.stubs.Stubs
import kotlin.test.Test
import kotlin.test.assertEquals

class FromTransportMappersTest {

    @Test
    fun test1() {
        val givenTransport = ArbitrageOpportunityReadRequest(
            id = "120",
            debug = ArbitrageOpportunityDebug(
                mode = ArbitrageOpportunityRequestDebugMode.TEST,
                stub = null
            ),
        )

        val context = Context()

        val expectedContext = Context(
            command = Command.READ,
            workMode = WorkMode.TEST,
            stubCase = Stubs.NONE,
            arbitrageOpportunityReadRequest = ArbitrageOpportunityId("120"),
        )

        context.fromTransport(givenTransport)

        assertEquals(expectedContext, context, "expected: $expectedContext, found: $context")
    }

    @Test
    fun test2() {
        val givenTransport = ArbitrageOpportunityReadRequest(
            id = "120",
            debug = ArbitrageOpportunityDebug(
                mode = ArbitrageOpportunityRequestDebugMode.STUB,
                stub = ArbitrageOpportunityRequestDebugStubs.NOT_FOUND
            ),
        )

        val context = Context()

        val expectedContext = Context(
            command = Command.READ,
            workMode = WorkMode.STUB,
            stubCase = Stubs.NOT_FOUND,
            arbitrageOpportunityReadRequest = ArbitrageOpportunityId(value = "120"),
        )

        context.fromTransport(givenTransport)

        assertEquals(expectedContext, context, "expected: $expectedContext, found: $context")
    }

    @Test
    fun test3() {
        val givenTransport = ArbitrageOpportunityReadRequest(
            id = "120",
            debug = ArbitrageOpportunityDebug(
                mode = ArbitrageOpportunityRequestDebugMode.STUB,
                stub = ArbitrageOpportunityRequestDebugStubs.SUCCESS
            ),
        )

        val context = Context()

        val expectedContext = Context(
            command = Command.READ,
            workMode = WorkMode.STUB,
            stubCase = Stubs.SUCCESS,
            arbitrageOpportunityReadRequest = ArbitrageOpportunityId(value = "120"),
        )

        context.fromTransport(givenTransport)

        assertEquals(expectedContext, context, "expected: $expectedContext, found: $context")
    }

    @Test
    fun test4() {
        val givenTransport = ArbitrageOpportunityReadRequest(
            id = "120",
            debug = ArbitrageOpportunityDebug(
                mode = ArbitrageOpportunityRequestDebugMode.STUB,
                stub = ArbitrageOpportunityRequestDebugStubs.NOT_FOUND
            ),
        )

        val context = Context()

        val expectedContext = Context(
            command = Command.READ,
            workMode = WorkMode.STUB,
            stubCase = Stubs.NOT_FOUND,
            arbitrageOpportunityReadRequest = ArbitrageOpportunityId(value = "120"),
        )

        context.fromTransport(givenTransport)

        assertEquals(expectedContext, context, "expected: $expectedContext, found: $context")
    }

    @Test
    fun test5() {
        val givenTransport = ArbitrageOpportunityReadRequest(
            id = "120",
            debug = ArbitrageOpportunityDebug(
                mode = ArbitrageOpportunityRequestDebugMode.PROD,
                stub = null
            ),
        )

        val context = Context()

        val expectedContext = Context(
            command = Command.READ,
            workMode = WorkMode.PROD,
            stubCase = Stubs.NONE,
            arbitrageOpportunityReadRequest = ArbitrageOpportunityId(value = "120"),
        )

        context.fromTransport(givenTransport)

        assertEquals(expectedContext, context, "expected: $expectedContext, found: $context")
    }

    @Test
    fun test6() {
        val givenTransport = ArbitrageOpportunitySearchRequest(
            debug = ArbitrageOpportunityDebug(
                mode = ArbitrageOpportunityRequestDebugMode.PROD,
                stub = null
            ),
            filter = ArbitrageOpportunitySearchFilter(
                dexTokenIds = emptySet(),
                dexExchangeIds = emptySet(),
                dexChainIds = emptySet(),
                cexTokenIds = emptySet(),
                cexExchangeIds = emptySet(),
                spread = null
            )
        )

        val context = Context()

        val expectedContext = Context(
            command = Command.SEARCH,
            workMode = WorkMode.PROD,
            stubCase = Stubs.NONE,
            arbitrageOpportunitySearchRequest = ArbitrageOpportunityFilter.DEFAULT,
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
            debug = ArbitrageOpportunityDebug(
                mode = ArbitrageOpportunityRequestDebugMode.TEST,
                stub = null
            ),
            filter = ArbitrageOpportunitySearchFilter(
                dexTokenIds = setOf("1234"),
                dexExchangeIds = setOf("12345"),
                dexChainIds = setOf("123456"),
                cexTokenIds = setOf("1234567"),
                cexExchangeIds = setOf("12345678"),
                spread = 20.0,
            )
        )

        val context = Context()

        val expectedContext = Context(
            command = Command.SEARCH,
            workMode = WorkMode.TEST,
            stubCase = Stubs.NONE,
            arbitrageOpportunitySearchRequest = ArbitrageOpportunityFilter(
                dexTokenIds = setOf(DexTokenId("1234")),
                dexExchangeIds = setOf(DexExchangeId("12345")),
                dexChainIds = setOf(DexChainId("123456")),
                cexTokenIds = setOf(CexTokenId("1234567")),
                cexExchangeIds = setOf(CexExchangeId("12345678")),
                spread = ArbitrageOpportunitySpread(20.0)
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