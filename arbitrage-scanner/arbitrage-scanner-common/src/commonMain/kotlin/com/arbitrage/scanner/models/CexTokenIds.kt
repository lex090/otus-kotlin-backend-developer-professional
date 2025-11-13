package com.arbitrage.scanner.models

import kotlin.jvm.JvmInline

/**
 * Value class для набора идентификаторов CEX токенов.
 * Используется для фильтрации арбитражных возможностей по списку токенов.
 */
@JvmInline
value class CexTokenIds(val value: Set<CexTokenId>) {

    /**
     * Проверяет, является ли набор невалидным (NONE - значение не задано)
     */
    fun isNone(): Boolean = this == NONE

    /**
     * Проверяет, что набор валидный (значение задано)
     */
    fun isNotNone(): Boolean = !isNone()

    companion object {
        /**
         * NONE - маркер "значение не установлено", используется как признак отсутствия фильтрации по токенам.
         * Представлен набором с невозможным токеном CexTokenId.NONE, так как пустой набор является валидным значением.
         */
        val NONE = CexTokenIds(setOf(CexTokenId.NONE))
    }
}
