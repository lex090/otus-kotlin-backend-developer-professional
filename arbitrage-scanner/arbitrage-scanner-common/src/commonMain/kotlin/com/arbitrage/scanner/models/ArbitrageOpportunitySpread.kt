package com.arbitrage.scanner.models

import kotlin.jvm.JvmInline

@JvmInline
value class ArbitrageOpportunitySpread(val value: Double = 0.0) {

    fun isDefault(): Boolean = this == DEFAULT

    fun isNotDefault(): Boolean = !isDefault()

    companion object {
        val DEFAULT = ArbitrageOpportunitySpread()
    }
}
