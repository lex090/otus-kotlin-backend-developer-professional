package com.arbitrage.scanner.repository

import com.arbitrage.scanner.StubsDataFactory
import com.arbitrage.scanner.models.CexToCexArbitrageOpportunityFilter
import com.arbitrage.scanner.models.ArbitrageOpportunitySpread
import com.arbitrage.scanner.models.ArbitrageOpportunityStatus
import com.arbitrage.scanner.models.CexExchangeId
import com.arbitrage.scanner.models.CexExchangeIds
import com.arbitrage.scanner.models.CexTokenId
import com.arbitrage.scanner.models.CexTokenIdsFilter
import com.arbitrage.scanner.models.CexToCexArbitrageOpportunity
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Абстрактный класс тестов для проверки операций поиска арбитражных возможностей
 * в репозитории IArbOpRepository.
 */
abstract class RepositoryArbOpSearchTest {

    protected abstract fun createRepository(): IArbOpRepository

    @Test
    fun testSearchAll() = runTest {
        // Arrange
        val repository = createRepository()

        // Act
        val searchRequest = IArbOpRepository.SearchArbOpRepoRequest.SearchCriteria(
            CexToCexArbitrageOpportunityFilter(
                cexTokenIdsFilter = CexTokenIdsFilter(setOf()),
                buyExchangeIds = CexExchangeIds(emptySet()),
                sellExchangeIds = CexExchangeIds(emptySet()),
                minSpread = ArbitrageOpportunitySpread(value = 0.0),
                maxSpread = null,
                status = ArbitrageOpportunityStatus.ALL,
                startTimestampFrom = null,
                startTimestampTo = null,
                endTimestampFrom = null,
                endTimestampTo = null,
            )
        )
        val response = repository.search(searchRequest)

        // Assert
        assertIs<IArbOpRepository.ArbOpRepoResponse.Multiple>(response)
        assertEquals(initObject.size, response.arbOps.size, "Should find all items")
    }

    @Test
    fun testSearchByTokenId() = runTest {
        // Arrange
        val repository = createRepository()

        // Act
        val filter = CexToCexArbitrageOpportunityFilter(
            cexTokenIdsFilter = CexTokenIdsFilter(setOf(CexTokenId("BTC"))),
            buyExchangeIds = CexExchangeIds(emptySet()),
            sellExchangeIds = CexExchangeIds(emptySet()),
            minSpread = ArbitrageOpportunitySpread(value = 0.0),
            maxSpread = null,
            status = ArbitrageOpportunityStatus.ALL,
            startTimestampFrom = null,
            startTimestampTo = null,
            endTimestampFrom = null,
            endTimestampTo = null,
        )
        val searchRequest = IArbOpRepository.SearchArbOpRepoRequest.SearchCriteria(filter)
        val response = repository.search(searchRequest)

        // Assert
        assertIs<IArbOpRepository.ArbOpRepoResponse.Multiple>(response)
        val results = response.arbOps
        assertEquals(1, results.size, "Should find exactly 1 BTC item")
        assertTrue(
            results.all { it.cexTokenId in filter.cexTokenIdsFilter.value },
            "All results should match filter"
        )
    }

    @Test
    fun testSearchByBuyExchangeId() = runTest {
        // Arrange
        val repository = createRepository()

        // Act
        val filter = CexToCexArbitrageOpportunityFilter(
            cexTokenIdsFilter = CexTokenIdsFilter(setOf()),
            buyExchangeIds = CexExchangeIds(setOf(CexExchangeId("binance"))),
            sellExchangeIds = CexExchangeIds(emptySet()),
            minSpread = ArbitrageOpportunitySpread(value = 0.0),
            maxSpread = null,
            status = ArbitrageOpportunityStatus.ALL,
            startTimestampFrom = null,
            startTimestampTo = null,
            endTimestampFrom = null,
            endTimestampTo = null,
        )
        val searchRequest = IArbOpRepository.SearchArbOpRepoRequest.SearchCriteria(filter)
        val response = repository.search(searchRequest)

        // Assert
        assertIs<IArbOpRepository.ArbOpRepoResponse.Multiple>(response)
        val results = response.arbOps
        assertEquals(2, results.size, "Should find exactly 2 items with binance as buy exchange")
        assertTrue(
            results.all { it.buyCexExchangeId in filter.buyExchangeIds.value },
            "Results should match buy exchange filter"
        )
    }

    @Test
    fun testSearchByMinSpread() = runTest {
        // Arrange
        val repository = createRepository()

        // Act
        val filter = CexToCexArbitrageOpportunityFilter(
            cexTokenIdsFilter = CexTokenIdsFilter(setOf()),
            buyExchangeIds = CexExchangeIds(emptySet()),
            sellExchangeIds = CexExchangeIds(emptySet()),
            minSpread = ArbitrageOpportunitySpread(2.5),
            maxSpread = null,
            status = ArbitrageOpportunityStatus.ALL,
            startTimestampFrom = null,
            startTimestampTo = null,
            endTimestampFrom = null,
            endTimestampTo = null,
        )
        val searchRequest = IArbOpRepository.SearchArbOpRepoRequest.SearchCriteria(filter)
        val response = repository.search(searchRequest)

        // Assert
        assertIs<IArbOpRepository.ArbOpRepoResponse.Multiple>(response)
        val results = response.arbOps
        assertEquals(2, results.size, "Should find exactly 2 items with spread >= 2.5")
        assertTrue(
            results.all { it.spread.value >= 2.5 },
            "All results should have spread >= 2.5"
        )
    }

    @Test
    fun testSearchWithCombinedFilters() = runTest {
        // Arrange
        val repository = createRepository()

        // Act
        val filter = CexToCexArbitrageOpportunityFilter(
            cexTokenIdsFilter = CexTokenIdsFilter(setOf(CexTokenId("BTC"))),
            buyExchangeIds = CexExchangeIds(setOf(CexExchangeId("binance"))),
            sellExchangeIds = CexExchangeIds(emptySet()),
            minSpread = ArbitrageOpportunitySpread(2.0),
            maxSpread = ArbitrageOpportunitySpread(5.0),
            status = ArbitrageOpportunityStatus.ALL,
            startTimestampFrom = null,
            startTimestampTo = null,
            endTimestampFrom = null,
            endTimestampTo = null,
        )
        val searchRequest = IArbOpRepository.SearchArbOpRepoRequest.SearchCriteria(filter)
        val response = repository.search(searchRequest)

        // Assert
        assertIs<IArbOpRepository.ArbOpRepoResponse.Multiple>(response)
        val results = response.arbOps
        assertEquals(1, results.size, "Should find exactly 1 item matching all filters")
        assertTrue(
            results.all { it.cexTokenId in filter.cexTokenIdsFilter.value },
            "All results should match token filter"
        )
        assertTrue(
            results.all { it.spread.value >= 2.0 },
            "All results should have spread >= 2.0"
        )
        assertTrue(
            results.all { it.spread.value <= 5.0 },
            "All results should have spread <= 5.0"
        )
        assertTrue(
            results.all { it.buyCexExchangeId in filter.buyExchangeIds.value },
            "All results should have binance as buy exchange"
        )
    }

    @Test
    fun testSearchInEmptyRepository() = runTest {
        // Arrange
        val repository = createRepository()
        // Сначала очищаем репозиторий
        repository.delete(IArbOpRepository.DeleteArbOpRepoRequest.All)

        // Act
        val filter = CexToCexArbitrageOpportunityFilter(
            cexTokenIdsFilter = CexTokenIdsFilter(setOf(CexTokenId("BTC"))),
            buyExchangeIds = CexExchangeIds(emptySet()),
            sellExchangeIds = CexExchangeIds(emptySet()),
            minSpread = ArbitrageOpportunitySpread(value = 0.0),
            maxSpread = null,
            status = ArbitrageOpportunityStatus.ALL,
            startTimestampFrom = null,
            startTimestampTo = null,
            endTimestampFrom = null,
            endTimestampTo = null,
        )
        val searchRequest = IArbOpRepository.SearchArbOpRepoRequest.SearchCriteria(filter)
        val response = repository.search(searchRequest)

        // Assert
        assertIs<IArbOpRepository.ArbOpRepoResponse.Multiple>(response)
        assertTrue(response.arbOps.isEmpty(), "Should return empty list for empty repository")
    }

    companion object : InitialObject<CexToCexArbitrageOpportunity> {
        override val initObject: List<CexToCexArbitrageOpportunity> =
            listOf(
                StubsDataFactory.createArbitrageOpportunity(
                    id = "test-id-search-1",
                    token = "BTC",
                    buyExchange = "binance",
                    sellExchange = "okx",
                    spread = 3.0
                ),
                StubsDataFactory.createArbitrageOpportunity(
                    id = "test-id-search-2",
                    token = "ETH",
                    buyExchange = "bybit",
                    sellExchange = "kraken",
                    spread = 2.0
                ),
                StubsDataFactory.createArbitrageOpportunity(
                    id = "test-id-search-3",
                    token = "BNB",
                    buyExchange = "binance",
                    sellExchange = "huobi",
                    spread = 4.0
                )
            )
    }
}
