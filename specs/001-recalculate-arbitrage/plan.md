# Implementation Plan: Recalculate Arbitrage Opportunities

**Branch**: `001-recalculate-arbitrage` | **Date**: 2025-10-26 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-recalculate-arbitrage/spec.md`

## Summary

Реализация обработки события RECALCULATE в бизнес-логике для автоматического пересчёта арбитражных возможностей. Система будет:
- Загружать все доступные ценовые данные CexPrice из in-memory репозитория с моковыми данными
- Применять оптимизированный алгоритм поиска арбитражных ситуаций (группировка по токенам для избежания O(n²))
- Сохранять найденные возможности в in-memory репозиторий арбитражных возможностей
- Возвращать результат с количеством найденных возможностей и временем обработки

Технический подход основан на существующей архитектуре с использованием Kotlin Coroutines для асинхронности, Kotlin CoR для цепочки обработки и простых in-memory структур (Map/List) для хранения данных.

## Technical Context

**Language/Version**: Kotlin 2.2.0 (language version 2.2), Java 21 (toolchain и compiler)
**Primary Dependencies**:
- Kotlin Coroutines 1.10.1 (асинхронная обработка)
- Kotlin CoR 0.6.0 (chain of responsibility для бизнес-логики)
- BigNum 0.3.10 (точные вычисления цен и спредов)
- Kotlinx Serialization 1.9.0 (сериализация моделей)
- Kotlin Test с JUnit Platform (тестирование)

**Storage**: In-memory репозитории (Map/MutableList) для CexPrice и ArbitrageOpportunity, без персистентного хранилища на данном этапе

**Testing**:
- Kotlin Test для unit тестов
- Integration тесты в `arbitrage-scanner-business-logic/src/commonTest`
- Мультиплатформенные тесты: jvmTest, linuxX64Test, macosArm64Test

**Target Platform**: Мультиплатформенные модули (JVM, Linux x64, macOS ARM64/x64) для business-logic и common

**Project Type**: Мультимодульный Kotlin Multiplatform проект с composite builds

**Performance Goals**:
- Обработка 1000 ценовых записей < 1 секунда
- Алгоритм поиска оптимизирован (группировка по токенам, не O(n²))
- Минимальные аллокации памяти при пересчёте

**Constraints**:
- Используется существующая структура Context для передачи данных
- Command.RECALCULATE уже определён
- Модели CexPrice, CexToCexArbitrageOpportunity, RecalculateResult уже существуют
- Минимальный порог спреда: 0.1%
- In-memory хранилище без внешних зависимостей

**Scale/Scope**:
- Моковый набор: минимум 100 ценовых записей
- Минимум 10 различных токенов, 5 бирж
- Ожидается найти минимум 10 арбитражных возможностей на тестовом наборе

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Principle I: Модульная Архитектура ✅ PASS

**Соответствие**:
- Функциональность будет реализована в существующем модуле `arbitrage-scanner-business-logic` (четкая граница ответственности)
- Новые репозитории будут добавлены как отдельные интерфейсы в `arbitrage-scanner-common`
- Реализации репозиториев в `arbitrage-scanner-business-logic` или новом модуле `arbitrage-scanner-repository-inmemory`
- Модули используют BuildPluginMultiplatform для стандартизации
- Зависимости односторонние: business-logic → common ← repository

**Проверка**: Нет циклических зависимостей, явные API границы через интерфейсы

### Principle II: Тестирование на Всех Уровнях ✅ PASS

**Соответствие**:
- Unit тесты для алгоритма поиска арбитража
- Unit тесты для репозиториев
- Integration тесты для полного цикла RECALCULATE → find → save
- Contract тесты для проверки Context заполнения
- Тесты на всех платформах: jvmTest, linuxX64Test, macosArm64Test

**Проверка**: Обязательный запуск `./gradlew check` перед коммитом

### Principle III: Производительность и Масштабируемость ✅ PASS

**Соответствие**:
- Используются Kotlin Coroutines для асинхронной обработки
- Алгоритм оптимизирован: группировка цен по токенам (O(n log n) вместо O(n²))
- Минимизация аллокаций: использование sequences где возможно
- Целевая метрика: < 1 сек для 1000 записей

**Проверка**: Performance тесты с замером времени выполнения, JMH benchmarks (опционально)

### Principle IV: Наблюдаемость ✅ PASS

**Соответствие**:
- Логирование начала/конца пересчёта через `arbitrage-scanner-lib-logging`
- Логирование количества обработанных цен и найденных возможностей
- Метрики: время обработки в RecalculateResult
- Уровни логов: DEBUG для деталей, INFO для результатов

**Проверка**: Все критические операции логируются с requestId контекстом

### Principle V: API-First Подход ⚠️ PARTIALLY APPLICABLE

**Соответствие**:
- Внутренняя бизнес-логика, не внешний API endpoint
- Context уже определён как "внутренний контракт"
- Интерфейсы репозиториев документированы

**Примечание**: Принцип применим к внешним API. Для внутренней логики используются типобезопасные Kotlin интерфейсы.

### Principle VI: Управление Конфигурацией ✅ PASS

**Соответствие**:
- Минимальный порог спреда (0.1%) может быть конфигурируемым через Koin
- Размер моковых данных конфигурируется
- Нет секретов в коде

**Проверка**: Конфигурационные параметры через DI

### Principle VII: Простота и Ясность ✅ PASS

**Соответствие**:
- Простые in-memory структуры (Map, MutableList)
- Алгоритм понятен: группировка → сравнение → фильтрация → сортировка
- Идиоматичный Kotlin: data classes, extension functions, sequences
- YAGNI: нет избыточной абстракции, только необходимое

**Проверка**: Code review, документация сложных мест

## Project Structure

### Documentation (this feature)

```text
specs/001-recalculate-arbitrage/
├── plan.md              # This file
├── research.md          # Phase 0: Algorithm research, repository patterns
├── data-model.md        # Phase 1: Repository interfaces, mock data structure
├── quickstart.md        # Phase 1: How to run and test recalculation
├── contracts/           # Phase 1: Internal interfaces (repository contracts)
└── checklists/
    └── requirements.md  # Specification quality checklist
```

### Source Code (repository root)

```text
arbitrage-scanner/
├── arbitrage-scanner-common/
│   └── src/commonMain/kotlin/com/arbitrage/scanner/
│       ├── models/                    # Существующие модели (CexPrice, CexToCexArbitrageOpportunity, RecalculateResult)
│       └── repository/                # НОВОЕ: Интерфейсы репозиториев
│           ├── CexPriceRepository.kt
│           └── ArbitrageOpportunityRepository.kt
│
├── arbitrage-scanner-business-logic/
│   └── src/
│       ├── commonMain/kotlin/com/arbitrage/scanner/
│       │   ├── workers/
│       │   │   └── recalculate/       # НОВОЕ: Workers для RECALCULATE
│       │   │       ├── LoadCexPricesWorker.kt
│       │   │       ├── FindArbitrageOpportunitiesWorker.kt
│       │   │       └── SaveOpportunitiesWorker.kt
│       │   │
│       │   └── services/              # НОВОЕ: Бизнес-сервисы
│       │       └── ArbitrageFinder.kt # Алгоритм поиска арбитража
│       │
│       └── commonTest/kotlin/com/arbitrage/scanner/
│           ├── BusinessLogicProcessorImplRecalculateTest.kt  # Существующий тест - расширить
│           └── services/
│               └── ArbitrageFinderTest.kt  # НОВОЕ: Unit тесты алгоритма
│
├── arbitrage-scanner-repository-inmemory/  # НОВОЕ: Отдельный модуль для in-memory реализаций
│   ├── build.gradle.kts
│   └── src/
│       ├── commonMain/kotlin/com/arbitrage/scanner/repository/inmemory/
│       │   ├── InMemoryCexPriceRepository.kt
│       │   ├── InMemoryArbitrageOpportunityRepository.kt
│       │   └── mocks/
│       │       └── MockCexPriceGenerator.kt  # Генератор моковых данных
│       │
│       └── commonTest/kotlin/com/arbitrage/scanner/repository/inmemory/
│           ├── InMemoryCexPriceRepositoryTest.kt
│           └── InMemoryArbitrageOpportunityRepositoryTest.kt
│
└── arbitrage-scanner-stubs/
    └── src/commonMain/kotlin/com/arbitrage/scanner/
        └── ArbOpStubs.kt              # Обновить: добавить моковые CexPrice для стабов
```

**Structure Decision**:
Выбрана модульная структура с созданием нового модуля `arbitrage-scanner-repository-inmemory` для изоляции реализаций репозиториев. Это соответствует Principle I (Модульная Архитектура) и позволяет легко заменить in-memory реализации на персистентные в будущем без изменения бизнес-логики.

Интерфейсы репозиториев в `arbitrage-scanner-common` обеспечивают:
- Независимость бизнес-логики от конкретной реализации хранилища
- Возможность легкого тестирования через моки
- Соблюдение Dependency Inversion Principle

## Complexity Tracking

> **Нет нарушений Constitution** - все принципы соблюдены

Добавление нового модуля `arbitrage-scanner-repository-inmemory` обосновано:
- Изоляция ответственности (Principle I)
- Упрощение замены реализации хранилища
- Чистые зависимости: business-logic → common ← repository-inmemory

Альтернатива (реализация в business-logic) отклонена из-за:
- Смешивания ответственностей (бизнес-логика + persistence)
- Усложнения тестирования
- Нарушения Single Responsibility Principle
