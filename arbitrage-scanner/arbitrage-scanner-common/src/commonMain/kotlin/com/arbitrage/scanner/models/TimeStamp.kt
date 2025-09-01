package com.arbitrage.scanner.models

import kotlin.jvm.JvmInline

@JvmInline
value class TimeStamp(val value: Long) {
    companion object {
        val DEFAULT = TimeStamp(0L)
    }
}
