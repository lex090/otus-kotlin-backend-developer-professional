package com.arbitrage.scanner.base

import kotlin.jvm.JvmInline

@JvmInline
value class Timestamp(val value: Long) {

    fun isNone(): Boolean = this == NONE

    fun isNotNone(): Boolean = !isNone()

    companion object {
        val NONE = Timestamp(0L)
    }
}
