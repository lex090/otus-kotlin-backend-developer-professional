# Data Model: Recalculate Arbitrage Opportunities

**Feature**: 001-recalculate-arbitrage
**Date**: 2025-10-26

## Overview

Описание моделей данных, интерфейсов репозиториев и их взаимодействия для функциональности пересчёта арбитражных возможностей.

## Existing Entities

### CexPrice

**Location**: `arbitrage-scanner-common/src/commonMain/kotlin/com/arbitrage/scanner/models/CexPrice.kt`

**Purpose**: Представляет ценовые данные с CEX биржи в конкретный момент времени

```kotlin
data class CexPrice(
    val tokenId: CexTokenId = CexTokenId.DEFAULT,
    val exchangeId: CexExchangeId = CexExchangeId.DEFAULT,
    val priceRaw: CexPriceRaw = CexPriceRaw.DEFAULT,
    val timeStamp: Timestamp = Timestamp.DEFAULT,
)
```

**Fields**:
- `tokenId`: Идентификатор криптовалютного токена (например, "BTC", "ETH")
- `exchangeId`: Идентификатор биржи (например, "BINANCE", "OKX")
- `priceRaw`: Цена токена в виде BigDecimal для точных вычислений
- `timeStamp`: Временная метка получения цены

**Validation Rules**:
- `tokenId` не должен быть пустым
- `exchangeId` не должен быть пустым
- `priceRaw.value` (BigDecimal) должен быть положительным числом (> BigDecimal.ZERO)
- `priceRaw` не должен быть DEFAULT
- `timeStamp` должен быть валидной временной меткой

**Type Safety**:
`CexPriceRaw` является inline value class, обёртывающим BigDecimal для:
- Избежания ошибок округления в финансовых вычислениях
- Типобезопасности (нельзя случайно передать обычный Double)
- Zero-cost abstraction (no runtime overhead благодаря inline)

---

### CexToCexArbitrageOpportunity

**Location**: `arbitrage-scanner-common/src/commonMain/kotlin/com/arbitrage/scanner/models/CexToCexArbitrageOpportunity.kt`

**Purpose**: Представляет найденную арбитражную возможность между двумя CEX биржами

```kotlin
data class CexToCexArbitrageOpportunity(
    val id: ArbitrageOpportunityId = ArbitrageOpportunityId.DEFAULT,
    val cexTokenId: CexTokenId = CexTokenId.DEFAULT,
    val buyCexExchangeId: CexExchangeId = CexExchangeId.DEFAULT,
    val buyCexPriceRaw: CexPriceRaw = CexPriceRaw.DEFAULT,
    val sellCexExchangeId: CexExchangeId = CexExchangeId.DEFAULT,
    val sellCexPriceRaw: CexPriceRaw = CexPriceRaw.DEFAULT,
    val spread: ArbitrageOpportunitySpread = ArbitrageOpportunitySpread.DEFAULT,
    val startTimestamp: Timestamp = Timestamp.DEFAULT,
    val endTimestamp: Timestamp? = null,
)
```

**Fields**:
- `id`: Уникальный идентификатор возможности
- `cexTokenId`: Токен для арбитража
- `buyCexExchangeId`: Биржа для покупки (где цена ниже)
- `buyCexPriceRaw`: Цена покупки
- `sellCexExchangeId`: Биржа для продажи (где цена выше)
- `sellCexPriceRaw`: Цена продажи
- `spread`: Процент спреда (прибыли)
- `startTimestamp`: Время обнаружения возможности
- `endTimestamp`: Время завершения возможности (null если активна)

**State Transitions**:
```
[Created] ---> [Active] ---> [Ended]
             (endTimestamp == null)  (endTimestamp != null)
```

**Validation Rules**:
- `buyCexExchangeId` ≠ `sellCexExchangeId` (разные биржи)
- `buyCexPriceRaw.value` < `sellCexPriceRaw.value` (положительный спред, сравнение BigDecimal)
- `spread.value` >= 0.1% (минимальный порог прибыльности)
- `startTimestamp` < `endTimestamp` (если endTimestamp не null)
- Обе цены не должны быть DEFAULT

**Type Safety**:
- `CexPriceRaw` обеспечивает точные вычисления через BigDecimal
- `ArbitrageOpportunitySpread` является inline value class для типобезопасности процентных значений

---

### RecalculateResult

**Location**: `arbitrage-scanner-common/src/commonMain/kotlin/com/arbitrage/scanner/models/RecalculateResult.kt`

**Purpose**: Результат выполнения операции пересчёта арбитражных возможностей

```kotlin
data class RecalculateResult(
    val opportunitiesCount: Int = 0,
    val processingTimeMs: Long = 0L,
)
```

**Fields**:
- `opportunitiesCount`: Количество найденных арбитражных возможностей
- `processingTimeMs`: Время обработки в миллисекундах

---

## New Interfaces

### CexPriceRepository

**Location**: `arbitrage-scanner-common/src/commonMain/kotlin/com/arbitrage/scanner/repository/CexPriceRepository.kt`

**Purpose**: Интерфейс для доступа к ценовым данным CEX бирж

```kotlin
package com.arbitrage.scanner.repository

import com.arbitrage.scanner.models.CexPrice
import com.arbitrage.scanner.models.CexTokenId
import com.arbitrage.scanner.models.CexExchangeId

/**
 * Репозиторий для управления ценовыми данными с CEX бирж
 */
interface CexPriceRepository {
    /**
     * Получить все доступные цены
     * @return список всех ценовых записей
     */
    suspend fun findAll(): List<CexPrice>

    /**
     * Найти цены по токену
     * @param tokenId идентификатор токена
     * @return список цен для указанного токена на разных биржах
     */
    suspend fun findByToken(tokenId: CexTokenId): List<CexPrice>

    /**
     * Найти цены по бирже
     * @param exchangeId идентификатор биржи
     * @return список цен различных токенов на указанной бирже
     */
    suspend fun findByExchange(exchangeId: CexExchangeId): List<CexPrice>

    /**
     * Сохранить одну ценовую запись
     * @param price ценовая запись для сохранения
     */
    suspend fun save(price: CexPrice)

    /**
     * Сохранить множество ценовых записей
     * @param prices список ценовых записей
     */
    suspend fun saveAll(prices: List<CexPrice>)

    /**
     * Очистить все ценовые данные
     */
    suspend fun clear()
}
```

**Usage Example**:
```kotlin
val repository: CexPriceRepository = InMemoryCexPriceRepository()

// Загрузить моковые данные
val mockPrices = MockCexPriceGenerator().generate()
repository.saveAll(mockPrices)

// Получить все цены для алгоритма
val allPrices = repository.findAll()

// Найти цены конкретного токена
val btcPrices = repository.findByToken(CexTokenId("BTC"))
```

---

### ArbitrageOpportunityRepository

**Location**: `arbitrage-scanner-common/src/commonMain/kotlin/com/arbitrage/scanner/repository/ArbitrageOpportunityRepository.kt`

**Purpose**: Интерфейс для управления найденными арбитражными возможностями

```kotlin
package com.arbitrage.scanner.repository

import com.arbitrage.scanner.models.CexToCexArbitrageOpportunity
import com.arbitrage.scanner.models.ArbitrageOpportunityId
import com.arbitrage.scanner.base.Timestamp

/**
 * Репозиторий для управления арбитражными возможностями
 */
interface ArbitrageOpportunityRepository {
    /**
     * Сохранить одну возможность
     * @param opportunity возможность для сохранения
     * @return сгенерированный или существующий ID
     */
    suspend fun save(opportunity: CexToCexArbitrageOpportunity): ArbitrageOpportunityId

    /**
     * Сохранить множество возможностей
     * @param opportunities список возможностей
     * @return список сгенерированных ID
     */
    suspend fun saveAll(opportunities: List<CexToCexArbitrageOpportunity>): List<ArbitrageOpportunityId>

    /**
     * Найти возможность по ID
     * @param id идентификатор возможности
     * @return найденная возможность или null
     */
    suspend fun findById(id: ArbitrageOpportunityId): CexToCexArbitrageOpportunity?

    /**
     * Получить все возможности
     * @return список всех сохранённых возможностей
     */
    suspend fun findAll(): List<CexToCexArbitrageOpportunity>

    /**
     * Найти только активные возможности
     * @return список возможностей с endTimestamp == null
     */
    suspend fun findActive(): List<CexToCexArbitrageOpportunity>

    /**
     * Пометить возможность как завершённую
     * @param id идентификатор возможности
     * @param endTimestamp время завершения
     */
    suspend fun markAsEnded(id: ArbitrageOpportunityId, endTimestamp: Timestamp)

    /**
     * Очистить все данные
     */
    suspend fun clear()
}
```

**Usage Example**:
```kotlin
val repository: ArbitrageOpportunityRepository = InMemoryArbitrageOpportunityRepository()

// Сохранить найденные возможности
val ids = repository.saveAll(foundOpportunities)

// Найти активные
val active = repository.findActive()

// Завершить старые возможности
active.forEach { old ->
    repository.markAsEnded(old.id, Timestamp.now())
}
```

---

## Implementation Model

### InMemoryCexPriceRepository

**Location**: `arbitrage-scanner-repository-inmemory/src/commonMain/kotlin/com/arbitrage/scanner/repository/inmemory/InMemoryCexPriceRepository.kt`

**Internal Structure**:
```kotlin
class InMemoryCexPriceRepository : CexPriceRepository {
    // Key: "${tokenId.value}:${exchangeId.value}"
    private val prices = ConcurrentHashMap<String, CexPrice>()

    private fun CexPrice.toKey(): String =
        "${tokenId.value}:${exchangeId.value}"

    override suspend fun findAll(): List<CexPrice> =
        prices.values.toList()

    override suspend fun findByToken(tokenId: CexTokenId): List<CexPrice> =
        prices.values.filter { it.tokenId == tokenId }

    // ... остальные методы
}
```

**Characteristics**:
- **Thread-safe**: ConcurrentHashMap
- **Key structure**: `"<tokenId>:<exchangeId>"` для быстрого доступа
- **Memory**: O(n) где n = количество уникальных пар токен-биржа

---

### InMemoryArbitrageOpportunityRepository

**Location**: `arbitrage-scanner-repository-inmemory/src/commonMain/kotlin/com/arbitrage/scanner/repository/inmemory/InMemoryArbitrageOpportunityRepository.kt`

**Internal Structure**:
```kotlin
class InMemoryArbitrageOpportunityRepository : ArbitrageOpportunityRepository {
    private val opportunities = ConcurrentHashMap<ArbitrageOpportunityId, CexToCexArbitrageOpportunity>()
    private val idGenerator = AtomicLong(0)

    override suspend fun save(opportunity: CexToCexArbitrageOpportunity): ArbitrageOpportunityId {
        val id = if (opportunity.id.isDefault()) {
            generateId()
        } else {
            opportunity.id
        }
        val withId = opportunity.copy(id = id)
        opportunities[id] = withId
        return id
    }

    private fun generateId(): ArbitrageOpportunityId =
        ArbitrageOpportunityId("arb-${idGenerator.incrementAndGet()}")

    // ... остальные методы
}
```

**Characteristics**:
- **Thread-safe**: ConcurrentHashMap + AtomicLong
- **Auto ID generation**: `arb-1`, `arb-2`, ...
- **Memory**: O(m) где m = количество сохранённых возможностей

---

## Data Flow

### Recalculate Flow

```
┌─────────────────────────────────────────────────────────────┐
│ 1. Trigger: Command.RECALCULATE                             │
└────────────────────┬────────────────────────────────────────┘
                     │
                     v
┌─────────────────────────────────────────────────────────────┐
│ 2. LoadCexPricesWorker                                      │
│    - CexPriceRepository.findAll()                           │
│    - Store in Context.loadedPrices                          │
└────────────────────┬────────────────────────────────────────┘
                     │
                     v
┌─────────────────────────────────────────────────────────────┐
│ 3. FindArbitrageOpportunitiesWorker                         │
│    - ArbitrageFinder.findOpportunities(loadedPrices)        │
│    - Store in Context.foundOpportunities                    │
└────────────────────┬────────────────────────────────────────┘
                     │
                     v
┌─────────────────────────────────────────────────────────────┐
│ 4. SaveOpportunitiesWorker                                  │
│    - Mark old as ended: repository.markAsEnded(...)         │
│    - Save new: repository.saveAll(foundOpportunities)       │
└────────────────────┬────────────────────────────────────────┘
                     │
                     v
┌─────────────────────────────────────────────────────────────┐
│ 5. PrepareRecalculateResponseWorker                         │
│    - Build RecalculateResult                                │
│    - Set Context.recalculateResponse                        │
└────────────────────┬────────────────────────────────────────┘
                     │
                     v
┌─────────────────────────────────────────────────────────────┐
│ 6. Result: RecalculateResult                                │
│    - opportunitiesCount: Int                                │
│    - processingTimeMs: Long                                 │
└─────────────────────────────────────────────────────────────┘
```

---

## Context Extensions

**Location**: `arbitrage-scanner-common/src/commonMain/kotlin/com/arbitrage/scanner/context/Context.kt`

**New Fields** (добавить в существующий Context):

```kotlin
data class Context(
    // ... существующие поля ...

    // START RECALCULATE (расширение существующей секции)
    var recalculateResponse: RecalculateResult = RecalculateResult.DEFAULT,

    // НОВОЕ: Временные данные для цепочки обработки
    var loadedPrices: List<CexPrice> = emptyList(),
    var foundOpportunities: List<CexToCexArbitrageOpportunity> = emptyList(),
    // END RECALCULATE
)
```

---

## Mock Data Structure

### MockCexPriceGenerator

**Location**: `arbitrage-scanner-repository-inmemory/src/commonMain/kotlin/com/arbitrage/scanner/repository/inmemory/mocks/MockCexPriceGenerator.kt`

**Configuration**:
```kotlin
data class MockDataConfig(
    val tokenCount: Int = 10,
    val exchangeCount: Int = 5,
    val basePriceUsd: Double = 100.0,
    val maxSpreadPercent: Double = 5.0,
    val guaranteedArbitrageCount: Int = 10,
    val seed: Long = 42 // для детерминированности
)
```

**Generated Data Characteristics**:
- **Default**: 70 ценовых записей (50 базовых + 20 для гарантированных арбитражей)
- **Tokens**: BTC, ETH, BNB, SOL, ADA, XRP, DOT, MATIC, AVAX, LINK
- **Exchanges**: BINANCE, OKX, BYBIT, KRAKEN, COINBASE
- **Spreads**: 50% < 0.1% (неприбыльные), 50% >= 0.1% (прибыльные)
- **Expected arbitrage opportunities**: минимум 10

---

## Performance Characteristics

### Memory Usage

| Component | Structure | Size (estimated) |
|-----------|-----------|------------------|
| CexPriceRepository | ConcurrentHashMap | ~100 KB для 1000 записей |
| ArbitrageOpportunityRepository | ConcurrentHashMap | ~50 KB для 500 возможностей |
| Context temporary data | Lists | ~100 KB во время обработки |
| **Total** | | **~250 KB** |

### Query Performance

| Operation | Complexity | Expected Time (1000 records) |
|-----------|------------|------------------------------|
| findAll() | O(n) | < 1ms |
| findByToken() | O(n) | < 1ms |
| findByExchange() | O(n) | < 1ms |
| save() | O(1) | < 0.1ms |
| findActive() | O(m) | < 1ms |

---

## Validation Rules Summary

### CexPrice Validation
- ✅ tokenId not empty
- ✅ exchangeId not empty
- ✅ priceRaw.value > BigDecimal.ZERO
- ✅ priceRaw not DEFAULT
- ✅ timestamp valid

### CexToCexArbitrageOpportunity Validation
- ✅ buyCexExchangeId ≠ sellCexExchangeId
- ✅ buyCexPriceRaw.value < sellCexPriceRaw.value (BigDecimal comparison)
- ✅ spread.value >= 0.1%
- ✅ startTimestamp < endTimestamp (if not null)
- ✅ id unique in repository
- ✅ Both prices not DEFAULT

### RecalculateResult Validation
- ✅ opportunitiesCount >= 0
- ✅ processingTimeMs >= 0

---

## Testing Strategy

### Unit Tests
- Repository CRUD operations
- Mock data generation
- Entity validation

### Integration Tests
- Full recalculate flow
- Repository integration
- Data consistency

### Performance Tests
- 1000 records < 1 second
- Memory usage < 1 MB
- Thread safety under concurrent access
