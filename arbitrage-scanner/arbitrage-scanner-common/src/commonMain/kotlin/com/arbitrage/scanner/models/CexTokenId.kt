package com.arbitrage.scanner.models

import kotlin.jvm.JvmInline

@JvmInline
value class CexTokenId(val value: String) {
    companion object {
        val DEFAULT = CexTokenId("")
    }
}
