package com.arbitrage.scanner.repository

import com.arbitrage.scanner.StubsDataFactory
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Абстрактный класс тестов для проверки операций создания арбитражных возможностей
 * в репозитории IArbOpRepository.
 *
 * Реализация паттерна "Contract Testing" - тестирует контракт интерфейса независимо
 * от конкретной реализации.
 *
 * ## Использование
 *
 * 1. Добавить зависимость на модуль `arbitrage-scanner-repo-tests` в testImplementation
 * 2. Создать класс-наследник в тестах конкретной реализации репозитория
 * 3. Реализовать метод `createRepository()`, возвращающий экземпляр тестируемого репозитория
 * 4. В каждом @Test методе вызывать соответствующий метод из базового класса
 *
 * ## Пример использования
 *
 * ```kotlin
 * // В build.gradle.kts модуля с реализацией репозитория
 * commonTest {
 *     dependencies {
 *         implementation(project(":arbitrage-scanner-repo-tests"))
 *     }
 * }
 *
 * // В тестовом классе
 * class InMemoryArbOpRepositoryCreateTest : RepositoryArbOpCreateTest() {
 *     override fun createRepository(): IArbOpRepository {
 *         return InMemoryArbOpRepository()
 *     }
 *
 *     @Test
 *     override fun testCreateSingleItem() = super.testCreateSingleItem()
 *
 *     @Test
 *     override fun testCreateMultipleItems() = super.testCreateMultipleItems()
 *
 *     @Test
 *     override fun testCreateEmptyList() = super.testCreateEmptyList()
 *
 *     @Test
 *     override fun testCreateSingleItemWithExistingId() = super.testCreateSingleItemWithExistingId()
 * }
 * ```
 *
 * ## Доступные тесты
 *
 * - `testCreateSingleItem()` - создание одного элемента с проверкой всех полей
 * - `testCreateMultipleItems()` - создание нескольких элементов с проверкой всех полей
 * - `testCreateEmptyList()` - создание пустого списка
 * - `testCreateSingleItemWithExistingId()` - создание с явно заданным ID
 */
abstract class RepositoryArbOpCreateTest {

    /**
     * Фабричный метод для создания экземпляра тестируемого репозитория.
     * Должен быть реализован в классах-наследниках.
     */
    protected abstract fun createRepository(): IArbOpRepository

    @Test
    fun testCreateSingleItem() = runTest {
        // Arrange
        val repository = createRepository()
        val arbOp = StubsDataFactory.createArbitrageOpportunity()
        val request = IArbOpRepository.CreateArbOpRepoRequest.Item(arbOp)

        // Act
        val response = repository.create(request)

        // Assert
        assertIs<IArbOpRepository.ArbOpRepoResponse.Single>(response)
        val result = response.arbOp

        // Проверяем все поля созданного объекта
        assertTrue(result.id.isNotDefault(), "Created item should have non-default ID")
        assertEquals(arbOp.cexTokenId, result.cexTokenId, "Token ID should match")
        assertEquals(arbOp.buyCexExchangeId, result.buyCexExchangeId, "Buy exchange ID should match")
        assertEquals(arbOp.buyCexPriceRaw, result.buyCexPriceRaw, "Buy price should match")
        assertEquals(arbOp.sellCexExchangeId, result.sellCexExchangeId, "Sell exchange ID should match")
        assertEquals(arbOp.sellCexPriceRaw, result.sellCexPriceRaw, "Sell price should match")
        assertEquals(arbOp.spread, result.spread, "Spread should match")
        assertEquals(arbOp.startTimestamp, result.startTimestamp, "Start timestamp should match")
        assertEquals(arbOp.endTimestamp, result.endTimestamp, "End timestamp should match")
    }

    @Test
    fun testCreateMultipleItems() = runTest {
        // Arrange
        val repository = createRepository()
        val arbOps = listOf(
            StubsDataFactory.createArbitrageOpportunity(token = "BTC"),
            StubsDataFactory.createArbitrageOpportunity(token = "ETH"),
            StubsDataFactory.createArbitrageOpportunity(token = "BNB")
        )
        val request = IArbOpRepository.CreateArbOpRepoRequest.Items(arbOps)

        // Act
        val response = repository.create(request)

        // Assert
        assertIs<IArbOpRepository.ArbOpRepoResponse.Multiple>(response)
        val results = response.arbOps
        assertEquals(3, results.size, "Should create all 3 items")

        // Проверяем что все элементы созданы корректно
        results.forEachIndexed { index, result ->
            val original = arbOps[index]
            assertTrue(result.id.isNotDefault(), "Each created item should have non-default ID")
            assertEquals(original.cexTokenId, result.cexTokenId, "Token ID should match for item $index")
            assertEquals(
                original.buyCexExchangeId,
                result.buyCexExchangeId,
                "Buy exchange ID should match for item $index"
            )
            assertEquals(original.buyCexPriceRaw, result.buyCexPriceRaw, "Buy price should match for item $index")
            assertEquals(
                original.sellCexExchangeId,
                result.sellCexExchangeId,
                "Sell exchange ID should match for item $index"
            )
            assertEquals(original.sellCexPriceRaw, result.sellCexPriceRaw, "Sell price should match for item $index")
            assertEquals(original.spread, result.spread, "Spread should match for item $index")
            assertEquals(original.startTimestamp, result.startTimestamp, "Start timestamp should match for item $index")
            assertEquals(original.endTimestamp, result.endTimestamp, "End timestamp should match for item $index")
        }

        // Проверяем что все токены были созданы
        val createdTokens = results.map { it.cexTokenId.value }.toSet()
        assertTrue(
            createdTokens.containsAll(setOf("BTC", "ETH", "BNB")),
            "All tokens should be created"
        )
    }

    @Test
    fun testCreateEmptyList() = runTest {
        // Arrange
        val repository = createRepository()
        val request = IArbOpRepository.CreateArbOpRepoRequest.Items(emptyList())

        // Act
        val response = repository.create(request)

        // Assert
        assertTrue(
            response is IArbOpRepository.ArbOpRepoResponse.Multiple,
            "Expected Multiple response for empty list, got ${response::class.simpleName}"
        )
        val results = response.arbOps
        assertTrue(results.isEmpty(), "Should return empty list when creating empty list")
    }

    @Test
    fun testCreateSingleItemWithExistingId() = runTest {
        // Arrange
        val repository = createRepository()
        val existingId = "test-id-12345"
        val arbOp = StubsDataFactory.createArbitrageOpportunity(id = existingId)
        val request = IArbOpRepository.CreateArbOpRepoRequest.Item(arbOp)

        // Act
        val response = repository.create(request)

        // Assert
        assertTrue(
            response is IArbOpRepository.ArbOpRepoResponse.Single,
            "Expected Single response, got ${response::class.simpleName}"
        )
        val result = response.arbOp

        // Проверяем что ID сохранился
        assertEquals(arbOp.id, result.id, "ID should be preserved when explicitly provided")
        assertTrue(result.id.isNotDefault(), "Created item should have non-default ID")

        // Проверяем остальные поля
        assertEquals(arbOp.cexTokenId, result.cexTokenId, "Token ID should match")
        assertEquals(arbOp.buyCexExchangeId, result.buyCexExchangeId, "Buy exchange ID should match")
        assertEquals(arbOp.buyCexPriceRaw, result.buyCexPriceRaw, "Buy price should match")
        assertEquals(arbOp.sellCexExchangeId, result.sellCexExchangeId, "Sell exchange ID should match")
        assertEquals(arbOp.sellCexPriceRaw, result.sellCexPriceRaw, "Sell price should match")
        assertEquals(arbOp.spread, result.spread, "Spread should match")
        assertEquals(arbOp.startTimestamp, result.startTimestamp, "Start timestamp should match")
        assertEquals(arbOp.endTimestamp, result.endTimestamp, "End timestamp should match")
    }
}
