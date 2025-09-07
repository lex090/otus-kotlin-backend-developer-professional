package com.arbitrage.scanner

import com.arbitrage.scanner.base.InternalError

fun Throwable.asError(
    code: String = "unknown",
    group: String = "exceptions",
    message: String = this.message.orEmpty(),
) = InternalError(
    code = code,
    group = group,
    field = "",
    message = message,
    exception = this,
)
