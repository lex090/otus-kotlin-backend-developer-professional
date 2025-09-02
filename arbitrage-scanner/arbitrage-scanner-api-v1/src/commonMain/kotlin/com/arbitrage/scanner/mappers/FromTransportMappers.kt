package com.arbitrage.scanner.mappers

import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityDebug
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityReadRequest
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityRequestDebugMode
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityRequestDebugStubs
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunitySearchFilter
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunitySearchRequest
import com.arbitrage.scanner.api.v1.models.IRequest
import com.arbitrage.scanner.base.Command
import com.arbitrage.scanner.base.RequestId
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.base.Timestamp
import com.arbitrage.scanner.base.WorkMode
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.models.ArbitrageOpportunity
import com.arbitrage.scanner.models.ArbitrageOpportunityFilter
import com.arbitrage.scanner.models.ArbitrageOpportunityId
import com.arbitrage.scanner.models.ArbitrageOpportunitySpread
import com.arbitrage.scanner.models.CexExchangeId
import com.arbitrage.scanner.models.CexTokenId
import com.arbitrage.scanner.models.DexChainId
import com.arbitrage.scanner.models.DexExchangeId
import com.arbitrage.scanner.models.DexTokenId
import com.arbitrage.scanner.stubs.Stubs

fun fromTransport(request: IRequest): Context {
    return when (request) {
        is ArbitrageOpportunityReadRequest -> fromTransport(request)
        is ArbitrageOpportunitySearchRequest -> fromTransport(request)
    }
}

fun fromTransport(request: ArbitrageOpportunityReadRequest): Context {
    return Context(
        command = Command.READ,
        state = State.NONE,
        error = emptyList(),
        workMode = request.debug.toWorkMode(),
        stubCase = request.debug.toStubCase(),
        requestId = RequestId.DEFAULT,
        startTimestamp = Timestamp.DEFAULT,
        arbitrageOpportunityReadRequest = request.id.toArbitrageOpportunityId(),
        arbitrageOpportunitySearchRequest = ArbitrageOpportunityFilter.DEFAULT,
        arbitrageOpportunityReadResponse = ArbitrageOpportunity.DexToCexSimpleArbitrageOpportunity.DEFAULT,
        arbitrageOpportunitySearchResponse = emptySet(),
    )
}

fun fromTransport(request: ArbitrageOpportunitySearchRequest): Context {
    return Context(
        command = Command.SEARCH,
        state = State.NONE,
        error = emptyList(),
        workMode = request.debug.toWorkMode(),
        stubCase = request.debug.toStubCase(),
        requestId = RequestId.DEFAULT,
        startTimestamp = Timestamp.DEFAULT,
        arbitrageOpportunityReadRequest = ArbitrageOpportunityId.DEFAULT,
        arbitrageOpportunitySearchRequest = request.filter.toArbitrageOpportunityFilter(),
        arbitrageOpportunityReadResponse = ArbitrageOpportunity.DexToCexSimpleArbitrageOpportunity.DEFAULT,
        arbitrageOpportunitySearchResponse = emptySet(),
    )
}

fun ArbitrageOpportunityDebug?.toWorkMode(): WorkMode {
    return when (this?.mode) {
        ArbitrageOpportunityRequestDebugMode.PROD -> WorkMode.PROD
        ArbitrageOpportunityRequestDebugMode.TEST -> WorkMode.TEST
        ArbitrageOpportunityRequestDebugMode.STUB -> WorkMode.STUB
        null -> WorkMode.PROD
    }
}

fun ArbitrageOpportunityDebug?.toStubCase(): Stubs {
    return when (this?.stub) {
        ArbitrageOpportunityRequestDebugStubs.SUCCESS -> Stubs.SUCCESS
        ArbitrageOpportunityRequestDebugStubs.NOT_FOUND -> Stubs.NOT_FOUND
        ArbitrageOpportunityRequestDebugStubs.BAD_ID -> Stubs.BAD_ID
        null -> Stubs.NONE
    }
}

fun String?.toArbitrageOpportunityId(): ArbitrageOpportunityId {
    return this?.let(::ArbitrageOpportunityId) ?: ArbitrageOpportunityId.DEFAULT
}

fun ArbitrageOpportunitySearchFilter?.toArbitrageOpportunityFilter(): ArbitrageOpportunityFilter {
    return ArbitrageOpportunityFilter(
        dexTokenIds = this?.dexTokenIds.transform(String::toDexTokenId),
        dexExchangeIds = this?.dexExchangeIds.transform(String::toDexExchangeId),
        dexChainIds = this?.dexChainIds.transform(String::toDexChainId),
        cexTokenIds = this?.cexTokenIds.transform(String::toCexTokenId),
        cexExchangeIds = this?.cexExchangeIds.transform(String::toCexExchangeId),
        spread = this?.spread.toArbitrageOpportunitySpread()
    )
}

fun String.toDexTokenId(): DexTokenId = DexTokenId(this)
fun String.toDexExchangeId(): DexExchangeId = DexExchangeId(this)
fun String.toDexChainId(): DexChainId = DexChainId(this)
fun String.toCexTokenId(): CexTokenId = CexTokenId(this)
fun String.toCexExchangeId(): CexExchangeId = CexExchangeId(this)

fun <T, R> Set<T>?.transform(block: (T) -> R): Set<R> = this.orEmpty().map(block).toSet()

fun Double?.toArbitrageOpportunitySpread(): ArbitrageOpportunitySpread =
    this?.let(::ArbitrageOpportunitySpread) ?: ArbitrageOpportunitySpread.DEFAULT