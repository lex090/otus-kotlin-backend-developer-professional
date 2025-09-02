package com.arbitrage.scanner.base

import kotlin.jvm.JvmInline

@JvmInline
value class Timestamp(val value: Long) {

    fun isDefault(): Boolean = this == DEFAULT

    fun isNotDefault(): Boolean = !isDefault()

    companion object {
        val DEFAULT = Timestamp(0L)
    }
}
