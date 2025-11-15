package com.arbitrage.scanner.algorithm

import com.arbitrage.scanner.StubsDataFactory
import com.arbitrage.scanner.base.Timestamp
import com.arbitrage.scanner.models.CexExchangeId
import com.arbitrage.scanner.models.CexPrice
import com.arbitrage.scanner.models.CexTokenId
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Тесты для CexToCexArbitrageFinderParallelImpl
 */
class CexToCexArbitrageFinderParallelImplTest {

    private val finder = CexToCexArbitrageFinderParallelImpl()

    @Test
    fun `test empty price list returns empty opportunities`() = runTest {
        // Given: Пустой список цен
        val prices = emptyList<CexPrice>()

        // When: Ищем арбитражные возможности
        val opportunities = finder.findOpportunities(prices)

        // Then: Должен вернуться пустой список
        assertTrue(
            opportunities.isEmpty(),
            "Для пустого списка цен должен вернуться пустой список возможностей"
        )
    }

    @Test
    fun `test single price returns empty opportunities`() = runTest {
        // Given: Только одна цена для токена
        val prices = listOf(
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "binance", price = 50000.0)
        )

        // When: Ищем арбитражные возможности
        val opportunities = finder.findOpportunities(prices)

        // Then: Должен вернуться пустой список (нужно минимум 2 биржи)
        assertTrue(
            opportunities.isEmpty(),
            "Для одной цены невозможен арбитраж"
        )
    }

    @Test
    fun `test two equal prices returns empty opportunities`() = runTest {
        // Given: Две одинаковые цены на разных биржах
        val prices = listOf(
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "binance", price = 50000.0),
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "okx", price = 50000.0)
        )

        // When: Ищем арбитражные возможности
        val opportunities = finder.findOpportunities(prices, minSpreadPercent = 0.1)

        // Then: Должен вернуться пустой список (спред = 0%)
        assertTrue(
            opportunities.isEmpty(),
            "Для одинаковых цен не должно быть арбитражных возможностей"
        )
    }

    @Test
    fun `test finds opportunity when spread above threshold`() = runTest {
        // Given: Две цены с разницей выше порога
        // Цена на Binance: $50000, цена на OKX: $50500
        // Спред: (50500 - 50000) / 50000 * 100 = 1%
        val prices = listOf(
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "binance", price = 50000.0),
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "okx", price = 50500.0)
        )

        // When: Ищем арбитражные возможности с порогом 0.1%
        val opportunities = finder.findOpportunities(prices, minSpreadPercent = 0.1)

        // Then: Должна быть найдена одна возможность
        assertEquals(1, opportunities.size, "Должна быть найдена одна возможность")

        val opportunity = opportunities.first()
        assertEquals(CexTokenId("BTC"), opportunity.cexTokenId)
        assertEquals(CexExchangeId("binance"), opportunity.buyCexExchangeId)
        assertEquals(CexExchangeId("okx"), opportunity.sellCexExchangeId)
        assertTrue(opportunity.spread.value >= 0.1, "Спред должен быть >= 0.1%")
    }

    @Test
    fun `test filters opportunities below threshold`() = runTest {
        // Given: Две цены с малой разницей
        // Цена на Binance: $50000, цена на OKX: $50005
        // Спред: (50005 - 50000) / 50000 * 100 = 0.01%
        val prices = listOf(
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "binance", price = 50000.0),
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "okx", price = 50005.0)
        )

        // When: Ищем арбитражные возможности с порогом 0.1%
        val opportunities = finder.findOpportunities(prices, minSpreadPercent = 0.1)

        // Then: Не должно быть возможностей (спред меньше порога)
        assertTrue(
            opportunities.isEmpty(),
            "Возможности со спредом ниже порога должны быть отфильтрованы"
        )
    }

    @Test
    fun `test finds all opportunities for all exchange pairs`() = runTest {
        // Given: Три биржи с разными ценами
        // Binance: $100, OKX: $102, Bybit: $103
        // Возможные пары:
        // 1. Buy Binance $100, Sell OKX $102 -> spread = 2%
        // 2. Buy Binance $100, Sell Bybit $103 -> spread = 3%
        // 3. Buy OKX $102, Sell Bybit $103 -> spread = ~0.98%
        val prices = listOf(
            StubsDataFactory.createCexPrice(token = "ETH", exchange = "binance", price = 100.0),
            StubsDataFactory.createCexPrice(token = "ETH", exchange = "okx", price = 102.0),
            StubsDataFactory.createCexPrice(token = "ETH", exchange = "bybit", price = 103.0)
        )

        // When: Ищем арбитражные возможности с порогом 0.5%
        val opportunities = finder.findOpportunities(prices, minSpreadPercent = 0.5)

        // Then: Должно быть найдено 3 возможности
        assertEquals(3, opportunities.size, "Должны быть найдены все возможные пары")

        // Проверяем, что все возможности для ETH
        assertTrue(
            opportunities.all { it.cexTokenId == CexTokenId("ETH") },
            "Все возможности должны быть для ETH"
        )

        // Проверяем, что все спреды >= 0.5%
        assertTrue(
            opportunities.all { it.spread.value >= 0.5 },
            "Все спреды должны быть >= 0.5%"
        )
    }

    @Test
    fun `test processes multiple tokens in parallel`() = runTest {
        // Given: Цены для нескольких токенов
        val prices = listOf(
            // BTC: 2 возможности
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "binance", price = 50000.0),
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "okx", price = 50500.0),
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "bybit", price = 50600.0),
            // ETH: 1 возможность
            StubsDataFactory.createCexPrice(token = "ETH", exchange = "binance", price = 3000.0),
            StubsDataFactory.createCexPrice(token = "ETH", exchange = "okx", price = 3030.0),
            // USDT: нет возможностей (маленький спред)
            StubsDataFactory.createCexPrice(token = "USDT", exchange = "binance", price = 1.0),
            StubsDataFactory.createCexPrice(token = "USDT", exchange = "okx", price = 1.001)
        )

        // When: Ищем арбитражные возможности с порогом 0.5%
        val opportunities = finder.findOpportunities(prices, minSpreadPercent = 0.5)

        // Then: Должны быть найдены возможности для BTC и ETH
        val btcOpportunities = opportunities.filter { it.cexTokenId == CexTokenId("BTC") }
        val ethOpportunities = opportunities.filter { it.cexTokenId == CexTokenId("ETH") }
        val usdtOpportunities = opportunities.filter { it.cexTokenId == CexTokenId("USDT") }

        assertTrue(btcOpportunities.isNotEmpty(), "Должны быть возможности для BTC")
        assertTrue(ethOpportunities.isNotEmpty(), "Должны быть возможности для ETH")
        assertTrue(usdtOpportunities.isEmpty(), "Не должно быть возможностей для USDT")

        // Проверяем общее количество
        assertTrue(opportunities.size >= 3, "Должно быть найдено минимум 3 возможности")
    }

    @Test
    fun `test correct spread calculation`() = runTest {
        // Given: Две цены с известным спредом
        // Buy: $1000, Sell: $1050
        // Expected spread: (1050 - 1000) / 1000 * 100 = 5%
        val prices = listOf(
            StubsDataFactory.createCexPrice(token = "TEST", exchange = "exchange1", price = 1000.0),
            StubsDataFactory.createCexPrice(token = "TEST", exchange = "exchange2", price = 1050.0)
        )

        // When: Ищем арбитражные возможности
        val opportunities = finder.findOpportunities(prices, minSpreadPercent = 0.1)

        // Then: Спред должен быть близок к 5%
        assertEquals(1, opportunities.size)
        val opportunity = opportunities.first()

        // Проверяем спред с погрешностью
        val expectedSpread = 5.0
        val actualSpread = opportunity.spread.value
        val tolerance = 0.01 // 0.01% погрешность

        assertTrue(
            kotlin.math.abs(actualSpread - expectedSpread) < tolerance,
            "Спред должен быть около $expectedSpread%, но был $actualSpread%"
        )
    }

    @Test
    fun `test buy exchange always has lower price than sell exchange`() = runTest {
        // Given: Несколько цен
        val prices = listOf(
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "binance", price = 50000.0),
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "okx", price = 50500.0),
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "bybit", price = 49500.0)
        )

        // When: Ищем арбитражные возможности
        val opportunities = finder.findOpportunities(prices, minSpreadPercent = 0.1)

        // Then: В каждой возможности цена покупки должна быть ниже цены продажи
        assertTrue(opportunities.isNotEmpty(), "Должны быть найдены возможности")

        opportunities.forEach { opportunity ->
            assertTrue(
                opportunity.buyCexPriceRaw.value < opportunity.sellCexPriceRaw.value,
                "Цена покупки (${opportunity.buyCexPriceRaw.value}) должна быть ниже " +
                        "цены продажи (${opportunity.sellCexPriceRaw.value})"
            )
        }
    }

    @Test
    fun `test all opportunities have positive spread`() = runTest {
        // Given: Разные цены на биржах
        val prices = listOf(
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "binance", price = 50000.0),
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "okx", price = 50500.0),
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "bybit", price = 51000.0)
        )

        // When: Ищем арбитражные возможности
        val opportunities = finder.findOpportunities(prices, minSpreadPercent = 0.1)

        // Then: Все возможности должны иметь положительный спред
        assertTrue(opportunities.isNotEmpty(), "Должны быть найдены возможности")

        opportunities.forEach { opportunity ->
            assertTrue(
                opportunity.spread.value > 0.0,
                "Спред должен быть положительным, но был ${opportunity.spread.value}%"
            )
        }
    }

    @Test
    fun `test timestamp is set for all opportunities`() = runTest {
        // Given: Цены с арбитражными возможностями
        val prices = listOf(
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "binance", price = 50000.0),
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "okx", price = 50500.0)
        )

        // When: Ищем арбитражные возможности
        val opportunities = finder.findOpportunities(prices, minSpreadPercent = 0.1)

        // Then: Все возможности должны иметь timestamp
        assertTrue(opportunities.isNotEmpty(), "Должны быть найдены возможности")

        opportunities.forEach { opportunity ->
            assertTrue(
                opportunity.startTimestamp != Timestamp.NONE,
                "startTimestamp должен быть установлен"
            )
            assertEquals(
                opportunity.endTimestamp,
                null,
                "endTimestamp должен быть null для новых возможностей"
            )
        }
    }

    @Test
    fun `test high precision spread calculation with small prices`() = runTest {
        // Given: Маленькие цены с маленькой разницей
        // Buy: $0.001, Sell: $0.00101
        // Expected spread: (0.00101 - 0.001) / 0.001 * 100 = 1%
        val prices = listOf(
            StubsDataFactory.createCexPrice(token = "SHIB", exchange = "binance", price = 0.001),
            StubsDataFactory.createCexPrice(token = "SHIB", exchange = "okx", price = 0.00101)
        )

        // When: Ищем арбитражные возможности
        val opportunities = finder.findOpportunities(prices, minSpreadPercent = 0.5)

        // Then: Должна быть найдена возможность с правильным спредом
        assertEquals(1, opportunities.size)

        val opportunity = opportunities.first()
        assertTrue(
            opportunity.spread.value >= 0.5,
            "Спред должен быть >= 0.5%, но был ${opportunity.spread.value}%"
        )
    }

    @Test
    fun `test different tokens dont interfere with each other`() = runTest {
        // Given: Цены для разных токенов
        val prices = listOf(
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "binance", price = 50000.0),
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "okx", price = 50500.0),
            StubsDataFactory.createCexPrice(token = "ETH", exchange = "binance", price = 3000.0),
            StubsDataFactory.createCexPrice(token = "ETH", exchange = "bybit", price = 3100.0)
        )

        // When: Ищем арбитражные возможности
        val opportunities = finder.findOpportunities(prices, minSpreadPercent = 0.5)

        // Then: Должны быть найдены возможности для обоих токенов
        val btcOpportunities = opportunities.filter { it.cexTokenId == CexTokenId("BTC") }
        val ethOpportunities = opportunities.filter { it.cexTokenId == CexTokenId("ETH") }

        assertTrue(btcOpportunities.isNotEmpty(), "Должны быть возможности для BTC")
        assertTrue(ethOpportunities.isNotEmpty(), "Должны быть возможности для ETH")

        // Проверяем, что возможности не смешиваются
        btcOpportunities.forEach { opportunity ->
            assertEquals(
                opportunity.cexTokenId,
                CexTokenId("BTC"),
                "Возможность для BTC не должна смешиваться с другими токенами"
            )
        }

        ethOpportunities.forEach { opportunity ->
            assertEquals(
                opportunity.cexTokenId,
                CexTokenId("ETH"),
                "Возможность для ETH не должна смешиваться с другими токенами"
            )
        }
    }

}
