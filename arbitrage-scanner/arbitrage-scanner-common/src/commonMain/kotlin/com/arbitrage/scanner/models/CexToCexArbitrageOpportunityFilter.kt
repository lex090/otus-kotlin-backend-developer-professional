package com.arbitrage.scanner.models

import com.arbitrage.scanner.base.Timestamp

/**
 * Фильтр для поиска арбитражных возможностей.
 * Все поля обязательны для заполнения. NONE означает невалидную/незаполненную модель.
 *
 * @property cexTokenIdsFilter Набор идентификаторов токенов для фильтрации
 * @property buyExchangeIds Набор идентификаторов бирж покупки
 * @property sellExchangeIds Набор идентификаторов бирж продажи
 * @property minSpread Минимальный спред в процентах
 * @property maxSpread Максимальный спред в процентах (null = не фильтровать)
 * @property status Статус арбитражной возможности (ACTIVE/INACTIVE/ALL)
 * @property startTimestampFrom Начало временного диапазона создания (null = не фильтровать)
 * @property startTimestampTo Конец временного диапазона создания (null = не фильтровать)
 * @property endTimestampFrom Начало временного диапазона завершения (null = не фильтровать)
 * @property endTimestampTo Конец временного диапазона завершения (null = не фильтровать)
 */
data class CexToCexArbitrageOpportunityFilter(
    val cexTokenIdsFilter: CexTokenIdsFilter,
    val buyExchangeIds: CexExchangeIds,
    val sellExchangeIds: CexExchangeIds,
    val minSpread: ArbitrageOpportunitySpread,
    val maxSpread: ArbitrageOpportunitySpread?,
    val status: ArbitrageOpportunityStatus,
    val startTimestampFrom: Timestamp?,
    val startTimestampTo: Timestamp?,
    val endTimestampFrom: Timestamp?,
    val endTimestampTo: Timestamp?,
) {
    companion object {
        val NONE = CexToCexArbitrageOpportunityFilter(
            cexTokenIdsFilter = CexTokenIdsFilter.NONE,
            buyExchangeIds = CexExchangeIds.NONE,
            sellExchangeIds = CexExchangeIds.NONE,
            minSpread = ArbitrageOpportunitySpread.NONE,
            maxSpread = null,
            status = ArbitrageOpportunityStatus.NONE,
            startTimestampFrom = null,
            startTimestampTo = null,
            endTimestampFrom = null,
            endTimestampTo = null,
        )
    }
}
