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
 * @property startTimestamp Фильтр по времени начала: если указан, возвращает возможности с startTimestamp >= указанного значения (null = не фильтровать)
 * @property endTimestamp Фильтр по времени окончания: если указан, возвращает возможности с endTimestamp <= указанного значения (null = не фильтровать)
 */
data class CexToCexArbitrageOpportunityFilter(
    val cexTokenIdsFilter: CexTokenIdsFilter,
    val buyExchangeIds: CexExchangeIds,
    val sellExchangeIds: CexExchangeIds,
    val minSpread: ArbitrageOpportunitySpread,
    val maxSpread: ArbitrageOpportunitySpread?,
    val status: ArbitrageOpportunityStatus,
    val startTimestamp: Timestamp?,
    val endTimestamp: Timestamp?,
) {
    companion object {
        val NONE = CexToCexArbitrageOpportunityFilter(
            cexTokenIdsFilter = CexTokenIdsFilter.NONE,
            buyExchangeIds = CexExchangeIds.NONE,
            sellExchangeIds = CexExchangeIds.NONE,
            minSpread = ArbitrageOpportunitySpread.NONE,
            maxSpread = null,
            status = ArbitrageOpportunityStatus.NONE,
            startTimestamp = null,
            endTimestamp = null,
        )
    }
}
