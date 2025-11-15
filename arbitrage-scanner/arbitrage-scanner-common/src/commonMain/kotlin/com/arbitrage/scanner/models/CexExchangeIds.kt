package com.arbitrage.scanner.models

import kotlin.jvm.JvmInline

/**
 * Value class для набора идентификаторов CEX бирж.
 * Используется для фильтрации арбитражных возможностей по списку бирж.
 */
@JvmInline
value class CexExchangeIds(val value: Set<CexExchangeId>) {

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
         * NONE - маркер "значение не установлено", используется как признак отсутствия фильтрации по биржам.
         * Представлен набором с невозможной биржей CexExchangeId.NONE, так как пустой набор является валидным значением.
         */
        val NONE = CexExchangeIds(setOf(CexExchangeId.NONE))
    }
}
