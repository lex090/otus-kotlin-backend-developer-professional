package com.arbitrage.scanner.mappers

import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityReadResponse
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunitySearchResponse
import com.arbitrage.scanner.api.v1.models.Error
import com.arbitrage.scanner.api.v1.models.IResponse
import com.arbitrage.scanner.api.v1.models.ResponseResult
import com.arbitrage.scanner.base.Command
import com.arbitrage.scanner.base.InternalError
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.models.ArbitrageOpportunityId

fun Context.toTransport(): IResponse {
    return when (command) {
        Command.READ -> toTransportRead()
        Command.SEARCH -> toTransportSearch()
        Command.NONE -> error("UnknownCommand command: $command, context: $this")
    }
}

fun Context.toTransportRead(): ArbitrageOpportunityReadResponse {
    return ArbitrageOpportunityReadResponse(
        result = state.toResponseResult(),
        errors = internalErrors.transform(InternalError::toTransportError),
        id = arbitrageOpportunityReadResponse.id.toTransportId(),
        dexPrice = TODO(),
        cexPrice = TODO(),
        spread = TODO(),
        status = TODO()
    )
}

private fun Context.toTransportSearch(): ArbitrageOpportunitySearchResponse {
    return ArbitrageOpportunitySearchResponse(
        result = state.toResponseResult(),
        errors = internalErrors.transform(InternalError::toTransportError),
        arbitrageOpportunities = TODO()
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
    this.takeIf { it != ArbitrageOpportunityId.DEFAULT }?.value