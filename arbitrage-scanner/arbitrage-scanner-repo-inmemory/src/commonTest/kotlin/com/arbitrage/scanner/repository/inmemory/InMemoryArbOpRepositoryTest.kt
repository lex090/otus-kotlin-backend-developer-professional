package com.arbitrage.scanner.repository.inmemory

import com.arbitrage.scanner.StubsDataFactory
import com.arbitrage.scanner.base.Timestamp
import com.arbitrage.scanner.models.*
import com.arbitrage.scanner.repository.IArbOpRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class InMemoryArbOpRepositoryTest {

    private lateinit var repository: InMemoryArbOpRepository

    @BeforeTest
    fun setup() {
        repository = InMemoryArbOpRepository()
    }

    @Test
    fun testCreateSingleItem() = runTest {
        // Arrange
        val arbOp = StubsDataFactory.createArbitrageOpportunity()
        val request = IArbOpRepository.CreateArbOpRepoRequest.Item(arbOp)

        // Act
        val response = repository.create(request)

        // Assert
        assertTrue(response is IArbOpRepository.ArbOpRepoResponse.Single)
        val result = response.arbOp
        assertTrue(result.id.isNotDefault())
        assertEquals(arbOp.cexTokenId, result.cexTokenId)
        assertEquals(arbOp.spread, result.spread)
    }

    @Test
    fun testCreateSingleItemWithExistingId() = runTest {
        // Arrange
        val id = ArbitrageOpportunityId("test-id-123")
        val arbOp = StubsDataFactory.createArbitrageOpportunity(id = id.value)
        val request = IArbOpRepository.CreateArbOpRepoRequest.Item(arbOp)

        // Act
        val response = repository.create(request)

        // Assert
        assertTrue(response is IArbOpRepository.ArbOpRepoResponse.Single)
        val result = response.arbOp
        assertEquals(id, result.id)
    }

    @Test
    fun testCreateMultipleItems() = runTest {
        // Arrange
        val arbOps = listOf(
            StubsDataFactory.createArbitrageOpportunity(token = "BTC"),
            StubsDataFactory.createArbitrageOpportunity(token = "ETH"),
            StubsDataFactory.createArbitrageOpportunity(token = "BNB")
        )
        val request = IArbOpRepository.CreateArbOpRepoRequest.Items(arbOps)

        // Act
        val response = repository.create(request)

        // Assert
        assertTrue(response is IArbOpRepository.ArbOpRepoResponse.Multiple)
        val results = response.arbOps
        assertEquals(3, results.size)
        results.forEach { assertTrue(it.id.isNotDefault()) }
    }

    @Test
    fun testDtoMappingPreservesAllFields() = runTest {
        // Arrange - создаем объект с конкретными значениями
        val specificBuyPrice = 49999.123456789
        val specificSellPrice = 51234.987654321
        val specificSpread = 2.47
        val specificStartTimestamp = 1640995200L
        val specificEndTimestamp = 1641081600L

        val arbOp = StubsDataFactory.createArbitrageOpportunity(
            id = "test-dto-mapping",
            token = "ETH",
            buyExchange = "binance",
            sellExchange = "okx",
            buyPrice = specificBuyPrice,
            sellPrice = specificSellPrice,
            spread = specificSpread,
            startTimestamp = specificStartTimestamp,
            endTimestamp = specificEndTimestamp
        )

        // Act - сохраняем и читаем обратно
        val createResponse = repository.create(IArbOpRepository.CreateArbOpRepoRequest.Item(arbOp))
        assertTrue(createResponse is IArbOpRepository.ArbOpRepoResponse.Single)
        val created = createResponse.arbOp

        val readResponse = repository.read(IArbOpRepository.ReadArbOpRepoRequest.ById(created.id))
        assertTrue(readResponse is IArbOpRepository.ArbOpRepoResponse.Single)
        val result = readResponse.arbOp

        // Assert - проверяем все поля
        assertEquals(created.id, result.id)
        assertEquals(CexTokenId("ETH"), result.cexTokenId)
        assertEquals(CexExchangeId("binance"), result.buyCexExchangeId)
        assertEquals(CexExchangeId("okx"), result.sellCexExchangeId)

        // Проверяем BigDecimal цены (важно для DTO маппинга)
        assertEquals(created.buyCexPriceRaw.value, result.buyCexPriceRaw.value)
        assertEquals(created.sellCexPriceRaw.value, result.sellCexPriceRaw.value)

        // Проверяем spread
        assertEquals(ArbitrageOpportunitySpread(specificSpread), result.spread)

        // Проверяем timestamps
        assertEquals(Timestamp(specificStartTimestamp), result.startTimestamp)
        assertEquals(Timestamp(specificEndTimestamp), result.endTimestamp)
    }

    @Test
    fun testDtoMappingWithNullEndTimestamp() = runTest {
        // Arrange
        val arbOp = StubsDataFactory.createArbitrageOpportunity(
            id = "test-null-timestamp",
            endTimestamp = null
        )

        // Act
        val createResponse = repository.create(IArbOpRepository.CreateArbOpRepoRequest.Item(arbOp))
        assertTrue(createResponse is IArbOpRepository.ArbOpRepoResponse.Single)
        val created = createResponse.arbOp

        val readResponse = repository.read(IArbOpRepository.ReadArbOpRepoRequest.ById(created.id))
        assertTrue(readResponse is IArbOpRepository.ArbOpRepoResponse.Single)
        val result = readResponse.arbOp

        // Assert
        assertNull(result.endTimestamp)
    }

    @Test
    fun testReadExistingItem() = runTest {
        // Arrange
        val created = createAndStoreArbOp()
        val request = IArbOpRepository.ReadArbOpRepoRequest.ById(created.id)

        // Act
        val response = repository.read(request)

        // Assert
        assertTrue(response is IArbOpRepository.ArbOpRepoResponse.Single)
        val result = response.arbOp
        assertEquals(created.id, result.id)
        assertEquals(created.cexTokenId, result.cexTokenId)
    }

    @Test
    fun testReadNonExistingItem() = runTest {
        // Arrange
        val request = IArbOpRepository.ReadArbOpRepoRequest.ById(ArbitrageOpportunityId("non-existing"))

        // Act
        val response = repository.read(request)

        // Assert
        assertTrue(response is IArbOpRepository.ArbOpRepoResponse.Error)
        val errors = response.errors
        assertTrue(errors.isNotEmpty())
        assertEquals("repo-not-found", errors.first().code)
    }

    @Test
    fun testUpdateExistingItem() = runTest {
        // Arrange
        val created = createAndStoreArbOp()
        val updated = created.copy(spread = ArbitrageOpportunitySpread(5.0))
        val request = IArbOpRepository.UpdateArbOpRepoRequest.Item(updated)

        // Act
        val response = repository.update(request)

        // Assert
        assertTrue(response is IArbOpRepository.ArbOpRepoResponse.Single)
        val result = response.arbOp
        assertEquals(updated.spread, result.spread)
        assertEquals(created.id, result.id)
    }

    @Test
    fun testUpdateNonExistingItem() = runTest {
        // Arrange
        val arbOp = StubsDataFactory.createArbitrageOpportunity(id = "non-existing")
        val request = IArbOpRepository.UpdateArbOpRepoRequest.Item(arbOp)

        // Act
        val response = repository.update(request)

        // Assert
        assertTrue(response is IArbOpRepository.ArbOpRepoResponse.Error)
        val errors = response.errors
        assertEquals("repo-not-found", errors.first().code)
    }

    @Test
    fun testUpdateMultipleItems() = runTest {
        // Arrange
        val created1 = createAndStoreArbOp(token = "BTC")
        val created2 = createAndStoreArbOp(token = "ETH")
        val updated1 = created1.copy(spread = ArbitrageOpportunitySpread(10.0))
        val updated2 = created2.copy(spread = ArbitrageOpportunitySpread(15.0))
        val request = IArbOpRepository.UpdateArbOpRepoRequest.Items(listOf(updated1, updated2))

        // Act
        val response = repository.update(request)

        // Assert
        assertTrue(response is IArbOpRepository.ArbOpRepoResponse.Multiple)
        val results = response.arbOps
        assertEquals(2, results.size)

        // Проверяем что spread действительно обновлен
        assertTrue(results.any { it.id == created1.id && it.spread.value == 10.0 })
        assertTrue(results.any { it.id == created2.id && it.spread.value == 15.0 })
    }

    @Test
    fun testDeleteExistingItem() = runTest {
        // Arrange
        val created = createAndStoreArbOp()
        val request = IArbOpRepository.DeleteArbOpRepoRequest.Item(created.id)

        // Act
        val response = repository.delete(request)

        // Assert
        assertTrue(response is IArbOpRepository.ArbOpRepoResponse.Single)

        // Verify item is deleted
        val readResponse = repository.read(IArbOpRepository.ReadArbOpRepoRequest.ById(created.id))
        assertTrue(readResponse is IArbOpRepository.ArbOpRepoResponse.Error)
    }

    @Test
    fun testDeleteNonExistingItem() = runTest {
        // Arrange
        val request = IArbOpRepository.DeleteArbOpRepoRequest.Item(ArbitrageOpportunityId("non-existing"))

        // Act
        val response = repository.delete(request)

        // Assert
        assertTrue(response is IArbOpRepository.ArbOpRepoResponse.Error)
        val errors = response.errors
        assertEquals("repo-not-found", errors.first().code)
    }

    @Test
    fun testUpdateMultipleItemsWithPartialErrors() = runTest {
        // Arrange - создаем 2 существующих элемента
        val created1 = createAndStoreArbOp(token = "BTC")
        val created2 = createAndStoreArbOp(token = "ETH")

        // Создаем обновления: 2 существующих + 1 несуществующий
        val updated1 = created1.copy(spread = ArbitrageOpportunitySpread(10.0))
        val updated2 = created2.copy(spread = ArbitrageOpportunitySpread(15.0))
        val nonExisting = StubsDataFactory.createArbitrageOpportunity(
            id = "non-existing-id",
            token = "BNB",
            spread = 20.0
        )

        val request = IArbOpRepository.UpdateArbOpRepoRequest.Items(
            listOf(updated1, updated2, nonExisting)
        )

        // Act
        val response = repository.update(request)

        // Assert - должна вернуться ошибка
        assertTrue(response is IArbOpRepository.ArbOpRepoResponse.Error)
        val errors = response.errors
        assertEquals(1, errors.size)
        assertEquals("repo-not-found", errors.first().code)
        assertTrue(errors.first().message.contains("non-existing-id"))

        // Важно: проверяем что существующие элементы БЫЛИ обновлены
        val read1 = repository.read(IArbOpRepository.ReadArbOpRepoRequest.ById(created1.id))
        assertTrue(read1 is IArbOpRepository.ArbOpRepoResponse.Single)
        assertEquals(ArbitrageOpportunitySpread(10.0), read1.arbOp.spread)

        val read2 = repository.read(IArbOpRepository.ReadArbOpRepoRequest.ById(created2.id))
        assertTrue(read2 is IArbOpRepository.ArbOpRepoResponse.Single)
        assertEquals(ArbitrageOpportunitySpread(15.0), read2.arbOp.spread)
    }

    @Test
    fun testDeleteMultipleItemsWithPartialErrors() = runTest {
        // Arrange - создаем 2 существующих элемента
        val created1 = createAndStoreArbOp()
        val created2 = createAndStoreArbOp()
        val nonExistingId = ArbitrageOpportunityId("non-existing-id")

        val request = IArbOpRepository.DeleteArbOpRepoRequest.Items(
            listOf(created1.id, created2.id, nonExistingId)
        )

        // Act
        val response = repository.delete(request)

        // Assert - должна вернуться ошибка
        assertTrue(response is IArbOpRepository.ArbOpRepoResponse.Error)
        val errors = response.errors
        assertEquals(1, errors.size)
        assertEquals("repo-not-found", errors.first().code)

        // Важно: проверяем что существующие элементы БЫЛИ удалены
        val read1 = repository.read(IArbOpRepository.ReadArbOpRepoRequest.ById(created1.id))
        assertTrue(read1 is IArbOpRepository.ArbOpRepoResponse.Error)

        val read2 = repository.read(IArbOpRepository.ReadArbOpRepoRequest.ById(created2.id))
        assertTrue(read2 is IArbOpRepository.ArbOpRepoResponse.Error)
    }

    @Test
    fun testDeleteMultipleItems() = runTest {
        // Arrange
        val created1 = createAndStoreArbOp()
        val created2 = createAndStoreArbOp()
        val request = IArbOpRepository.DeleteArbOpRepoRequest.Items(listOf(created1.id, created2.id))

        // Act
        val response = repository.delete(request)

        // Assert
        assertTrue(response is IArbOpRepository.ArbOpRepoResponse.Multiple)
        val results = response.arbOps
        assertEquals(2, results.size)
    }

    @Test
    fun testDeleteAll() = runTest {
        // Arrange
        createAndStoreArbOp()
        createAndStoreArbOp()
        createAndStoreArbOp()
        val request = IArbOpRepository.DeleteArbOpRepoRequest.All

        // Act
        val response = repository.delete(request)

        // Assert
        assertTrue(response is IArbOpRepository.ArbOpRepoResponse.Multiple)
        val results = response.arbOps
        assertEquals(3, results.size)

        // Verify all items are deleted
        val searchResponse = repository.search(
            IArbOpRepository.SearchArbOpRepoRequest.SearchCriteria(ArbitrageOpportunityFilter())
        )
        assertTrue(searchResponse is IArbOpRepository.ArbOpRepoResponse.Multiple)
        val searchResults = searchResponse.arbOps
        assertTrue(searchResults.isEmpty())
    }

    @Test
    fun testSearchAll() = runTest {
        // Arrange
        createAndStoreArbOp(token = "BTC")
        createAndStoreArbOp(token = "ETH")
        val request = IArbOpRepository.SearchArbOpRepoRequest.SearchCriteria(
            ArbitrageOpportunityFilter()
        )

        // Act
        val response = repository.search(request)

        // Assert
        assertTrue(response is IArbOpRepository.ArbOpRepoResponse.Multiple)
        val results = response.arbOps
        assertEquals(2, results.size)
    }

    @Test
    fun testSearchByTokenId() = runTest {
        // Arrange
        createAndStoreArbOp(token = "BTC")
        createAndStoreArbOp(token = "ETH")
        createAndStoreArbOp(token = "BNB")

        val filter = ArbitrageOpportunityFilter(
            cexTokenIds = setOf(CexTokenId("BTC"), CexTokenId("ETH"))
        )
        val request = IArbOpRepository.SearchArbOpRepoRequest.SearchCriteria(filter)

        // Act
        val response = repository.search(request)

        // Assert
        assertTrue(response is IArbOpRepository.ArbOpRepoResponse.Multiple)
        val results = response.arbOps
        assertEquals(2, results.size)
        assertTrue(results.all { it.cexTokenId in filter.cexTokenIds })
    }

    @Test
    fun testSearchByExchangeId() = runTest {
        // Arrange
        createAndStoreArbOp(buyExchange = "Binance", sellExchange = "coinbase")
        createAndStoreArbOp(buyExchange = "kraken", sellExchange = "OKX")
        createAndStoreArbOp(
            buyExchange = "Bybit",
            sellExchange = "Kraken"
        )

        val filter = ArbitrageOpportunityFilter(
            cexExchangeIds = setOf(CexExchangeId("Binance"), CexExchangeId("OKX"))
        )
        val request = IArbOpRepository.SearchArbOpRepoRequest.SearchCriteria(filter)

        // Act
        val response = repository.search(request)

        // Assert
        assertTrue(response is IArbOpRepository.ArbOpRepoResponse.Multiple)
        val results = response.arbOps
        assertEquals(2, results.size)

        // Проверяем что результаты содержат правильные exchange IDs
        assertTrue(results.all { arbOp ->
            arbOp.buyCexExchangeId in filter.cexExchangeIds ||
            arbOp.sellCexExchangeId in filter.cexExchangeIds
        })
    }

    @Test
    fun testSearchByMinSpread() = runTest {
        // Arrange
        createAndStoreArbOp(spread = 1.0)
        createAndStoreArbOp(spread = 3.0)
        createAndStoreArbOp(spread = 5.0)

        val filter = ArbitrageOpportunityFilter(
            spread = ArbitrageOpportunitySpread(2.5)
        )
        val request = IArbOpRepository.SearchArbOpRepoRequest.SearchCriteria(filter)

        // Act
        val response = repository.search(request)

        // Assert
        assertTrue(response is IArbOpRepository.ArbOpRepoResponse.Multiple)
        val results = response.arbOps
        assertEquals(2, results.size)
        assertTrue(results.all { it.spread.value >= 2.5 })
    }

    @Test
    fun testSearchWithCombinedFilters() = runTest {
        // Arrange
        createAndStoreArbOp(
            token = "BTC",
            spread = 5.0,
            buyExchange = "Binance",
            sellExchange = "kraken"
        )
        createAndStoreArbOp(
            token = "BTC",
            spread = 1.0,
            buyExchange = "Binance",
            sellExchange = "kraken"
        )
        createAndStoreArbOp(
            token = "ETH",
            spread = 5.0,
            buyExchange = "OKX",
            sellExchange = "bybit"
        )

        val filter = ArbitrageOpportunityFilter(
            cexTokenIds = setOf(CexTokenId("BTC")),
            cexExchangeIds = setOf(CexExchangeId("Binance")),
            spread = ArbitrageOpportunitySpread(3.0)
        )
        val request = IArbOpRepository.SearchArbOpRepoRequest.SearchCriteria(filter)

        // Act
        val response = repository.search(request)

        // Assert
        assertTrue(response is IArbOpRepository.ArbOpRepoResponse.Multiple)
        val results = response.arbOps
        assertEquals(1, results.size)

        val result = results.first()
        // Проверяем все условия фильтра
        assertEquals(CexTokenId("BTC"), result.cexTokenId)
        assertTrue(result.spread.value >= 3.0)
        assertTrue(
            result.buyCexExchangeId == CexExchangeId("Binance") ||
            result.sellCexExchangeId == CexExchangeId("Binance")
        )
    }

    // Edge case tests

    @Test
    fun testCreateEmptyList() = runTest {
        // Arrange
        val request = IArbOpRepository.CreateArbOpRepoRequest.Items(emptyList())

        // Act
        val response = repository.create(request)

        // Assert
        assertTrue(response is IArbOpRepository.ArbOpRepoResponse.Multiple)
        val results = response.arbOps
        assertTrue(results.isEmpty())
    }

    @Test
    fun testUpdateEmptyList() = runTest {
        // Arrange
        val request = IArbOpRepository.UpdateArbOpRepoRequest.Items(emptyList())

        // Act
        val response = repository.update(request)

        // Assert
        assertTrue(response is IArbOpRepository.ArbOpRepoResponse.Multiple)
        val results = response.arbOps
        assertTrue(results.isEmpty())
    }

    @Test
    fun testDeleteEmptyList() = runTest {
        // Arrange
        val request = IArbOpRepository.DeleteArbOpRepoRequest.Items(emptyList())

        // Act
        val response = repository.delete(request)

        // Assert
        assertTrue(response is IArbOpRepository.ArbOpRepoResponse.Multiple)
        val results = response.arbOps
        assertTrue(results.isEmpty())
    }

    @Test
    fun testDeleteAllOnEmptyRepository() = runTest {
        // Arrange - пустой репозиторий
        val request = IArbOpRepository.DeleteArbOpRepoRequest.All

        // Act
        val response = repository.delete(request)

        // Assert
        assertTrue(response is IArbOpRepository.ArbOpRepoResponse.Multiple)
        val results = response.arbOps
        assertTrue(results.isEmpty())
    }

    @Test
    fun testSearchOnEmptyRepository() = runTest {
        // Arrange
        val filter = ArbitrageOpportunityFilter(
            cexTokenIds = setOf(CexTokenId("BTC"))
        )
        val request = IArbOpRepository.SearchArbOpRepoRequest.SearchCriteria(filter)

        // Act
        val response = repository.search(request)

        // Assert
        assertTrue(response is IArbOpRepository.ArbOpRepoResponse.Multiple)
        val results = response.arbOps
        assertTrue(results.isEmpty())
    }

    // Helper methods

    private suspend fun createAndStoreArbOp(
        id: String = "",
        token: String = "BTC",
        buyExchange: String = "binance",
        sellExchange: String = "okx",
        spread: Double = 2.5
    ): CexToCexArbitrageOpportunity {
        val arbOp = StubsDataFactory.createArbitrageOpportunity(
            id = id,
            token = token,
            buyExchange = buyExchange,
            sellExchange = sellExchange,
            spread = spread
        )
        val response = repository.create(IArbOpRepository.CreateArbOpRepoRequest.Item(arbOp))
        return (response as IArbOpRepository.ArbOpRepoResponse.Single).arbOp
    }
}
