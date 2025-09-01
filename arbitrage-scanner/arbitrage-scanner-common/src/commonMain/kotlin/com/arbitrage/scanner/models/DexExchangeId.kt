package com.arbitrage.scanner.models

import kotlin.jvm.JvmInline

@JvmInline
value class DexExchangeId(val value: String) {
    companion object {
        val DEFAULT = DexExchangeId("")
    }
}
