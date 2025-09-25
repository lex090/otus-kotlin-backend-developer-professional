# План реализации асинхронного транспорта Kafka для Arbitrage Scanner

**Дата создания:** 25 сентября 2025  
**Версия:** 1.0  
**Цель:** Создать новый модуль `arbitrage-scanner-kafka` с аналогичной функциональностью как модуль `arbitrage-scanner-ktor`

## 1. Анализ текущего состояния проекта

### 1.1 Архитектура проекта
На основе изучения ARCHITECTURE.md и кодовой базы установлено:

- **Чистая архитектура** с разделением на слои (Presentation, Application, Domain, Infrastructure)
- **Мультиплатформенная архитектура** (JVM, Linux, macOS) с использованием Kotlin Multiplatform
- **Composite builds** структура с модулями:
  - `arbitrage-scanner-ktor/` - HTTP транспорт на Ktor
  - `arbitrage-scanner-api-v1/` - API модели (автогенерация OpenAPI)
  - `arbitrage-scanner-business-logic/` - бизнес-логика
  - `arbitrage-scanner-common/` - доменные модели
  - `arbitrage-scanner-libs/` - инфраструктурные библиотеки

### 1.2 Анализ модуля arbitrage-scanner-ktor

**Структура:**
```
arbitrage-scanner-ktor/
├── src/main/kotlin/com/arbitrage/scanner/
│   ├── Application.kt              # Точка входа приложения
│   ├── KoinConfiguration.kt        # DI конфигурация
│   ├── processors/
│   │   ├── ContextProcessor.kt     # Обработка контекста
│   │   └── RequestProcessor.kt     # Обработка HTTP запросов
│   └── routing/
│       ├── RoutingConfiguration.kt # Конфигурация маршрутов
│       └── v1/
│           ├── RoutingV1.kt        # Маршруты API v1
│           └── Routs.kt            # Endpoint handlers
```

**API Endpoints:**
- `POST /v1/arbitrage_opportunities/read` - чтение арбитражной возможности по ID
- `POST /v1/arbitrage_opportunities/search` - поиск арбитражных возможностей по фильтрам

**Модели данных:**
- **Requests:** `ArbitrageOpportunityReadRequest`, `ArbitrageOpportunitySearchRequest`
- **Responses:** `ArbitrageOpportunityReadResponse`, `ArbitrageOpportunitySearchResponse`
- **Base:** `IRequest`, `IResponse` с полиморфной JSON сериализацией

**Поток обработки запроса:**
1. HTTP Request → Ktor Router → RequestProcessor
2. Context.fromTransport() - преобразование из Transport в Domain модели
3. BusinessLogicProcessor.exec() - выполнение бизнес-логики
4. Context.toTransport() - преобразование из Domain в Transport модели
5. HTTP Response

### 1.3 Система маппинга Transport ↔ Domain
- **FromTransportMappers.kt:** Transport модели → Domain модели
- **ToTransportMappers.kt:** Domain модели → Transport модели
- Используется единый Context для передачи данных между слоями
- Полиморфная сериализация через sealed classes и discriminator поля

## 2. Цели и требования

### 2.1 Основные требования
- **Функциональная эквивалентность** с модулем arbitrage-scanner-ktor
- **Асинхронная обработка** через Kafka topics
- **JSON сериализация/десериализация** с идентичными моделями данных
- **Аналогичный запуск приложения** (gradle tasks)
- **Интеграция с существующей бизнес-логикой** без изменений

### 2.2 Дополнительные требования
- **Обработка ошибок** на уровне Kafka consumer/producer
- **Логирование** с использованием существующей системы ArbScanLoggerProvider
- **Мониторинг** производительности и метрик Kafka
- **Тестирование** с embedded Kafka для unit/integration тестов

## 3. Архитектурные решения

### 3.1 Паттерн реализации: Request-Reply через Kafka
```
┌─────────────┐    ┌───────────────┐    ┌──────────────┐    ┌─────────────┐
│   Client    │───►│ Request Topic │───►│ Kafka Module │───►│ Reply Topic │
│             │    │               │    │              │    │             │
└─────────────┘    └───────────────┘    └──────────────┘    └─────────────┘
                                               │
                                               ▼
                                    ┌──────────────────┐
                                    │ Business Logic   │
                                    │ Processor        │
                                    └──────────────────┘
```

### 3.2 Kafka Topics дизайн

**Request Topics:**
- `arbitrage-opportunities-read-requests` - запросы на чтение
- `arbitrage-opportunities-search-requests` - запросы на поиск

**Response Topics:**
- `arbitrage-opportunities-read-responses` - ответы на чтение
- `arbitrage-opportunities-search-responses` - ответы на поиск

**Message Headers:**
- `correlation-id` - для связи request и response
- `request-type` - тип запроса (read/search)
- `timestamp` - время создания сообщения

### 3.3 Структура Kafka модуля

```
arbitrage-scanner-kafka/
├── build.gradle.kts
├── src/main/kotlin/com/arbitrage/scanner/
│   ├── Application.kt                    # Kafka приложение
│   ├── KafkaConfiguration.kt             # Kafka и DI конфигурация
│   ├── config/
│   │   ├── KafkaProducerConfig.kt        # Конфигурация producer
│   │   ├── KafkaConsumerConfig.kt        # Конфигурация consumer
│   │   └── TopicsConfig.kt               # Конфигурация topics
│   ├── processors/
│   │   ├── KafkaMessageProcessor.kt      # Обработка Kafka сообщений
│   │   └── ContextProcessor.kt           # Переиспользование из Ktor модуля
│   ├── consumers/
│   │   ├── ArbitrageOpportunityReadConsumer.kt    # Consumer для read запросов
│   │   └── ArbitrageOpportunitySearchConsumer.kt  # Consumer для search запросов
│   ├── producers/
│   │   └── ResponseProducer.kt           # Producer для ответов
│   ├── serialization/
│   │   ├── KafkaJsonSerializer.kt        # JSON сериализатор для Kafka
│   │   └── KafkaJsonDeserializer.kt      # JSON десериализатор для Kafka
│   └── monitoring/
│       ├── KafkaHealthCheck.kt           # Health check для Kafka
│       └── KafkaMetrics.kt               # Метрики производительности
├── src/main/resources/
│   ├── application.yaml                  # Конфигурация приложения
│   └── kafka.properties                  # Kafka специфичная конфигурация
└── src/test/kotlin/
    ├── EmbeddedKafkaTest.kt              # Интеграционные тесты
    └── KafkaMessageProcessorTest.kt      # Unit тесты
```

## 4. Технологический стек

### 4.1 Основные зависимости

**Apache Kafka Client:**
- `org.apache.kafka:kafka-clients:3.7.0` - официальный Kafka клиент
- Поддержка Kotlin Multiplatform через JVM target

**Kotlinx Serialization:**
- `org.jetbrains.kotlinx:kotlinx-serialization-json` - уже используется в проекте
- Переиспользование существующих сериализаторов для IRequest/IResponse

**Coroutines:**
- `org.jetbrains.kotlinx:kotlinx-coroutines-core` - для асинхронной обработки
- `org.jetbrains.kotlinx:kotlinx-coroutines-jdk8` - интеграция с Java concurrency

**Logging:**
- Переиспользование `ArbScanLoggerProvider` из существующего модуля libs

**Dependency Injection:**
- `io.insert-koin:koin-core` - аналогично Ktor модулю

**Testing:**
- `org.apache.kafka:kafka-streams-test-utils:3.7.0` - для embedded Kafka
- `io.kotest:kotest-extensions-embedded-kafka` - Kotest расширения

### 4.2 Альтернативные решения (рассмотренные)

**Kafka Streams** - избыточен для простых request-reply операций  
**Spring Kafka** - не подходит из-за отсутствия Spring в проекте  
**Ktor Kafka plugin** - пока не стабилен для production использования

## 5. Этапы реализации

### 5.1 Этап 1: Создание базовой структуры модуля
**Задачи:**
- Создать модуль `arbitrage-scanner-kafka`
- Настроить `build.gradle.kts` с необходимыми зависимостями
- Создать базовую структуру пакетов
- Настроить application plugin для запуска приложения

**Затрагиваемые файлы:**
- `arbitrage-scanner/settings.gradle.kts` (добавить новый модуль)
- `arbitrage-scanner-kafka/build.gradle.kts` (новый)
- `arbitrage-scanner-kafka/src/main/kotlin/com/arbitrage/scanner/Application.kt` (новый)

### 5.2 Этап 2: Конфигурация Kafka
**Задачи:**
- Создать конфигурацию для Kafka Producer и Consumer
- Настроить топики с учетом request-reply паттерна
- Реализовать JSON сериализацию/десериализацию для Kafka
- Создать конфигурационные файлы

**Затрагиваемые файлы:**
- `config/KafkaProducerConfig.kt` (новый)
- `config/KafkaConsumerConfig.kt` (новый) 
- `config/TopicsConfig.kt` (новый)
- `serialization/KafkaJsonSerializer.kt` (новый)
- `serialization/KafkaJsonDeserializer.kt` (новый)
- `src/main/resources/application.yaml` (новый)

### 5.3 Этап 3: Реализация Consumer'ов
**Задачи:**
- Создать consumer для чтения арбитражных возможностей
- Создать consumer для поиска арбитражных возможностей  
- Реализовать обработку correlation ID для связи request-response
- Интегрировать с существующими мапперами fromTransport

**Затрагиваемые файлы:**
- `consumers/ArbitrageOpportunityReadConsumer.kt` (новый)
- `consumers/ArbitrageOpportunitySearchConsumer.kt` (новый)
- `processors/KafkaMessageProcessor.kt` (новый)

### 5.4 Этап 4: Реализация Producer'а ответов
**Задачи:**
- Создать producer для отправки ответов
- Реализовать логику определения response topic по типу запроса
- Интегрировать с существующими мапперами toTransport
- Обработка ошибок и retry логика

**Затрагиваемые файлы:**
- `producers/ResponseProducer.kt` (новый)
- Обновление `processors/KafkaMessageProcessor.kt`

### 5.5 Этап 5: Интеграция бизнес-логики
**Задачи:**
- Переиспользовать `BusinessLogicProcessor` из существующего модуля
- Переиспользовать систему мапперов и Context
- Настроить DI контейнер аналогично Ktor модулю
- Интеграция логирования

**Затрагиваемые файлы:**
- `KafkaConfiguration.kt` (новый)
- Обновление consumers и processors

### 5.6 Этап 6: Система мониторинга и здоровья
**Задачи:**
- Реализовать health checks для Kafka соединений
- Добавить метрики производительности
- Настроить логирование ошибок и статистики
- Создать graceful shutdown

**Затрагиваемые файлы:**
- `monitoring/KafkaHealthCheck.kt` (новый)
- `monitoring/KafkaMetrics.kt` (новый)
- Обновление `Application.kt`

### 5.7 Этап 7: Тестирование
**Задачи:**
- Создать unit тесты для всех компонентов
- Реализовать интеграционные тесты с embedded Kafka
- Тесты сериализации/десериализации
- Тесты обработки ошибок

**Затрагиваемые файлы:**
- `src/test/kotlin/EmbeddedKafkaTest.kt` (новый)
- `src/test/kotlin/KafkaMessageProcessorTest.kt` (новый)
- `src/test/kotlin/SerializationTest.kt` (новый)

### 5.8 Этап 8: Документация и финализация
**Задачи:**
- Обновить ARCHITECTURE.md с описанием Kafka модуля
- Создать README для модуля с инструкциями по запуску
- Настроить Docker конфигурацию (аналогично Ktor)
- Финальное тестирование и отладка

## 6. Затрагиваемые модули

### 6.1 Новые модули
- `arbitrage-scanner-kafka/` - **полностью новый модуль**

### 6.2 Изменения в существующих модулях

**arbitrage-scanner/settings.gradle.kts:**
```kotlin
include(":arbitrage-scanner-kafka")
```

**arbitrage-scanner/build.gradle.kts:**
```kotlin
// Возможно потребуется обновление версий Kafka зависимостей
```

### 6.3 Переиспользуемые модули (без изменений)
- `arbitrage-scanner-api-v1/` - полное переиспользование API моделей
- `arbitrage-scanner-business-logic/` - полное переиспользование бизнес-логики
- `arbitrage-scanner-common/` - полное переиспользование доменных моделей
- `arbitrage-scanner-libs/` - полное переиспользование библиотек

## 7. Потенциальные риски и способы решения

### 7.1 Производительность
**Риск:** Увеличенная латентность по сравнению с синхронным HTTP  
**Решение:** 
- Оптимизация конфигурации Kafka (batch size, linger.ms)
- Использование partition key для равномерного распределения
- Мониторинг метрик и тюнинг производительности

### 7.2 Надежность доставки
**Риск:** Потеря сообщений или дублирование  
**Решение:**
- Настройка `acks=all` для гарантии записи
- Идемпотентность producer'а
- Proper offset management в consumer'ах
- Retry механизмы с exponential backoff

### 7.3 Сложность отладки
**Риск:** Асинхронная природа усложняет отладку  
**Решение:**
- Детальное логирование с correlation ID
- Структурированное логирование с контекстом
- Health checks и monitoring dashboards
- Tracing запросов через систему

### 7.4 Совместимость версий
**Риск:** Конфликты версий Kafka клиента с другими библиотеками  
**Решение:**
- Тщательный анализ dependency tree
- Использование `implementation` вместо `api` в gradle
- Тестирование на различных версиях Kotlin

### 7.5 Тестирование
**Риск:** Сложность написания интеграционных тестов  
**Решение:**
- Использование Testcontainers для Kafka в тестах
- Embedded Kafka для unit тестов
- Моки для сложных сценариев
- Отдельные test profiles

## 8. Критерии готовности

### 8.1 Функциональные критерии
- ✅ Поддержка всех endpoint'ов из Ktor модуля (read, search)
- ✅ Идентичная JSON сериализация/десериализация
- ✅ Корректная обработка всех типов запросов и ответов
- ✅ Работающая система ошибок и их обработка

### 8.2 Нефункциональные критерии  
- ✅ Запуск приложения через `./gradlew :arbitrage-scanner-kafka:run`
- ✅ Создание fat JAR через `./gradlew :arbitrage-scanner-kafka:shadowJar`
- ✅ Прохождение всех тестов `./gradlew :arbitrage-scanner-kafka:test`
- ✅ Интеграция с существующей системой логирования

### 8.3 Качественные критерии
- ✅ Покрытие тестами минимум 80%
- ✅ Отсутствие memory leaks при длительной работе
- ✅ Graceful shutdown при остановке приложения
- ✅ Соответствие архитектурным принципам проекта

### 8.4 Документационные критерии
- ✅ Обновленная ARCHITECTURE.md
- ✅ README с инструкциями по запуску
- ✅ API документация для Kafka интеграции
- ✅ Примеры конфигурации для различных окружений

## 9. Следующие шаги после реализации

### 9.1 Производственное развертывание
- Настройка Kafka кластера
- Конфигурация мониторинга и алертов
- Load testing и performance tuning
- Blue-green deployment стратегия

### 9.2 Расширение функциональности
- Поддержка batch операций
- Внедрение schema registry для версионирования
- Dead letter queue для failed messages
- Metrics dashboard и alerting

### 9.3 Интеграция с другими системами
- Event sourcing capabilities
- CQRS pattern реализация
- Микросервисная коммуникация через Kafka
- Real-time streaming analytics

## 10. Заключение

Данный план обеспечивает создание полнофункционального Kafka транспорта, который:

1. **Полностью эквивалентен** существующему Ktor модулю по функциональности
2. **Интегрируется** с существующей архитектурой без нарушения принципов
3. **Переиспользует** максимальное количество существующего кода
4. **Обеспечивает** высокую производительность и надежность
5. **Поддерживает** тестируемость и сопровождаемость

Реализация займет приблизительно 3-4 недели при полной занятости разработчика и включает все этапы от создания базовой структуры до полного тестирования и документирования.