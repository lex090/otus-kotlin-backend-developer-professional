package com.arbitrage.scanner.services

import com.arbitrage.scanner.StubsDataFactory
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.TimeSource
import kotlin.math.round

/**
 * Функциональные тесты для ArbitrageFinder
 *
 * Проверяем:
 * 1. Корректность - находятся ВСЕ возможности
 * 2. Правильность фильтрации по минимальному спреду
 * 3. Производительность
 */

// Multiplatform-совместимая функция форматирования
private fun formatDecimal(value: Double, decimals: Int = 2): String {
    val multiplier = when (decimals) {
        1 -> 10.0
        2 -> 100.0
        else -> 100.0
    }
    return (round(value * multiplier) / multiplier).toString()
}

class ArbitrageFinderTest {

    private val finder = ArbitrageFinderParallelImpl()

    @Test
    fun `test finds opportunities between exchanges`() = runTest {
        // Given: Несколько токенов с разными ценами
        val prices = listOf(
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "binance", price = 50000.0),
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "okx", price = 55000.0),
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "kraken", price = 52000.0),
            StubsDataFactory.createCexPrice(token = "ETH", exchange = "binance", price = 3000.0),
            StubsDataFactory.createCexPrice(token = "ETH", exchange = "okx", price = 3300.0)
        )

        // When: Запускаем поиск
        val result = finder.findOpportunities(prices, minSpreadPercent = 0.1)

        // Then: Должны быть найдены возможности
        assertTrue(result.isNotEmpty(), "Should find opportunities")

        // Проверяем структуру результатов
        result.forEach { opp ->
            assertTrue(opp.spread.value > 0.0, "Spread should be positive")
            assertTrue(
                opp.buyCexExchangeId != opp.sellCexExchangeId,
                "Buy and sell exchanges should be different"
            )
        }
    }

    @Test
    fun `test finds all opportunities for 3 exchanges`() = runTest {
        // Given: BTC на 3 биржах с разными ценами
        val prices = listOf(
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "binance", price = 50000.0),
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "okx", price = 55000.0),
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "kraken", price = 52000.0)
        )

        // When: Запускаем поиск
        val opps = finder.findOpportunities(prices, minSpreadPercent = 0.1)

        // Then: Должны быть найдены все 3 возможности
        assertEquals(3, opps.size, "Should find 3 opportunities")

        // Проверяем конкретные пары
        val pairs = opps.map { "${it.buyCexExchangeId.value}->${it.sellCexExchangeId.value}" }.toSet()

        assertTrue(pairs.contains("binance->okx"), "Should find binance->okx")
        assertTrue(pairs.contains("binance->kraken"), "Should find binance->kraken")
        assertTrue(pairs.contains("kraken->okx"), "Should find kraken->okx")
    }

    @Test
    fun `test filters by minimum spread`() = runTest {
        // Given: Цены с разными спредами
        val prices = listOf(
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "binance", price = 100.0),
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "okx", price = 101.0),     // 1% спред
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "kraken", price = 110.0)   // 10% спред
        )

        // When: Фильтруем по минимальному спреду 5%
        val opps = finder.findOpportunities(prices, minSpreadPercent = 5.0)

        // Then: Должны остаться только возможности со спредом >= 5%
        assertTrue(opps.all { it.spread.value >= 5.0 }, "All opportunities should have spread >= 5%")
        assertTrue(opps.any { it.sellCexExchangeId.value == "kraken" }, "Should include kraken with 10% spread")
    }

    @Test
    fun `test returns empty list for equal prices`() = runTest {
        // Given: Одинаковые цены на всех биржах
        val prices = listOf(
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "binance", price = 50000.0),
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "okx", price = 50000.0),
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "kraken", price = 50000.0)
        )

        // When: Запускаем поиск
        val opps = finder.findOpportunities(prices, minSpreadPercent = 0.1)

        // Then: Не должно быть найдено возможностей
        assertTrue(opps.isEmpty(), "Should find no opportunities when all prices are equal")
    }

    @Test
    fun `test handles large dataset correctly`() = runTest {
        // Given: Большой датасет
        val prices = StubsDataFactory.generateCexPrices(tokenCount = 50, exchangeCount = 5)

        // When: Запускаем поиск
        val result = finder.findOpportunities(prices, minSpreadPercent = 0.5)

        // Then: Результаты должны быть валидными
        result.forEach { opp ->
            assertTrue(opp.spread.value >= 0.5, "Spread should be at least 0.5%")
            assertTrue(opp.buyCexExchangeId != opp.sellCexExchangeId, "Different exchanges")
        }
    }

    @Test
    fun `benchmark - performance on various datasets`() = runTest {
        println("\n=== Benchmark: Performance on Various Datasets ===")

        // Test 1: Small dataset
        val smallPrices = StubsDataFactory.generateCexPrices(10, 5)  // 50 records
        val smallTime = measureTimeSuspend {
            finder.findOpportunities(smallPrices, minSpreadPercent = 0.5)
        }
        println("Small (10 tokens × 5 exchanges): ${smallTime.inWholeMilliseconds}ms")

        // Test 2: Medium dataset
        val mediumPrices = StubsDataFactory.generateCexPrices(50, 5)  // 250 records
        val mediumTime = measureTimeSuspend {
            val opps = finder.findOpportunities(mediumPrices, minSpreadPercent = 0.5)
            println("  Found ${opps.size} opportunities")
        }
        println("Medium (50 tokens × 5 exchanges): ${mediumTime.inWholeMilliseconds}ms")

        // Test 3: Large dataset
        val largePrices = StubsDataFactory.generateCexPrices(100, 10)  // 1000 records
        val largeTime = measureTimeSuspend {
            val opps = finder.findOpportunities(largePrices, minSpreadPercent = 0.5)
            println("  Found ${opps.size} opportunities")
        }
        println("Large (100 tokens × 10 exchanges): ${largeTime.inWholeMilliseconds}ms")

        // Verify performance target
        assertTrue(largeTime.inWholeMilliseconds < 2000, "Should process 1000 records in < 2 seconds")
    }

    @Test
    fun `test opportunity count for different exchange counts`() = runTest {
        println("\n=== Opportunity Count Analysis ===")

        val token = "BTC"
        val basePrice = 50000.0

        for (exchangeCount in 2..5) {
            val prices = (1..exchangeCount).map { i ->
                StubsDataFactory.createCexPrice(
                    token = token,
                    exchange = "exchange$i",
                    price = basePrice + (i * 1000.0)  // Цены растут
                )
            }

            val opportunities = finder.findOpportunities(prices, minSpreadPercent = 0.1)

            // Теоретический максимум: C(n,2) = n*(n-1)/2 пар с положительным спредом
            val expectedMax = exchangeCount * (exchangeCount - 1) / 2

            println("$exchangeCount exchanges: ${opportunities.size} opportunities (max possible: $expectedMax)")
            assertTrue(opportunities.size <= expectedMax, "Should not exceed theoretical maximum")
        }
    }

    @Test
    fun `test results are sorted by spread descending`() = runTest {
        // Given: Цены с разными спредами
        val prices = listOf(
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "binance", price = 100.0),
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "okx", price = 105.0),
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "kraken", price = 110.0),
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "bybit", price = 103.0)
        )

        // When: Запускаем поиск
        val opps = finder.findOpportunities(prices, minSpreadPercent = 0.1)

        // Then: Результаты должны быть отсортированы по убыванию спреда
        for (i in 0 until opps.size - 1) {
            assertTrue(
                opps[i].spread.value >= opps[i + 1].spread.value,
                "Results should be sorted by spread descending"
            )
        }
    }

    private suspend fun measureTimeSuspend(block: suspend () -> Unit): Duration {
        val startMark = TimeSource.Monotonic.markNow()
        block()
        return startMark.elapsedNow()
    }
}
