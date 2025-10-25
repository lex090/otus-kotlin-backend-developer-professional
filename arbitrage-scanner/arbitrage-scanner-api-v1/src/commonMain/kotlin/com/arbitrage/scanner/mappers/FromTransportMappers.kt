package com.arbitrage.scanner.mappers

import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityDebugApi
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityReadRequest
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityRecalculateRequest
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityRequestDebugModeApi
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityRequestDebugStubsApi
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunitySearchFilterApi
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunitySearchRequest
import com.arbitrage.scanner.api.v1.models.IRequest
import com.arbitrage.scanner.base.Command
import com.arbitrage.scanner.base.StubCase
import com.arbitrage.scanner.base.WorkMode
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.models.ArbitrageOpportunityFilter
import com.arbitrage.scanner.models.ArbitrageOpportunityId
import com.arbitrage.scanner.models.ArbitrageOpportunitySpread
import com.arbitrage.scanner.models.CexExchangeId
import com.arbitrage.scanner.models.CexTokenId

fun Context.fromTransport(request: IRequest) {
    return when (request) {
        is ArbitrageOpportunityReadRequest -> fromTransport(request)
        is ArbitrageOpportunitySearchRequest -> fromTransport(request)
        is ArbitrageOpportunityRecalculateRequest -> fromTransport(request)
    }
}

fun Context.fromTransport(request: ArbitrageOpportunityReadRequest) {
    command = Command.READ
    workMode = request.debug.toWorkMode()
    stubCase = request.debug.toStubCase()
    arbitrageOpportunityReadRequest = request.id.toArbitrageOpportunityId()
}

fun Context.fromTransport(request: ArbitrageOpportunitySearchRequest) {
    command = Command.SEARCH
    workMode = request.debug.toWorkMode()
    stubCase = request.debug.toStubCase()
    arbitrageOpportunitySearchRequest = request.filter.toArbitrageOpportunityFilter()
}

fun Context.fromTransport(request: ArbitrageOpportunityRecalculateRequest) {
    command = Command.RECALCULATE
    workMode = request.debug.toWorkMode()
    stubCase = request.debug.toStubCase()
}

private fun ArbitrageOpportunityDebugApi?.toWorkMode(): WorkMode {
    return when (this?.mode) {
        ArbitrageOpportunityRequestDebugModeApi.PROD -> WorkMode.PROD
        ArbitrageOpportunityRequestDebugModeApi.TEST -> WorkMode.TEST
        ArbitrageOpportunityRequestDebugModeApi.STUB -> WorkMode.STUB
        null -> WorkMode.PROD
    }
}

private fun ArbitrageOpportunityDebugApi?.toStubCase(): StubCase {
    return when (this?.stub) {
        ArbitrageOpportunityRequestDebugStubsApi.SUCCESS -> StubCase.SUCCESS
        ArbitrageOpportunityRequestDebugStubsApi.NOT_FOUND -> StubCase.NOT_FOUND
        ArbitrageOpportunityRequestDebugStubsApi.BAD_ID -> StubCase.BAD_ID
        null -> StubCase.NONE
    }
}

private fun String?.toArbitrageOpportunityId(): ArbitrageOpportunityId {
    return this?.let(::ArbitrageOpportunityId) ?: ArbitrageOpportunityId.DEFAULT
}

private fun ArbitrageOpportunitySearchFilterApi?.toArbitrageOpportunityFilter(): ArbitrageOpportunityFilter {
    return ArbitrageOpportunityFilter(
        cexTokenIds = this?.cexTokenIds.transform(String::toCexTokenId),
        cexExchangeIds = this?.cexExchangeIds.transform(String::toCexExchangeId),
        spread = this?.spread.toArbitrageOpportunitySpread()
    )
}

private fun String.toCexTokenId(): CexTokenId = CexTokenId(this)
private fun String.toCexExchangeId(): CexExchangeId = CexExchangeId(this)

private fun <T, R> Set<T>?.transform(block: (T) -> R): Set<R> = this.orEmpty().map(block).toSet()

private fun Double?.toArbitrageOpportunitySpread(): ArbitrageOpportunitySpread =
    this?.let(::ArbitrageOpportunitySpread) ?: ArbitrageOpportunitySpread.DEFAULT