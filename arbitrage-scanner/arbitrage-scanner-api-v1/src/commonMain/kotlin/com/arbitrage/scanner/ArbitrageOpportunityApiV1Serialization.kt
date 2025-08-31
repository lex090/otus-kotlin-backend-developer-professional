package com.arbitrage.scanner

import com.arbitrage.scanner.api.v1.models.IRequest
import com.arbitrage.scanner.api.v1.models.IResponse
import kotlinx.serialization.json.Json

fun <I : IRequest> Json.toRequestJsonString(request: I): String =
    encodeToString(IRequest.serializer(), request)

inline fun <reified I : IRequest> Json.fromRequestJsonString(json: String): I =
    decodeFromString(IRequest.serializer(), json) as I

fun <I : IResponse> Json.toResponseJsonString(response: I): String =
    encodeToString(IResponse.serializer(), response)

inline fun <reified I : IResponse> Json.fromResponseJsonStringResponse(json: String): I =
    decodeFromString(IResponse.serializer(), json) as I
