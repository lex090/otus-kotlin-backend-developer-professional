package com.arbitrage.scanner.mappers

import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityReadResponse
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityRecalculateResponse
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunitySearchResponse
import com.arbitrage.scanner.api.v1.models.CexPriceApi
import com.arbitrage.scanner.api.v1.models.CexToCexArbitrageOpportunityApi
import com.arbitrage.scanner.api.v1.models.Error
import com.arbitrage.scanner.api.v1.models.IResponse
import com.arbitrage.scanner.api.v1.models.ResponseResult
import com.arbitrage.scanner.base.Command
import com.arbitrage.scanner.base.InternalError
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.base.Timestamp
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.models.ArbitrageOpportunityId
import com.arbitrage.scanner.models.ArbitrageOpportunitySpread
import com.arbitrage.scanner.models.CexExchangeId
import com.arbitrage.scanner.models.CexPrice
import com.arbitrage.scanner.models.CexPrice.CexPriceRaw
import com.arbitrage.scanner.models.CexToCexArbitrageOpportunity
import com.arbitrage.scanner.models.CexTokenId
import com.arbitrage.scanner.models.LockToken

fun Context.toTransport(): IResponse {
    return when (command) {
        Command.READ -> toTransportRead()
        Command.SEARCH -> toTransportSearch()
        Command.RECALCULATE -> toTransportRecalculate()
        Command.NONE -> error("UnknownCommand command: $command, context: $this")
    }
}

private fun Context.toTransportRead(): ArbitrageOpportunityReadResponse {
    return ArbitrageOpportunityReadResponse(
        result = state.toResponseResult(),
        errors = internalErrors.transform(InternalError::toTransportError),
        arbitrageOpportunity = arbitrageOpportunityReadResponse.toTransport(),
    )
}

fun CexToCexArbitrageOpportunity.toTransport(): CexToCexArbitrageOpportunityApi {
    return CexToCexArbitrageOpportunityApi(
        id = id.toTransportId(),
        cexTokenId = cexTokenId.toTransportId(),
        buyCexExchangeId = buyCexExchangeId.toTransportId(),
        sellCexExchangeId = sellCexExchangeId.toTransportId(),
        buyCexPriceRaw = buyCexPriceRaw.toTransportRawPrice(),
        sellCexPriceRaw = sellCexPriceRaw.toTransportRawPrice(),
        spread = spread.toTransport(),
        timestampStart = startTimestamp.toTransport(),
        timestampEnd = endTimestamp.toTransport(),
        lockToken = lockToken.toTransport()
    )
}

private fun Context.toTransportSearch(): ArbitrageOpportunitySearchResponse {
    return ArbitrageOpportunitySearchResponse(
        result = state.toResponseResult(),
        errors = internalErrors.transform(InternalError::toTransportError),
        arbitrageOpportunities = arbitrageOpportunitySearchResponse
            .map(CexToCexArbitrageOpportunity::toTransport)
            .takeIf(List<CexToCexArbitrageOpportunityApi>::isNotEmpty)
            .orEmpty()
    )
}

private fun Context.toTransportRecalculate(): ArbitrageOpportunityRecalculateResponse {
    return ArbitrageOpportunityRecalculateResponse(
        result = state.toResponseResult(),
        errors = internalErrors.transform(InternalError::toTransportError),
        opportunitiesCount = recalculateResponse.opportunitiesCount,
        processingTimeMs = recalculateResponse.processingTimeMs,
    )
}

private fun State.toResponseResult(): ResponseResult {
    return when (this) {
        State.FAILING -> ResponseResult.ERROR
        State.FINISHING -> ResponseResult.SUCCESS
        State.RUNNING,
        State.NONE -> error("State can't be == $this")
    }
}

private inline fun <reified T, R> List<T>.transform(block: (T) -> R): List<R>? =
    this.map(block).takeIf(List<R>::isNotEmpty)

private fun InternalError.toTransportError() = Error(
    code = code.takeIf(String::isNotBlank),
    group = group.takeIf(String::isNotBlank),
    field = field.takeIf(String::isNotBlank),
    message = message.takeIf(String::isNotBlank)
)

fun ArbitrageOpportunityId.toTransportId(): String? =
    this.takeIf(ArbitrageOpportunityId::isNotDefault)?.value

private fun CexPrice.toTransportCexPrice(): CexPriceApi {
    return CexPriceApi(
        tokenId = tokenId.toTransportId(),
        exchangeId = exchangeId.toTransportId(),
        priceRaw = priceRaw.toTransportRawPrice()
    )
}

fun CexTokenId.toTransportId(): String? =
    this.takeIf(CexTokenId::isNotNone)?.value

fun CexExchangeId.toTransportId(): String? =
    this.takeIf(CexExchangeId::isNotDefault)?.value

fun CexPriceRaw.toTransportRawPrice(): Double? =
    this.takeIf(CexPriceRaw::isNotDefault)?.value?.doubleValue(exactRequired = false)

fun ArbitrageOpportunitySpread.toTransport(): Double? =
    this.takeIf(ArbitrageOpportunitySpread::isNotNone)?.value

fun Timestamp?.toTransport(): Long? =
    this?.takeIf(Timestamp::isNotNone)?.value

fun LockToken.toTransport(): String? =
    this.takeIf(LockToken::isNotDefault)?.value