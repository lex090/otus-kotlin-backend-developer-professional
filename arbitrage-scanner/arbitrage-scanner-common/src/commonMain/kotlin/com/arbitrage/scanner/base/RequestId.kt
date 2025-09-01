package com.arbitrage.scanner.base

import kotlin.jvm.JvmInline

@JvmInline
value class RequestId(private val value: String) {
    companion object {
        val DEFAULT = RequestId("")
    }
}
