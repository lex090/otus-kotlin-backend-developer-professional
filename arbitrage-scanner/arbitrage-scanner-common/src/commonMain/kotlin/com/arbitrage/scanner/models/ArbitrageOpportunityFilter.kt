package com.arbitrage.scanner.models

import com.arbitrage.scanner.base.Timestamp

/**
 * Фильтр для поиска арбитражных возможностей.
 *
 * @property cexTokenIds Набор идентификаторов токенов для фильтрации
 * @property buyExchangeIds Набор идентификаторов бирж покупки (пустой Set = не фильтровать)
 * @property sellExchangeIds Набор идентификаторов бирж продажи (пустой Set = не фильтровать)
 * @property minSpread Минимальный спред в процентах (DEFAULT = не фильтровать)
 * @property maxSpread Максимальный спред в процентах (null = не фильтровать)
 * @property status Статус арбитражной возможности (ACTIVE/INACTIVE/ALL)
 * @property startTimestampFrom Начало временного диапазона создания (null = не фильтровать)
 * @property startTimestampTo Конец временного диапазона создания (null = не фильтровать)
 * @property endTimestampFrom Начало временного диапазона завершения (null = не фильтровать)
 * @property endTimestampTo Конец временного диапазона завершения (null = не фильтровать)
 */
data class ArbitrageOpportunityFilter(
    val cexTokenIds: Set<CexTokenId> = emptySet(),
    val buyExchangeIds: Set<CexExchangeId> = emptySet(),
    val sellExchangeIds: Set<CexExchangeId> = emptySet(),
    val minSpread: ArbitrageOpportunitySpread = ArbitrageOpportunitySpread.DEFAULT,
    val maxSpread: ArbitrageOpportunitySpread? = null,
    val status: ArbitrageOpportunityStatus = ArbitrageOpportunityStatus.DEFAULT,
    val startTimestampFrom: Timestamp? = null,
    val startTimestampTo: Timestamp? = null,
    val endTimestampFrom: Timestamp? = null,
    val endTimestampTo: Timestamp? = null,
) {
    companion object {
        val DEFAULT = ArbitrageOpportunityFilter()
    }
}
