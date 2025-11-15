package com.arbitrage.scanner.algorithm

import com.arbitrage.scanner.base.Timestamp
import com.arbitrage.scanner.models.ArbitrageOpportunityId
import com.arbitrage.scanner.models.CexPrice
import com.arbitrage.scanner.models.CexToCexArbitrageOpportunity
import com.arbitrage.scanner.models.CexTokenId
import com.arbitrage.scanner.models.LockToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Параллельная реализация алгоритма поиска ВСЕХ арбитражных возможностей
 */
@OptIn(ExperimentalTime::class)
class CexToCexArbitrageFinderParallelImpl : CexToCexArbitrageFinder() {

    /**
     * Находит все арбитражные возможности с параллельной обработкой
     *
     * @param prices список ценовых данных с различных бирж
     * @param minSpreadPercent минимальный порог прибыльности (по умолчанию 0.1%)
     * @return список всех найденных возможностей
     */
    override suspend fun findOpportunities(
        prices: List<CexPrice>,
        minSpreadPercent: Double
    ): List<CexToCexArbitrageOpportunity> {
        if (prices.isEmpty()) {
            return emptyList()
        }

        val currentTimestamp = Timestamp(Clock.System.now().epochSeconds)

        val pricesByToken = prices.groupBy { it.tokenId }

        val allOpportunities = coroutineScope {
            pricesByToken.map { (tokenId, tokenPrices) ->
                async(Dispatchers.Default) {
                    findAllOpportunitiesForToken(tokenId, tokenPrices, minSpreadPercent, currentTimestamp)
                }
            }.awaitAll()
                .flatten()
        }

        return allOpportunities
    }

    /**
     * Находит ВСЕ арбитражные возможности для одного токена
     *
     * @param tokenId идентификатор токена
     * @param prices список цен этого токена на разных биржах
     * @param minSpreadPercent минимальный порог прибыльности
     * @param timestamp временная метка создания возможностей
     * @return список всех возможностей для данного токена
     */
    private fun findAllOpportunitiesForToken(
        tokenId: CexTokenId,
        prices: List<CexPrice>,
        minSpreadPercent: Double,
        timestamp: Timestamp
    ): List<CexToCexArbitrageOpportunity> {
        // Нужно минимум 2 цены для арбитража
        if (prices.size < 2) {
            return emptyList()
        }

        // Это позволяет проверять только пары где sellPrice > buyPrice
        val sortedPrices = prices.sortedBy { it.priceRaw.value }

        val opportunities = mutableListOf<CexToCexArbitrageOpportunity>()

        // Для каждой цены покупки проверяем только цены ВЫШЕ
        for (i in sortedPrices.indices) {
            val buyPrice = sortedPrices[i]

            // Проверяем только цены выше текущей (j > i)
            // Благодаря сортировке, все цены с индексом > i гарантированно выше buyPrice
            for (j in (i + 1) until sortedPrices.size) {
                val sellPrice = sortedPrices[j]

                // Проверка sellPrice > buyPrice не нужна - гарантировано сортировкой!

                // Вычислить спред
                val spread = calculateSpread(buyPrice.priceRaw, sellPrice.priceRaw)

                // Фильтрация по минимальному порогу
                if (spread.value >= minSpreadPercent) {
                    // Создать возможность
                    val opportunity = CexToCexArbitrageOpportunity(
                        id = ArbitrageOpportunityId.NONE,
                        cexTokenId = tokenId,
                        buyCexExchangeId = buyPrice.exchangeId,
                        buyCexPriceRaw = buyPrice.priceRaw,
                        sellCexExchangeId = sellPrice.exchangeId,
                        sellCexPriceRaw = sellPrice.priceRaw,
                        spread = spread,
                        startTimestamp = timestamp,
                        endTimestamp = null,
                        lockToken = LockToken.NONE,
                    )

                    opportunities.add(opportunity)
                }
            }
        }

        return opportunities
    }
}
