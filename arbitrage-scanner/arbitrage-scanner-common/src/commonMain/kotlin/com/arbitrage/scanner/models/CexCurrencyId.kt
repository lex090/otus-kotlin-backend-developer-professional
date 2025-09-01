package com.arbitrage.scanner.models

import kotlin.jvm.JvmInline

@JvmInline
value class CexCurrencyId(val value: String) {
    companion object {
        val DEFAULT = CexCurrencyId("")
    }
}
