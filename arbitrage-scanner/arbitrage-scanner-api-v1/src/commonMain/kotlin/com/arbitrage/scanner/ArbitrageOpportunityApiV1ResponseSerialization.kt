package com.arbitrage.scanner

import com.arbitrage.scanner.api.v1.models.IResponse
import kotlinx.serialization.json.Json

fun <I : IResponse> Json.toResponseJsonString(response: I): String =
    encodeToString(IResponse.serializer(), response)

inline fun <reified I : IResponse> Json.fromResponseJsonStringResponse(json: String): I =
    decodeFromString(IResponse.serializer(), json) as I
