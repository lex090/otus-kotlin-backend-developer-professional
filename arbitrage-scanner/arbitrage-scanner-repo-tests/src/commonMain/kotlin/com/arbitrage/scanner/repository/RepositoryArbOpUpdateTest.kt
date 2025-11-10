package com.arbitrage.scanner.repository

import com.arbitrage.scanner.StubsDataFactory
import com.arbitrage.scanner.models.ArbitrageOpportunitySpread
import com.arbitrage.scanner.models.CexToCexArbitrageOpportunity
import com.arbitrage.scanner.models.LockToken
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
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
        assertIs<IArbOpRepository.ArbOpRepoResponse.Single>(updateResponse)
        val result = updateResponse.arbOp

        // Проверяем что ID не изменился
        assertEquals(existing.id, result.id, "ID should remain the same")

        // Проверяем обновленное поле
        assertEquals(ArbitrageOpportunitySpread(5.0), result.spread, "Spread should be updated")

        // Проверяем что другие поля НЕ изменились (кроме lockToken, который должен измениться)
        assertEquals(existing.cexTokenId, result.cexTokenId, "Token ID should not change")
        assertEquals(existing.buyCexExchangeId, result.buyCexExchangeId, "Buy exchange ID should not change")
        assertEquals(existing.sellCexExchangeId, result.sellCexExchangeId, "Sell exchange ID should not change")
        assertEquals(existing.buyCexPriceRaw, result.buyCexPriceRaw, "Buy price should not change")
        assertEquals(existing.sellCexPriceRaw, result.sellCexPriceRaw, "Sell price should not change")
        assertEquals(existing.startTimestamp, result.startTimestamp, "Start timestamp should not change")
        assertEquals(existing.endTimestamp, result.endTimestamp, "End timestamp should not change")

        // Проверяем что lockToken ИЗМЕНИЛСЯ (оптимистичная блокировка)
        assertTrue(existing.lockToken != result.lockToken, "LockToken should change after update")
        assertTrue(result.lockToken.isNotDefault(), "LockToken should not be DEFAULT after update")
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
        assertIs<IArbOpRepository.ArbOpRepoResponse.Error>(response)
        assertTrue(response.errors.isNotEmpty(), "Should return error")
        assertEquals("repo-not-found", response.errors.first().code, "Error code should be repo-not-found")
    }

    @Test
    fun testUpdateWithWrongLockToken() = runTest {
        // Arrange
        val repository = createRepository()
        val existing = initObject.first()

        // Используем неправильный lockToken
        val itemWithWrongToken = existing.copy(
            spread = ArbitrageOpportunitySpread(5.0),
            lockToken = LockToken("wrong-lock-token")
        )

        // Act
        val updateRequest = IArbOpRepository.UpdateArbOpRepoRequest.Item(itemWithWrongToken)
        val updateResponse = repository.update(updateRequest)

        // Assert
        assertIs<IArbOpRepository.ArbOpRepoResponse.Error>(updateResponse)
        assertTrue(updateResponse.errors.isNotEmpty(), "Should return error")
        assertEquals(
            "repo-version-conflict",
            updateResponse.errors.first().code,
            "Error code should be repo-version-conflict"
        )

        // Verify: запись НЕ должна быть изменена
        val readRequest = IArbOpRepository.ReadArbOpRepoRequest.ById(existing.id)
        val readResponse = repository.read(readRequest)
        assertIs<IArbOpRepository.ArbOpRepoResponse.Single>(readResponse)
        assertEquals(
            existing.spread,
            readResponse.arbOp.spread,
            "Item should NOT be updated when lockToken is wrong"
        )
    }

    @Test
    fun testUpdateWithOutdatedLockToken_SimulatesLostUpdate() = runTest {
        // Arrange
        val repository = createRepository()
        val original = initObject.first()

        // Первое обновление (lockToken меняется)
        val firstUpdate = original.copy(spread = ArbitrageOpportunitySpread(5.0))
        val firstUpdateRequest = IArbOpRepository.UpdateArbOpRepoRequest.Item(firstUpdate)
        val firstUpdateResponse = repository.update(firstUpdateRequest)
        assertIs<IArbOpRepository.ArbOpRepoResponse.Single>(firstUpdateResponse)
        val afterFirstUpdate = firstUpdateResponse.arbOp

        // Проверяем что lockToken действительно изменился
        assertTrue(
            original.lockToken != afterFirstUpdate.lockToken,
            "LockToken should change after first update"
        )

        // Act: Пытаемся обновить с ОРИГИНАЛЬНЫМ (устаревшим) lockToken
        // Это симулирует "Lost Update Problem" - два пользователя читают одновременно,
        // первый обновляет, второй пытается обновить с устаревшими данными
        val secondUpdateWithOldToken = original.copy(spread = ArbitrageOpportunitySpread(10.0))
        val secondUpdateRequest = IArbOpRepository.UpdateArbOpRepoRequest.Item(secondUpdateWithOldToken)
        val secondUpdateResponse = repository.update(secondUpdateRequest)

        // Assert
        assertIs<IArbOpRepository.ArbOpRepoResponse.Error>(secondUpdateResponse)
        assertTrue(secondUpdateResponse.errors.isNotEmpty(), "Should return error")
        assertEquals(
            "repo-version-conflict",
            secondUpdateResponse.errors.first().code,
            "Error code should be repo-version-conflict"
        )

        // Verify: запись должна сохранить значение после первого обновления
        val readRequest = IArbOpRepository.ReadArbOpRepoRequest.ById(original.id)
        val readResponse = repository.read(readRequest)
        assertIs<IArbOpRepository.ArbOpRepoResponse.Single>(readResponse)
        assertEquals(
            ArbitrageOpportunitySpread(5.0),
            readResponse.arbOp.spread,
            "Item should keep value from first update (not be overwritten by second)"
        )
    }

    @Test
    fun testUpdateMultipleItemsWithWrongLockToken_ShouldRollbackAll() = runTest {
        // Arrange
        val repository = createRepository()

        // Первый элемент с правильным lockToken
        val firstItem = initObject.first().copy(spread = ArbitrageOpportunitySpread(10.0))

        // Второй элемент с НЕПРАВИЛЬНЫМ lockToken
        val secondItem = initObject[1].copy(
            spread = ArbitrageOpportunitySpread(20.0),
            lockToken = LockToken("wrong-lock-token-2")
        )

        // Act
        val updateRequest = IArbOpRepository.UpdateArbOpRepoRequest.Items(
            listOf(firstItem, secondItem)
        )
        val updateResponse = repository.update(updateRequest)

        // Assert
        assertIs<IArbOpRepository.ArbOpRepoResponse.Error>(updateResponse)
        assertTrue(updateResponse.errors.isNotEmpty(), "Should return error")
        assertEquals(
            "repo-version-conflict",
            updateResponse.errors.first().code,
            "Error code should be repo-version-conflict"
        )

        // Verify: первый элемент НЕ должен быть обновлен (транзакция откатилась)
        val readRequest = IArbOpRepository.ReadArbOpRepoRequest.ById(initObject.first().id)
        val readResponse = repository.read(readRequest)
        assertIs<IArbOpRepository.ArbOpRepoResponse.Single>(readResponse)

        // Проверяем что spread остался прежним (НЕ изменился на 10.0)
        assertEquals(
            initObject.first().spread,
            readResponse.arbOp.spread,
            "First item should NOT be updated when batch fails due to version conflict"
        )
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
        assertIs<IArbOpRepository.ArbOpRepoResponse.Multiple>(updateResponse)
        val results = updateResponse.arbOps
        assertEquals(initObject.size, results.size, "Should update all items")
        assertTrue(
            results.all { it.spread.value == 10.0 },
            "All items should have updated spread"
        )
        // Проверяем что у всех lockToken изменились
        results.forEachIndexed { index, result ->
            assertTrue(
                initObject[index].lockToken != result.lockToken,
                "LockToken should change for item $index"
            )
        }
    }

    @Test
    fun testUpdateEmptyList() = runTest {
        // Arrange
        val repository = createRepository()

        // Act
        val request = IArbOpRepository.UpdateArbOpRepoRequest.Items(emptyList())
        val response = repository.update(request)

        // Assert
        assertIs<IArbOpRepository.ArbOpRepoResponse.Multiple>(response)
        assertTrue(response.arbOps.isEmpty(), "Should return empty list")
    }

    @Test
    fun testUpdateMultipleItemsWithOneNonExisting_ShouldRollbackAll() = runTest {
        // Arrange
        val repository = createRepository()
        val existingItem = initObject.first().copy(spread = ArbitrageOpportunitySpread(10.0))
        val nonExistingItem = StubsDataFactory.createArbitrageOpportunity(
            id = "non-existing-id",
            token = "XXX",
            spread = 20.0,
            lockToken = "some-random-lock-token" // Несуществующий lockToken
        )

        // Act
        val updateRequest = IArbOpRepository.UpdateArbOpRepoRequest.Items(
            listOf(existingItem, nonExistingItem)
        )
        val updateResponse = repository.update(updateRequest)

        // Assert
        assertIs<IArbOpRepository.ArbOpRepoResponse.Error>(updateResponse)
        assertTrue(updateResponse.errors.isNotEmpty(), "Should return error")
        assertEquals("repo-not-found", updateResponse.errors.first().code, "Error code should be repo-not-found")

        // Verify: первый элемент НЕ должен быть обновлен (транзакция откатилась)
        val readRequest = IArbOpRepository.ReadArbOpRepoRequest.ById(initObject.first().id)
        val readResponse = repository.read(readRequest)
        assertIs<IArbOpRepository.ArbOpRepoResponse.Single>(readResponse)

        // Проверяем что spread остался прежним (НЕ изменился на 10.0)
        assertEquals(
            initObject.first().spread,
            readResponse.arbOp.spread,
            "First item should NOT be updated when batch fails"
        )
    }

    companion object : InitialObject<CexToCexArbitrageOpportunity> {
        override val initObject: List<CexToCexArbitrageOpportunity> =
            listOf(
                StubsDataFactory.createArbitrageOpportunity(
                    id = "test-id-1",
                    token = "BTC",
                    spread = 2.0,
                    lockToken = "test-lock-token-1"
                ),
                StubsDataFactory.createArbitrageOpportunity(
                    id = "test-id-2",
                    token = "ETH",
                    spread = 3.0,
                    lockToken = "test-lock-token-2"
                )
            )
    }
}
