package com.arbitrage.scanner.base

import kotlin.jvm.JvmInline

@JvmInline
value class RequestId(private val value: String) {

    fun isDefault(): Boolean = this == DEFAULT

    fun isNotDefault(): Boolean = !isDefault()

    companion object {
        val DEFAULT = RequestId("")
    }
}
