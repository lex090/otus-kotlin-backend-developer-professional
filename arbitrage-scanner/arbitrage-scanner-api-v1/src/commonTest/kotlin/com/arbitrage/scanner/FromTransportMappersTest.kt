package com.arbitrage.scanner

import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityDebug
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityReadRequest
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityRequestDebugMode
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityRequestDebugStubs
import com.arbitrage.scanner.base.Command
import com.arbitrage.scanner.base.RequestId
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.base.Timestamp
import com.arbitrage.scanner.base.WorkMode
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.mappers.fromTransport
import com.arbitrage.scanner.models.ArbitrageOpportunity
import com.arbitrage.scanner.models.ArbitrageOpportunityFilter
import com.arbitrage.scanner.models.ArbitrageOpportunityId
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

        val givenContext = Context(
            command = Command.READ,
            state = State.NONE,
            internalErrors = emptyList(),
            workMode = WorkMode.TEST,
            stubCase = Stubs.NONE,
            requestId = RequestId.DEFAULT,
            startTimestamp = Timestamp.DEFAULT,
            arbitrageOpportunityReadRequest = ArbitrageOpportunityId(value = "120"),
            arbitrageOpportunitySearchRequest = ArbitrageOpportunityFilter.DEFAULT,
            arbitrageOpportunityReadResponse = ArbitrageOpportunity.DexToCexSimpleArbitrageOpportunity.DEFAULT,
            arbitrageOpportunitySearchResponse = emptySet()
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

        val givenContext = Context(
            command = Command.READ,
            state = State.NONE,
            internalErrors = emptyList(),
            workMode = WorkMode.STUB,
            stubCase = Stubs.NOT_FOUND,
            requestId = RequestId.DEFAULT,
            startTimestamp = Timestamp.DEFAULT,
            arbitrageOpportunityReadRequest = ArbitrageOpportunityId(value = "120"),
            arbitrageOpportunitySearchRequest = ArbitrageOpportunityFilter.DEFAULT,
            arbitrageOpportunityReadResponse = ArbitrageOpportunity.DexToCexSimpleArbitrageOpportunity.DEFAULT,
            arbitrageOpportunitySearchResponse = emptySet()
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

        val givenContext = Context(
            command = Command.READ,
            state = State.NONE,
            internalErrors = emptyList(),
            workMode = WorkMode.STUB,
            stubCase = Stubs.SUCCESS,
            requestId = RequestId.DEFAULT,
            startTimestamp = Timestamp.DEFAULT,
            arbitrageOpportunityReadRequest = ArbitrageOpportunityId(value = "120"),
            arbitrageOpportunitySearchRequest = ArbitrageOpportunityFilter.DEFAULT,
            arbitrageOpportunityReadResponse = ArbitrageOpportunity.DexToCexSimpleArbitrageOpportunity.DEFAULT,
            arbitrageOpportunitySearchResponse = emptySet()
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

        val givenContext = Context(
            command = Command.READ,
            state = State.NONE,
            internalErrors = emptyList(),
            workMode = WorkMode.STUB,
            stubCase = Stubs.NOT_FOUND,
            requestId = RequestId.DEFAULT,
            startTimestamp = Timestamp.DEFAULT,
            arbitrageOpportunityReadRequest = ArbitrageOpportunityId(value = "120"),
            arbitrageOpportunitySearchRequest = ArbitrageOpportunityFilter.DEFAULT,
            arbitrageOpportunityReadResponse = ArbitrageOpportunity.DexToCexSimpleArbitrageOpportunity.DEFAULT,
            arbitrageOpportunitySearchResponse = emptySet()
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

        val givenContext = Context(
            command = Command.READ,
            state = State.NONE,
            internalErrors = emptyList(),
            workMode = WorkMode.PROD,
            stubCase = Stubs.NONE,
            requestId = RequestId.DEFAULT,
            startTimestamp = Timestamp.DEFAULT,
            arbitrageOpportunityReadRequest = ArbitrageOpportunityId(value = "120"),
            arbitrageOpportunitySearchRequest = ArbitrageOpportunityFilter.DEFAULT,
            arbitrageOpportunityReadResponse = ArbitrageOpportunity.DexToCexSimpleArbitrageOpportunity.DEFAULT,
            arbitrageOpportunitySearchResponse = emptySet()
        )

        val context = fromTransport(givenTransport)

        assertEquals(givenContext, context, "expected: $givenContext, found: $context")
    }
}