package com.arbitrage.scanner

import com.arbitrage.scanner.base.InternalError
import com.arbitrage.scanner.base.State
import com.arbitrage.scanner.context.Context

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

fun Context.addError(vararg error: InternalError) = errors.addAll(error)

fun Context.fail(error: InternalError) {
    addError(error)
    state = State.FAILING
}

fun Context.fail(errors: Collection<InternalError>) {
    addError(error = errors.toTypedArray())
    state = State.FAILING
}

fun errorSystem(
    violationCode: String,
    e: Throwable,
) = InternalError(
    code = "system-$violationCode",
    group = "system",
    message = "System error occurred.",
    exception = e,
)

