# План реализации модуля arbitrage-scanner-kafka

## Дата создания
2025-09-29

## Цель задачи
Реализовать новый транспортный модуль `arbitrage-scanner-kafka`, который обеспечит асинхронную обработку запросов через Apache Kafka, аналогично существующему модулю `arbitrage-scanner-ktor`, но с использованием message broker вместо HTTP.

---

## 1. Анализ текущего состояния

### 1.1 Существующая архитектура Ktor модуля

**Модуль**: `arbitrage-scanner-ktor`

**Основные компоненты**:
- `Application.kt` - точка входа приложения с `main()` и `module()` функциями
- `KoinConfiguration.kt` - настройка Dependency Injection (3 модуля: json, businessLogicProcessor, logging)
- `RoutingConfiguration.kt` - конфигурация HTTP маршрутов
- `routing/v1/Routs.kt` - обработчики HTTP endpoints (read, search)
- `processors/RequestProcessor.kt` - обработка запросов с десериализацией
- `processors/ContextProcessor.kt` - центральная логика обработки с логированием и обработкой ошибок

**Зависимости модуля**:
- `arbitrage-scanner-common` - доменные модели (Context, ArbitrageOpportunity, etc.)
- `arbitrage-scanner-api-v1` - транспортные модели (IRequest/IResponse) и мапперы
- `arbitrage-scanner-business-logic` - бизнес-логика (BusinessLogicProcessor)
- `arbitrage-scanner-libs:arbitrage-scanner-lib-logging-logback` - логирование
- Ktor (server-core, content-negotiation, serialization, netty, config-yaml)
- Koin (DI контейнер)

**Поток обработки HTTP запроса**:
```
HTTP Request (JSON)
  ↓
Ktor Router (routing/v1/RoutingV1.kt)
  ↓
processRequest<Req, Resp>() - десериализация
  ↓
processContext() - создание Context
  ↓
fromTransport() - преобразование Req → Context
  ↓
BusinessLogicProcessor.exec(context) - бизнес-логика
  ↓
toTransport() - преобразование Context → Resp
  ↓
respond(response) - сериализация и отправка
  ↓
HTTP Response (JSON)
```

### 1.2 Существующие компоненты для переиспользования

#### Полностью переиспользуемые:
1. **Бизнес-логика**: `BusinessLogicProcessor` и `BusinessLogicProcessorSimpleImpl`
2. **Мапперы**: `FromTransportMappers.kt` и `ToTransportMappers.kt`
3. **API модели**: `ArbitrageOpportunityReadRequest/Response`, `ArbitrageOpportunitySearchRequest/Response`
4. **Доменные модели**: `Context`, `ArbitrageOpportunity`, `InternalError`
5. **Логирование**: `ArbScanLoggerProvider` и `arbScanLoggerLogback`
6. **Процессор контекста**: `processContext()` функция может быть адаптирована

#### Требуют адаптации:
1. **RequestProcessor** - нужна Kafka-специфичная версия (без Ktor ApplicationCall)
2. **Application** - другая точка входа (Kafka consumer вместо Ktor server)
3. **Configuration** - настройка Kafka вместо Ktor

### 1.3 Ключевые паттерны и принципы проекта

**Архитектурные принципы**:
- Clean Architecture - разделение на слои
- Domain-Driven Design - доменные модели в центре
- SOLID принципы
- Dependency Inversion - зависимость от абстракций

**Используемые паттерны**:
- **Context Object** - передача данных через все слои
- **Strategy Pattern** - BusinessLogicProcessor
- **Transport Object Pattern** - разделение транспортных и доменных моделей
- **Dependency Injection** - Koin для управления зависимостями
- **Builder Pattern** - построение контекста через DSL

**Технические особенности**:
- Kotlin Multiplatform (JVM, Linux, macOS)
- Composite builds (lessons, arbitrage-scanner, build-logic)
- Кастомные build плагины (BuildPluginJvm, BuildPluginMultiplatform)
- Kotlinx.serialization для JSON
- Корутины для асинхронности
- Централизованная обработка ошибок через Context.internalErrors

---

## 2. Цели и требования

### 2.1 Функциональные требования

**FR-1**: Поддержка операций Read и Search
- Прием и обработка `ArbitrageOpportunityReadRequest`
- Прием и обработка `ArbitrageOpportunitySearchRequest`
- Отправка соответствующих Response моделей

**FR-2**: Асинхронная обработка через Kafka
- Consumer для приема запросов из топика `arbitrage-scanner-requests`
- Producer для отправки ответов в топик `arbitrage-scanner-responses`
- Поддержка Request-Reply паттерна с correlation ID

**FR-3**: JSON сериализация/десериализация
- Использование Kotlinx.serialization
- Полиморфная сериализация IRequest/IResponse
- Совместимость с существующими API моделями

**FR-4**: Переиспользование бизнес-логики
- Использование того же BusinessLogicProcessor
- Использование тех же мапперов (fromTransport/toTransport)
- Идентичная обработка Context

**FR-5**: Логирование
- Использование ArbScanLoggerProvider
- Логирование на всех этапах обработки
- Структурированные логи с маркерами

### 2.2 Нефункциональные требования

**NFR-1**: Производительность
- Асинхронная обработка через корутины
- Параллельная обработка нескольких сообщений
- Настраиваемое количество consumer threads

**NFR-2**: Надежность
- Обработка ошибок на всех уровнях
- Graceful shutdown для корректного завершения
- Dead Letter Queue для проблемных сообщений (опционально)

**NFR-3**: Конфигурируемость
- Настройка через application.yaml
- Конфигурация Kafka (bootstrap servers, group id, topics)
- Настройка параллелизма (concurrency level)

**NFR-4**: Мониторинг
- Логирование метрик обработки
- Отслеживание времени обработки
- Логирование ошибок с контекстом

**NFR-5**: Тестируемость
- Unit тесты для компонентов
- Integration тесты с Kafka test containers
- Возможность mock dependencies через Koin

---

## 3. Архитектурные решения

### 3.1 Выбор Kafka клиента

**Рекомендация**: Apache Kafka Clients + Kotlin Coroutines wrapper

**Обоснование**:
1. **apache-kafka-clients** (официальная библиотека)
   - Стабильная и широко используемая
   - Полная поддержка всех функций Kafka
   - Хорошая производительность

2. **kotlinx-coroutines-reactive** для integration с Kafka
   - Нативная поддержка корутин
   - Неблокирующая обработка
   - Совместимость с существующим асинхронным кодом

**Альтернативы** (не рекомендуются для первой версии):
- `kotlin-kafka` - менее зрелая библиотека
- `reactor-kafka` - требует дополнительной зависимости на Project Reactor

### 3.2 Request-Reply паттерн через Kafka

**Схема реализации**:

```
Client Side (не реализуется в этом модуле):
  1. Генерирует correlation ID (UUID)
  2. Отправляет Request в топик "arbitrage-scanner-requests"
  3. Подписывается на топик "arbitrage-scanner-responses" с фильтром по correlation ID
  4. Ждет Response с таймаутом

Server Side (наш модуль):
  1. Consumer читает из "arbitrage-scanner-requests"
  2. Извлекает correlation ID из заголовка сообщения
  3. Обрабатывает запрос
  4. Producer отправляет Response в "arbitrage-scanner-responses" с тем же correlation ID
```

**Структура Kafka сообщения**:
```
Headers:
  - correlation-id: String (UUID)
  - request-type: String ("READ" | "SEARCH")
  - timestamp: Long (epoch millis)

Key: correlation-id (для партиционирования)

Value: JSON {
  // IRequest или IResponse
}
```

### 3.3 Топология Kafka топиков

**Request Topic**: `arbitrage-scanner-requests`
- Количество партиций: 3 (настраиваемо)
- Replication factor: 1 (для dev), 3 (для prod)
- Retention: 1 day

**Response Topic**: `arbitrage-scanner-responses`
- Количество партиций: 3 (настраиваемо)
- Replication factor: 1 (для dev), 3 (для prod)
- Retention: 1 hour

**DLQ Topic** (опционально для будущих версий): `arbitrage-scanner-requests-dlq`

### 3.4 Структура модуля

```
arbitrage-scanner-kafka/
├── build.gradle.kts
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── com/arbitrage/scanner/
│   │   │       ├── Application.kt              # Точка входа
│   │   │       ├── KafkaConfiguration.kt       # Конфигурация Kafka
│   │   │       ├── KoinConfiguration.kt        # DI настройка
│   │   │       ├── consumers/
│   │   │       │   ├── KafkaRequestConsumer.kt     # Consumer запросов
│   │   │       │   └── ConsumerConfig.kt           # Настройки consumer
│   │   │       ├── producers/
│   │   │       │   ├── KafkaResponseProducer.kt    # Producer ответов
│   │   │       │   └── ProducerConfig.kt           # Настройки producer
│   │   │       ├── processors/
│   │   │       │   ├── KafkaMessageProcessor.kt    # Обработка сообщений
│   │   │       │   └── KafkaContextProcessor.kt    # Адаптация processContext
│   │   │       └── models/
│   │   │           └── KafkaMessage.kt         # Обертка для Kafka сообщений
│   │   └── resources/
│   │       ├── application.yaml                # Конфигурация приложения
│   │       └── logback.xml                     # Конфигурация логирования
│   └── test/
│       └── kotlin/
│           └── com/arbitrage/scanner/
│               ├── KafkaApplicationTest.kt     # Интеграционные тесты
│               └── KafkaMessageProcessorTest.kt # Unit тесты
└── docker/
    └── docker-compose-kafka.yml                # Локальный Kafka для тестирования
```

### 3.5 Обработка ошибок

**Стратегия обработки**:

1. **Recoverable Errors** (бизнес-ошибки):
   - Обрабатываются в processContext
   - Добавляются в Context.internalErrors
   - Response отправляется с ошибками
   - Kafka offset коммитится

2. **Non-recoverable Errors** (технические ошибки):
   - Логируются с полным контекстом
   - Сообщение не обрабатывается повторно (skip)
   - Kafka offset коммитится после N попыток
   - Опционально: отправка в DLQ

3. **Serialization Errors**:
   - Логируются как критические
   - Сообщение пропускается
   - Offset коммитится

**Уровни обработки**:
```
try {
    // Десериализация
} catch (SerializationException e) {
    logger.error("Failed to deserialize message", e)
    // Skip and commit offset
}

try {
    // Бизнес-логика (через processContext)
} catch (Exception e) {
    // Обрабатывается в processContext
    // Ошибка добавляется в Context
    // Response отправляется с ошибкой
}
```

### 3.6 Graceful Shutdown

**Последовательность завершения**:
1. Перестать принимать новые сообщения (pause consumer)
2. Дождаться завершения обработки текущих сообщений (с таймаутом)
3. Закрыть producer (flush pending messages)
4. Закрыть consumer (commit offsets)
5. Завершить корутины (coroutineScope.cancel)

**Реализация**:
```kotlin
// Регистрация shutdown hook
Runtime.getRuntime().addShutdownHook(Thread {
    runBlocking {
        application.shutdown()
    }
})
```

---

## 4. Этапы реализации

### Этап 1: Настройка проекта и зависимостей

**Цель**: Создать базовую структуру модуля с необходимыми зависимостями

**Задачи**:

1. **Создать модуль в settings.gradle.kts**
   - Добавить `include("arbitrage-scanner-kafka")` в файл `arbitrage-scanner/settings.gradle.kts`

2. **Создать build.gradle.kts для модуля**
   - Применить плагин `alias(libs.plugins.build.plugin.jvm)`
   - Применить плагин `alias(libs.plugins.kotlinx.serialization)`
   - Добавить зависимости:
     ```kotlin
     dependencies {
         // Внутренние модули
         implementation(project(":arbitrage-scanner-common"))
         implementation(project(":arbitrage-scanner-api-v1"))
         implementation(project(":arbitrage-scanner-business-logic"))
         implementation(project(":arbitrage-scanner-libs:arbitrage-scanner-lib-logging-logback"))

         // Kafka
         implementation("org.apache.kafka:kafka-clients:3.8.1")

         // Kotlinx
         implementation(libs.kotlinx.serialization.json)
         implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")

         // Koin
         implementation("io.insert-koin:koin-core:${libs.versions.koin.get()}")

         // Конфигурация
         implementation("com.sksamuel.hoplite:hoplite-core:2.9.0")
         implementation("com.sksamuel.hoplite:hoplite-yaml:2.9.0")

         // Тестирование
         testImplementation(libs.kotlin.test)
         testImplementation("org.testcontainers:testcontainers:1.20.4")
         testImplementation("org.testcontainers:kafka:1.20.4")
         testImplementation("io.insert-koin:koin-test:${libs.versions.koin.get()}")
     }
     ```

3. **Создать application блок в build.gradle.kts**
   ```kotlin
   application {
       mainClass.set("com.arbitrage.scanner.ApplicationKt")
   }
   ```

4. **Обновить libs.versions.toml** (если нужно)
   - Добавить версии для kafka, hoplite если их нет

**Критерии готовности**:
- Модуль успешно компилируется: `./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:build`
- Все зависимости резолвятся
- Проект виден в `./gradlew projects`

---

### Этап 2: Конфигурация приложения

**Цель**: Реализовать систему конфигурации для Kafka и приложения

**Задачи**:

1. **Создать application.yaml**
   ```yaml
   kafka:
     bootstrap-servers: "localhost:9092"
     group-id: "arbitrage-scanner-group"

     consumer:
       topic: "arbitrage-scanner-requests"
       auto-offset-reset: "earliest"
       enable-auto-commit: false
       max-poll-records: 100
       concurrency: 3

     producer:
       topic: "arbitrage-scanner-responses"
       acks: "all"
       retries: 3
       compression-type: "snappy"

   logging:
     level:
       root: INFO
       com.arbitrage.scanner: DEBUG
       org.apache.kafka: WARN
   ```

2. **Создать data классы для конфигурации**
   ```kotlin
   // KafkaConfiguration.kt

   data class AppConfig(
       val kafka: KafkaConfig,
       val logging: LoggingConfig
   )

   data class KafkaConfig(
       val bootstrapServers: String,
       val groupId: String,
       val consumer: ConsumerConfig,
       val producer: ProducerConfig
   )

   data class ConsumerConfig(
       val topic: String,
       val autoOffsetReset: String,
       val enableAutoCommit: Boolean,
       val maxPollRecords: Int,
       val concurrency: Int
   )

   data class ProducerConfig(
       val topic: String,
       val acks: String,
       val retries: Int,
       val compressionType: String
   )

   data class LoggingConfig(
       val level: Map<String, String>
   )
   ```

3. **Создать ConfigLoader**
   ```kotlin
   object ConfigLoader {
       fun load(): AppConfig {
           return ConfigLoaderBuilder.default()
               .addResourceSource("/application.yaml")
               .build()
               .loadConfigOrThrow<AppConfig>()
       }
   }
   ```

**Критерии готовности**:
- Конфигурация успешно загружается из application.yaml
- Unit тесты для ConfigLoader проходят
- Конфигурация доступна через Koin DI

---

### Этап 3: Kafka Producer (отправка ответов)

**Цель**: Реализовать компонент для отправки Response сообщений в Kafka

**Задачи**:

1. **Создать KafkaResponseProducer**
   ```kotlin
   interface ResponseProducer {
       suspend fun sendResponse(
           correlationId: String,
           response: IResponse
       )

       suspend fun close()
   }

   class KafkaResponseProducer(
       private val config: ProducerConfig,
       private val json: Json,
       private val logger: ArbScanLogWrapper
   ) : ResponseProducer {

       private val producer: KafkaProducer<String, String>

       init {
           val props = Properties().apply {
               put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.bootstrapServers)
               put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
               put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
               put(ProducerConfig.ACKS_CONFIG, config.acks)
               put(ProducerConfig.RETRIES_CONFIG, config.retries)
               put(ProducerConfig.COMPRESSION_TYPE_CONFIG, config.compressionType)
           }
           producer = KafkaProducer(props)
       }

       override suspend fun sendResponse(
           correlationId: String,
           response: IResponse
       ) = withContext(Dispatchers.IO) {
           try {
               val jsonValue = json.toResponseJsonString(response)

               val record = ProducerRecord<String, String>(
                   config.topic,
                   correlationId, // key для партиционирования
                   jsonValue
               ).apply {
                   headers().add("correlation-id", correlationId.toByteArray())
                   headers().add("response-type", response::class.simpleName?.toByteArray())
                   headers().add("timestamp", System.currentTimeMillis().toString().toByteArray())
               }

               producer.send(record).get() // Синхронная отправка для надежности

               logger.info(
                   msg = "Response sent",
                   marker = "KAFKA",
                   data = "correlationId=$correlationId, type=${response::class.simpleName}"
               )
           } catch (e: Exception) {
               logger.error(
                   msg = "Failed to send response",
                   marker = "KAFKA",
                   e = e
               )
               throw e
           }
       }

       override suspend fun close() = withContext(Dispatchers.IO) {
           producer.close()
       }
   }
   ```

2. **Создать unit тесты для KafkaResponseProducer**
   - Тест успешной отправки
   - Тест обработки ошибок
   - Тест корректности заголовков

**Критерии готовности**:
- Producer успешно отправляет сообщения
- Unit тесты проходят
- Логирование работает корректно

---

### Этап 4: Kafka Message Processor

**Цель**: Адаптировать существующую логику обработки для Kafka

**Задачи**:

1. **Создать KafkaMessage модель**
   ```kotlin
   data class KafkaMessage(
       val correlationId: String,
       val requestType: String,
       val timestamp: Long,
       val payload: String
   )

   fun ConsumerRecord<String, String>.toKafkaMessage(): KafkaMessage {
       val correlationId = headers().lastHeader("correlation-id")
           ?.value()?.decodeToString() ?: key()

       val requestType = headers().lastHeader("request-type")
           ?.value()?.decodeToString() ?: "UNKNOWN"

       val timestamp = headers().lastHeader("timestamp")
           ?.value()?.decodeToString()?.toLongOrNull() ?: System.currentTimeMillis()

       return KafkaMessage(
           correlationId = correlationId,
           requestType = requestType,
           timestamp = timestamp,
           payload = value()
       )
   }
   ```

2. **Создать KafkaMessageProcessor**
   ```kotlin
   class KafkaMessageProcessor(
       private val businessLogicProcessor: BusinessLogicProcessor,
       private val responseProducer: ResponseProducer,
       private val json: Json,
       private val loggerProvider: ArbScanLoggerProvider
   ) {
       private val logger = loggerProvider.logger(KafkaMessageProcessor::class)

       suspend fun processMessage(message: KafkaMessage) {
           try {
               // Десериализация запроса
               val request = deserializeRequest(message.payload)

               // Создание и заполнение контекста
               val context = createContext(message)
               context.fromTransport(request)

               // Обработка бизнес-логики
               processContext(
                   context = context,
                   businessLogicProcessor = businessLogicProcessor,
                   loggerProvider = loggerProvider
               )

               // Преобразование в Response
               val response = context.toTransport()

               // Отправка ответа
               responseProducer.sendResponse(message.correlationId, response)

           } catch (e: Exception) {
               logger.error(
                   msg = "Failed to process message",
                   marker = "KAFKA",
                   data = "correlationId=${message.correlationId}",
                   e = e
               )
               // Отправляем ответ с ошибкой
               sendErrorResponse(message.correlationId, e)
           }
       }

       private fun deserializeRequest(payload: String): IRequest {
           return json.fromRequestJsonString<IRequest>(payload)
       }

       private fun createContext(message: KafkaMessage): Context {
           return Context(
               requestId = RequestId(message.correlationId),
               startTimestamp = Timestamp(message.timestamp)
           )
       }

       private suspend fun sendErrorResponse(correlationId: String, error: Throwable) {
           // Создать response с ошибкой и отправить
       }
   }
   ```

3. **Адаптировать processContext для Kafka**
   ```kotlin
   // KafkaContextProcessor.kt

   suspend fun processContext(
       context: Context,
       businessLogicProcessor: BusinessLogicProcessor,
       loggerProvider: ArbScanLoggerProvider
   ) {
       val logger = loggerProvider.logger("KafkaContextProcessor")

       try {
           logger.info(
               msg = "Request started",
               marker = "BIZ",
               data = context.toString()
           )

           businessLogicProcessor.exec(context)

           logger.info(
               msg = "Request processed",
               marker = "BIZ",
               data = context.toString()
           )

       } catch (throwable: Throwable) {
           logger.error(
               msg = "Request failed",
               marker = "BIZ",
               data = context.toString(),
               e = throwable
           )
           context.state = State.FAILING
           context.internalErrors.add(throwable.asError())
       }
   }
   ```

**Критерии готовности**:
- Успешная десериализация IRequest из JSON
- Корректное создание и заполнение Context
- Вызов BusinessLogicProcessor
- Преобразование Context в IResponse
- Unit тесты проходят

---

### Этап 5: Kafka Consumer (прием запросов)

**Цель**: Реализовать компонент для приема Request сообщений из Kafka

**Задачи**:

1. **Создать KafkaRequestConsumer**
   ```kotlin
   class KafkaRequestConsumer(
       private val config: ConsumerConfig,
       private val messageProcessor: KafkaMessageProcessor,
       private val logger: ArbScanLogWrapper
   ) {

       private val consumer: KafkaConsumer<String, String>
       private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
       @Volatile private var isRunning = false

       init {
           val props = Properties().apply {
               put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.bootstrapServers)
               put(ConsumerConfig.GROUP_ID_CONFIG, config.groupId)
               put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
               put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
               put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, config.autoOffsetReset)
               put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, config.enableAutoCommit)
               put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, config.maxPollRecords)
           }
           consumer = KafkaConsumer(props)
           consumer.subscribe(listOf(config.topic))
       }

       fun start() {
           isRunning = true

           // Запуск нескольких корутин для параллельной обработки
           repeat(config.concurrency) { workerId ->
               scope.launch {
                   consumeLoop(workerId)
               }
           }

           logger.info(
               msg = "Kafka consumer started",
               marker = "KAFKA",
               data = "topic=${config.topic}, concurrency=${config.concurrency}"
           )
       }

       private suspend fun consumeLoop(workerId: Int) {
           while (isRunning) {
               try {
                   val records = withContext(Dispatchers.IO) {
                       consumer.poll(Duration.ofMillis(100))
                   }

                   for (record in records) {
                       processRecord(record, workerId)
                   }

                   // Manual commit после обработки batch
                   if (records.count() > 0) {
                       withContext(Dispatchers.IO) {
                           consumer.commitSync()
                       }
                   }

               } catch (e: WakeupException) {
                   // Игнорируем - это сигнал к завершению
                   break
               } catch (e: Exception) {
                   logger.error(
                       msg = "Error in consume loop",
                       marker = "KAFKA",
                       data = "workerId=$workerId",
                       e = e
                   )
               }
           }
       }

       private suspend fun processRecord(record: ConsumerRecord<String, String>, workerId: Int) {
           val message = record.toKafkaMessage()

           logger.debug(
               msg = "Processing message",
               marker = "KAFKA",
               data = "workerId=$workerId, correlationId=${message.correlationId}"
           )

           messageProcessor.processMessage(message)
       }

       suspend fun shutdown() {
           logger.info(msg = "Shutting down Kafka consumer", marker = "KAFKA")

           isRunning = false

           // Пробуждаем consumer для выхода из poll()
           withContext(Dispatchers.IO) {
               consumer.wakeup()
           }

           // Ждем завершения всех корутин
           scope.coroutineContext.job.children.forEach { it.join() }

           // Закрываем consumer
           withContext(Dispatchers.IO) {
               consumer.close()
           }

           logger.info(msg = "Kafka consumer shut down", marker = "KAFKA")
       }
   }
   ```

2. **Альтернативная реализация с Flow API** (для будущих улучшений)
   ```kotlin
   // Можно использовать для более functional подхода
   fun KafkaConsumer<String, String>.asFlow(): Flow<ConsumerRecord<String, String>> = flow {
       while (isActive) {
           val records = withContext(Dispatchers.IO) {
               poll(Duration.ofMillis(100))
           }
           records.forEach { emit(it) }
       }
   }
   ```

**Критерии готовности**:
- Consumer успешно подписывается на топик
- Сообщения корректно обрабатываются
- Parallel processing работает (concurrency > 1)
- Manual commit offset происходит после обработки batch
- Graceful shutdown работает корректно

---

### Этап 6: Koin DI Configuration

**Цель**: Настроить Dependency Injection для всех компонентов

**Задачи**:

1. **Создать KoinConfiguration.kt**
   ```kotlin
   val configModule = module {
       single { ConfigLoader.load() }
       single { get<AppConfig>().kafka }
   }

   val jsonModule = module {
       single {
           Json {
               prettyPrint = true
               isLenient = true
               ignoreUnknownKeys = true
           }
       }
   }

   val businessLogicModule = module {
       single<BusinessLogicProcessor> { BusinessLogicProcessorSimpleImpl() }
   }

   val loggingModule = module {
       single { ArbScanLoggerProvider(::arbScanLoggerLogback) }
   }

   val kafkaModule = module {
       single<ResponseProducer> {
           KafkaResponseProducer(
               config = get<KafkaConfig>().producer,
               json = get(),
               logger = get<ArbScanLoggerProvider>().logger(KafkaResponseProducer::class)
           )
       }

       single {
           KafkaMessageProcessor(
               businessLogicProcessor = get(),
               responseProducer = get(),
               json = get(),
               loggerProvider = get()
           )
       }

       single {
           KafkaRequestConsumer(
               config = get<KafkaConfig>().consumer,
               messageProcessor = get(),
               logger = get<ArbScanLoggerProvider>().logger(KafkaRequestConsumer::class)
           )
       }
   }

   val allModules = listOf(
       configModule,
       jsonModule,
       businessLogicModule,
       loggingModule,
       kafkaModule
   )
   ```

2. **Создать функцию инициализации Koin**
   ```kotlin
   fun initKoin(): KoinApplication {
       return startKoin {
           printLogger()
           modules(allModules)
       }
   }
   ```

**Критерии готовности**:
- Koin успешно инициализируется
- Все зависимости резолвятся
- Unit тесты с Koin проходят

---

### Этап 7: Application (точка входа)

**Цель**: Создать главный класс приложения с lifecycle management

**Задачи**:

1. **Создать Application.kt**
   ```kotlin
   class KafkaApplication(
       private val consumer: KafkaRequestConsumer,
       private val producer: ResponseProducer,
       private val logger: ArbScanLogWrapper
   ) {

       fun start() {
           logger.info(
               msg = "Starting Arbitrage Scanner Kafka Application",
               marker = "APP"
           )

           // Регистрация shutdown hook
           Runtime.getRuntime().addShutdownHook(Thread {
               runBlocking {
                   shutdown()
               }
           })

           // Запуск consumer
           consumer.start()

           logger.info(
               msg = "Arbitrage Scanner Kafka Application started",
               marker = "APP"
           )
       }

       suspend fun shutdown() {
           logger.info(
               msg = "Shutting down Arbitrage Scanner Kafka Application",
               marker = "APP"
           )

           try {
               // 1. Остановить consumer
               consumer.shutdown()

               // 2. Закрыть producer
               producer.close()

               logger.info(
                   msg = "Arbitrage Scanner Kafka Application shut down gracefully",
                   marker = "APP"
               )
           } catch (e: Exception) {
               logger.error(
                   msg = "Error during shutdown",
                   marker = "APP",
                   e = e
               )
           }
       }
   }

   fun main() {
       // Инициализация Koin
       val koinApp = initKoin()
       val koin = koinApp.koin

       // Получение зависимостей
       val consumer = koin.get<KafkaRequestConsumer>()
       val producer = koin.get<ResponseProducer>()
       val loggerProvider = koin.get<ArbScanLoggerProvider>()
       val logger = loggerProvider.logger("main")

       // Создание и запуск приложения
       val application = KafkaApplication(
           consumer = consumer,
           producer = producer,
           logger = logger
       )

       try {
           application.start()

           // Держим приложение запущенным
           Thread.currentThread().join()

       } catch (e: InterruptedException) {
           logger.info(msg = "Application interrupted", marker = "APP")
       } catch (e: Exception) {
           logger.error(msg = "Application error", marker = "APP", e = e)
           throw e
       }
   }
   ```

**Критерии готовности**:
- Приложение запускается без ошибок
- Consumer начинает обрабатывать сообщения
- Shutdown hook корректно завершает приложение
- Логирование работает на всех этапах

---

### Этап 8: Интеграционные тесты

**Цель**: Создать тесты для проверки работы всей системы

**Задачи**:

1. **Создать docker-compose для локального Kafka**
   ```yaml
   # docker/docker-compose-kafka.yml

   version: '3.8'

   services:
     zookeeper:
       image: confluentinc/cp-zookeeper:7.8.0
       environment:
         ZOOKEEPER_CLIENT_PORT: 2181
         ZOOKEEPER_TICK_TIME: 2000
       ports:
         - "2181:2181"

     kafka:
       image: confluentinc/cp-kafka:7.8.0
       depends_on:
         - zookeeper
       ports:
         - "9092:9092"
       environment:
         KAFKA_BROKER_ID: 1
         KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
         KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
         KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
         KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"
   ```

2. **Создать KafkaApplicationTest.kt**
   ```kotlin
   class KafkaApplicationTest {

       companion object {
           @Container
           val kafka = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.8.0"))
       }

       @Test
       fun `should process read request and send response`() = runBlocking {
           // Given
           val config = createTestConfig(kafka.bootstrapServers)
           val koin = startKoin {
               modules(allModules)
               // Override config
           }

           val testProducer = createTestProducer(kafka.bootstrapServers)
           val testConsumer = createTestConsumer(kafka.bootstrapServers)

           // When
           val correlationId = UUID.randomUUID().toString()
           val request = ArbitrageOpportunityReadRequest(id = "test-id")

           sendTestRequest(testProducer, correlationId, request)

           // Then
           val response = receiveTestResponse(testConsumer, correlationId, timeout = 5000)

           assertNotNull(response)
           assertTrue(response is ArbitrageOpportunityReadResponse)
       }

       @Test
       fun `should process search request and send response`() = runBlocking {
           // Similar test for search
       }

       @Test
       fun `should handle serialization errors gracefully`() = runBlocking {
           // Test error handling
       }
   }
   ```

3. **Создать KafkaMessageProcessorTest.kt**
   ```kotlin
   class KafkaMessageProcessorTest {

       @Test
       fun `should process valid read request`() = runBlocking {
           // Given
           val mockBusinessLogic = mockk<BusinessLogicProcessor>()
           val mockProducer = mockk<ResponseProducer>()

           val processor = KafkaMessageProcessor(
               businessLogicProcessor = mockBusinessLogic,
               responseProducer = mockProducer,
               json = Json { ... },
               loggerProvider = mockk()
           )

           // When
           val message = KafkaMessage(
               correlationId = "test-id",
               requestType = "READ",
               timestamp = System.currentTimeMillis(),
               payload = """{"id":"test-123"}"""
           )

           processor.processMessage(message)

           // Then
           coVerify { mockBusinessLogic.exec(any()) }
           coVerify { mockProducer.sendResponse(eq("test-id"), any()) }
       }
   }
   ```

**Критерии готовности**:
- Интеграционные тесты запускаются с Testcontainers
- Все сценарии обработки покрыты тестами
- Тесты проходят стабильно
- Code coverage > 80%

---

### Этап 9: Документация и примеры использования

**Цель**: Создать документацию для использования модуля

**Задачи**:

1. **Создать README.md для модуля**
   ```markdown
   # Arbitrage Scanner Kafka Transport

   Асинхронный транспортный модуль для Arbitrage Scanner, использующий Apache Kafka.

   ## Запуск

   ### Локально с Docker Kafka

   1. Запустить Kafka:
      ```bash
      docker-compose -f docker/docker-compose-kafka.yml up -d
      ```

   2. Создать топики:
      ```bash
      docker exec -it kafka kafka-topics --create \
        --topic arbitrage-scanner-requests \
        --bootstrap-server localhost:9092 \
        --partitions 3 \
        --replication-factor 1

      docker exec -it kafka kafka-topics --create \
        --topic arbitrage-scanner-responses \
        --bootstrap-server localhost:9092 \
        --partitions 3 \
        --replication-factor 1
      ```

   3. Запустить приложение:
      ```bash
      ./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:run
      ```

   ## Конфигурация

   Конфигурация находится в `application.yaml`. Основные параметры:

   - `kafka.bootstrap-servers` - адрес Kafka брокера
   - `kafka.group-id` - consumer group ID
   - `kafka.consumer.concurrency` - количество параллельных обработчиков

   ## Отправка запросов

   ### Через kafka-console-producer

   ```bash
   docker exec -it kafka kafka-console-producer \
     --topic arbitrage-scanner-requests \
     --bootstrap-server localhost:9092 \
     --property "parse.key=true" \
     --property "key.separator=:"

   # В интерактивном режиме вводим:
   test-correlation-id:{"requestType":"read","id":"123"}
   ```

   ### Через Kafka client (Kotlin)

   ```kotlin
   val producer = KafkaProducer<String, String>(props)

   val correlationId = UUID.randomUUID().toString()
   val request = ArbitrageOpportunityReadRequest(id = "123")

   val record = ProducerRecord(
       "arbitrage-scanner-requests",
       correlationId,
       json.encodeToString(request)
   ).apply {
       headers().add("correlation-id", correlationId.toByteArray())
       headers().add("request-type", "READ".toByteArray())
   }

   producer.send(record).get()
   ```

   ## Прием ответов

   ```bash
   docker exec -it kafka kafka-console-consumer \
     --topic arbitrage-scanner-responses \
     --bootstrap-server localhost:9092 \
     --from-beginning \
     --property "print.key=true" \
     --property "print.headers=true"
   ```
   ```

2. **Создать примеры использования**
   ```kotlin
   // examples/KafkaClientExample.kt

   fun main() = runBlocking {
       val config = mapOf(
           ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
           // ...
       )

       val producer = KafkaProducer<String, String>(config)
       val consumer = KafkaConsumer<String, String>(config)

       // Отправка запроса
       val correlationId = UUID.randomUUID().toString()
       sendReadRequest(producer, correlationId, "opportunity-123")

       // Прием ответа
       val response = receiveResponse(consumer, correlationId, timeout = 5000)
       println("Response: $response")

       producer.close()
       consumer.close()
   }
   ```

**Критерии готовности**:
- README содержит все необходимые инструкции
- Примеры кода работают
- Документация покрывает основные use cases

---

### Этап 10: Тестирование и финализация

**Цель**: Провести комплексное тестирование и подготовить к production

**Задачи**:

1. **Ручное тестирование**
   - Запустить Kafka локально
   - Отправить различные типы запросов
   - Проверить корректность ответов
   - Проверить логирование
   - Проверить graceful shutdown

2. **Нагрузочное тестирование** (опционально)
   - Отправка множества запросов параллельно
   - Мониторинг производительности
   - Проверка стабильности при высокой нагрузке

3. **Code review и рефакторинг**
   - Проверить соответствие code style
   - Оптимизировать код
   - Добавить недостающие комментарии

4. **Обновить общую документацию проекта**
   - Добавить информацию о Kafka модуле в ARCHITECTURE.md
   - Обновить CLAUDE.md с командами для Kafka модуля

**Критерии готовности**:
- Все тесты проходят: `./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:test`
- Приложение запускается без ошибок
- Ручное тестирование пройдено
- Code review завершен
- Документация обновлена

---

## 5. Затрагиваемые модули

### Модули для изменения

**Создаются новые**:
- `arbitrage-scanner-kafka` - полностью новый модуль

**Изменяются существующие**:
- `arbitrage-scanner/settings.gradle.kts` - добавить новый модуль
- `ARCHITECTURE.md` - документировать Kafka транспорт
- `CLAUDE.md` - добавить команды для работы с Kafka модулем

**Остаются без изменений**:
- `arbitrage-scanner-common` - используется как есть
- `arbitrage-scanner-api-v1` - используется как есть
- `arbitrage-scanner-business-logic` - используется как есть
- `arbitrage-scanner-libs` - используется как есть
- `arbitrage-scanner-ktor` - не изменяется

### Зависимости между модулями

```
arbitrage-scanner-kafka
  ├── arbitrage-scanner-common (доменные модели)
  ├── arbitrage-scanner-api-v1 (транспортные модели и мапперы)
  ├── arbitrage-scanner-business-logic (BusinessLogicProcessor)
  └── arbitrage-scanner-libs:arbitrage-scanner-lib-logging-logback (логирование)
```

---

## 6. Потенциальные риски и способы решения

### Риск 1: Сложность Request-Reply паттерна в Kafka

**Описание**: Kafka не предназначена для синхронного Request-Reply, что может усложнить реализацию.

**Влияние**: Среднее

**Вероятность**: Высокая

**Решение**:
- Использовать correlation ID для сопоставления запросов и ответов
- Клиенты должны подписываться на response топик и фильтровать по correlation ID
- Для первой версии можно не реализовывать клиентскую часть, только серверную
- Рассмотреть использование паттерна "Reply-To" с отдельными топиками для каждого клиента (для будущих версий)

### Риск 2: Управление offset'ами при ошибках

**Описание**: Неправильное управление offset'ами может привести к потере сообщений или их повторной обработке.

**Влияние**: Высокое

**Вероятность**: Средняя

**Решение**:
- Использовать manual commit (enable.auto.commit = false)
- Коммитить offset только после успешной обработки и отправки ответа
- При критических ошибках логировать и пропускать сообщение (skip)
- В будущем добавить Dead Letter Queue для проблемных сообщений

### Риск 3: Graceful Shutdown

**Описание**: Некорректное завершение приложения может привести к потере данных или некорректному состоянию consumer group.

**Влияние**: Среднее

**Вероятность**: Средняя

**Решение**:
- Регистрация shutdown hook для корректного завершения
- Последовательное завершение: consumer → producer → корутины
- Использование wakeup() для прерывания poll()
- Таймауты на shutdown операции

### Риск 4: Производительность при высокой нагрузке

**Описание**: Неоптимальная конфигурация может привести к низкой пропускной способности.

**Влияние**: Среднее

**Вероятность**: Средняя

**Решение**:
- Параллельная обработка через concurrency setting
- Batch processing сообщений
- Настройка max.poll.records для оптимального размера batch
- Асинхронная обработка через корутины
- Мониторинг и тюнинг producer/consumer параметров

### Риск 5: Совместимость версий Kafka

**Описание**: Разные версии Kafka могут иметь несовместимые API или поведение.

**Влияние**: Низкое

**Вероятность**: Низкая

**Решение**:
- Использовать стабильную версию Kafka Clients (3.8.1)
- Документировать минимальную поддерживаемую версию Kafka (2.8+)
- Использовать только стабильные API без deprecated методов

### Риск 6: Сложность отладки

**Описание**: Асинхронная природа Kafka усложняет отладку проблем.

**Влияние**: Среднее

**Вероятность**: Высокая

**Решение**:
- Детальное логирование на всех этапах
- Логирование correlation ID во всех сообщениях
- Использование структурированных логов с маркерами
- Создание утилит для проверки состояния consumer group
- Документирование типичных проблем и их решений

---

## 7. Критерии готовности проекта

### Must Have (обязательные требования)

- [x] Модуль успешно компилируется и собирается
- [x] Consumer успешно читает сообщения из Kafka
- [x] Producer успешно отправляет сообщения в Kafka
- [x] Реализована обработка Read запросов
- [x] Реализована обработка Search запросов
- [x] Используется BusinessLogicProcessor из существующего модуля
- [x] Используются мапперы fromTransport/toTransport
- [x] Корректная обработка ошибок
- [x] Логирование на всех этапах
- [x] Graceful shutdown
- [x] Unit тесты с coverage > 80%
- [x] Интеграционные тесты с Testcontainers
- [x] Конфигурация через application.yaml
- [x] Koin DI настроен
- [x] README с инструкциями по запуску

### Should Have (желательные требования)

- [ ] Нагрузочные тесты
- [ ] Метрики производительности
- [ ] Примеры клиентского кода
- [ ] Docker образ приложения
- [ ] Helm chart для Kubernetes (опционально)

### Could Have (опциональные требования)

- [ ] Dead Letter Queue для ошибочных сообщений
- [ ] Retry механизм для transient ошибок
- [ ] Health check endpoint
- [ ] Prometheus metrics
- [ ] Distributed tracing (OpenTelemetry)

---

## 8. Следующие шаги после завершения

1. **Создание клиентской библиотеки**
   - Модуль `arbitrage-scanner-kafka-client` для отправки запросов
   - Упрощенное API для Request-Reply паттерна

2. **Мониторинг и метрики**
   - Интеграция с Prometheus
   - Dashboards в Grafana
   - Алерты на критические ошибки

3. **Production-ready улучшения**
   - Dead Letter Queue
   - Circuit Breaker для resilience
   - Rate limiting
   - Security (SSL/TLS, SASL)

4. **Оптимизация производительности**
   - Batch processing improvements
   - Compression tuning
   - Partitioning strategy
   - Consumer group scaling

5. **Документация**
   - Operational runbook
   - Troubleshooting guide
   - Architecture Decision Records (ADRs)

---

## 9. Временная оценка

| Этап | Описание | Оценка времени |
|------|----------|----------------|
| 1 | Настройка проекта и зависимостей | 2 часа |
| 2 | Конфигурация приложения | 2 часа |
| 3 | Kafka Producer | 3 часа |
| 4 | Kafka Message Processor | 4 часа |
| 5 | Kafka Consumer | 4 часа |
| 6 | Koin DI Configuration | 1 час |
| 7 | Application (точка входа) | 2 часа |
| 8 | Интеграционные тесты | 4 часа |
| 9 | Документация | 2 часа |
| 10 | Тестирование и финализация | 4 часа |
| **Итого** | | **28 часов** (~4 рабочих дня) |

---

## 10. Команды для разработки

### Сборка
```bash
# Собрать модуль
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:build

# Собрать все модули
./gradlew build
```

### Тестирование
```bash
# Запустить все тесты модуля
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:test

# Запустить интеграционные тесты
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:integrationTest

# Запустить с coverage
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:test jacocoTestReport
```

### Запуск
```bash
# Запустить приложение
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:run

# Запустить с конкретным config
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:run --args="--config=/path/to/config.yaml"
```

### Docker
```bash
# Запустить локальный Kafka
docker-compose -f arbitrage-scanner/arbitrage-scanner-kafka/docker/docker-compose-kafka.yml up -d

# Остановить Kafka
docker-compose -f arbitrage-scanner/arbitrage-scanner-kafka/docker/docker-compose-kafka.yml down

# Создать топики
docker exec -it kafka kafka-topics --create \
  --topic arbitrage-scanner-requests \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 1

docker exec -it kafka kafka-topics --create \
  --topic arbitrage-scanner-responses \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 1

# Просмотр сообщений в топике
docker exec -it kafka kafka-console-consumer \
  --topic arbitrage-scanner-responses \
  --bootstrap-server localhost:9092 \
  --from-beginning
```

### Проверка кода
```bash
# Проверить code style
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:detekt

# Проверить все
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:check
```

---

## 11. Заключение

Данный план описывает полную реализацию модуля `arbitrage-scanner-kafka` с учетом всех архитектурных принципов проекта. Модуль будет:

1. **Переиспользовать существующую инфраструктуру**: BusinessLogicProcessor, мапперы, API модели, логирование
2. **Следовать архитектурным паттернам**: Clean Architecture, DDD, SOLID, Context Object, Strategy
3. **Обеспечивать надежность**: Graceful shutdown, error handling, manual offset management
4. **Быть производительным**: Корутины, parallel processing, batch handling
5. **Быть тестируемым**: Unit тесты, интеграционные тесты с Testcontainers
6. **Быть документированным**: README, примеры, комментарии в коде

Модуль станет альтернативным транспортом для Arbitrage Scanner, позволяя использовать асинхронную обработку через Kafka вместо синхронного HTTP API.