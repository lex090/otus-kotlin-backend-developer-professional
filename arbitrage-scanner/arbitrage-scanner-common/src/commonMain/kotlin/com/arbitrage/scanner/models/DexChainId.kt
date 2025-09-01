package com.arbitrage.scanner.models

import kotlin.jvm.JvmInline

@JvmInline
value class DexChainId(val value: String) {
    companion object {
        val DEFAULT = DexChainId("")
    }
}
