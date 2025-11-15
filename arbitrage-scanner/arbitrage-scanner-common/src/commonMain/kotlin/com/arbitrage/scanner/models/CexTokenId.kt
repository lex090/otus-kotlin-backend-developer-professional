package com.arbitrage.scanner.models

import kotlin.jvm.JvmInline

@JvmInline
value class CexTokenId(val value: String) {

    fun isNone(): Boolean = this == NONE

    fun isNotNone(): Boolean = !isNone()

    companion object {
        /**
         * NONE - маркер "значение не установлено", используется как признак невалидного токена.
         * Это невозможное значение (пустая строка), которое не должно использоваться в бизнес-логике.
         */
        val NONE = CexTokenId("")
    }
}
