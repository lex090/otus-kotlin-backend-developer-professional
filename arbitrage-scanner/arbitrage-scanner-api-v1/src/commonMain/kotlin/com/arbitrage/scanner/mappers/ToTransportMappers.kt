package com.arbitrage.scanner.mappers

import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityReadResponse
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunitySearchResponse
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityStatusType
import com.arbitrage.scanner.api.v1.models.Error
import com.arbitrage.scanner.api.v1.models.IResponse
import com.arbitrage.scanner.api.v1.models.ResponseResult
import com.arbitrage.scanner.base.Command
import com.arbitrage.scanner.base.InternalError
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.base.Timestamp
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.models.ArbitrageOpportunity
import com.arbitrage.scanner.models.ArbitrageOpportunity.DexToCexSimpleArbitrageOpportunity
import com.arbitrage.scanner.models.ArbitrageOpportunityId
import com.arbitrage.scanner.models.ArbitrageOpportunitySpread
import com.arbitrage.scanner.models.ArbitrageOpportunityStatus
import com.arbitrage.scanner.models.CexExchangeId
import com.arbitrage.scanner.models.CexPrice
import com.arbitrage.scanner.models.CexPrice.CexPriceRaw
import com.arbitrage.scanner.models.CexTokenId
import com.arbitrage.scanner.models.DexChainId
import com.arbitrage.scanner.models.DexExchangeId
import com.arbitrage.scanner.models.DexPrice
import com.arbitrage.scanner.models.DexPrice.DexPriceRaw
import com.arbitrage.scanner.models.DexTokenId
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunity as ArbitrageOpportunityApi
import com.arbitrage.scanner.api.v1.models.CexPrice as CexPriceApi
import com.arbitrage.scanner.api.v1.models.DexPrice as DexPriceApi
import com.arbitrage.scanner.api.v1.models.DexToCexSimpleArbitrageOpportunity as DexToCexSimpleArbitrageOpportunityApi

fun Context.toTransport(): IResponse {
    return when (command) {
        Command.READ -> toTransportRead()
        Command.SEARCH -> toTransportSearch()
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

private fun ArbitrageOpportunity.toTransport(): ArbitrageOpportunityApi {
    return when (this) {
        is DexToCexSimpleArbitrageOpportunity -> toTransport()
    }
}

fun DexToCexSimpleArbitrageOpportunity.toTransport(): DexToCexSimpleArbitrageOpportunityApi {
    return DexToCexSimpleArbitrageOpportunityApi(
        id = this.id.toTransportId(),
        dexPrice = this.dexPrice.toTransportDexPrice(),
        cexPrice = this.cexPrice.toTransportCexPrice(),
        spread = this.spread.toTransport(),
        statusType = this.status.toTransport(),
        timestampStart = this.startTimestamp.toTransport(),
        timestampEnd = this.endTimestamp.toTransport(),
    )
}

private fun Context.toTransportSearch(): ArbitrageOpportunitySearchResponse {
    return ArbitrageOpportunitySearchResponse(
        result = state.toResponseResult(),
        errors = internalErrors.transform(InternalError::toTransportError),
        arbitrageOpportunities = arbitrageOpportunitySearchResponse
            .map(ArbitrageOpportunity::toTransport)
            .takeIf(List<ArbitrageOpportunityApi>::isNotEmpty)
            .orEmpty()
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

private fun ArbitrageOpportunityId.toTransportId(): String? =
    this.takeIf(ArbitrageOpportunityId::isNotDefault)?.value

private fun DexPrice.toTransportDexPrice(): DexPriceApi {
    return DexPriceApi(
        tokenId = tokenId.toTransportId(),
        chainId = chainId.toTransportId(),
        exchangeId = exchangeId.toTransportId(),
        priceRaw = priceRaw.toTransportRawPrice()
    )
}

fun DexTokenId.toTransportId(): String? =
    this.takeIf(DexTokenId::isNotDefault)?.value

fun DexChainId.toTransportId(): String? =
    this.takeIf(DexChainId::isNotDefault)?.value

fun DexExchangeId.toTransportId(): String? =
    this.takeIf(DexExchangeId::isNotDefault)?.value

fun DexPriceRaw.toTransportRawPrice(): Double? =
    this.takeIf(DexPriceRaw::isNotDefault)?.value?.doubleValue(exactRequired = true)

private fun CexPrice.toTransportCexPrice(): CexPriceApi {
    return CexPriceApi(
        tokenId = tokenId.toTransportId(),
        exchangeId = exchangeId.toTransportId(),
        priceRaw = priceRaw.toTransportRawPrice()
    )
}

fun CexTokenId.toTransportId(): String? =
    this.takeIf(CexTokenId::isNotDefault)?.value

fun CexExchangeId.toTransportId(): String? =
    this.takeIf(CexExchangeId::isNotDefault)?.value

fun CexPriceRaw.toTransportRawPrice(): Double? =
    this.takeIf(CexPriceRaw::isNotDefault)?.value?.doubleValue(exactRequired = true)

fun ArbitrageOpportunitySpread.toTransport(): Double? =
    this.takeIf(ArbitrageOpportunitySpread::isNotDefault)?.value

fun ArbitrageOpportunityStatus.toTransport(): ArbitrageOpportunityStatusType {
    return when (this) {
        ArbitrageOpportunityStatus.ACTIVE -> ArbitrageOpportunityStatusType.ACTIVE
        ArbitrageOpportunityStatus.EXPIRED -> ArbitrageOpportunityStatusType.EXPIRED
    }
}

fun Timestamp?.toTransport(): Long? =
    this?.takeIf(Timestamp::isNotDefault)?.value