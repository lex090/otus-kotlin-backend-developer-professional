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

        val givenContext = Context.DEFAULT.copy(
            command = Command.READ,
            workMode = WorkMode.TEST,
            stubCase = Stubs.NONE,
            arbitrageOpportunityReadRequest = ArbitrageOpportunityId("120"),
        )

        val context = fromTransport(givenTransport)

        assertEquals(givenContext, context, "expected: $givenContext, found: $context")
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

        val givenContext = Context.DEFAULT.copy(
            command = Command.READ,
            workMode = WorkMode.STUB,
            stubCase = Stubs.NOT_FOUND,
            arbitrageOpportunityReadRequest = ArbitrageOpportunityId(value = "120"),
        )

        val context = fromTransport(givenTransport)

        assertEquals(givenContext, context, "expected: $givenContext, found: $context")
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

        val givenContext = Context.DEFAULT.copy(
            command = Command.READ,
            workMode = WorkMode.STUB,
            stubCase = Stubs.SUCCESS,
            arbitrageOpportunityReadRequest = ArbitrageOpportunityId(value = "120"),
        )

        val context = fromTransport(givenTransport)

        assertEquals(givenContext, context, "expected: $givenContext, found: $context")
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

        val givenContext = Context.DEFAULT.copy(
            command = Command.READ,
            workMode = WorkMode.STUB,
            stubCase = Stubs.NOT_FOUND,
            arbitrageOpportunityReadRequest = ArbitrageOpportunityId(value = "120"),
        )

        val context = fromTransport(givenTransport)

        assertEquals(givenContext, context, "expected: $givenContext, found: $context")
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

        val givenContext = Context.DEFAULT.copy(
            command = Command.READ,
            workMode = WorkMode.PROD,
            stubCase = Stubs.NONE,
            arbitrageOpportunityReadRequest = ArbitrageOpportunityId(value = "120"),
        )

        val context = fromTransport(givenTransport)

        assertEquals(givenContext, context, "expected: $givenContext, found: $context")
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

        val givenContext = Context.DEFAULT.copy(
            command = Command.SEARCH,
            workMode = WorkMode.PROD,
            stubCase = Stubs.NONE,
            arbitrageOpportunitySearchRequest = ArbitrageOpportunityFilter.DEFAULT,
        )

        val context = fromTransport(givenTransport)

        assertEquals(
            givenContext.command,
            context.command,
            "expected: ${givenContext.command}, found: ${context.command}"
        )
        assertEquals(givenContext.state, context.state, "expected: ${givenContext.state}, found: ${context.state}")
        assertEquals(
            givenContext.internalErrors,
            context.internalErrors,
            "expected: ${givenContext.internalErrors}, found: ${context.internalErrors}"
        )
        assertEquals(
            givenContext.workMode,
            context.workMode,
            "expected: ${givenContext.workMode}, found: ${context.workMode}"
        )
        assertEquals(
            givenContext.stubCase,
            context.stubCase,
            "expected: ${givenContext.stubCase}, found: ${context.stubCase}"
        )
        assertEquals(
            givenContext.requestId,
            context.requestId,
            "expected: ${givenContext.requestId}, found: ${context.requestId}"
        )
        assertEquals(
            givenContext.startTimestamp,
            context.startTimestamp,
            "expected: ${givenContext.startTimestamp}, found: ${context.startTimestamp}"
        )
        assertEquals(
            givenContext.arbitrageOpportunityReadRequest,
            context.arbitrageOpportunityReadRequest,
            "expected: ${givenContext.arbitrageOpportunityReadRequest}, found: ${context.arbitrageOpportunityReadRequest}"
        )
        assertEquals(
            givenContext.arbitrageOpportunitySearchRequest,
            context.arbitrageOpportunitySearchRequest,
            "expected: ${givenContext.arbitrageOpportunitySearchRequest}, found: ${context.arbitrageOpportunitySearchRequest}"
        )
        assertEquals(
            givenContext.arbitrageOpportunityReadResponse,
            context.arbitrageOpportunityReadResponse,
            "expected: ${givenContext.arbitrageOpportunityReadResponse}, found: ${context.arbitrageOpportunityReadResponse}"
        )
        assertEquals(
            givenContext.arbitrageOpportunitySearchResponse,
            context.arbitrageOpportunitySearchResponse,
            "expected: ${givenContext.arbitrageOpportunitySearchResponse}, found: ${context.arbitrageOpportunitySearchResponse}"
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

        val givenContext = Context.DEFAULT.copy(
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

        val context = fromTransport(givenTransport)

        assertEquals(
            givenContext.command,
            context.command,
            "expected: ${givenContext.command}, found: ${context.command}"
        )
        assertEquals(givenContext.state, context.state, "expected: ${givenContext.state}, found: ${context.state}")
        assertEquals(
            givenContext.internalErrors,
            context.internalErrors,
            "expected: ${givenContext.internalErrors}, found: ${context.internalErrors}"
        )
        assertEquals(
            givenContext.workMode,
            context.workMode,
            "expected: ${givenContext.workMode}, found: ${context.workMode}"
        )
        assertEquals(
            givenContext.stubCase,
            context.stubCase,
            "expected: ${givenContext.stubCase}, found: ${context.stubCase}"
        )
        assertEquals(
            givenContext.requestId,
            context.requestId,
            "expected: ${givenContext.requestId}, found: ${context.requestId}"
        )
        assertEquals(
            givenContext.startTimestamp,
            context.startTimestamp,
            "expected: ${givenContext.startTimestamp}, found: ${context.startTimestamp}"
        )
        assertEquals(
            givenContext.arbitrageOpportunityReadRequest,
            context.arbitrageOpportunityReadRequest,
            "expected: ${givenContext.arbitrageOpportunityReadRequest}, found: ${context.arbitrageOpportunityReadRequest}"
        )
        assertEquals(
            givenContext.arbitrageOpportunitySearchRequest,
            context.arbitrageOpportunitySearchRequest,
            "expected: ${givenContext.arbitrageOpportunitySearchRequest}, found: ${context.arbitrageOpportunitySearchRequest}"
        )
        assertEquals(
            givenContext.arbitrageOpportunityReadResponse,
            context.arbitrageOpportunityReadResponse,
            "expected: ${givenContext.arbitrageOpportunityReadResponse}, found: ${context.arbitrageOpportunityReadResponse}"
        )
        assertEquals(
            givenContext.arbitrageOpportunitySearchResponse,
            context.arbitrageOpportunitySearchResponse,
            "expected: ${givenContext.arbitrageOpportunitySearchResponse}, found: ${context.arbitrageOpportunitySearchResponse}"
        )
    }
}