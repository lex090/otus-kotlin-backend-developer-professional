package com.arbitrage.scanner.repository

import com.arbitrage.scanner.StubsDataFactory
import com.arbitrage.scanner.models.ArbitrageOpportunitySpread
import com.arbitrage.scanner.models.CexToCexArbitrageOpportunity
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


/**
 * Абстрактный класс тестов для проверки операций обновления арбитражных возможностей
 * в репозитории IArbOpRepository.
 */
abstract class RepositoryArbOpUpdateTest {

    protected abstract fun createRepository(): IArbOpRepository

    @Test
    fun testUpdateExistingItem() = runTest {
        // Arrange
        val repository = createRepository()
        val existing = initObject.first()
        val updated = existing.copy(spread = ArbitrageOpportunitySpread(5.0))

        // Act
        val updateRequest = IArbOpRepository.UpdateArbOpRepoRequest.Item(updated)
        val updateResponse = repository.update(updateRequest)

        // Assert
        assertTrue(
            updateResponse is IArbOpRepository.ArbOpRepoResponse.Single,
            "Expected Single response, got ${updateResponse::class.simpleName}. updateResponse -> $updateResponse"
        )
        val result = updateResponse.arbOp
        assertEquals(existing.id, result.id, "ID should remain the same")
        assertEquals(ArbitrageOpportunitySpread(5.0), result.spread, "Spread should be updated")
    }

    @Test
    fun testUpdateNonExistingItem() = runTest {
        // Arrange
        val repository = createRepository()

        // Act
        val request = IArbOpRepository.UpdateArbOpRepoRequest.Item(
            StubsDataFactory.createArbitrageOpportunity(token = "TestToken", spread = 2.0)
        )
        val response = repository.update(request)

        // Assert
        assertTrue(
            response is IArbOpRepository.ArbOpRepoResponse.Error,
            "Expected Error response for non-existing item, got ${response::class.simpleName}"
        )
        val errors = response.errors
        assertTrue(errors.isNotEmpty(), "Should return error")
        assertEquals("repo-not-found", errors.first().code, "Error code should be repo-not-found")
    }

    @Test
    fun testUpdateMultipleItems() = runTest {
        // Arrange
        val repository = createRepository()

        val updated = initObject.map {
            it.copy(spread = ArbitrageOpportunitySpread(10.0))
        }

        // Act
        val updateRequest = IArbOpRepository.UpdateArbOpRepoRequest.Items(updated)
        val updateResponse = repository.update(updateRequest)

        // Assert
        assertTrue(
            updateResponse is IArbOpRepository.ArbOpRepoResponse.Multiple,
            "Expected Multiple response, got ${updateResponse::class.simpleName}"
        )
        val results = updateResponse.arbOps
        assertEquals(2, results.size, "Should update all items")
        assertTrue(
            results.all { it.spread.value == 10.0 },
            "All items should have updated spread"
        )
    }

    @Test
    fun testUpdateEmptyList() = runTest {
        // Arrange
        val repository = createRepository()

        // Act
        val request = IArbOpRepository.UpdateArbOpRepoRequest.Items(emptyList())
        val response = repository.update(request)

        // Assert
        assertTrue(
            response is IArbOpRepository.ArbOpRepoResponse.Multiple,
            "Expected Multiple response for empty list, got ${response::class.simpleName}"
        )
        val results = response.arbOps
        assertTrue(results.isEmpty(), "Should return empty list")
    }

    companion object : InitialObject<CexToCexArbitrageOpportunity> {
        override val initObject: List<CexToCexArbitrageOpportunity> =
            listOf(
                StubsDataFactory.createArbitrageOpportunity(id = "test-id-1", token = "BTC", spread = 2.0),
                StubsDataFactory.createArbitrageOpportunity(id = "test-id-2", token = "ETH", spread = 3.0)
            )
    }
}
