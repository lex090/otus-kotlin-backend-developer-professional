package com.arbitrage.scanner

import com.arbitrage.scanner.api.v1.models.IRequest
import kotlinx.serialization.json.Json

fun <I : IRequest> Json.toRequestJsonString(request: I): String =
    encodeToString(IRequest.serializer(), request)

inline fun <reified I : IRequest> Json.fromRequestJsonString(json: String): I =
    decodeFromString(IRequest.serializer(), json) as I
