# Quickstart: Recalculate Arbitrage Opportunities

**Feature**: 001-recalculate-arbitrage
**Date**: 2025-10-26

## Overview

Краткое руководство по запуску, тестированию и использованию функциональности пересчёта арбитражных возможностей.

## Prerequisites

- Kotlin 2.2.0+
- Java 21+
- Gradle 8.x

## Build

```bash
# Собрать все модули
./gradlew :arbitrage-scanner:buildAll

# Собрать только затронутые модули
./gradlew :arbitrage-scanner:arbitrage-scanner-common:build
./gradlew :arbitrage-scanner:arbitrage-scanner-business-logic:build
./gradlew :arbitrage-scanner:arbitrage-scanner-repository-inmemory:build
```

## Run Tests

### Unit Tests

```bash
# Запустить все тесты
./gradlew :arbitrage-scanner:testAll

# Тесты бизнес-логики (включая recalculate)
./gradlew :arbitrage-scanner:arbitrage-scanner-business-logic:test

# Тесты репозиториев
./gradlew :arbitrage-scanner:arbitrage-scanner-repository-inmemory:test

# Мультиплатформенные тесты
./gradlew :arbitrage-scanner:arbitrage-scanner-business-logic:jvmTest
./gradlew :arbitrage-scanner:arbitrage-scanner-business-logic:linuxX64Test
./gradlew :arbitrage-scanner:arbitrage-scanner-business-logic:macosArm64Test
```

### Integration Tests

```bash
# Тест полного цикла RECALCULATE
./gradlew :arbitrage-scanner:arbitrage-scanner-business-logic:commonTest \
    --tests "com.arbitrage.scanner.BusinessLogicProcessorImplRecalculateTest"
```

## Quick Example

### 1. Basic Usage

```kotlin
import com.arbitrage.scanner.*
import com.arbitrage.scanner.base.*
import com.arbitrage.scanner.context.Context
import com.arbitrage.scanner.repository.inmemory.*

// 1. Setup repositories
val priceRepo = InMemoryCexPriceRepository()
val opportunityRepo = InMemoryArbitrageOpportunityRepository()

// 2. Load mock data
val mockPrices = MockCexPriceGenerator().generate()
priceRepo.saveAll(mockPrices)

// 3. Create business logic processor with dependencies
val deps = BusinessLogicProcessorImplDeps(
    cexPriceRepository = priceRepo,
    arbitrageOpportunityRepository = opportunityRepo,
    // ... другие dependencies
)
val processor = BusinessLogicProcessorImpl(deps)

// 4. Execute recalculate
val ctx = Context(
    command = Command.RECALCULATE,
    workMode = WorkMode.PROD,
    requestId = RequestId("req-123"),
    startTimestamp = Timestamp.now()
)

processor.exec(ctx)

// 5. Check result
println("Found ${ctx.recalculateResponse.opportunitiesCount} opportunities")
println("Processing time: ${ctx.recalculateResponse.processingTimeMs}ms")

// 6. Verify active opportunities
val activeOpportunities = opportunityRepo.findActive()
activeOpportunities.forEach { opp ->
    // CexPriceRaw.value содержит BigDecimal, конвертируем для отображения
    val buyPrice = opp.buyCexPriceRaw.value.doubleValue(exactRequired = false)
    val sellPrice = opp.sellCexPriceRaw.value.doubleValue(exactRequired = false)

    println("${opp.cexTokenId.value}: Buy on ${opp.buyCexExchangeId.value} " +
            "at $$buyPrice, " +
            "Sell on ${opp.sellCexExchangeId.value} " +
            "at $$sellPrice, " +
            "Spread: ${opp.spread.value}%")
}
```

### 2. Stub Mode (для тестирования без реализации)

```kotlin
val ctx = Context(
    command = Command.RECALCULATE,
    workMode = WorkMode.STUB,  // <-- Stub mode
    stubCase = StubCase.SUCCESS,
    requestId = RequestId("req-stub-1")
)

processor.exec(ctx)

// Получить стабовый результат
println("Stub result: ${ctx.recalculateResponse.opportunitiesCount} opportunities")
```

## Configuration

### Mock Data Configuration

Настройка генерации моковых данных:

```kotlin
val config = MockDataConfig(
    tokenCount = 20,           // Количество токенов
    exchangeCount = 10,        // Количество бирж
    basePriceUsd = 50000.0,   // Базовая цена в USD
    maxSpreadPercent = 10.0,  // Максимальный разброс цен
    guaranteedArbitrageCount = 15,  // Гарантированных арбитражей
    seed = 42                 // Seed для детерминированности
)

val mockPrices = MockCexPriceGenerator(config.seed).generate(config)
```

### Minimum Spread Threshold

Настройка минимального порога прибыльности через Koin DI:

```kotlin
val koinModule = module {
    single<ArbitrageFinder> {
        ArbitrageFinderImpl(minSpreadPercent = 0.5) // 0.5% вместо 0.1%
    }
}
```

## Performance Testing

### Benchmark с большим набором данных

```kotlin
val largeConfig = MockDataConfig(
    tokenCount = 100,
    exchangeCount = 10,
    basePriceUsd = 100.0,
    maxSpreadPercent = 5.0,
    guaranteedArbitrageCount = 50,
    seed = 42
)

val largeMockPrices = MockCexPriceGenerator(42).generate(largeConfig)
println("Generated ${largeMockPrices.size} price records")

priceRepo.clear()
priceRepo.saveAll(largeMockPrices)

val startTime = System.currentTimeMillis()
processor.exec(ctx)
val endTime = System.currentTimeMillis()

println("Processing time: ${endTime - startTime}ms")
println("Performance: ${largeMockPrices.size / (endTime - startTime).toDouble() * 1000} records/sec")

// Expected: < 1000ms for 1000 records
assert(ctx.recalculateResponse.processingTimeMs < 1000) {
    "Performance target not met!"
}
```

## Common Tasks

### Clear All Data

```kotlin
priceRepo.clear()
opportunityRepo.clear()
```

### Query Active Opportunities

```kotlin
val active = opportunityRepo.findActive()
active.sortedByDescending { it.spread.value }.take(10).forEach { opp ->
    println("Top opportunity: ${opp.cexTokenId.value} with ${opp.spread.value}% spread")
}
```

### Query Opportunities by Token

```kotlin
val btcPrices = priceRepo.findByToken(CexTokenId("BTC"))
println("BTC prices on ${btcPrices.size} exchanges")
```

### Mark Opportunities as Ended

```kotlin
val oldOpportunities = opportunityRepo.findActive()
oldOpportunities.forEach { opp ->
    opportunityRepo.markAsEnded(opp.id, Timestamp.now())
}
```

## Debugging

### Enable Verbose Logging

```kotlin
// В logback.xml или через код
val logger = LoggerFactory.getLogger("com.arbitrage.scanner")
logger.level = Level.DEBUG
```

### Check Processing State

```kotlin
// После выполнения processor.exec(ctx)
println("State: ${ctx.state}")
println("Errors: ${ctx.errors}")
println("Loaded prices: ${ctx.loadedPrices.size}")
println("Found opportunities: ${ctx.foundOpportunities.size}")
```

### Validate Data Integrity

```kotlin
val all = opportunityRepo.findAll()
val active = opportunityRepo.findActive()
val ended = all.filter { it.endTimestamp != null }

println("Total opportunities: ${all.size}")
println("Active: ${active.size}")
println("Ended: ${ended.size}")

// Проверка консистентности
assert(active.size + ended.size == all.size) { "Data integrity issue!" }
```

## Troubleshooting

### Issue: No opportunities found

**Причина**: Недостаточный спред в моковых данных

**Решение**:
```kotlin
val config = MockDataConfig(
    maxSpreadPercent = 10.0,  // Увеличить разброс
    guaranteedArbitrageCount = 20  // Больше гарантированных арбитражей
)
```

### Issue: Performance < 1 sec for 1000 records

**Причина**: Неоптимальный алгоритм или большие аллокации

**Решение**:
1. Проверить, что используется группировка по токенам (O(n))
2. Использовать sequences вместо lists где возможно
3. Профилировать с помощью VisualVM или JProfiler

### Issue: ConcurrentModificationException

**Причина**: Параллельные изменения коллекций

**Решение**:
```kotlin
// Используйте thread-safe репозитории
val repo = InMemoryArbitrageOpportunityRepository() // уже thread-safe с ConcurrentHashMap
```

### Issue: Memory leak при большом количестве тестов

**Причина**: Не очищаются репозитории между тестами

**Решение**:
```kotlin
@AfterTest
fun cleanup() {
    priceRepo.clear()
    opportunityRepo.clear()
}
```

## Next Steps

После успешного запуска:

1. **Запустите полный тест-сюит**:
   ```bash
   ./gradlew :arbitrage-scanner:checkAll
   ```

2. **Интегрируйте с Ktor приложением** (будущая задача):
   - Добавить REST endpoint для trigger recalculate
   - Настроить периодический пересчёт (scheduler)

3. **Добавьте мониторинг**:
   - Метрики производительности
   - Алерты на низкую производительность
   - Dashboard с активными возможностями

## References

- [Specification](./spec.md)
- [Implementation Plan](./plan.md)
- [Research](./research.md)
- [Data Model](./data-model.md)
- [Constitution](../.specify/memory/constitution.md)
