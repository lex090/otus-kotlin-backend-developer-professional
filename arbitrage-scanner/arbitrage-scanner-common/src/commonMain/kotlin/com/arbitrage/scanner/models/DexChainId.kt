package com.arbitrage.scanner.models

import kotlin.jvm.JvmInline

@JvmInline
value class DexChainId(val value: String) {

    fun isDefault(): Boolean = this == DEFAULT

    fun isNotDefault(): Boolean = !isDefault()

    companion object {
        val DEFAULT = DexChainId("")
    }
}
