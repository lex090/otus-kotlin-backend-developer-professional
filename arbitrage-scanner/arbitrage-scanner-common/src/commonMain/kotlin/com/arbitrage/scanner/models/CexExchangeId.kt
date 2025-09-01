package com.arbitrage.scanner.models

import kotlin.jvm.JvmInline

@JvmInline
value class CexExchangeId(val value: String) {
    companion object {
        val DEFAULT = CexExchangeId("")
    }
}
