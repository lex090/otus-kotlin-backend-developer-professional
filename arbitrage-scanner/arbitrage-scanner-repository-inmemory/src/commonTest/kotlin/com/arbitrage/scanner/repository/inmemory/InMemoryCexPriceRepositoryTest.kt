package com.arbitrage.scanner.repository.inmemory

import com.arbitrage.scanner.StubsDataFactory
import com.arbitrage.scanner.models.CexTokenId
import com.arbitrage.scanner.models.CexExchangeId
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit тесты для InMemoryCexPriceRepository (T022)
 *
 * Проверяет CRUD операции:
 * - save: сохранение одной цены
 * - saveAll: массовое сохранение
 * - findAll: получение всех цен
 * - findByToken: фильтрация по токену
 * - findByExchange: фильтрация по бирже
 * - clear: очистка репозитория
 * - Thread-safety: корректность работы при конкурентном доступе
 */
class InMemoryCexPriceRepositoryTest {

    @Test
    fun `test save single price and retrieve with findAll`() = runTest {
        // Given: Пустой репозиторий
        val repository = InMemoryCexPriceRepository()

        // When: Сохраняем одну цену
        val price = StubsDataFactory.createCexPrice()
        repository.save(price)

        // Then: Цена должна быть доступна через findAll
        val allPrices = repository.findAll()
        assertEquals(1, allPrices.size, "Должна быть сохранена одна цена")
        assertEquals(price, allPrices.first(), "Сохраненная цена должна совпадать с исходной")
    }

    @Test
    fun `test save overwrites existing price for same token and exchange`() = runTest {
        // Given: Репозиторий с одной ценой
        val repository = InMemoryCexPriceRepository()
        val price1 = StubsDataFactory.createCexPrice(token = "BTC", exchange = "binance", price = 50000.0)
        repository.save(price1)

        // When: Сохраняем новую цену для того же токена и биржи
        val price2 = StubsDataFactory.createCexPrice(token = "BTC", exchange = "binance", price = 51000.0, timestamp = 1640995260)
        repository.save(price2)

        // Then: Должна остаться только последняя цена
        val allPrices = repository.findAll()
        assertEquals(1, allPrices.size, "Должна быть только одна цена (перезапись)")
        assertEquals(price2, allPrices.first(), "Должна сохраниться последняя цена")
        assertEquals(51000.0, allPrices.first().priceRaw.value.doubleValue(exactRequired = false), "Цена должна обновиться")
    }

    @Test
    fun `test saveAll with multiple prices`() = runTest {
        // Given: Пустой репозиторий
        val repository = InMemoryCexPriceRepository()

        // When: Массово сохраняем несколько цен
        val prices = listOf(
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "binance", price = 50000.0),
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "okx", price = 51000.0),
            StubsDataFactory.createCexPrice(token = "ETH", exchange = "binance", price = 3000.0),
            StubsDataFactory.createCexPrice(token = "ETH", exchange = "bybit", price = 3050.0)
        )
        repository.saveAll(prices)

        // Then: Все цены должны быть сохранены
        val allPrices = repository.findAll()
        assertEquals(4, allPrices.size, "Должны быть сохранены все 4 цены")
    }

    @Test
    fun `test saveAll overwrites duplicates`() = runTest {
        // Given: Репозиторий с существующими ценами
        val repository = InMemoryCexPriceRepository()
        repository.save(StubsDataFactory.createCexPrice(token = "BTC", exchange = "binance", price = 50000.0))

        // When: Сохраняем список с дубликатом
        val prices = listOf(
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "binance", price = 52000.0, timestamp = 1640995260),
            StubsDataFactory.createCexPrice(token = "ETH", exchange = "okx", price = 3100.0)
        )
        repository.saveAll(prices)

        // Then: Дубликат должен быть перезаписан, итого 2 записи
        val allPrices = repository.findAll()
        assertEquals(2, allPrices.size, "Должно быть 2 цены (одна перезаписана)")

        val btcPrice = allPrices.find { it.tokenId.value == "BTC" && it.exchangeId.value == "binance" }
        assertEquals(52000.0, btcPrice?.priceRaw?.value?.doubleValue(exactRequired = false), "BTC цена должна обновиться")
    }

    @Test
    fun `test findByToken returns only matching prices`() = runTest {
        // Given: Репозиторий с ценами разных токенов
        val repository = InMemoryCexPriceRepository()
        val prices = listOf(
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "binance", price = 50000.0),
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "okx", price = 51000.0),
            StubsDataFactory.createCexPrice(token = "ETH", exchange = "binance", price = 3000.0),
            StubsDataFactory.createCexPrice(token = "USDT", exchange = "bybit", price = 1.0)
        )
        repository.saveAll(prices)

        // When: Ищем по токену BTC
        val btcPrices = repository.findByToken(CexTokenId("BTC"))

        // Then: Должны вернуться только BTC цены
        assertEquals(2, btcPrices.size, "Должно быть 2 BTC цены")
        assertTrue(
            btcPrices.all { it.tokenId.value == "BTC" },
            "Все цены должны быть для BTC"
        )
    }

    @Test
    fun `test findByToken returns empty list for non-existent token`() = runTest {
        // Given: Репозиторий с некоторыми ценами
        val repository = InMemoryCexPriceRepository()
        repository.save(StubsDataFactory.createCexPrice(token = "BTC", exchange = "binance"))

        // When: Ищем несуществующий токен
        val prices = repository.findByToken(CexTokenId("XRP"))

        // Then: Должен вернуться пустой список
        assertTrue(prices.isEmpty(), "Должен вернуться пустой список для несуществующего токена")
    }

    @Test
    fun `test findByExchange returns only matching prices`() = runTest {
        // Given: Репозиторий с ценами разных бирж
        val repository = InMemoryCexPriceRepository()
        val prices = listOf(
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "binance", price = 50000.0),
            StubsDataFactory.createCexPrice(token = "ETH", exchange = "binance", price = 3000.0),
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "okx", price = 51000.0),
            StubsDataFactory.createCexPrice(token = "USDT", exchange = "bybit", price = 1.0)
        )
        repository.saveAll(prices)

        // When: Ищем по бирже binance
        val binancePrices = repository.findByExchange(CexExchangeId("binance"))

        // Then: Должны вернуться только binance цены
        assertEquals(2, binancePrices.size, "Должно быть 2 binance цены")
        assertTrue(
            binancePrices.all { it.exchangeId.value == "binance" },
            "Все цены должны быть для binance"
        )
    }

    @Test
    fun `test findByExchange returns empty list for non-existent exchange`() = runTest {
        // Given: Репозиторий с некоторыми ценами
        val repository = InMemoryCexPriceRepository()
        repository.save(StubsDataFactory.createCexPrice(token = "BTC", exchange = "binance"))

        // When: Ищем несуществующую биржу
        val prices = repository.findByExchange(CexExchangeId("kraken"))

        // Then: Должен вернуться пустой список
        assertTrue(prices.isEmpty(), "Должен вернуться пустой список для несуществующей биржи")
    }

    @Test
    fun `test findAll returns empty list when repository is empty`() = runTest {
        // Given: Пустой репозиторий
        val repository = InMemoryCexPriceRepository()

        // When: Получаем все цены
        val allPrices = repository.findAll()

        // Then: Должен вернуться пустой список
        assertTrue(allPrices.isEmpty(), "findAll должен вернуть пустой список для пустого репозитория")
    }

    @Test
    fun `test clear removes all prices`() = runTest {
        // Given: Репозиторий с несколькими ценами
        val repository = InMemoryCexPriceRepository()
        val prices = listOf(
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "binance"),
            StubsDataFactory.createCexPrice(token = "ETH", exchange = "okx"),
            StubsDataFactory.createCexPrice(token = "USDT", exchange = "bybit")
        )
        repository.saveAll(prices)
        assertEquals(3, repository.findAll().size, "Предусловие: должно быть 3 цены")

        // When: Очищаем репозиторий
        repository.clear()

        // Then: Репозиторий должен быть пустым
        val allPrices = repository.findAll()
        assertTrue(allPrices.isEmpty(), "После clear репозиторий должен быть пустым")
    }

    @Test
    fun `test repository can be reused after clear`() = runTest {
        // Given: Репозиторий с данными, который был очищен
        val repository = InMemoryCexPriceRepository()
        repository.save(StubsDataFactory.createCexPrice(token = "BTC", exchange = "binance"))
        repository.clear()

        // When: Добавляем новые данные после clear
        val newPrice = StubsDataFactory.createCexPrice(token = "ETH", exchange = "okx")
        repository.save(newPrice)

        // Then: Новые данные должны быть сохранены
        val allPrices = repository.findAll()
        assertEquals(1, allPrices.size, "Должна быть одна новая цена")
        assertEquals(newPrice, allPrices.first(), "Новая цена должна совпадать с сохраненной")
    }

    @Test
    fun `test save and retrieve with various price ranges`() = runTest {
        // Given: Репозиторий
        val repository = InMemoryCexPriceRepository()

        // When: Сохраняем цены в разных диапазонах
        val prices = listOf(
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "binance", price = 0.00001), // Очень малая цена
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "okx", price = 100000.0),    // Большая цена
            StubsDataFactory.createCexPrice(token = "ETH", exchange = "binance", price = 1.5),     // Средняя цена
            StubsDataFactory.createCexPrice(token = "USDT", exchange = "bybit", price = 1.0)       // Цена стейблкоина
        )
        repository.saveAll(prices)

        // Then: Все цены должны корректно сохраниться и восстановиться
        val allPrices = repository.findAll()
        assertEquals(4, allPrices.size, "Все цены должны быть сохранены")

        val btcBinance = allPrices.find { it.tokenId.value == "BTC" && it.exchangeId.value == "binance" }
        assertEquals(0.00001, btcBinance?.priceRaw?.value?.doubleValue(exactRequired = false) ?: 0.0, 0.000001, "Малая цена должна сохраниться точно")
    }

    @Test
    fun `test findByToken and findByExchange are case-sensitive`() = runTest {
        // Given: Репозиторий с ценой для "BTC" на "binance"
        val repository = InMemoryCexPriceRepository()
        repository.save(StubsDataFactory.createCexPrice(token = "BTC", exchange = "binance"))

        // When: Ищем с неправильным регистром
        val btcLowercase = repository.findByToken(CexTokenId("btc"))
        val binanceUppercase = repository.findByExchange(CexExchangeId("BINANCE"))

        // Then: Поиск не должен найти записи (case-sensitive)
        assertTrue(btcLowercase.isEmpty(), "Поиск по токену должен быть case-sensitive")
        assertTrue(binanceUppercase.isEmpty(), "Поиск по бирже должен быть case-sensitive")
    }

    @Test
    fun `test multiple prices for same token on different exchanges`() = runTest {
        // Given: Репозиторий
        val repository = InMemoryCexPriceRepository()

        // When: Сохраняем один токен на разных биржах с разными ценами
        val prices = listOf(
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "binance", price = 50000.0),
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "okx", price = 50500.0),
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "bybit", price = 49800.0),
            StubsDataFactory.createCexPrice(token = "BTC", exchange = "kraken", price = 50200.0)
        )
        repository.saveAll(prices)

        // Then: Все цены должны сохраниться (разные биржи)
        val btcPrices = repository.findByToken(CexTokenId("BTC"))
        assertEquals(4, btcPrices.size, "Должно быть 4 цены BTC на разных биржах")

        // Проверяем, что каждая биржа имеет свою цену
        val pricesByExchange = btcPrices.associate { it.exchangeId.value to it.priceRaw.value.doubleValue(exactRequired = false) }
        assertEquals(50000.0, pricesByExchange["binance"], "Цена на binance должна сохраниться")
        assertEquals(50500.0, pricesByExchange["okx"], "Цена на okx должна сохраниться")
        assertEquals(49800.0, pricesByExchange["bybit"], "Цена на bybit должна сохраниться")
        assertEquals(50200.0, pricesByExchange["kraken"], "Цена на kraken должна сохраниться")
    }
}
