package com.arbitrage.scanner.services

import com.arbitrage.scanner.models.CexPrice
import com.arbitrage.scanner.models.CexToCexArbitrageOpportunity
import com.arbitrage.scanner.models.CexPrice.CexPriceRaw
import com.arbitrage.scanner.models.ArbitrageOpportunitySpread
import com.ionspin.kotlin.bignum.decimal.BigDecimal

/**
 * Сервис для поиска арбитражных возможностей
 *
 * Этот интерфейс определяет контракт для алгоритма поиска арбитражных ситуаций
 * между CEX биржами на основе ценовых данных.
 *
 * Алгоритм должен:
 * 1. Группировать цены по токенам
 * 2. Для каждого токена находить min (buy) и max (sell) цены
 * 3. Вычислять спред: (sellPrice - buyPrice) / buyPrice * 100
 * 4. Фильтровать по минимальному порогу прибыльности
 * 5. Сортировать по убыванию спреда
 *
 * Важно: Использует CexPriceRaw (BigDecimal) для точных вычислений
 * и ArbitrageOpportunitySpread для результата.
 */
interface ArbitrageFinder {
    /**
     * Найти все арбитражные возможности из набора цен
     *
     * Реализация должна быть оптимизирована для больших объёмов данных:
     * - Целевая производительность: 1000 цен < 1 секунда
     * - Сложность алгоритма: O(n) или O(n log n)
     *
     * @param prices список ценовых данных с различных бирж
     * @param minSpreadPercent минимальный порог прибыльности (по умолчанию 0.1%)
     * @return список найденных возможностей, отсортированный по убыванию спреда
     */
    fun findOpportunities(
        prices: List<CexPrice>,
        minSpreadPercent: Double = 0.1
    ): List<CexToCexArbitrageOpportunity>

    /**
     * Вычислить спред между двумя ценами
     *
     * Использует точные вычисления с BigDecimal для избежания ошибок округления.
     * Формула: (sellPrice - buyPrice) / buyPrice * 100
     *
     * @param buyPrice цена покупки (BigDecimal wrapper)
     * @param sellPrice цена продажи (BigDecimal wrapper)
     * @return процент спреда в виде ArbitrageOpportunitySpread
     */
    fun calculateSpread(buyPrice: CexPriceRaw, sellPrice: CexPriceRaw): ArbitrageOpportunitySpread {
        require(!buyPrice.isDefault()) { "Buy price must not be default" }
        require(!sellPrice.isDefault()) { "Sell price must not be default" }
        require(buyPrice.value > BigDecimal.ZERO) { "Buy price must be positive" }
        require(sellPrice.value >= buyPrice.value) { "Sell price must be >= buy price" }

        // (sellPrice - buyPrice) / buyPrice * 100
        val spread = (sellPrice.value - buyPrice.value) / buyPrice.value * BigDecimal.fromInt(100)

        // Конвертируем в Double для ArbitrageOpportunitySpread
        return ArbitrageOpportunitySpread(spread.doubleValue(exactRequired = false))
    }
}
