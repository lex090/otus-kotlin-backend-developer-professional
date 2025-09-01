package com.arbitrage.scanner.models

import kotlin.jvm.JvmInline

@JvmInline
value class ArbitrageOpportunitySpread(val value: Double) {
    companion object {
        val DEFAULT = ArbitrageOpportunitySpread(0.0)
    }
}
