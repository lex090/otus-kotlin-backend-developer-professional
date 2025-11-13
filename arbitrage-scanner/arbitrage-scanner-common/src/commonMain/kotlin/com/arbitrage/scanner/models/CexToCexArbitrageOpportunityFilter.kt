package com.arbitrage.scanner.models

import com.arbitrage.scanner.base.Timestamp

/**
 * Фильтр для поиска арбитражных возможностей.
 *
 * @property cexTokenIdsFilter Набор идентификаторов токенов для фильтрации (NONE = не фильтровать, значение не задано)
 * @property buyExchangeIds Набор идентификаторов бирж покупки (пустой Set = не фильтровать)
 * @property sellExchangeIds Набор идентификаторов бирж продажи (пустой Set = не фильтровать)
 * @property minSpread Минимальный спред в процентах (NONE = не фильтровать, значение не задано)
 * @property maxSpread Максимальный спред в процентах (null = не фильтровать)
 * @property status Статус арбитражной возможности (ACTIVE/INACTIVE/ALL, NONE = не задан)
 * @property startTimestampFrom Начало временного диапазона создания (null = не фильтровать)
 * @property startTimestampTo Конец временного диапазона создания (null = не фильтровать)
 * @property endTimestampFrom Начало временного диапазона завершения (null = не фильтровать)
 * @property endTimestampTo Конец временного диапазона завершения (null = не фильтровать)
 */
data class CexToCexArbitrageOpportunityFilter(
    val cexTokenIdsFilter: CexTokenIdsFilter = CexTokenIdsFilter.NONE,
    val buyExchangeIds: Set<CexExchangeId> = emptySet(),
    val sellExchangeIds: Set<CexExchangeId> = emptySet(),
    val minSpread: ArbitrageOpportunitySpread = ArbitrageOpportunitySpread.NONE,
    val maxSpread: ArbitrageOpportunitySpread? = null,
    val status: ArbitrageOpportunityStatus = ArbitrageOpportunityStatus.NONE,
    val startTimestampFrom: Timestamp? = null,
    val startTimestampTo: Timestamp? = null,
    val endTimestampFrom: Timestamp? = null,
    val endTimestampTo: Timestamp? = null,
) {
    companion object {
        val DEFAULT = CexToCexArbitrageOpportunityFilter()
    }
}
