package com.arbitrage.scanner.models

import kotlin.jvm.JvmInline

@JvmInline
value class ArbitrageOpportunityId(val value: String) {
    companion object {
        val DEFAULT = ArbitrageOpportunityId("")
    }
}
