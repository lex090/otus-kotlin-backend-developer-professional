package com.arbitrage.scanner.models

import kotlin.jvm.JvmInline

@JvmInline
value class DexTokenId(val value: String) {
    companion object {
        val DEFAULT = DexTokenId("")
    }
}
