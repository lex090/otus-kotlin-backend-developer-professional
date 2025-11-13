package com.arbitrage.scanner.models

import kotlin.jvm.JvmInline

@JvmInline
value class ArbitrageOpportunityId(val value: String = "") {

    fun isDefault(): Boolean = this == NONE

    fun isNotDefault(): Boolean = !isDefault()

    companion object {
        val NONE = ArbitrageOpportunityId()
    }
}
