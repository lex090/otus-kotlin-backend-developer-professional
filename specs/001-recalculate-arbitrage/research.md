# Research: Recalculate Arbitrage Opportunities

**Feature**: 001-recalculate-arbitrage
**Date**: 2025-10-26
**Status**: Complete

## Overview

Исследование оптимальных подходов для реализации алгоритма поиска арбитражных возможностей, паттернов репозиториев и генерации моковых данных для тестирования производительности.

## Research Tasks

### 1. Arbitrage Detection Algorithm

**Decision**: Использовать группировку по токенам с поиском min/max цен

**Rationale**:
Для эффективного поиска арбитражных возможностей между биржами критично избежать наивного O(n²) сравнения всех пар. Оптимальный подход:

1. **Группировка по токенам** (O(n))
   ```kotlin
   val pricesByToken: Map<CexTokenId, List<CexPrice>> =
       allPrices.groupBy { it.tokenId }
   ```

2. **Поиск min/max для каждого токена** (O(k) где k = количество бирж для токена, обычно k << n)
   ```kotlin
   pricesByToken.forEach { (tokenId, prices) ->
       val minPrice = prices.minByOrNull { it.priceRaw.value }
       val maxPrice = prices.maxByOrNull { it.priceRaw.value }
       // Calculate spread and create opportunity if profitable
   }
   ```

3. **Общая сложность**: O(n) + O(n) = O(n) где n = общее количество ценовых записей

**Alternatives Considered**:
- **Наивный O(n²)**: Сравнение каждой пары цен - отклонён из-за неприемлемой производительности
- **Индексирование с помощью B-Tree**: Избыточно для in-memory структур, Map достаточно эффективен
- **Parallel streams**: Усложняет код, для 1000 записей overhead больше выигрыша

**Implementation Details**:
```kotlin
fun findArbitrageOpportunities(
    prices: List<CexPrice>,
    minSpreadPercent: Double = 0.1
): List<CexToCexArbitrageOpportunity> {
    return prices
        .groupBy { it.tokenId }
        .flatMap { (tokenId, tokenPrices) ->
            // Для каждого токена найти все пары с прибыльным спредом
            findOpportunitiesForToken(tokenId, tokenPrices, minSpreadPercent)
        }
        .sortedByDescending { it.spread.value }
}

private fun findOpportunitiesForToken(
    tokenId: CexTokenId,
    prices: List<CexPrice>,
    minSpreadPercent: Double
): List<CexToCexArbitrageOpportunity> {
    if (prices.size < 2) return emptyList()

    // Найти минимальную и максимальную цену (используя BigDecimal из CexPriceRaw)
    val buyPrice = prices.minByOrNull { it.priceRaw.value } ?: return emptyList()
    val sellPrice = prices.maxByOrNull { it.priceRaw.value } ?: return emptyList()

    // Если биржи совпадают или спред недостаточен - пропустить
    if (buyPrice.exchangeId == sellPrice.exchangeId) return emptyList()

    // calculateSpread принимает CexPriceRaw и возвращает ArbitrageOpportunitySpread
    val spread = calculateSpread(buyPrice.priceRaw, sellPrice.priceRaw)
    if (spread.value < minSpreadPercent) return emptyList()

    return listOf(createOpportunity(tokenId, buyPrice, sellPrice, spread))
}

private fun createOpportunity(
    tokenId: CexTokenId,
    buyPrice: CexPrice,
    sellPrice: CexPrice,
    spread: ArbitrageOpportunitySpread
): CexToCexArbitrageOpportunity {
    return CexToCexArbitrageOpportunity(
        id = ArbitrageOpportunityId.DEFAULT, // Будет сгенерирован при сохранении
        cexTokenId = tokenId,
        buyCexExchangeId = buyPrice.exchangeId,
        buyCexPriceRaw = buyPrice.priceRaw,
        sellCexExchangeId = sellPrice.exchangeId,
        sellCexPriceRaw = sellPrice.priceRaw,
        spread = spread,
        startTimestamp = Timestamp.now(),
        endTimestamp = null
    )
}
```

**Performance Characteristics**:
- **Time Complexity**: O(n) где n = количество ценовых записей
- **Space Complexity**: O(n) для группировки
- **Expected Performance**: < 10ms для 1000 записей на современном CPU

---

### 2. In-Memory Repository Pattern

**Decision**: Repository Pattern с простыми Map/MutableList структурами

**Rationale**:
Repository Pattern обеспечивает:
- Абстракцию источника данных
- Легкую замену реализации (in-memory → database)
- Упрощённое тестирование через моки
- Соблюдение Dependency Inversion Principle

**Interface Design**:
```kotlin
// CexPriceRepository.kt в arbitrage-scanner-common
interface CexPriceRepository {
    suspend fun findAll(): List<CexPrice>
    suspend fun findByToken(tokenId: CexTokenId): List<CexPrice>
    suspend fun findByExchange(exchangeId: CexExchangeId): List<CexPrice>
    suspend fun save(price: CexPrice)
    suspend fun saveAll(prices: List<CexPrice>)
    suspend fun clear()
}

// ArbitrageOpportunityRepository.kt в arbitrage-scanner-common
interface ArbitrageOpportunityRepository {
    suspend fun save(opportunity: CexToCexArbitrageOpportunity): ArbitrageOpportunityId
    suspend fun saveAll(opportunities: List<CexToCexArbitrageOpportunity>): List<ArbitrageOpportunityId>
    suspend fun findById(id: ArbitrageOpportunityId): CexToCexArbitrageOpportunity?
    suspend fun findAll(): List<CexToCexArbitrageOpportunity>
    suspend fun findActive(): List<CexToCexArbitrageOpportunity> // endTimestamp == null
    suspend fun markAsEnded(id: ArbitrageOpportunityId, endTimestamp: Timestamp)
    suspend fun clear()
}
```

**Implementation Strategy**:
```kotlin
// InMemoryCexPriceRepository.kt
class InMemoryCexPriceRepository : CexPriceRepository {
    private val prices = ConcurrentHashMap<String, CexPrice>()

    override suspend fun findAll(): List<CexPrice> =
        prices.values.toList()

    override suspend fun findByToken(tokenId: CexTokenId): List<CexPrice> =
        prices.values.filter { it.tokenId == tokenId }

    // ... остальные методы
}

// InMemoryArbitrageOpportunityRepository.kt
class InMemoryArbitrageOpportunityRepository : ArbitrageOpportunityRepository {
    private val opportunities = ConcurrentHashMap<ArbitrageOpportunityId, CexToCexArbitrageOpportunity>()
    private val idGenerator = AtomicLong(0)

    override suspend fun save(opportunity: CexToCexArbitrageOpportunity): ArbitrageOpportunityId {
        val id = if (opportunity.id.isDefault()) {
            ArbitrageOpportunityId("arb-${idGenerator.incrementAndGet()}")
        } else {
            opportunity.id
        }
        val withId = opportunity.copy(id = id)
        opportunities[id] = withId
        return id
    }

    // ... остальные методы
}
```

**Alternatives Considered**:
- **Direct access to collections**: Отклонено - нарушает Dependency Inversion, усложняет тестирование
- **Generic Repository<T>**: Избыточно для простых случаев, YAGNI principle
- **CQRS with separate read/write models**: Overkill для in-memory хранилища

**Thread Safety**:
Используется `ConcurrentHashMap` для потокобезопасности, т.к. Kotlin Coroutines могут выполняться на разных потоках. Для дополнительной защиты можно добавить `Mutex` на критичные секции, но для MVP достаточно ConcurrentHashMap.

---

### 3. Mock Data Generation Strategy

**Decision**: Программная генерация с контролируемым распределением спредов

**Rationale**:
Для тестирования алгоритма и производительности необходим детерминированный набор данных с известными характеристиками:
- Контролируемое количество токенов и бирж
- Гарантированное наличие арбитражных возможностей
- Реалистичные цены и спреды
- Воспроизводимость для регрессионного тестирования

**Generation Algorithm**:
```kotlin
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.arbitrage.scanner.models.CexPrice
import com.arbitrage.scanner.models.CexPrice.CexPriceRaw
import com.arbitrage.scanner.models.CexTokenId
import com.arbitrage.scanner.models.CexExchangeId
import com.arbitrage.scanner.base.Timestamp
import kotlin.random.Random

class MockCexPriceGenerator(private val seed: Long = 42) {
    private val random = Random(seed)

    data class Config(
        val tokenCount: Int = 10,
        val exchangeCount: Int = 5,
        val basePrice: Double = 100.0,
        val maxSpreadPercent: Double = 5.0,
        val guaranteedArbitrageCount: Int = 10
    )

    fun generate(config: Config = Config()): List<CexPrice> {
        val prices = mutableListOf<CexPrice>()
        val tokens = generateTokens(config.tokenCount)
        val exchanges = generateExchanges(config.exchangeCount)

        // 1. Создать базовые цены для каждого токена на каждой бирже
        tokens.forEach { token ->
            val basePrice = config.basePrice * (0.5 + random.nextDouble())
            exchanges.forEach { exchange ->
                val variance = 1.0 + (random.nextDouble() - 0.5) * config.maxSpreadPercent / 100
                val finalPrice = basePrice * variance

                val price = CexPrice(
                    tokenId = token,
                    exchangeId = exchange,
                    priceRaw = CexPriceRaw(BigDecimal.parseString(finalPrice.toString())),
                    timeStamp = Timestamp.now()
                )
                prices.add(price)
            }
        }

        // 2. Гарантированно добавить N арбитражных возможностей
        repeat(config.guaranteedArbitrageCount) { i ->
            val token = tokens[i % tokens.size]
            val buyExchange = exchanges[random.nextInt(exchanges.size)]
            val sellExchange = exchanges.filter { it != buyExchange }.random(random)

            // Создать пару с гарантированным спредом > 0.1% (0.5-2.5%)
            val buyPrice = 100.0 + i
            val spreadPercent = 0.5 + random.nextDouble() * 2.0  // 0.5-2.5%
            val sellPrice = buyPrice * (1.0 + spreadPercent / 100.0)

            prices.add(
                CexPrice(
                    tokenId = token,
                    exchangeId = buyExchange,
                    priceRaw = CexPriceRaw(BigDecimal.parseString(buyPrice.toString())),
                    timeStamp = Timestamp.now()
                )
            )
            prices.add(
                CexPrice(
                    tokenId = token,
                    exchangeId = sellExchange,
                    priceRaw = CexPriceRaw(BigDecimal.parseString(sellPrice.toString())),
                    timeStamp = Timestamp.now()
                )
            )
        }

        return prices
    }

    private fun generateTokens(count: Int): List<CexTokenId> =
        listOf("BTC", "ETH", "BNB", "SOL", "ADA", "XRP", "DOT", "MATIC", "AVAX", "LINK")
            .take(count)
            .map { CexTokenId(it) }

    private fun generateExchanges(count: Int): List<CexExchangeId> =
        listOf("BINANCE", "OKX", "BYBIT", "KRAKEN", "COINBASE")
            .take(count)
            .map { CexExchangeId(it) }
}
```

**Data Characteristics**:
- **Default config**: 10 токенов × 5 бирж = 50 базовых цен + 20 (10×2) для гарантированных арбитражей = **70 записей**
- **Large dataset for performance**: 100 токенов × 10 бирж = 1000 записей
- **Spread distribution**: 50% записей с малым спредом (< 0.1%), 50% с прибыльным (> 0.1%)
- **Deterministic**: Фиксированный seed обеспечивает воспроизводимость

**Alternatives Considered**:
- **Hardcoded JSON fixtures**: Неудобно поддерживать, нет гибкости
- **Random без seed**: Невоспроизводимые тесты, сложнее отладка
- **Реальные данные с бирж**: Зависимость от внешних API, нестабильность тестов

---

### 4. Kotlin CoR Integration Pattern

**Decision**: Добавить новую цепочку workers для Command.RECALCULATE

**Rationale**:
Проект уже использует Kotlin CoR (Chain of Responsibility) для обработки команд. Необходимо интегрироваться в существующую архитектуру:

**Current Architecture** (из `BusinessLogicProcessorImpl.kt`):
```kotlin
commandProcessor(title = "Обработка события recalculate", command = Command.RECALCULATE) {
    workModProcessor(title = "Обработка в режиме стабов", workMode = WorkMode.STUB) {
        recalculateSuccessStubWorker(title = "Обработка стаба SUCCESS")
        noStubCaseWorker(title = "...")
    }
}
```

**Extended Architecture**:
```kotlin
commandProcessor(title = "Обработка события recalculate", command = Command.RECALCULATE) {
    workModProcessor(title = "Обработка в режиме стабов", workMode = WorkMode.STUB) {
        recalculateSuccessStubWorker(title = "Обработка стаба SUCCESS")
        noStubCaseWorker(title = "...")
    }

    // НОВОЕ: Production mode processing
    workModProcessor(title = "Обработка в production режиме", workMode = WorkMode.PROD) {
        // Phase 1: Load prices
        loadCexPricesWorker(title = "Загрузка цен из репозитория")

        // Phase 2: Find opportunities
        findArbitrageOpportunitiesWorker(title = "Поиск арбитражных возможностей")

        // Phase 3: Save results
        saveOpportunitiesWorker(title = "Сохранение найденных возможностей")

        // Phase 4: Prepare response
        prepareRecalculateResponseWorker(title = "Формирование ответа")
    }
}
```

**Worker Responsibilities**:
1. **LoadCexPricesWorker**:
   - Dependency inject CexPriceRepository
   - Вызов `repository.findAll()`
   - Сохранение результата во временную переменную Context (например, `loadedPrices`)

2. **FindArbitrageOpportunitiesWorker**:
   - Dependency inject ArbitrageFinder service
   - Вызов `finder.findOpportunities(ctx.loadedPrices)`
   - Сохранение результата в `ctx.foundOpportunities`

3. **SaveOpportunitiesWorker**:
   - Dependency inject ArbitrageOpportunityRepository
   - Логика обновления: mark old as ended, save new
   - Вызов `repository.saveAll(ctx.foundOpportunities)`

4. **PrepareRecalculateResponseWorker**:
   - Формирование `RecalculateResult` с метриками
   - Установка `ctx.recalculateResponse`

**Alternatives Considered**:
- **Single monolithic worker**: Нарушает Single Responsibility, сложнее тестировать
- **Service layer without CoR**: Нарушает существующую архитектуру проекта
- **Event-driven architecture**: Overkill для текущих требований

---

## Summary

### Key Decisions

| Aspect | Decision | Rationale |
|--------|----------|-----------|
| Algorithm | Группировка по токенам + min/max | O(n) complexity, простота, производительность |
| Repository | Interface в common, Map-based impl | Dependency Inversion, простая замена реализации |
| Mock Data | Программная генерация с seed | Детерминированность, гибкость, контроль |
| Integration | Kotlin CoR workers chain | Соответствие существующей архитектуре |

### Performance Expectations

- **Small dataset (100 records)**: < 5ms
- **Medium dataset (1000 records)**: < 50ms
- **Large dataset (10000 records)**: < 500ms

### Dependencies

**New**:
- Нет новых внешних зависимостей
- Используются существующие: Kotlin CoR, BigNum, Coroutines

**Modified**:
- `arbitrage-scanner-common`: добавление интерфейсов репозиториев
- `arbitrage-scanner-business-logic`: новые workers
- Новый модуль: `arbitrage-scanner-repository-inmemory`

### Risks & Mitigations

| Risk | Mitigation |
|------|-----------|
| Thread safety issues в ConcurrentHashMap | Добавить Mutex для критичных операций если потребуется |
| Memory leak при большом количестве данных | Периодическая очистка старых возможностей (будущая оптимизация) |
| Недостаточная производительность | Профилирование, оптимизация hot paths, рассмотреть parallel processing |

## Next Steps

Phase 1: Design & Contracts
- Создать data-model.md с детальным описанием интерфейсов
- Создать contracts/ с Kotlin интерфейсами репозиториев
- Создать quickstart.md с инструкциями по запуску и тестированию
