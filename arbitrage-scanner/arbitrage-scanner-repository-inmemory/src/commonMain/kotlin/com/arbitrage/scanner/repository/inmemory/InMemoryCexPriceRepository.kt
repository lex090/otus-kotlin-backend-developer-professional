package com.arbitrage.scanner.repository.inmemory

import com.arbitrage.scanner.models.CexExchangeId
import com.arbitrage.scanner.models.CexPrice
import com.arbitrage.scanner.models.CexTokenId
import com.arbitrage.scanner.repository.CexPriceRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * In-memory реализация репозитория для ценовых данных CEX бирж
 *
 * Эта реализация хранит данные в памяти и обеспечивает thread-safety через корутинную синхронизацию.
 * Подходит для:
 * - MVP и прототипирования
 * - Тестирования
 * - Небольших объёмов данных (< 10000 записей)
 *
 * ## Характеристики производительности
 *
 * - **Memory**: O(n) где n = количество уникальных пар (токен, биржа)
 * - **findAll()**: O(n) - линейное копирование всех записей
 * - **findByToken()**: O(n) - линейный поиск с фильтрацией
 * - **findByExchange()**: O(n) - линейный поиск с фильтрацией
 * - **save()**: O(1) - константное время для вставки/обновления
 * - **saveAll()**: O(m) где m = количество сохраняемых записей
 * - **clear()**: O(1) - очистка мапы
 *
 * ## Thread Safety
 *
 * Все операции защищены Mutex для корректной работы в многопоточной среде.
 * Использует upsert семантику - обновляет существующую запись или создаёт новую.
 *
 * ## Ключ хранилища
 *
 * Внутренний формат ключа: `"${tokenId.value}:${exchangeId.value}"`
 * Пример: "BTC:BINANCE", "ETH:OKX"
 *
 * Это обеспечивает:
 * - Уникальность пары (токен, биржа)
 * - Быстрый доступ O(1) по составному ключу
 * - Автоматическую замену устаревших цен при повторной вставке
 *
 * @see CexPriceRepository
 */
class InMemoryCexPriceRepository : CexPriceRepository {

    /**
     * Внутреннее хранилище ценовых данных
     * Key format: "${tokenId.value}:${exchangeId.value}"
     */
    private val prices = mutableMapOf<String, CexPrice>()

    /**
     * Mutex для обеспечения thread-safety всех операций
     */
    private val mutex = Mutex()

    /**
     * Генерирует ключ для хранения в мапе из ценовой записи
     *
     * @receiver CexPrice запись, для которой нужно сгенерировать ключ
     * @return строковый ключ в формате "tokenId:exchangeId"
     */
    private fun CexPrice.toKey(): String =
        "${tokenId.value}:${exchangeId.value}"

    /**
     * Получить все доступные цены
     *
     * Возвращает snapshot данных на момент вызова.
     * Последующие изменения в репозитории не влияют на возвращённый список.
     *
     * @return новый список всех ценовых записей (не модифицируемая копия)
     */
    override suspend fun findAll(): List<CexPrice> = mutex.withLock {
        prices.values.toList()
    }

    /**
     * Найти цены по токену
     *
     * Возвращает все цены указанного токена на разных биржах.
     * Полезно для анализа ценовых расхождений конкретного токена между биржами.
     *
     * Пример использования:
     * ```kotlin
     * val btcPrices = repository.findByToken(CexTokenId("BTC"))
     * // Результат: [
     * //   CexPrice(tokenId=BTC, exchangeId=BINANCE, price=50000),
     * //   CexPrice(tokenId=BTC, exchangeId=OKX, price=50100),
     * //   CexPrice(tokenId=BTC, exchangeId=BYBIT, price=49900)
     * // ]
     * ```
     *
     * @param tokenId идентификатор токена для поиска
     * @return список цен указанного токена на различных биржах (может быть пустым)
     */
    override suspend fun findByToken(tokenId: CexTokenId): List<CexPrice> = mutex.withLock {
        prices.values.filter { it.tokenId == tokenId }
    }

    /**
     * Найти цены по бирже
     *
     * Возвращает цены всех токенов на указанной бирже.
     * Полезно для анализа ценовой ситуации на конкретной бирже.
     *
     * Пример использования:
     * ```kotlin
     * val binancePrices = repository.findByExchange(CexExchangeId("BINANCE"))
     * // Результат: [
     * //   CexPrice(tokenId=BTC, exchangeId=BINANCE, price=50000),
     * //   CexPrice(tokenId=ETH, exchangeId=BINANCE, price=3000),
     * //   CexPrice(tokenId=BNB, exchangeId=BINANCE, price=400)
     * // ]
     * ```
     *
     * @param exchangeId идентификатор биржи для поиска
     * @return список цен различных токенов на указанной бирже (может быть пустым)
     */
    override suspend fun findByExchange(exchangeId: CexExchangeId): List<CexPrice> = mutex.withLock {
        prices.values.filter { it.exchangeId == exchangeId }
    }

    /**
     * Сохранить одну ценовую запись
     *
     * Использует upsert семантику:
     * - Если запись с таким же токеном и биржей уже существует, она будет заменена
     * - Если записи не существует, она будет создана
     *
     * Это поведение полезно при обновлении цен с бирж - старая цена автоматически
     * заменяется на актуальную без необходимости явного удаления.
     *
     * Пример использования:
     * ```kotlin
     * val price = CexPrice(
     *     tokenId = CexTokenId("BTC"),
     *     exchangeId = CexExchangeId("BINANCE"),
     *     priceRaw = CexPrice.CexPriceRaw(BigDecimal.parseString("50000")),
     *     timeStamp = Timestamp.now()
     * )
     * repository.save(price) // Сохранит или обновит
     * ```
     *
     * @param price ценовая запись для сохранения/обновления
     */
    override suspend fun save(price: CexPrice): Unit = mutex.withLock {
        prices[price.toKey()] = price
    }

    /**
     * Сохранить множество ценовых записей
     *
     * Batch операция для эффективного сохранения большого количества цен.
     * Также использует upsert семантику для каждой записи.
     *
     * Операция не является атомарной в текущей реализации - если произойдёт исключение
     * в середине процесса, часть записей будет сохранена. Для production use case
     * с базой данных следует обернуть в транзакцию.
     *
     * Пример использования:
     * ```kotlin
     * val prices = listOf(
     *     CexPrice(tokenId = CexTokenId("BTC"), exchangeId = CexExchangeId("BINANCE"), ...),
     *     CexPrice(tokenId = CexTokenId("ETH"), exchangeId = CexExchangeId("OKX"), ...),
     *     // ... ещё 1000 записей
     * )
     * repository.saveAll(prices) // Сохранит все за один проход
     * ```
     *
     * @param prices список ценовых записей для сохранения
     */
    override suspend fun saveAll(prices: List<CexPrice>): Unit = mutex.withLock {
        prices.forEach { price ->
            this.prices[price.toKey()] = price
        }
    }

    /**
     * Очистить все ценовые данные
     *
     * ВНИМАНИЕ: Деструктивная операция!
     * Удаляет все данные из репозитория без возможности восстановления.
     *
     * Используется в следующих сценариях:
     * - Сброс состояния в тестах
     * - Очистка кеша перед полной перезагрузкой данных
     * - Освобождение памяти
     *
     * Пример использования:
     * ```kotlin
     * // В тестах
     * @BeforeEach
     * fun setup() {
     *     repository.clear() // Чистое состояние перед каждым тестом
     * }
     * ```
     */
    override suspend fun clear(): Unit = mutex.withLock {
        prices.clear()
    }
}
