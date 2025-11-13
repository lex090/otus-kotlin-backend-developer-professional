package com.arbitrage.scanner.models

import kotlin.jvm.JvmInline

@JvmInline
value class ArbitrageOpportunitySpread(val value: Double) {

    fun isNone(): Boolean = this == NONE

    fun isNotNone(): Boolean = !isNone()

    companion object {
        /**
         * NONE - маркер "значение не установлено", используется как признак отсутствия фильтрации.
         * Это невалидное значение (-1.0), которое не должно использоваться в бизнес-логике.
         */
        val NONE = ArbitrageOpportunitySpread(-1.0)
    }
}
