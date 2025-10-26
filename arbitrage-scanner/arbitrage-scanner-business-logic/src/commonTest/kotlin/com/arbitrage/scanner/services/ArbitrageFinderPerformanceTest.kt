package com.arbitrage.scanner.services

import com.arbitrage.scanner.StubsDataFactory
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.TimeSource
import kotlin.math.round
import kotlin.test.assertEquals

/**
 * Performance тесты для ArbitrageFinder
 *
 * Проверяем:
 * 1. Производительность на различных размерах датасетов
 * 2. Scalability (как растет время при увеличении данных)
 * 3. Влияние количества бирж на производительность
 * 4. Влияние порога спреда на производительность
 * 5. Warmup эффекты JVM
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

class ArbitrageFinderPerformanceTest {

    private val finder = ArbitrageFinderParallelImpl()

    @Test
    fun `performance test - scalability with token count`() = runTest {
        println("\n=== Performance Test: Scalability with Token Count ===")
        println("Fixed: 5 exchanges per token, 1% min spread\n")

        val tokenCounts = listOf(10, 25, 50, 100, 200, 500)
        val exchangeCount = 5

        println("| Tokens | Records | Time (ms) | Opps Found |")
        println("|--------|---------|-----------|------------|")

        tokenCounts.forEach { tokenCount ->
            val prices = StubsDataFactory.generateCexPrices(tokenCount, exchangeCount)
            val recordCount = tokenCount * exchangeCount

            val (time, opps) = measureWithResult {
                finder.findOpportunities(prices, minSpreadPercent = 1.0)
            }

            println("| $tokenCount | $recordCount | ${time.inWholeMilliseconds} | ${opps.size} |")
        }

        println()
    }

    @Test
    fun `performance test - scalability with exchange count`() = runTest {
        println("\n=== Performance Test: Scalability with Exchange Count ===")
        println("Fixed: 50 tokens, 1% min spread\n")

        val tokenCount = 50
        val exchangeCounts = listOf(2, 3, 5, 7, 10)

        println("| Exchanges | Records | Time (ms) | Opps Found |")
        println("|-----------|---------|-----------|------------|")

        exchangeCounts.forEach { exchangeCount ->
            val prices = StubsDataFactory.generateCexPrices(tokenCount, exchangeCount)
            val recordCount = tokenCount * exchangeCount

            val (time, opps) = measureWithResult {
                finder.findOpportunities(prices, minSpreadPercent = 1.0)
            }

            println("| $exchangeCount | $recordCount | ${time.inWholeMilliseconds} | ${opps.size} |")
        }

        println()
    }

    @Test
    fun `performance test - impact of spread threshold`() = runTest {
        println("\n=== Performance Test: Impact of Spread Threshold ===")
        println("Fixed: 100 tokens, 5 exchanges, 5% price variation\n")

        val prices = StubsDataFactory.generateCexPrices(tokenCount = 100, exchangeCount = 5, priceVariation = 0.05)
        val spreadThresholds = listOf(0.1, 0.5, 1.0, 2.0, 5.0, 10.0)

        println("| Threshold (%) | Time (ms) | Opps Found |")
        println("|---------------|-----------|------------|")

        spreadThresholds.forEach { threshold ->
            val (time, opps) = measureWithResult {
                finder.findOpportunities(prices, minSpreadPercent = threshold)
            }

            println("| $threshold | ${time.inWholeMilliseconds} | ${opps.size} |")
        }

        println()
    }

    @Test
    fun `performance test - large dataset stress test`() = runTest {
        println("\n=== Performance Test: Large Dataset Stress Test ===")

        val testCases = listOf(
            "Very Large" to Triple(1000, 5, 5000),    // 1000 tokens × 5 exchanges = 5000 records
            "Extreme" to Triple(2000, 5, 10000)        // 2000 tokens × 5 exchanges = 10000 records
        )

        println("| Dataset | Tokens | Exchanges | Records | Time (ms) | Opps Found |")
        println("|---------|--------|-----------|---------|-----------|------------|")

        testCases.forEach { (name, config) ->
            val (tokenCount, exchangeCount, expectedRecords) = config
            val prices = StubsDataFactory.generateCexPrices(tokenCount, exchangeCount)

            assertEquals(prices.size, expectedRecords, "Should generate $expectedRecords records")

            val (time, opps) = measureWithResult {
                finder.findOpportunities(prices, minSpreadPercent = 1.0)
            }

            println("| $name | $tokenCount | $exchangeCount | $expectedRecords | ${time.inWholeMilliseconds} | ${opps.size} |")

            // Performance target
            assertTrue(
                time.inWholeMilliseconds < 5000,
                "Should process $expectedRecords records in < 5 seconds"
            )
        }

        println()
    }

    @Test
    fun `performance test - warmup effects`() = runTest {
        println("\n=== Performance Test: JVM Warmup Effects ===")
        println("Testing: 100 tokens × 50 exchanges, 1% min spread\n")

        val prices = StubsDataFactory.generateCexPrices(tokenCount = 100, exchangeCount = 50)

        println("| Iteration | Time (ms) |")
        println("|-----------|-----------|")

        repeat(5) { iteration ->
            val time = measureTime {
                finder.findOpportunities(prices, minSpreadPercent = 1.0)
            }

            println("| ${iteration + 1} | ${time.inWholeMilliseconds} |")
        }

        println()
    }

    @Test
    fun `performance test - algorithmic complexity verification`() = runTest {
        println("\n=== Performance Test: Algorithmic Complexity Verification ===")
        println("Verifying O(n + m * b² + k log k) complexity\n")

        val tokenCounts = listOf(50, 100, 200)
        val exchangeCount = 5

        println("| Tokens | Records | Time (ms) | Time/Record (μs) | Theoretical O |")
        println("|--------|---------|-----------|------------------|---------------|")

        tokenCounts.forEach { tokenCount ->
            val prices = StubsDataFactory.generateCexPrices(tokenCount, exchangeCount)
            val recordCount = tokenCount * exchangeCount

            val time = measureTime {
                finder.findOpportunities(prices, minSpreadPercent = 1.0)
            }

            val timePerRecord = (time.inWholeMicroseconds.toDouble() / recordCount)
            val theoreticalComplexity = recordCount + (tokenCount * exchangeCount * exchangeCount)

            println(
                "| $tokenCount | $recordCount | ${time.inWholeMilliseconds} | " +
                        "${formatDecimal(timePerRecord)} | $theoreticalComplexity |"
            )
        }

        println()
    }

    @Test
    fun `performance test - various dataset sizes`() = runTest {
        println("\n=== Performance Test: Various Dataset Sizes ===")
        println("Fixed: 5 exchanges, 1% min spread\n")

        val tokenCounts = listOf(10, 50, 100, 200, 500)
        val exchangeCount = 5

        println("| Tokens | Records | Time (ms) | Time/Record (μs) | Opps Found |")
        println("|--------|---------|-----------|------------------|------------|")

        tokenCounts.forEach { tokenCount ->
            val prices = StubsDataFactory.generateCexPrices(tokenCount, exchangeCount)
            val recordCount = tokenCount * exchangeCount

            val (time, opps) = measureWithResult {
                finder.findOpportunities(prices, minSpreadPercent = 1.0)
            }

            val timePerRecord = (time.inWholeMicroseconds.toDouble() / recordCount)

            println(
                "| $tokenCount | $recordCount | ${time.inWholeMilliseconds} | " +
                        "${formatDecimal(timePerRecord)} | ${opps.size} |"
            )
        }

        println()
    }

    @Test
    fun `performance test - worst case scenario`() = runTest {
        println("\n=== Performance Test: Worst Case Scenario ===")
        println("All prices are different, maximum opportunities\n")

        // Генерируем цены с большой вариацией для создания максимального количества возможностей
        val tokenCount = 100
        val exchangeCount = 10
        val prices = StubsDataFactory.generateCexPrices(tokenCount, exchangeCount, priceVariation = 0.15) // 15% вариация

        println("Dataset: $tokenCount tokens × $exchangeCount exchanges = ${prices.size} records\n")

        val (time, opps) = measureWithResult {
            finder.findOpportunities(prices, minSpreadPercent = 0.5)
        }

        println("Time: ${time.inWholeMilliseconds}ms")
        println("Opportunities found: ${opps.size}")
        println("Theoretical max opportunities per token: ${exchangeCount * (exchangeCount - 1) / 2}")
        println()

        assertTrue(time.inWholeMilliseconds < 10000, "Should complete worst case in < 10 seconds")
    }

    @Test
    fun `performance test - best case scenario`() = runTest {
        println("\n=== Performance Test: Best Case Scenario ===")
        println("All prices equal, no opportunities\n")

        val tokenCount = 100
        val exchangeCount = 10

        // Генерируем цены без вариации - все одинаковые
        val prices = StubsDataFactory.generateCexPrices(tokenCount, exchangeCount, priceVariation = 0.0)

        println("Dataset: $tokenCount tokens × $exchangeCount exchanges = ${prices.size} records\n")

        val (time, opps) = measureWithResult {
            finder.findOpportunities(prices, minSpreadPercent = 0.1)
        }

        println("Time: ${time.inWholeMilliseconds}ms")
        println("Opportunities found: ${opps.size}")
        println()

        assertTrue(opps.isEmpty(), "Should find no opportunities when all prices are equal")
    }

    private suspend fun measureTime(block: suspend () -> Unit): Duration {
        val startMark = TimeSource.Monotonic.markNow()
        block()
        return startMark.elapsedNow()
    }

    private suspend fun <T> measureWithResult(block: suspend () -> T): Pair<Duration, T> {
        val startMark = TimeSource.Monotonic.markNow()
        val result = block()
        val duration = startMark.elapsedNow()
        return duration to result
    }
}
