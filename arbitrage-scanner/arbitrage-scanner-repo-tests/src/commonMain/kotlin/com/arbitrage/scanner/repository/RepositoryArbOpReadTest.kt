package com.arbitrage.scanner.repository

import com.arbitrage.scanner.StubsDataFactory
import com.arbitrage.scanner.models.ArbitrageOpportunityId
import com.arbitrage.scanner.models.CexToCexArbitrageOpportunity
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Абстрактный класс тестов для проверки операций чтения арбитражных возможностей
 * в репозитории IArbOpRepository.
 */
abstract class RepositoryArbOpReadTest {

    protected abstract fun createRepository(): IArbOpRepository

    @Test
    open fun testReadExistingItem() = runTest {
        // Arrange
        val repository = createRepository()
        val existing = initObject.first()

        // Act
        val readRequest = IArbOpRepository.ReadArbOpRepoRequest.ById(existing.id)
        val readResponse = repository.read(readRequest)

        // Assert
        assertIs<IArbOpRepository.ArbOpRepoResponse.Single>(readResponse)
        val result = readResponse.arbOp

        // Проверяем все поля прочитанного объекта
        assertEquals(existing.id, result.id, "ID should match")
        assertEquals(existing.cexTokenId, result.cexTokenId, "Token ID should match")
        assertEquals(existing.buyCexExchangeId, result.buyCexExchangeId, "Buy exchange ID should match")
        assertEquals(existing.sellCexExchangeId, result.sellCexExchangeId, "Sell exchange ID should match")
        assertEquals(existing.buyCexPriceRaw, result.buyCexPriceRaw, "Buy price should match")
        assertEquals(existing.sellCexPriceRaw, result.sellCexPriceRaw, "Sell price should match")
        assertEquals(existing.spread, result.spread, "Spread should match")
        assertEquals(existing.startTimestamp, result.startTimestamp, "Start timestamp should match")
        assertEquals(existing.endTimestamp, result.endTimestamp, "End timestamp should match")
    }

    @Test
    open fun testReadNonExistingItem() = runTest {
        // Arrange
        val repository = createRepository()
        val nonExistingId = ArbitrageOpportunityId("non-existing-id")

        // Act
        val request = IArbOpRepository.ReadArbOpRepoRequest.ById(nonExistingId)
        val response = repository.read(request)

        // Assert
        assertIs<IArbOpRepository.ArbOpRepoResponse.Error>(response)
        assertTrue(response.errors.isNotEmpty(), "Should return error for non-existing item")
        assertEquals("repo-not-found", response.errors.first().code, "Error code should be repo-not-found")
    }

    companion object : InitialObject<CexToCexArbitrageOpportunity> {
        override val initObject: List<CexToCexArbitrageOpportunity> =
            listOf(
                StubsDataFactory.createArbitrageOpportunity(id = "test-id-read-1", token = "BTC", spread = 2.0),
                StubsDataFactory.createArbitrageOpportunity(id = "test-id-read-2", token = "ETH", spread = 3.0)
            )
    }
}
