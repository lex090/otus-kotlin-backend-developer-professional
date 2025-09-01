package com.arbitrage.scanner.base

import kotlin.jvm.JvmInline

@JvmInline
value class Timestamp(val value: Long) {
    companion object {
        val DEFAULT = Timestamp(0L)
    }
}
