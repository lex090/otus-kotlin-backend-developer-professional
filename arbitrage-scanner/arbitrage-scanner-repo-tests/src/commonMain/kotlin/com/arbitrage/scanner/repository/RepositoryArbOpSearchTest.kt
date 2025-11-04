package com.arbitrage.scanner.repository

import com.arbitrage.scanner.StubsDataFactory
import com.arbitrage.scanner.models.ArbitrageOpportunityFilter
import com.arbitrage.scanner.models.ArbitrageOpportunitySpread
import com.arbitrage.scanner.models.CexExchangeId
import com.arbitrage.scanner.models.CexTokenId
import com.arbitrage.scanner.models.CexToCexArbitrageOpportunity
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Абстрактный класс тестов для проверки операций поиска арбитражных возможностей
 * в репозитории IArbOpRepository.
 */
abstract class RepositoryArbOpSearchTest {

    protected abstract fun createRepository(): IArbOpRepository

    @Test
    open fun testSearchAll() = runTest {
        // Arrange
        val repository = createRepository()

        // Act
        val searchRequest = IArbOpRepository.SearchArbOpRepoRequest.SearchCriteria(ArbitrageOpportunityFilter())
        val response = repository.search(searchRequest)

        // Assert
        assertTrue(
            response is IArbOpRepository.ArbOpRepoResponse.Multiple,
            "Expected Multiple response, got ${response::class.simpleName}"
        )
        assertEquals(initObject.size, response.arbOps.size, "Should find all items")
    }

    @Test
    open fun testSearchByTokenId() = runTest {
        // Arrange
        val repository = createRepository()

        // Act
        val filter = ArbitrageOpportunityFilter(
            cexTokenIds = setOf(CexTokenId("BTC"))
        )
        val searchRequest = IArbOpRepository.SearchArbOpRepoRequest.SearchCriteria(filter)
        val response = repository.search(searchRequest)

        // Assert
        assertTrue(
            response is IArbOpRepository.ArbOpRepoResponse.Multiple,
            "Expected Multiple response, got ${response::class.simpleName}"
        )
        val results = response.arbOps
        assertTrue(results.isNotEmpty(), "Should find items")
        assertTrue(
            results.all { it.cexTokenId in filter.cexTokenIds },
            "All results should match filter"
        )
    }

    @Test
    open fun testSearchByExchangeId() = runTest {
        // Arrange
        val repository = createRepository()

        // Act
        val filter = ArbitrageOpportunityFilter(
            cexExchangeIds = setOf(CexExchangeId("binance"))
        )
        val searchRequest = IArbOpRepository.SearchArbOpRepoRequest.SearchCriteria(filter)
        val response = repository.search(searchRequest)

        // Assert
        assertTrue(
            response is IArbOpRepository.ArbOpRepoResponse.Multiple,
            "Expected Multiple response, got ${response::class.simpleName}"
        )
        val results = response.arbOps
        assertTrue(
            results.all { it.buyCexExchangeId in filter.cexExchangeIds || it.sellCexExchangeId in filter.cexExchangeIds },
            "Results should match exchange filter"
        )
    }

    @Test
    open fun testSearchByMinSpread() = runTest {
        // Arrange
        val repository = createRepository()

        // Act
        val filter = ArbitrageOpportunityFilter(
            spread = ArbitrageOpportunitySpread(2.5)
        )
        val searchRequest = IArbOpRepository.SearchArbOpRepoRequest.SearchCriteria(filter)
        val response = repository.search(searchRequest)

        // Assert
        assertTrue(
            response is IArbOpRepository.ArbOpRepoResponse.Multiple,
            "Expected Multiple response, got ${response::class.simpleName}"
        )
        val results = response.arbOps
        assertTrue(
            results.all { it.spread.value >= 2.5 },
            "All results should have spread >= 2.5"
        )
    }

    @Test
    open fun testSearchWithCombinedFilters() = runTest {
        // Arrange
        val repository = createRepository()

        // Act
        val filter = ArbitrageOpportunityFilter(
            cexTokenIds = setOf(CexTokenId("BTC")),
            cexExchangeIds = setOf(CexExchangeId("binance")),
            spread = ArbitrageOpportunitySpread(2.0)
        )
        val searchRequest = IArbOpRepository.SearchArbOpRepoRequest.SearchCriteria(filter)
        val response = repository.search(searchRequest)

        // Assert
        assertTrue(
            response is IArbOpRepository.ArbOpRepoResponse.Multiple,
            "Expected Multiple response, got ${response::class.simpleName}"
        )
        val results = response.arbOps
        assertTrue(
            results.all { it.cexTokenId in filter.cexTokenIds },
            "All results should match token filter"
        )
        assertTrue(
            results.all { it.spread.value >= 2.0 },
            "All results should have spread >= 2.0"
        )
        assertTrue(
            results.all { it.buyCexExchangeId in filter.cexExchangeIds || it.sellCexExchangeId in filter.cexExchangeIds },
            "All results should involve specified exchange"
        )
    }

    @Test
    open fun testSearchInEmptyRepository() = runTest {
        // Arrange
        val repository = createRepository()
        // Сначала очищаем репозиторий
        repository.delete(IArbOpRepository.DeleteArbOpRepoRequest.All)

        // Act
        val filter = ArbitrageOpportunityFilter(
            cexTokenIds = setOf(CexTokenId("BTC"))
        )
        val searchRequest = IArbOpRepository.SearchArbOpRepoRequest.SearchCriteria(filter)
        val response = repository.search(searchRequest)

        // Assert
        assertTrue(
            response is IArbOpRepository.ArbOpRepoResponse.Multiple,
            "Expected Multiple response, got ${response::class.simpleName}"
        )
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
