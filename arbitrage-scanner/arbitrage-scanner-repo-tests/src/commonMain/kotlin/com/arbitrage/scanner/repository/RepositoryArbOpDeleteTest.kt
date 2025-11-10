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
 * Абстрактный класс тестов для проверки операций удаления арбитражных возможностей
 * в репозитории IArbOpRepository.
 */
abstract class RepositoryArbOpDeleteTest {

    protected abstract fun createRepository(): IArbOpRepository

    @Test
    fun testDeleteExistingItem() = runTest {
        // Arrange
        val repository = createRepository()
        val existing = initObject.first()

        // Act
        val deleteRequest = IArbOpRepository.DeleteArbOpRepoRequest.Item(existing.id)
        val deleteResponse = repository.delete(deleteRequest)

        // Assert
        assertIs<IArbOpRepository.ArbOpRepoResponse.Single>(deleteResponse)
        assertEquals(existing.id, deleteResponse.arbOp.id, "Deleted item ID should match")
    }

    @Test
    fun testDeleteNonExistingItem() = runTest {
        // Arrange
        val repository = createRepository()
        val nonExistingId = ArbitrageOpportunityId("non-existing-id")

        // Act
        val request = IArbOpRepository.DeleteArbOpRepoRequest.Item(nonExistingId)
        val response = repository.delete(request)

        // Assert
        assertIs<IArbOpRepository.ArbOpRepoResponse.Error>(response)
        assertTrue(response.errors.isNotEmpty(), "Should return error")
        assertEquals("repo-not-found", response.errors.first().code, "Error code should be repo-not-found")
    }

    @Test
    fun testDeleteMultipleItems() = runTest {
        // Arrange
        val repository = createRepository()
        val itemsToDelete = initObject.map { it.id }

        // Act
        val deleteRequest = IArbOpRepository.DeleteArbOpRepoRequest.Items(itemsToDelete)
        val deleteResponse = repository.delete(deleteRequest)

        // Assert
        assertIs<IArbOpRepository.ArbOpRepoResponse.Multiple>(deleteResponse)
        assertEquals(initObject.size, deleteResponse.arbOps.size, "Should delete all items")
    }

    @Test
    fun testDeleteAll() = runTest {
        // Arrange
        val repository = createRepository()

        // Act
        val deleteResponse = repository.delete(IArbOpRepository.DeleteArbOpRepoRequest.All)

        // Assert
        assertIs<IArbOpRepository.ArbOpRepoResponse.Multiple>(deleteResponse)
        assertEquals(initObject.size, deleteResponse.arbOps.size, "Should delete all items from init")
    }

    @Test
    fun testDeleteAllFromEmptyRepository() = runTest {
        // Arrange
        val repository = createRepository()
        // Сначала очищаем репозиторий
        repository.delete(IArbOpRepository.DeleteArbOpRepoRequest.All)

        // Act
        val response = repository.delete(IArbOpRepository.DeleteArbOpRepoRequest.All)

        // Assert
        assertIs<IArbOpRepository.ArbOpRepoResponse.Multiple>(response)
        assertTrue(response.arbOps.isEmpty(), "Should return empty list when deleting from empty repository")
    }

    @Test
    fun testDeleteEmptyList() = runTest {
        // Arrange
        val repository = createRepository()

        // Act
        val request = IArbOpRepository.DeleteArbOpRepoRequest.Items(emptyList())
        val response = repository.delete(request)

        // Assert
        assertIs<IArbOpRepository.ArbOpRepoResponse.Multiple>(response)
        assertTrue(response.arbOps.isEmpty(), "Should return empty list")
    }

    @Test
    fun testDeleteMultipleItemsWithOneNonExisting_ShouldRollbackAll() = runTest {
        // Arrange
        val repository = createRepository()
        val existingId = initObject.first().id
        val nonExistingId = ArbitrageOpportunityId("non-existing-delete-id")

        // Act
        val deleteRequest = IArbOpRepository.DeleteArbOpRepoRequest.Items(
            listOf(existingId, nonExistingId)
        )
        val deleteResponse = repository.delete(deleteRequest)

        // Assert
        assertIs<IArbOpRepository.ArbOpRepoResponse.Error>(deleteResponse)
        assertTrue(deleteResponse.errors.isNotEmpty(), "Should return error")
        assertEquals("repo-not-found", deleteResponse.errors.first().code, "Error code should be repo-not-found")

        // Verify: первый элемент НЕ должен быть удален (транзакция откатилась)
        val readRequest = IArbOpRepository.ReadArbOpRepoRequest.ById(existingId)
        val readResponse = repository.read(readRequest)
        assertIs<IArbOpRepository.ArbOpRepoResponse.Single>(readResponse)
        assertEquals(existingId, readResponse.arbOp.id, "First item should NOT be deleted when batch fails")
    }

    companion object : InitialObject<CexToCexArbitrageOpportunity> {
        override val initObject: List<CexToCexArbitrageOpportunity> =
            listOf(
                StubsDataFactory.createArbitrageOpportunity(id = "test-id-delete-1", token = "BTC", spread = 2.0),
                StubsDataFactory.createArbitrageOpportunity(id = "test-id-delete-2", token = "ETH", spread = 3.0)
            )
    }
}
