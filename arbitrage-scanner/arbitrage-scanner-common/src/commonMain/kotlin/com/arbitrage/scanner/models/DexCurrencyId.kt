package com.arbitrage.scanner.models

import kotlin.jvm.JvmInline

@JvmInline
value class DexCurrencyId(val value: String) {
    companion object {
        val DEFAULT = DexCurrencyId("")
    }
}
