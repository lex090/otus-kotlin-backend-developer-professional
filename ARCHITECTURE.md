# Архитектурная документация Arbitrage Scanner

## Оглавление

1. [Обзор системы](#обзор-системы)
2. [Архитектурные принципы](#архитектурные-принципы)
3. [Слоистая архитектура](#слоистая-архитектура)
4. [Основные компоненты](#основные-компоненты)
5. [Модель данных](#модель-данных)
6. [Паттерны проектирования](#паттерны-проектирования)
7. [Взаимодействие компонентов](#взаимодействие-компонентов)
8. [Диаграммы архитектуры](#диаграммы-архитектуры)
9. [Технические решения](#технические-решения)

## Обзор системы

**Arbitrage Scanner** — это микросервисная система для поиска арбитражных возможностей между децентрализованными (DEX) и централизованными (CEX) криптовалютными биржами в экосистеме Binance Smart Chain.

### Ключевые возможности:
- Мониторинг цен токенов на различных биржах
- Автоматическое выявление ценовых расхождений
- Расчет прибыльности с учетом комиссий и gas-fees
- REST API для интеграции с внешними системами
- Система логирования и мониторинга

## Архитектурные принципы

### 1. Чистая архитектура (Clean Architecture)
- **Разделение ответственности**: Четкое разделение на слои с минимальной связанностью
- **Независимость от фреймворков**: Бизнес-логика не зависит от веб-фреймворка или БД
- **Тестируемость**: Каждый слой может быть протестирован независимо

### 2. Domain-Driven Design (DDD)
- **Доменная модель**: Центральные концепции системы представлены в виде доменных объектов
- **Bounded Context**: Четкие границы между различными областями предметной области
- **Ubiquitous Language**: Единый язык для всех участников проекта

### 3. SOLID принципы
- **Single Responsibility**: Каждый класс имеет одну ответственность
- **Open/Closed**: Открыт для расширения, закрыт для изменения
- **Liskov Substitution**: Возможность замены реализаций без нарушения функциональности
- **Interface Segregation**: Интерфейсы разделены по ответственности
- **Dependency Inversion**: Зависимость от абстракций, а не от конкретных реализаций

## Слоистая архитектура

```
┌─────────────────────────────────────────┐
│           Presentation Layer            │  ← HTTP API, Routing, Serialization
├─────────────────────────────────────────┤
│           Application Layer             │  ← Business Logic Processors
├─────────────────────────────────────────┤
│             Domain Layer                │  ← Models, Entities, Value Objects
├─────────────────────────────────────────┤
│         Infrastructure Layer            │  ← Logging, DI, External APIs
└─────────────────────────────────────────┘
```

### Presentation Layer (Слой представления)
**Модули**: `arbitrage-scanner-ktor`, `arbitrage-scanner-api-v1`

**Ответственность**:
- Обработка HTTP запросов и ответов
- Сериализация/десериализация JSON
- Маршрутизация запросов
- Валидация входных данных

**Ключевые компоненты**:
- `RoutingConfiguration` - конфигурация маршрутов
- `RoutingV1` - маршруты API v1
- Transport mappers (`ToTransportMappers`, `FromTransportMappers`)
- Request/Response processors

### Application Layer (Слой приложения)
**Модули**: `arbitrage-scanner-business-logic`

**Ответственность**:
- Оркестрация бизнес-процессов
- Координация между различными доменными сервисами
- Обработка use cases системы

**Ключевые компоненты**:
- `BusinessLogicProcessor` (интерфейс)
- `BusinessLogicProcessorSimpleImpl` (реализация)

### Domain Layer (Доменный слой)
**Модули**: `arbitrage-scanner-common`, `arbitrage-scanner-stubs`

**Ответственность**:
- Инкапсуляция бизнес-правил и логики
- Определение доменных сущностей и value objects
- Бизнес-валидация

**Ключевые компоненты**:
- Доменные модели (`ArbitrageOpportunity`, `DexPrice`, `CexPrice`)
- Енумы состояний и типов
- Контекст выполнения (`Context`)

### Infrastructure Layer (Инфраструктурный слой)
**Модули**: `arbitrage-scanner-libs`

**Ответственность**:
- Внешние интеграции
- Логирование и мониторинг
- Инъекция зависимостей
- Кроссплатформенные утилиты

**Ключевые компоненты**:
- `ArbScanLoggerProvider` - система логирования
- Koin configuration - DI контейнер
- Multiplatform libraries

## Основные компоненты

### 1. Context (Контекст выполнения)

```kotlin
data class Context(
    var command: Command = Command.NONE,
    var state: State = State.NONE,
    var internalErrors: MutableList<InternalError> = mutableListOf(),
    var workMode: WorkMode = WorkMode.PROD,
    var stubCase: Stubs = Stubs.NONE,
    var requestId: String = "",
    var startTimestamp: Instant = NONE,

    // Request objects
    var arbitrageOpportunityReadRequest: ArbitrageOpportunityReadRequest = ArbitrageOpportunityReadRequest(),
    var arbitrageOpportunitySearchRequest: ArbitrageOpportunitySearchRequest = ArbitrageOpportunitySearchRequest(),

    // Response objects
    var arbitrageOpportunityReadResponse: ArbitrageOpportunityReadResponse = ArbitrageOpportunityReadResponse(),
    var arbitrageOpportunitySearchResponse: ArbitrageOpportunitySearchResponse = ArbitrageOpportunitySearchResponse(),
)
```

**Назначение**: Центральный объект, содержащий все данные запроса, состояние обработки и результаты выполнения.

**Паттерн**: Context Object - передача данных через все слои приложения в едином объекте.

### 2. Business Logic Processor

```kotlin
interface BusinessLogicProcessor {
    suspend fun exec(ctx: Context)
}

class BusinessLogicProcessorSimpleImpl : BusinessLogicProcessor {
    override suspend fun exec(ctx: Context) {
        // Простая реализация бизнес-логики
    }
}
```

**Назначение**: Обработка бизнес-логики системы на основе переданного контекста.

**Паттерны**:
- Strategy Pattern - возможность замены алгоритмов обработки
- Command Pattern - инкапсуляция бизнес-операций

### 3. Доменные модели

#### ArbitrageOpportunity (Арбитражная возможность)

```kotlin
sealed class ArbitrageOpportunity {
    abstract val id: String
    abstract val opportunityType: ArbitrageOpportunityType
    abstract val spread: Double
    abstract val statusType: ArbitrageOpportunityStatus
    abstract val timestampStart: Long
    abstract val timestampEnd: Long
}

data class DexToCexSimpleArbitrageOpportunity(
    override val id: String,
    override val opportunityType: ArbitrageOpportunityType,
    val dexPrice: DexPrice,
    val cexPrice: CexPrice,
    override val spread: Double,
    override val statusType: ArbitrageOpportunityStatus,
    override val timestampStart: Long,
    override val timestampEnd: Long
) : ArbitrageOpportunity()
```

**Паттерны**:
- Sealed Class - ограниченная иерархия типов
- Value Object - неизменяемые объекты с бизнес-значением
- Factory Method - различные способы создания арбитражных возможностей

#### Price Models

```kotlin
data class DexPrice(
    val tokenId: String,
    val chainId: String,
    val exchangeId: String,
    val priceRaw: Double
)

data class CexPrice(
    val tokenId: String,
    val exchangeId: String,
    val priceRaw: Double
)
```

**Назначение**: Представление цен на различных типах бирж с соответствующими метаданными.

## Паттерны проектирования

### 1. Dependency Injection (Koin)

```kotlin
fun Application.configureDI() {
    install(Koin) {
        slf4jLogger()
        modules(ktorModule)
    }
}

val ktorModule = module {
    factory<Json> { jsonConfig() }
    factory<BusinessLogicProcessor> { BusinessLogicProcessorSimpleImpl() }
    factory<ArbScanLoggerProvider> { ArbScanLoggerProvider(::arbScanLoggerLogback) }
}
```

**Преимущества**:
- Слабая связанность между компонентами
- Легкость тестирования (mock dependencies)
- Гибкость конфигурации

### 2. Transport Object Pattern

```kotlin
// Конвертация доменной модели в транспортный объект
fun Context.toTransport(): IResponse {
    return when(command) {
        Command.READ -> toTransportRead()
        Command.SEARCH -> toTransportSearch()
        else -> throw IllegalStateException("Unknown command: $command")
    }
}

// Конвертация транспортного объекта в доменную модель
fun ArbitrageOpportunityReadRequest.fromTransport(context: Context) {
    context.arbitrageOpportunityReadRequest = this.toArbitrageOpportunityReadRequest()
}
```

**Назначение**: Разделение внутренних доменных моделей от внешних API объектов.

### 3. Strategy Pattern

```kotlin
interface BusinessLogicProcessor {
    suspend fun exec(ctx: Context)
}

// Различные стратегии обработки
class BusinessLogicProcessorSimpleImpl : BusinessLogicProcessor
class BusinessLogicProcessorAdvancedImpl : BusinessLogicProcessor  // Потенциальная реализация
class BusinessLogicProcessorTestImpl : BusinessLogicProcessor       // Тестовая реализация
```

### 4. Builder Pattern (Context Configuration)

```kotlin
// Построение контекста через DSL-подобный синтаксис
suspend fun ApplicationCall.processContext(
    request: IRequest,
    businessLogicProcessor: BusinessLogicProcessor,
    prepareContextFromRequest: suspend Context.() -> Unit,
    resolveContextToResponse: suspend Context.() -> Unit
): IResponse {
    val context = Context().apply {
        this.prepareContextFromRequest()
    }

    businessLogicProcessor.exec(context)

    context.resolveContextToResponse()
    return context.toTransport()
}
```

### 5. Factory Method Pattern

```kotlin
object ArbOpStubs {
    fun getDexToCexSimpleArbitrageOpportunityRead() = DexToCexSimpleArbitrageOpportunity(
        id = "test-id",
        opportunityType = ArbitrageOpportunityType.DEX_TO_CEX_SIMPLE_ARBITRAGE_OPPORTUNITY,
        dexPrice = DexPrice(/*...*/),
        cexPrice = CexPrice(/*...*/),
        spread = 2.0,
        statusType = ArbitrageOpportunityStatus.ACTIVE,
        timestampStart = 1640995200000L,
        timestampEnd = 1640995260000L
    )
}
```

## Взаимодействие компонентов

### Поток обработки запроса

```
HTTP Request → Ktor Router → Request Processor → Context Creation →
Business Logic Processor → Context Update → Response Mapper → HTTP Response
```

#### Детальный поток:

1. **HTTP Request Reception**
   ```kotlin
   // arbitrage-scanner-ktor/routing/v1/RoutingV1.kt
   post("/arbitrage-opportunity/read") {
       call.readArbitrageOpportunity(businessLogicProcessor, loggerProvider, json)
   }
   ```

2. **Request Processing**
   ```kotlin
   // processors/RequestProcessor.kt
   suspend inline fun <reified Req : IRequest, reified Resp : IResponse>
   ApplicationCall.processRequest(
       businessLogicProcessor: BusinessLogicProcessor,
       // ...
   ): Unit {
       val request = receiveRequest<Req>()
       val response = processContext(request, businessLogicProcessor, /*...*/)
       respondWithResponse(response)
   }
   ```

3. **Context Preparation**
   ```kotlin
   // mappers/FromTransportMappers.kt
   fun ArbitrageOpportunityReadRequest.fromTransport(context: Context) {
       context.command = Command.READ
       context.arbitrageOpportunityReadRequest = this.toArbitrageOpportunityReadRequest()
   }
   ```

4. **Business Logic Execution**
   ```kotlin
   // BusinessLogicProcessorSimpleImpl.kt
   override suspend fun exec(ctx: Context) {
       // Бизнес-логика обработки
       when(ctx.command) {
           Command.READ -> handleRead(ctx)
           Command.SEARCH -> handleSearch(ctx)
       }
   }
   ```

5. **Response Generation**
   ```kotlin
   // mappers/ToTransportMappers.kt
   fun Context.toTransport(): IResponse {
       return when(command) {
           Command.READ -> toTransportRead()
           Command.SEARCH -> toTransportSearch()
       }
   }
   ```

### Система логирования

```kotlin
class ArbScanLoggerProvider(
    private val provider: (String) -> ArbScanLogWrapper = { ArbScanLogWrapper.DEFAULT }
) {
    fun logger(loggerId: String): ArbScanLogWrapper = provider(loggerId)
    fun logger(clazz: KClass<*>): ArbScanLogWrapper = provider(clazz.qualifiedName ?: "(unknown)")
    fun logger(function: KFunction<*>): ArbScanLogWrapper = provider(function.name)
}
```

**Интеграция в бизнес-процессы**:
```kotlin
suspend fun ApplicationCall.processContext(/*...*/) {
    val logger = loggerProvider.logger(kFun)
    logger.info("Processing request: $logId")

    try {
        // Основная логика
    } catch (throwable: Throwable) {
        logger.error("Error processing request: $logId", throwable)
    }
}
```

## Диаграммы архитектуры

### 1. Компонентная диаграмма

```
┌──────────────────────┐    ┌──────────────────────┐
│   Ktor Web Layer     │    │    API v1 Layer      │
│  - RoutingV1         │    │  - Transport Objects │
│  - RequestProcessor  │◄───┤  - Mappers           │
│  - ContextProcessor  │    │  - Serialization     │
└──────────┬───────────┘    └──────────────────────┘
           │
           ▼
┌──────────────────────┐    ┌──────────────────────┐
│  Business Logic      │    │     Common Models    │
│  - BLProcessor       │◄───┤  - ArbitrageOpp      │
│  - SimpleImpl        │    │  - Context           │
│                      │    │  - Price Models      │
└──────────┬───────────┘    └──────────────────────┘
           │
           ▼
┌──────────────────────┐    ┌──────────────────────┐
│   Infrastructure     │    │      Stubs           │
│  - LoggerProvider    │    │  - Test Data         │
│  - Koin DI          │    │  - Mock Objects      │
│  - Multiplatform    │    │                      │
└──────────────────────┘    └──────────────────────┘
```

### 2. Диаграмма последовательности (Sequence Diagram)

```
Client          Ktor           RequestProcessor    BusinessLogic       Context
  │               │                     │               │               │
  ├─HTTP Request──►│                     │               │               │
  │               ├─processRequest──────►│               │               │
  │               │                     ├─fromTransport─►│               │
  │               │                     │               ├─prepareContext►│
  │               │                     ├─exec──────────►│               │
  │               │                     │               ├─process───────►│
  │               │                     │               ◄─response──────┤
  │               │                     ├─toTransport───►│               │
  │               │                     ◄─response──────┤               │
  │               ◄─HTTP Response───────┤               │               │
  ◄─JSON Response─┤                     │               │               │
```

### 3. Диаграмма классов (основные сущности)

```
┌─────────────────────────┐
│     Context             │
├─────────────────────────┤
│ + command: Command      │
│ + state: State          │
│ + workMode: WorkMode    │
│ + readRequest: Request  │
│ + readResponse: Response│
└─────────────────────────┘
           │
           ▼
┌─────────────────────────┐     ┌──────────────────────┐
│ BusinessLogicProcessor  │     │  ArbitrageOpportunity│
├─────────────────────────┤     ├──────────────────────┤
│ + exec(ctx: Context)    │     │ + id: String         │
└─────────────────────────┘     │ + spread: Double     │
           △                     │ + status: Status     │
           │                     └──────────────────────┘
┌─────────────────────────┐                △
│BusinessLogicProcessorSimpleImpl│         │
├─────────────────────────┤                │
│ + exec(ctx: Context)    │     ┌──────────────────────┐
└─────────────────────────┘     │DexToCexSimpleArbitrage│
                                ├──────────────────────┤
                                │ + dexPrice: DexPrice │
                                │ + cexPrice: CexPrice │
                                └──────────────────────┘
```

## Технические решения

### 1. Мультиплатформенная архитектура

**Структура проекта**:
- `commonMain` - общий код для всех платформ
- `jvmMain` - JVM-специфичный код
- `linuxX64Main` - Linux-специфичный код
- `macosArm64Main` - macOS ARM-специфичный код

**Преимущества**:
- Переиспользование кода между платформами
- Единые бизнес-правила для всех таргетов
- Гибкость развертывания

### 2. Composite Builds

```
root/
├── arbitrage-scanner/          # Основное приложение
│   ├── arbitrage-scanner-api-v1/
│   ├── arbitrage-scanner-business-logic/
│   ├── arbitrage-scanner-common/
│   ├── arbitrage-scanner-ktor/
│   └── arbitrage-scanner-libs/
├── lessons/                    # Учебные модули
│   └── m1l1-first/
└── build-logic/               # Кастомные build плагины
    └── src/main/kotlin/plugins/
```

**Преимущества**:
- Изоляция различных областей проекта
- Переиспользование конфигурации сборки
- Упрощение dependency management

### 3. Кастомные Build Плагины

```kotlin
// BuildPluginJvm.kt
class BuildPluginJvm : Plugin<Project> {
    override fun apply(project: Project) = with(project) {
        pluginManager.run {
            apply("org.jetbrains.kotlin.jvm")
        }

        configureKotlinJvm()
    }
}

// BuildPluginMultiplatform.kt
internal class BuildPluginMultiplatform : Plugin<Project> {
    override fun apply(project: Project) = with(project) {
        pluginManager.run {
            apply("org.jetbrains.kotlin.multiplatform")
        }

        configureKotlinMultiplatform()
    }
}
```

**Преимущества**:
- Стандартизация конфигурации проекта
- DRY принцип в build скриптах
- Централизованное управление версиями

### 4. Система сериализации

```kotlin
// ArbitrageOpportunityApiV1RequestSerialization.kt
fun <I : IRequest> Json.toRequestJsonString(request: I): String =
    encodeToString(IRequest.serializer(), request)

inline fun <reified I : IRequest> Json.fromRequestJsonString(json: String): I =
    decodeFromString<IRequest>(json) as I

// ArbitrageOpportunityApiV1ResponseSerialization.kt
fun <I : IResponse> Json.toResponseJsonString(response: I): String =
    encodeToString(IResponse.serializer(), response)

inline fun <reified I : IResponse> Json.fromResponseJsonString(json: String): I =
    decodeFromString<IResponse>(json) as I
```

**Особенности**:
- Полиморфная сериализация через sealed классы
- Type-safe десериализация с reified generics
- Поддержка различных форматов через Kotlinx.serialization

### 5. Обработка ошибок

```kotlin
data class InternalError(
    val code: String = "",
    val group: String = "",
    val field: String = "",
    val message: String = "",
    val exception: Throwable? = null,
)

// В Context
var internalErrors: MutableList<InternalError> = mutableListOf()

// Использование
try {
    businessLogicProcessor.exec(context)
} catch (throwable: Throwable) {
    logger.error("Error processing request: $logId", throwable)
    context.internalErrors.add(
        InternalError(
            code = "PROCESSING_ERROR",
            message = throwable.message ?: "Unknown error",
            exception = throwable
        )
    )
}
```

**Принципы**:
- Централизованное накопление ошибок в контексте
- Структурированные ошибки с метаданными
- Логирование ошибок с контекстной информацией

## Заключение

Архитектура Arbitrage Scanner построена на принципах чистой архитектуры с четким разделением ответственности между слоями. Использование современных паттернов проектирования, мультиплатформенного подхода и dependency injection обеспечивает:

1. **Масштабируемость** - легкость добавления новых функций и интеграций
2. **Тестируемость** - изолированное тестирование каждого компонента
3. **Поддерживаемость** - четкая структура и документированные взаимосвязи
4. **Производительность** - оптимизированная обработка запросов через корутины
5. **Гибкость** - возможность замены реализаций без изменения интерфейсов

Система готова к расширению новыми типами арбитражных стратегий, интеграции с дополнительными биржами и развертыванию на различных платформах.