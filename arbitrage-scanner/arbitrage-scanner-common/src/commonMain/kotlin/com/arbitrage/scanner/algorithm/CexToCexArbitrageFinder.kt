package com.arbitrage.scanner.algorithm

import com.arbitrage.scanner.models.CexPrice
import com.arbitrage.scanner.models.CexToCexArbitrageOpportunity
import com.arbitrage.scanner.models.CexPrice.CexPriceRaw
import com.arbitrage.scanner.models.ArbitrageOpportunitySpread
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode

/**
 * Базовый класс для поиска арбитражных возможностей между CEX биржами
 */
abstract class CexToCexArbitrageFinder {
    /**
     * Найти все арбитражные возможности из набора цен
     *
     * @param prices список ценовых данных с различных бирж
     * @param minSpreadPercent минимальный порог прибыльности (по умолчанию 0.1%)
     * @return список найденных возможностей, отсортированных по убыванию спреда
     */
    abstract suspend fun findOpportunities(
        prices: List<CexPrice>,
        minSpreadPercent: Double = 0.1
    ): List<CexToCexArbitrageOpportunity>

    /**
     * Вычислить спред между двумя ценами
     *
     * Использует точные вычисления с BigDecimal для избежания ошибок округления.
     * Формула: (sellPrice - buyPrice) / buyPrice * 100
     *
     * Защищенный метод для использования в наследниках.
     *
     * @param buyPrice цена покупки (BigDecimal wrapper)
     * @param sellPrice цена продажи (BigDecimal wrapper)
     * @return процент спреда в виде ArbitrageOpportunitySpread
     * @throws IllegalArgumentException если цены невалидны
     */
    protected fun calculateSpread(buyPrice: CexPriceRaw, sellPrice: CexPriceRaw): ArbitrageOpportunitySpread {
        require(!buyPrice.isNone()) { "Buy price must not be default" }
        require(!sellPrice.isNone()) { "Sell price must not be default" }
        require(buyPrice.value > BigDecimal.ZERO) { "Buy price must be positive" }
        require(sellPrice.value > BigDecimal.ZERO) { "Sell price must be positive" }
        require(sellPrice.value >= buyPrice.value) { "Sell price must be >= buy price" }

        // Используем DecimalMode с точностью 18 знаков и округлением ROUND_HALF_AWAY_FROM_ZERO для финансовых вычислений
        // ROUND_HALF_AWAY_FROM_ZERO более точен чем CEILING и соответствует стандартной математической практике
        val decimalMode = DecimalMode(decimalPrecision = 18, roundingMode = RoundingMode.ROUND_HALF_AWAY_FROM_ZERO)

        // (sellPrice - buyPrice) / buyPrice * 100
        val priceDiff = sellPrice.value - buyPrice.value
        val spreadRatio = priceDiff.divide(buyPrice.value, decimalMode)
        val spread = spreadRatio * BigDecimal.fromInt(100)

        // Конвертируем в Double для ArbitrageOpportunitySpread
        return ArbitrageOpportunitySpread(spread.doubleValue(exactRequired = false))
    }

    companion object {
        val NONE = object : CexToCexArbitrageFinder() {
            override suspend fun findOpportunities(
                prices: List<CexPrice>,
                minSpreadPercent: Double
            ): List<CexToCexArbitrageOpportunity> {
                throw NotImplementedError("CexToCexArbitrageFinder.NONE must not be used")
            }
        }
    }
}
