package com.arbitrage.scanner.algorithm

import com.arbitrage.scanner.StubsDataFactory
import kotlinx.coroutines.test.runTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.measureTime

/**
 * Performance тесты для CexToCexArbitrageFinderParallelImpl
 *
 * Проверяют производительность алгоритма при различных условиях:
 * - Малое количество токенов и бирж
 * - Среднее количество токенов и бирж
 * - Большое количество токенов и бирж
 *
 * Тесты также демонстрируют эффективность оптимизации с сортировкой.
 */
class CexToCexArbitrageFinderPerformanceTest {

    private val finder = CexToCexArbitrageFinderParallelImpl()

    @Test
    fun `performance test with small dataset - 5 tokens x 3 exchanges`() = runTest {
        // Given: 5 токенов, 3 биржи, вариация 5%
        val prices = StubsDataFactory.generateCexPrices(
            tokenCount = 5,
            exchangeCount = 3,
            priceVariation = 0.05
        )

        println("\n=== Performance Test: Small Dataset ===")
        println("Tokens: 5, Exchanges: 3, Total prices: ${prices.size}")

        // When: Измеряем время выполнения
        val duration = measureTime {
            val opportunities = finder.findOpportunities(prices, minSpreadPercent = 0.1)
            println("Found opportunities: ${opportunities.size}")
        }

        // Then: Выводим результат
        println("Execution time: ${duration.inWholeMilliseconds}ms")
        println("Average per price: ${duration.inWholeMicroseconds.toDouble() / prices.size}μs")

        // Проверяем что выполнение быстрое (должно быть < 100ms)
        assertTrue(
            duration.inWholeMilliseconds < 100,
            "Поиск для малого датасета должен занимать < 100ms, но занял ${duration.inWholeMilliseconds}ms"
        )
    }

    @Test
    fun `performance test with medium dataset - 50 tokens x 5 exchanges`() = runTest {
        // Given: 50 токенов, 5 бирж, вариация 3%
        val prices = StubsDataFactory.generateCexPrices(
            tokenCount = 50,
            exchangeCount = 5,
            priceVariation = 0.03
        )

        println("\n=== Performance Test: Medium Dataset ===")
        println("Tokens: 50, Exchanges: 5, Total prices: ${prices.size}")

        // When: Измеряем время выполнения
        val duration = measureTime {
            val opportunities = finder.findOpportunities(prices, minSpreadPercent = 0.1)
            println("Found opportunities: ${opportunities.size}")
        }

        // Then: Выводим результат
        println("Execution time: ${duration.inWholeMilliseconds}ms")
        println("Average per price: ${duration.inWholeMicroseconds.toDouble() / prices.size}μs")

        // Проверяем что выполнение быстрое (должно быть < 500ms)
        assertTrue(
            duration.inWholeMilliseconds < 500,
            "Поиск для среднего датасета должен занимать < 500ms, но занял ${duration.inWholeMilliseconds}ms"
        )
    }

    @Test
    fun `performance test with large dataset - 100 tokens x 10 exchanges`() = runTest {
        // Given: 100 токенов, 10 бирж, вариация 2%
        val prices = StubsDataFactory.generateCexPrices(
            tokenCount = 100,
            exchangeCount = 10,
            priceVariation = 0.02
        )

        println("\n=== Performance Test: Large Dataset ===")
        println("Tokens: 100, Exchanges: 10, Total prices: ${prices.size}")

        // When: Измеряем время выполнения
        val duration = measureTime {
            val opportunities = finder.findOpportunities(prices, minSpreadPercent = 0.1)
            println("Found opportunities: ${opportunities.size}")
        }

        // Then: Выводим результат
        println("Execution time: ${duration.inWholeMilliseconds}ms")
        println("Average per price: ${duration.inWholeMicroseconds.toDouble() / prices.size}μs")

        // Проверяем что выполнение быстрое (должно быть < 2000ms = 2 секунды)
        assertTrue(
            duration.inWholeMilliseconds < 2000,
            "Поиск для большого датасета должен занимать < 2000ms, но занял ${duration.inWholeMilliseconds}ms"
        )
    }

    @Test
    fun `performance test with many exchanges - 10 tokens x 10 exchanges`() = runTest {
        // Given: 10 токенов, 10 бирж - тест оптимизации с сортировкой
        // С 10 биржами на токен: 10*(10-1)/2 = 45 пар для проверки каждого токена
        val prices = StubsDataFactory.generateCexPrices(
            tokenCount = 10,
            exchangeCount = 10,
            priceVariation = 0.04
        )

        println("\n=== Performance Test: Many Exchanges ===")
        println("Tokens: 10, Exchanges: 10, Total prices: ${prices.size}")
        println("Expected pairs per token: 45")

        // When: Измеряем время выполнения
        val duration = measureTime {
            val opportunities = finder.findOpportunities(prices, minSpreadPercent = 0.1)
            println("Found opportunities: ${opportunities.size}")
        }

        // Then: Выводим результат
        println("Execution time: ${duration.inWholeMilliseconds}ms")
        println("Pairs per ms: ${(10 * 45).toDouble() / duration.inWholeMilliseconds}")

        // Проверяем что выполнение быстрое (должно быть < 500ms)
        assertTrue(
            duration.inWholeMilliseconds < 500,
            "Поиск с большим количеством бирж должен занимать < 500ms, но занял ${duration.inWholeMilliseconds}ms"
        )
    }

    @Ignore
    @Test
    fun `performance comparison test - multiple runs for consistency`() = runTest {
        // Given: Средний датасет для повторных замеров
        val prices = StubsDataFactory.generateCexPrices(
            tokenCount = 30,
            exchangeCount = 6,
            priceVariation = 0.03
        )

        println("\n=== Performance Consistency Test ===")
        println("Tokens: 30, Exchanges: 6, Total prices: ${prices.size}")
        println("Running 5 iterations...")

        val durations = mutableListOf<Long>()

        // When: Выполняем 5 раз для проверки консистентности
        repeat(5) { iteration ->
            val duration = measureTime {
                finder.findOpportunities(prices, minSpreadPercent = 0.1)
            }
            durations.add(duration.inWholeMilliseconds)
            println("Iteration ${iteration + 1}: ${duration.inWholeMilliseconds}ms")
        }

        // Then: Вычисляем статистику
        val avgDuration = durations.average()
        val minDuration = durations.min()
        val maxDuration = durations.max()

        println("\nStatistics:")
        println("  Min: ${minDuration}ms")
        println("  Max: ${maxDuration}ms")
        println("  Avg: ${(avgDuration * 100).toLong() / 100.0}ms")
        println("  Variance: ${maxDuration - minDuration}ms")

        // Проверяем что среднее время приемлемое
        assertTrue(
            avgDuration < 500,
            "Среднее время выполнения должно быть < 500ms, но было ${(avgDuration * 100).toLong() / 100.0}ms"
        )

        // Проверяем что разброс небольшой (не более 50% от среднего)
        val variance = maxDuration - minDuration
        assertTrue(
            variance < avgDuration * 0.5,
            "Разброс времени выполнения слишком большой: ${variance}ms (${(variance / avgDuration * 100).toInt()}% от среднего)"
        )
    }

    @Test
    fun `performance test with high spread threshold - early filtering`() = runTest {
        // Given: Большой датасет с высоким порогом
        // Высокий порог должен отфильтровать много результатов
        val prices = StubsDataFactory.generateCexPrices(
            tokenCount = 100,
            exchangeCount = 8,
            priceVariation = 0.02
        )

        println("\n=== Performance Test: High Threshold Filtering ===")
        println("Tokens: 100, Exchanges: 8, Total prices: ${prices.size}")

        // When: Измеряем время с высоким порогом
        val duration = measureTime {
            val opportunities = finder.findOpportunities(prices, minSpreadPercent = 1.5)
            println("Found opportunities with 1.5% threshold: ${opportunities.size}")
        }

        // Then: Выводим результат
        println("Execution time: ${duration.inWholeMilliseconds}ms")

        // С высоким порогом должно быть быстрее (меньше результатов для сортировки)
        assertTrue(
            duration.inWholeMilliseconds < 2000,
            "Поиск с высоким порогом должен быть быстрым, но занял ${duration.inWholeMilliseconds}ms"
        )
    }

    @Test
    fun `stress test - extreme dataset`() = runTest {
        // Given: Очень большой датасет для стресс-теста
        val prices = StubsDataFactory.generateCexPrices(
            tokenCount = 200,
            exchangeCount = 10,
            priceVariation = 0.03
        )

        println("\n=== Stress Test: Extreme Dataset ===")
        println("Tokens: 200, Exchanges: 10, Total prices: ${prices.size}")
        println("Expected pairs to check: ${200 * 45} = 9000")

        // When: Измеряем время выполнения
        val duration = measureTime {
            val opportunities = finder.findOpportunities(prices, minSpreadPercent = 0.1)
            println("Found opportunities: ${opportunities.size}")
        }

        // Then: Выводим результат
        println("Execution time: ${duration.inWholeMilliseconds}ms")
        println("Pairs per second: ${(9000.0 / duration.inWholeMilliseconds * 1000).toInt()}")

        // Даже для очень большого датасета должно быть разумное время (< 5 секунд)
        assertTrue(
            duration.inWholeMilliseconds < 5000,
            "Даже стресс-тест должен завершиться за < 5000ms, но занял ${duration.inWholeMilliseconds}ms"
        )
    }
}
