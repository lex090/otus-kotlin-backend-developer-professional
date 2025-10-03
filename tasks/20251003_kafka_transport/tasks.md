# Атомарные подзадачи для реализации Kafka транспорта Arbitrage Scanner

> Дата создания: 2025-10-03
> Проект: Arbitrage Scanner
> Фича: Асинхронный транспорт на базе Apache Kafka

## Статус выполнения задач

- [ ] - Задача не выполнена
- [x] - Задача выполнена

---

## Этап 1: Создание структуры модуля и базовая конфигурация

### Subtask-1: Добавление версий Kafka зависимостей в libs.versions.toml

**Цель и обоснование:**
Перед созданием модуля необходимо определить версии используемых библиотек Kafka в централизованном каталоге версий проекта. Это обеспечит единообразие версий и упростит управление зависимостями.

**Описание:**
Добавить в файл `gradle/libs.versions.toml` версии для:
- Apache Kafka clients (3.6.1)
- Typesafe Config для HOCON конфигурации (1.4.3)
- Testcontainers Kafka для интеграционных тестов (1.19.7)
- MockK для unit тестов (1.13.10)

**Архитектурный слой:** Infrastructure - управление зависимостями

**Критерии выполнения:**
- В секции `[versions]` добавлены версии: `kafka-clients`, `typesafe-config`, `testcontainers`, `mockk`
- В секции `[libraries]` добавлены library definitions для всех зависимостей
- Версии соответствуют рекомендуемым в плане

**Критерии приемки:**
- Файл `gradle/libs.versions.toml` корректно парсится
- Проект компилируется командой `./gradlew build`
- Зависимости доступны для использования в модулях

**Зависимости:** Нет

**Команды для проверки:**
```bash
./gradlew build --dry-run
cat gradle/libs.versions.toml | grep -A 5 "kafka-clients"
```

---

### Subtask-2: Создание структуры директорий модуля arbitrage-scanner-kafka

**Цель и обоснование:**
Создать физическую структуру нового модуля с необходимыми директориями для исходного кода, ресурсов и тестов. Структура должна соответствовать архитектурным принципам проекта.

**Описание:**
Создать следующую структуру директорий:
```
arbitrage-scanner/arbitrage-scanner-kafka/
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── com/arbitrage/scanner/
│   │   │       ├── kafka/
│   │   │       │   └── config/
│   │   │       └── processors/
│   │   └── resources/
│   └── test/
│       ├── kotlin/
│       │   └── com/arbitrage/scanner/
│       └── resources/
```

**Архитектурный слой:** Infrastructure - структура проекта

**Критерии выполнения:**
- Все директории созданы согласно указанной структуре
- Структура пакетов следует convention проекта `com.arbitrage.scanner`
- Созданы отдельные директории для production и test кода

**Критерии приемки:**
- Команда `tree arbitrage-scanner/arbitrage-scanner-kafka` показывает правильную структуру
- Структура соответствует структуре модуля `arbitrage-scanner-ktor`

**Зависимости:** Subtask-1

**Команды для проверки:**
```bash
tree arbitrage-scanner/arbitrage-scanner-kafka -L 5
ls -la arbitrage-scanner/arbitrage-scanner-kafka/src/main/kotlin/com/arbitrage/scanner/
```

---

### Subtask-3: Создание build.gradle.kts для модуля arbitrage-scanner-kafka

**Цель и обоснование:**
Настроить build скрипт модуля с применением кастомного build plugin, подключением необходимых зависимостей и конфигурацией application plugin для запуска приложения.

**Описание:**
Создать файл `arbitrage-scanner/arbitrage-scanner-kafka/build.gradle.kts` с:
- Применением плагина `build.plugin.jvm`
- Применением `application` plugin
- Применением `kotlinx-serialization` plugin
- Зависимостями на внутренние модули:
  - `arbitrage-scanner-common`
  - `arbitrage-scanner-api-v1`
  - `arbitrage-scanner-business-logic`
  - `arbitrage-scanner-lib-logging-logback`
- Зависимостями на внешние библиотеки:
  - Apache Kafka clients
  - Kotlinx coroutines
  - Kotlinx serialization JSON
  - Typesafe Config
  - Koin core
- Тестовыми зависимостями:
  - kotlin-test
  - testcontainers-kafka
  - kotlinx-coroutines-test
  - mockk

**Архитектурный слой:** Infrastructure - конфигурация сборки

**Критерии выполнения:**
- Файл `build.gradle.kts` создан с корректным синтаксисом Kotlin DSL
- Все зависимости указаны с использованием catalog версий (`libs.`)
- `mainClass` настроен для application plugin (временно указать placeholder)
- Группа и версия проекта установлены согласно стандартам проекта

**Критерии приемки:**
- Проект компилируется командой `./gradlew :arbitrage-scanner-kafka:build`
- Команда `./gradlew :arbitrage-scanner-kafka:dependencies` показывает все зависимости
- Все внутренние модули корректно резолвятся

**Зависимости:** Subtask-1, Subtask-2

**Команды для проверки:**
```bash
./gradlew :arbitrage-scanner-kafka:build
./gradlew :arbitrage-scanner-kafka:dependencies --configuration compileClasspath
```

---

### Subtask-4: Регистрация модуля в settings.gradle.kts

**Цель и обоснование:**
Добавить новый модуль в корневой settings.gradle.kts, чтобы Gradle мог его обнаружить и включить в граф зависимостей проекта.

**Описание:**
В файле `arbitrage-scanner/settings.gradle.kts` добавить:
```kotlin
include("arbitrage-scanner-kafka")
```

Убедиться, что модуль находится в правильной секции вместе с другими модулями arbitrage-scanner.

**Архитектурный слой:** Infrastructure - конфигурация проекта

**Критерии выполнения:**
- Модуль `arbitrage-scanner-kafka` добавлен в `settings.gradle.kts`
- Модуль находится в логической группе с другими arbitrage-scanner модулями

**Критерии приемки:**
- Команда `./gradlew projects` показывает модуль `:arbitrage-scanner-kafka`
- Команда `./gradlew :arbitrage-scanner-kafka:tasks` выводит список задач модуля
- Проект компилируется командой `./gradlew build`

**Зависимости:** Subtask-3

**Команды для проверки:**
```bash
./gradlew projects | grep kafka
./gradlew :arbitrage-scanner-kafka:tasks --group="build"
```

---

## Этап 2: Конфигурация Kafka и Koin DI

### Subtask-5: Создание HOCON конфигурации application.conf

**Цель и обоснование:**
Создать конфигурационный файл для параметров подключения к Kafka, настройки consumer/producer и общих параметров приложения. HOCON формат обеспечивает гибкость конфигурации с поддержкой переменных окружения.

**Описание:**
Создать файл `src/main/resources/application.conf` с конфигурацией:
```hocon
kafka {
  bootstrap.servers = "localhost:9092"
  bootstrap.servers = ${?KAFKA_BOOTSTRAP_SERVERS}

  consumer {
    group.id = "arbitrage-scanner-consumer"
    topics = ["arbitrage-scanner-requests"]
    auto.offset.reset = "earliest"
    enable.auto.commit = false
    max.poll.records = 100
    session.timeout.ms = 30000
    heartbeat.interval.ms = 10000
  }

  producer {
    topic.responses = "arbitrage-scanner-responses"
    acks = "all"
    retries = 3
    enable.idempotence = true
    compression.type = "lz4"
  }
}

application {
  name = "arbitrage-scanner-kafka"
  shutdown.timeout = 30000
}
```

**Архитектурный слой:** Infrastructure - конфигурация приложения

**Критерии выполнения:**
- Файл `application.conf` создан в `src/main/resources/`
- Все параметры Kafka consumer и producer заданы согласно спецификации
- Поддержка переопределения через ENV переменные через синтаксис `${?ENV_VAR}`
- Добавлены комментарии для критичных параметров

**Критерии приемки:**
- Файл валидный HOCON формат (парсится без ошибок)
- Конфигурация содержит все необходимые параметры для Kafka
- Значения по умолчанию подходят для локальной разработки

**Зависимости:** Subtask-4

**Команды для проверки:**
```bash
cat arbitrage-scanner/arbitrage-scanner-kafka/src/main/resources/application.conf
```

---

### Subtask-6: Реализация KafkaProperties для типобезопасной конфигурации

**Цель и обоснование:**
Создать data классы для типобезопасного доступа к конфигурации Kafka вместо работы с raw строками. Это предотвращает ошибки конфигурации и упрощает рефакторинг.

**Описание:**
Создать файл `kafka/config/KafkaProperties.kt` с:
```kotlin
data class KafkaProperties(
    val bootstrapServers: String,
    val consumer: ConsumerProperties,
    val producer: ProducerProperties
)

data class ConsumerProperties(
    val groupId: String,
    val topics: List<String>,
    val autoOffsetReset: String,
    val enableAutoCommit: Boolean,
    val maxPollRecords: Int,
    val sessionTimeoutMs: Int,
    val heartbeatIntervalMs: Int
)

data class ProducerProperties(
    val topicResponses: String,
    val acks: String,
    val retries: Int,
    val enableIdempotence: Boolean,
    val compressionType: String
)

data class ApplicationProperties(
    val name: String,
    val shutdownTimeout: Long
)
```

Реализовать функцию загрузки:
```kotlin
fun loadKafkaProperties(): KafkaProperties {
    val config = ConfigFactory.load()
    // Парсинг HOCON конфига в data классы
}

fun loadApplicationProperties(): ApplicationProperties {
    val config = ConfigFactory.load()
    // Парсинг application секции
}
```

**Архитектурный слой:** Infrastructure - конфигурация

**Критерии выполнения:**
- Data классы `KafkaProperties`, `ConsumerProperties`, `ProducerProperties`, `ApplicationProperties` созданы
- Функции `loadKafkaProperties()` и `loadApplicationProperties()` корректно парсят HOCON конфиг
- Обработка ошибок конфигурации с понятными сообщениями
- Все поля конфигурации из `application.conf` представлены в data классах

**Критерии приемки:**
- Проект компилируется командой `./gradlew :arbitrage-scanner-kafka:build`
- Конфигурация успешно загружается при старте приложения
- Написан unit тест `KafkaPropertiesTest` для проверки загрузки конфигурации

**Зависимости:** Subtask-5

**Команды для проверки:**
```bash
./gradlew :arbitrage-scanner-kafka:build
./gradlew :arbitrage-scanner-kafka:test --tests "*KafkaPropertiesTest"
```

---

### Subtask-7: Создание Koin модулей для конфигурации

**Цель и обоснование:**
Настроить Dependency Injection через Koin для управления жизненным циклом компонентов Kafka транспорта. Koin обеспечит слабую связанность и упростит тестирование.

**Описание:**
Создать файл `KoinConfiguration.kt` с модулями:

```kotlin
// Модуль конфигурации
val kafkaConfigModule = module {
    single { loadKafkaProperties() }
    single { loadApplicationProperties() }
}

// Модуль JSON сериализации
val jsonModule = module {
    single {
        Json {
            prettyPrint = false
            isLenient = true
            ignoreUnknownKeys = true
            classDiscriminator = "type"
        }
    }
}

// Модуль логирования
val loggingModule = module {
    single { ArbScanLoggerProvider(::arbScanLoggerLogback) }
}

// Модуль бизнес-логики
val businessLogicProcessorModule = module {
    single<BusinessLogicProcessor> { BusinessLogicProcessorSimpleImpl() }
}

// Агрегирующий модуль
val allModules = listOf(
    kafkaConfigModule,
    jsonModule,
    loggingModule,
    businessLogicProcessorModule
)
```

**Архитектурный слой:** Infrastructure - Dependency Injection

**Критерии выполнения:**
- Файл `KoinConfiguration.kt` создан в корне пакета `com.arbitrage.scanner`
- Все необходимые Koin модули определены
- JSON конфигурация идентична конфигурации из Ktor модуля
- Модули логически разделены по ответственности

**Критерии приемки:**
- Проект компилируется командой `./gradlew :arbitrage-scanner-kafka:build`
- Написан тест `KoinModulesTest` для проверки корректности конфигурации DI
- Все зависимости корректно резолвятся при старте Koin

**Зависимости:** Subtask-6

**Команды для проверки:**
```bash
./gradlew :arbitrage-scanner-kafka:build
./gradlew :arbitrage-scanner-kafka:test --tests "*KoinModulesTest"
```

---

## Этап 3: Сериализация и десериализация Kafka сообщений

### Subtask-8: Реализация KafkaJsonSerializer для отправки ответов

**Цель и обоснование:**
Создать кастомный Kafka сериализатор для преобразования доменных объектов IResponse в JSON байты. Это обеспечит корректную передачу ответов через Kafka топики.

**Описание:**
Создать файл `kafka/KafkaJsonSerializer.kt`:

```kotlin
class KafkaJsonSerializer<T>(
    private val json: Json
) : Serializer<T> {
    override fun configure(configs: Map<String, *>, isKey: Boolean) {
        // No-op
    }

    override fun serialize(topic: String, data: T?): ByteArray? {
        if (data == null) return null

        return try {
            val jsonString = when (data) {
                is IResponse -> json.toResponseJsonString(data)
                else -> json.encodeToString(data)
            }
            jsonString.encodeToByteArray()
        } catch (e: Exception) {
            throw SerializationException("Error serializing data to JSON", e)
        }
    }

    override fun close() {
        // No-op
    }
}
```

**Архитектурный слой:** Infrastructure - сериализация данных

**Критерии выполнения:**
- Класс `KafkaJsonSerializer<T>` реализует интерфейс `org.apache.kafka.common.serialization.Serializer<T>`
- Поддержка полиморфной сериализации через `IResponse.serializer()`
- Обработка null значений
- Обработка исключений сериализации с преобразованием в `SerializationException`

**Критерии приемки:**
- Проект компилируется командой `./gradlew :arbitrage-scanner-kafka:build`
- Написан unit тест `KafkaJsonSerializerTest` с проверкой:
  - Сериализации различных типов IResponse
  - Обработки null значений
  - Корректности JSON формата
- Все существующие тесты проходят

**Зависимости:** Subtask-7

**Команды для проверки:**
```bash
./gradlew :arbitrage-scanner-kafka:build
./gradlew :arbitrage-scanner-kafka:test --tests "*KafkaJsonSerializerTest"
```

---

### Subtask-9: Реализация KafkaJsonDeserializer для чтения запросов

**Цель и обоснование:**
Создать кастомный Kafka десериализатор для преобразования JSON байтов в доменные объекты IRequest. Это обеспечит корректное чтение и валидацию входящих запросов из Kafka топиков.

**Описание:**
Создать файл `kafka/KafkaJsonDeserializer.kt`:

```kotlin
class KafkaJsonDeserializer<T>(
    private val json: Json,
    private val deserializer: DeserializationStrategy<T>
) : Deserializer<T> {
    override fun configure(configs: Map<String, *>, isKey: Boolean) {
        // No-op
    }

    override fun deserialize(topic: String, data: ByteArray?): T? {
        if (data == null) return null

        return try {
            val jsonString = data.decodeToString()
            json.decodeFromString(deserializer, jsonString)
        } catch (e: Exception) {
            throw SerializationException("Error deserializing JSON to object", e)
        }
    }

    override fun close() {
        // No-op
    }
}

// Factory функции для удобного создания
fun createRequestDeserializer(json: Json): KafkaJsonDeserializer<IRequest> {
    return KafkaJsonDeserializer(json, IRequest.serializer())
}
```

**Архитектурный слой:** Infrastructure - десериализация данных

**Критерии выполнения:**
- Класс `KafkaJsonDeserializer<T>` реализует интерфейс `org.apache.kafka.common.serialization.Deserializer<T>`
- Поддержка полиморфной десериализации через `DeserializationStrategy`
- Обработка null значений
- Обработка ошибок десериализации с понятными сообщениями
- Factory функция `createRequestDeserializer()` для создания десериализатора IRequest

**Критерии приемки:**
- Проект компилируется командой `./gradlew :arbitrage-scanner-kafka:build`
- Написан unit тест `KafkaJsonDeserializerTest` с проверкой:
  - Десериализации всех типов IRequest (READ, SEARCH)
  - Обработки null значений
  - Обработки некорректного JSON
  - Обработки JSON с неизвестными полями
- Все существующие тесты проходят

**Зависимости:** Subtask-8

**Команды для проверки:**
```bash
./gradlew :arbitrage-scanner-kafka:build
./gradlew :arbitrage-scanner-kafka:test --tests "*KafkaJsonDeserializerTest"
```

---

## Этап 4: Реализация Kafka Producer Service

### Subtask-10: Создание интерфейса и реализации KafkaProducerService

**Цель и обоснование:**
Реализовать сервис для асинхронной отправки ответов в Kafka топик. Producer должен обеспечить надежную доставку сообщений с retry механизмом и graceful shutdown.

**Описание:**
Создать файл `kafka/KafkaProducerService.kt`:

```kotlin
interface KafkaProducerService {
    suspend fun sendResponse(
        correlationId: String,
        response: IResponse,
        replyToTopic: String
    ): Result<Unit>

    fun close()
}

class KafkaProducerServiceImpl(
    private val config: KafkaProperties,
    private val json: Json,
    private val logger: ArbScanLogWrapper
) : KafkaProducerService {

    private val producer: KafkaProducer<String, IResponse> by lazy {
        createProducer()
    }

    private fun createProducer(): KafkaProducer<String, IResponse> {
        val props = Properties().apply {
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.bootstrapServers)
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaJsonSerializer::class.java.name)
            put(ProducerConfig.ACKS_CONFIG, config.producer.acks)
            put(ProducerConfig.RETRIES_CONFIG, config.producer.retries)
            put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, config.producer.enableIdempotence)
            put(ProducerConfig.COMPRESSION_TYPE_CONFIG, config.producer.compressionType)
        }

        return KafkaProducer(
            props,
            StringSerializer(),
            KafkaJsonSerializer(json)
        )
    }

    override suspend fun sendResponse(
        correlationId: String,
        response: IResponse,
        replyToTopic: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { continuation ->
            try {
                val record = ProducerRecord<String, IResponse>(
                    replyToTopic,
                    correlationId,
                    response
                ).apply {
                    headers().add("correlation-id", correlationId.toByteArray())
                    headers().add("timestamp", System.currentTimeMillis().toString().toByteArray())
                }

                producer.send(record) { metadata, exception ->
                    if (exception != null) {
                        logger.error("Failed to send response for correlationId: $correlationId", exception)
                        continuation.resume(Result.failure(exception))
                    } else {
                        logger.debug("Response sent successfully to ${metadata.topic()}, partition ${metadata.partition()}, offset ${metadata.offset()}")
                        continuation.resume(Result.success(Unit))
                    }
                }
            } catch (e: Exception) {
                logger.error("Error sending response for correlationId: $correlationId", e)
                continuation.resume(Result.failure(e))
            }
        }
    }

    override fun close() {
        try {
            logger.info("Closing Kafka Producer")
            producer.flush()
            producer.close(Duration.ofSeconds(5))
        } catch (e: Exception) {
            logger.error("Error closing Kafka Producer", e)
        }
    }
}
```

**Архитектурный слой:** Infrastructure - интеграция с Kafka

**Критерии выполнения:**
- Интерфейс `KafkaProducerService` определен с методами `sendResponse()` и `close()`
- Класс `KafkaProducerServiceImpl` реализует интерфейс
- Producer настраивается через `KafkaProperties`
- Используется lazy инициализация producer для оптимизации ресурсов
- Асинхронная отправка через `suspendCancellableCoroutine`
- Добавление headers: correlation-id, timestamp
- Логирование успешной отправки и ошибок
- Graceful shutdown с flush и timeout

**Критерии приемки:**
- Проект компилируется командой `./gradlew :arbitrage-scanner-kafka:build`
- Написан unit тест `KafkaProducerServiceTest` с mock проверкой отправки
- Producer корректно закрывается при вызове `close()`
- Все существующие тесты проходят

**Зависимости:** Subtask-9

**Команды для проверки:**
```bash
./gradlew :arbitrage-scanner-kafka:build
./gradlew :arbitrage-scanner-kafka:test --tests "*KafkaProducerServiceTest"
```

---

### Subtask-11: Добавление KafkaProducerService в Koin модуль

**Цель и обоснование:**
Зарегистрировать KafkaProducerService в DI контейнере для автоматического управления жизненным циклом и инъекции в зависимые компоненты.

**Описание:**
Обновить файл `KoinConfiguration.kt`:

```kotlin
val kafkaProducerModule = module {
    single<KafkaProducerService> {
        KafkaProducerServiceImpl(
            config = get(),
            json = get(),
            logger = get<ArbScanLoggerProvider>().logger(KafkaProducerServiceImpl::class)
        )
    }
}

// Обновить allModules
val allModules = listOf(
    kafkaConfigModule,
    jsonModule,
    loggingModule,
    businessLogicProcessorModule,
    kafkaProducerModule  // Добавить
)
```

**Архитектурный слой:** Infrastructure - Dependency Injection

**Критерии выполнения:**
- Модуль `kafkaProducerModule` добавлен в конфигурацию Koin
- KafkaProducerService регистрируется как singleton
- Все зависимости (config, json, logger) корректно инжектятся
- Модуль добавлен в список `allModules`

**Критерии приемки:**
- Проект компилируется командой `./gradlew :arbitrage-scanner-kafka:build`
- Тест `KoinModulesTest` проходит успешно
- KafkaProducerService может быть получен из Koin контейнера
- Все существующие тесты проходят

**Зависимости:** Subtask-10

**Команды для проверки:**
```bash
./gradlew :arbitrage-scanner-kafka:build
./gradlew :arbitrage-scanner-kafka:test --tests "*KoinModulesTest"
```

---

## Этап 5: Реализация Kafka Consumer Service

### Subtask-12: Создание интерфейса и базовой реализации KafkaConsumerService

**Цель и обоснование:**
Реализовать сервис для чтения запросов из Kafka топика. Consumer должен обеспечить надежное чтение сообщений с manual commit и корректной обработкой ошибок.

**Описание:**
Создать файл `kafka/KafkaConsumerService.kt`:

```kotlin
interface KafkaConsumerService {
    suspend fun start()
    fun stop()
}

class KafkaConsumerServiceImpl(
    private val config: KafkaProperties,
    private val json: Json,
    private val messageProcessor: MessageProcessor,
    private val logger: ArbScanLogWrapper
) : KafkaConsumerService {

    @Volatile
    private var isRunning = false

    private val consumer: KafkaConsumer<String, IRequest> by lazy {
        createConsumer()
    }

    private fun createConsumer(): KafkaConsumer<String, IRequest> {
        val props = Properties().apply {
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.bootstrapServers)
            put(ConsumerConfig.GROUP_ID_CONFIG, config.consumer.groupId)
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaJsonDeserializer::class.java.name)
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, config.consumer.autoOffsetReset)
            put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, config.consumer.enableAutoCommit)
            put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, config.consumer.maxPollRecords)
            put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, config.consumer.sessionTimeoutMs)
            put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, config.consumer.heartbeatIntervalMs)
        }

        return KafkaConsumer(
            props,
            StringDeserializer(),
            createRequestDeserializer(json)
        )
    }

    override suspend fun start() = withContext(Dispatchers.IO) {
        isRunning = true
        consumer.subscribe(config.consumer.topics)
        logger.info("Kafka Consumer started, subscribed to topics: ${config.consumer.topics}")

        try {
            while (isRunning) {
                val records = consumer.poll(Duration.ofMillis(1000))

                for (record in records) {
                    processRecord(record)
                }

                if (records.count() > 0) {
                    consumer.commitSync()
                }
            }
        } catch (e: WakeupException) {
            logger.info("Consumer wakeup called, shutting down")
        } catch (e: Exception) {
            logger.error("Error in consumer loop", e)
            throw e
        } finally {
            close()
        }
    }

    private suspend fun processRecord(record: ConsumerRecord<String, IRequest>) {
        // Будет реализовано в следующей задаче
    }

    override fun stop() {
        logger.info("Stopping Kafka Consumer")
        isRunning = false
        consumer.wakeup()
    }

    private fun close() {
        try {
            logger.info("Closing Kafka Consumer")
            consumer.close(Duration.ofSeconds(5))
        } catch (e: Exception) {
            logger.error("Error closing Kafka Consumer", e)
        }
    }
}
```

**Архитектурный слой:** Infrastructure - интеграция с Kafka

**Критерии выполнения:**
- Интерфейс `KafkaConsumerService` определен с методами `start()` и `stop()`
- Класс `KafkaConsumerServiceImpl` реализует интерфейс
- Consumer настраивается через `KafkaProperties`
- Реализован бесконечный цикл poll() с корректной обработкой WakeupException
- Manual commit после успешной обработки batch сообщений
- Graceful shutdown через `wakeup()` и флаг `isRunning`
- Логирование старта, остановки и ошибок

**Критерии приемки:**
- Проект компилируется командой `./gradlew :arbitrage-scanner-kafka:build`
- Consumer успешно подписывается на топики
- Graceful shutdown работает корректно
- Все существующие тесты проходят

**Зависимости:** Subtask-11

**Команды для проверки:**
```bash
./gradlew :arbitrage-scanner-kafka:build
./gradlew :arbitrage-scanner-kafka:test
```

---

### Subtask-13: Реализация обработки записей в KafkaConsumerService

**Цель и обоснование:**
Добавить логику извлечения метаданных из Kafka записи (correlation ID, reply-to топик, command type) и передачи в MessageProcessor для обработки бизнес-логики.

**Описание:**
Обновить метод `processRecord()` в `KafkaConsumerServiceImpl`:

```kotlin
private suspend fun processRecord(record: ConsumerRecord<String, IRequest>) {
    val correlationId = record.key() ?: UUID.randomUUID().toString()
    val request = record.value() ?: run {
        logger.warn("Received null request for correlationId: $correlationId")
        return
    }

    // Извлечение metadata из headers
    val replyToTopic = record.headers()
        .lastHeader("reply-to")
        ?.value()
        ?.decodeToString()
        ?: config.producer.topicResponses

    val commandType = record.headers()
        .lastHeader("command-type")
        ?.value()
        ?.decodeToString()

    logger.info("Processing message: correlationId=$correlationId, command=$commandType, replyTo=$replyToTopic")

    try {
        messageProcessor.processMessage(
            correlationId = correlationId,
            request = request,
            replyToTopic = replyToTopic
        ).onFailure { error ->
            logger.error("Failed to process message for correlationId: $correlationId", error)
            // В будущем можно отправить в DLQ
        }
    } catch (e: Exception) {
        logger.error("Unexpected error processing message for correlationId: $correlationId", e)
        // В будущем можно отправить в DLQ
    }
}
```

**Архитектурный слой:** Infrastructure - обработка сообщений

**Критерии выполнения:**
- Извлечение correlation ID из ключа записи (fallback на UUID если отсутствует)
- Извлечение reply-to топика из headers (fallback на дефолтный топик)
- Извлечение command-type из headers для логирования
- Обработка null значений request
- Передача в `MessageProcessor.processMessage()`
- Логирование начала обработки с контекстной информацией
- Обработка ошибок с логированием (подготовка к DLQ)

**Критерии приемки:**
- Проект компилируется командой `./gradlew :arbitrage-scanner-kafka:build`
- Все метаданные корректно извлекаются из Kafka записи
- Обработка null значений не приводит к падению consumer
- Все существующие тесты проходят

**Зависимости:** Subtask-12

**Команды для проверки:**
```bash
./gradlew :arbitrage-scanner-kafka:build
./gradlew :arbitrage-scanner-kafka:test
```

---

### Subtask-14: Добавление KafkaConsumerService в Koin модуль

**Цель и обоснование:**
Зарегистрировать KafkaConsumerService в DI контейнере для управления жизненным циклом и обеспечения доступности в Application.

**Описание:**
Обновить файл `KoinConfiguration.kt`:

```kotlin
val kafkaConsumerModule = module {
    single<KafkaConsumerService> {
        KafkaConsumerServiceImpl(
            config = get(),
            json = get(),
            messageProcessor = get(),
            logger = get<ArbScanLoggerProvider>().logger(KafkaConsumerServiceImpl::class)
        )
    }
}

// Обновить allModules
val allModules = listOf(
    kafkaConfigModule,
    jsonModule,
    loggingModule,
    businessLogicProcessorModule,
    kafkaProducerModule,
    kafkaConsumerModule  // Добавить
)
```

**Архитектурный слой:** Infrastructure - Dependency Injection

**Критерии выполнения:**
- Модуль `kafkaConsumerModule` добавлен в конфигурацию Koin
- KafkaConsumerService регистрируется как singleton
- Все зависимости корректно инжектятся (включая messageProcessor)
- Модуль добавлен в список `allModules`

**Критерии приемки:**
- Проект компилируется командой `./gradlew :arbitrage-scanner-kafka:build`
- Тест `KoinModulesTest` проходит успешно
- KafkaConsumerService может быть получен из Koin контейнера
- Все существующие тесты проходят

**Зависимости:** Subtask-13

**Команды для проверки:**
```bash
./gradlew :arbitrage-scanner-kafka:build
./gradlew :arbitrage-scanner-kafka:test --tests "*KoinModulesTest"
```

---

## Этап 6: Реализация процессора сообщений (Business Logic Integration)

### Subtask-15: Создание MessageProcessor для обработки запросов

**Цель и обоснование:**
Реализовать процессор сообщений, который адаптирует бизнес-логику из Ktor модуля для работы с Kafka. Процессор должен обеспечить идентичную обработку запросов через те же mappers и BusinessLogicProcessor.

**Описание:**
Создать файл `processors/MessageProcessor.kt`:

```kotlin
class MessageProcessor(
    private val businessLogicProcessor: BusinessLogicProcessor,
    private val kafkaProducerService: KafkaProducerService,
    private val loggerProvider: ArbScanLoggerProvider,
    private val json: Json
) {
    private val logger = loggerProvider.logger(MessageProcessor::class)

    suspend fun processMessage(
        correlationId: String,
        request: IRequest,
        replyToTopic: String
    ): Result<Unit> = runCatching {
        val marker = Markers.append("correlationId", correlationId)

        // Создание контекста аналогично Ktor модулю
        val context = Context().apply {
            requestId = correlationId
            startTimestamp = Clock.System.now()
        }

        logger.info(marker, "BIZ | Started processing request: $correlationId, type: ${request::class.simpleName}")

        try {
            // Заполнение контекста из транспортного объекта
            request.fromTransport(context)

            // Выполнение бизнес-логики
            businessLogicProcessor.exec(context)

            logger.info(
                marker,
                "BIZ | Finished processing request: $correlationId, state: ${context.state}, " +
                "errors: ${context.internalErrors.size}"
            )

            // Конвертация в транспортный ответ
            val response = context.toTransport()

            // Отправка ответа через Kafka Producer
            kafkaProducerService.sendResponse(
                correlationId = correlationId,
                response = response,
                replyToTopic = replyToTopic
            ).getOrThrow()

        } catch (throwable: Throwable) {
            logger.error(marker, "BIZ | Error processing request: $correlationId", throwable)

            // Добавление ошибки в контекст
            context.internalErrors.add(
                InternalError(
                    code = "PROCESSING_ERROR",
                    group = "business-logic",
                    message = throwable.message ?: "Unknown error",
                    exception = throwable
                )
            )

            // Отправка ответа с ошибкой
            val errorResponse = context.toTransport()
            kafkaProducerService.sendResponse(
                correlationId = correlationId,
                response = errorResponse,
                replyToTopic = replyToTopic
            ).getOrThrow()

            throw throwable
        }
    }
}
```

**Архитектурный слой:** Application - обработка запросов

**Критерии выполнения:**
- Класс `MessageProcessor` создан с зависимостями на BusinessLogicProcessor, KafkaProducerService, LoggerProvider, Json
- Метод `processMessage()` возвращает `Result<Unit>` для обработки ошибок
- Создание контекста с `requestId` (correlation ID) и `startTimestamp`
- Использование тех же mappers: `fromTransport()` и `toTransport()`
- Вызов `businessLogicProcessor.exec(context)` аналогично Ktor модулю
- Логирование с marker "BIZ" и correlation ID
- Обработка исключений с добавлением в `context.internalErrors`
- Отправка ответа через `kafkaProducerService` даже в случае ошибки

**Критерии приемки:**
- Проект компилируется командой `./gradlew :arbitrage-scanner-kafka:build`
- Обработка запросов идентична Ktor модулю
- Написан unit тест `MessageProcessorTest` с mock зависимостями
- Все существующие тесты проходят

**Зависимости:** Subtask-14

**Команды для проверки:**
```bash
./gradlew :arbitrage-scanner-kafka:build
./gradlew :arbitrage-scanner-kafka:test --tests "*MessageProcessorTest"
```

---

### Subtask-16: Добавление MessageProcessor в Koin модуль

**Цель и обоснование:**
Зарегистрировать MessageProcessor в DI контейнере для автоматического управления зависимостями и инъекции в KafkaConsumerService.

**Описание:**
Обновить файл `KoinConfiguration.kt`:

```kotlin
val messageProcessorModule = module {
    single {
        MessageProcessor(
            businessLogicProcessor = get(),
            kafkaProducerService = get(),
            loggerProvider = get(),
            json = get()
        )
    }
}

// Обновить allModules (важно: messageProcessorModule должен быть ДО kafkaConsumerModule)
val allModules = listOf(
    kafkaConfigModule,
    jsonModule,
    loggingModule,
    businessLogicProcessorModule,
    kafkaProducerModule,
    messageProcessorModule,  // Добавить перед kafkaConsumerModule
    kafkaConsumerModule
)
```

**Архитектурный слой:** Infrastructure - Dependency Injection

**Критерии выполнения:**
- Модуль `messageProcessorModule` добавлен в конфигурацию Koin
- MessageProcessor регистрируется как singleton
- Все зависимости корректно инжектятся
- Модуль добавлен в правильном порядке в `allModules` (до kafkaConsumerModule)

**Критерии приемки:**
- Проект компилируется командой `./gradlew :arbitrage-scanner-kafka:build`
- Тест `KoinModulesTest` проходит успешно
- MessageProcessor может быть получен из Koin контейнера
- KafkaConsumerService получает MessageProcessor через DI
- Все существующие тесты проходят

**Зависимости:** Subtask-15

**Команды для проверки:**
```bash
./gradlew :arbitrage-scanner-kafka:build
./gradlew :arbitrage-scanner-kafka:test --tests "*KoinModulesTest"
```

---

## Этап 7: Application и Lifecycle Management

### Subtask-17: Создание класса Application для управления жизненным циклом

**Цель и обоснование:**
Реализовать главный класс приложения, который управляет запуском и остановкой всех сервисов, обеспечивает graceful shutdown и корректную обработку сигналов завершения.

**Описание:**
Создать файл `Application.kt`:

```kotlin
class KafkaApplication(
    private val kafkaConsumerService: KafkaConsumerService,
    private val kafkaProducerService: KafkaProducerService,
    private val applicationProperties: ApplicationProperties,
    private val logger: ArbScanLogWrapper
) {
    @Volatile
    private var isRunning = false

    suspend fun start() {
        logger.info("Starting ${applicationProperties.name}")
        isRunning = true

        try {
            // Запуск consumer в отдельной корутине
            kafkaConsumerService.start()
        } catch (e: Exception) {
            logger.error("Error starting application", e)
            stop()
            throw e
        }
    }

    fun stop() {
        if (!isRunning) return

        logger.info("Stopping ${applicationProperties.name}")
        isRunning = false

        try {
            // Graceful shutdown: сначала consumer, потом producer
            kafkaConsumerService.stop()

            // Даем время на обработку последних сообщений
            Thread.sleep(1000)

            kafkaProducerService.close()

            logger.info("${applicationProperties.name} stopped successfully")
        } catch (e: Exception) {
            logger.error("Error stopping application", e)
        }
    }
}

suspend fun main() {
    // Инициализация Koin
    val koinApp = startKoin {
        printLogger()
        modules(allModules)
    }

    val logger = koinApp.koin.get<ArbScanLoggerProvider>().logger("Main")
    val app = koinApp.koin.get<KafkaApplication>()

    // Регистрация shutdown hook
    Runtime.getRuntime().addShutdownHook(thread(start = false) {
        logger.info("Shutdown signal received")
        app.stop()
    })

    // Запуск приложения
    try {
        app.start()
    } catch (e: Exception) {
        logger.error("Application failed to start", e)
        koinApp.close()
        exitProcess(1)
    }
}
```

**Архитектурный слой:** Presentation - точка входа приложения

**Критерии выполнения:**
- Класс `KafkaApplication` создан с методами `start()` и `stop()`
- Функция `main()` настраивает Koin и запускает приложение
- Shutdown hook корректно обрабатывает сигналы завершения (SIGTERM, SIGINT)
- Graceful shutdown: остановка consumer → ожидание завершения обработки → закрытие producer
- Логирование всех событий жизненного цикла
- Обработка ошибок с корректным exitProcess(1)

**Критерии приемки:**
- Проект компилируется командой `./gradlew :arbitrage-scanner-kafka:build`
- Приложение запускается через `./gradlew :arbitrage-scanner-kafka:run`
- Graceful shutdown работает при Ctrl+C
- Все существующие тесты проходят

**Зависимости:** Subtask-16

**Команды для проверки:**
```bash
./gradlew :arbitrage-scanner-kafka:build
./gradlew :arbitrage-scanner-kafka:run
# В другом терминале: kill -SIGTERM <PID>
```

---

### Subtask-18: Добавление KafkaApplication в Koin модуль и настройка mainClass

**Цель и обоснование:**
Зарегистрировать KafkaApplication в DI контейнере и настроить application plugin для корректного запуска приложения.

**Описание:**
1. Обновить файл `KoinConfiguration.kt`:

```kotlin
val applicationModule = module {
    single {
        KafkaApplication(
            kafkaConsumerService = get(),
            kafkaProducerService = get(),
            applicationProperties = get(),
            logger = get<ArbScanLoggerProvider>().logger(KafkaApplication::class)
        )
    }
}

// Обновить allModules
val allModules = listOf(
    kafkaConfigModule,
    jsonModule,
    loggingModule,
    businessLogicProcessorModule,
    kafkaProducerModule,
    messageProcessorModule,
    kafkaConsumerModule,
    applicationModule  // Добавить
)
```

2. Обновить `build.gradle.kts`:

```kotlin
application {
    mainClass.set("com.arbitrage.scanner.ApplicationKt")
}
```

**Архитектурный слой:** Infrastructure - конфигурация приложения

**Критерии выполнения:**
- Модуль `applicationModule` добавлен в конфигурацию Koin
- KafkaApplication регистрируется как singleton
- В `build.gradle.kts` установлен правильный `mainClass`
- Модуль добавлен в список `allModules`

**Критерии приемки:**
- Проект компилируется командой `./gradlew :arbitrage-scanner-kafka:build`
- Приложение запускается через `./gradlew :arbitrage-scanner-kafka:run`
- Команда `./gradlew :arbitrage-scanner-kafka:tasks --group=application` показывает run задачу
- Тест `KoinModulesTest` проходит успешно
- Все существующие тесты проходят

**Зависимости:** Subtask-17

**Команды для проверки:**
```bash
./gradlew :arbitrage-scanner-kafka:build
./gradlew :arbitrage-scanner-kafka:tasks --group=application
./gradlew :arbitrage-scanner-kafka:run
```

---

## Этап 8: Docker конфигурация и интеграция

### Subtask-19: Настройка Jib plugin для создания Docker образа

**Цель и обоснование:**
Настроить Jib для автоматической сборки Docker образа без необходимости писать Dockerfile. Jib обеспечивает оптимизированную послойную сборку и кросс-платформенные образы.

**Описание:**
Обновить файл `build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.build.plugin.jvm)
    alias(libs.plugins.kotlinx.serialization)
    application
    id("com.google.cloud.tools.jib") version "3.4.0"
}

jib {
    from {
        image = "eclipse-temurin:21-jre"
        platforms {
            platform {
                architecture = "arm64"
                os = "linux"
            }
            platform {
                architecture = "amd64"
                os = "linux"
            }
        }
    }

    to {
        image = "arbitrage-scanner-kafka"
        tags = setOf(project.version.toString(), "latest")
    }

    container {
        mainClass = "com.arbitrage.scanner.ApplicationKt"
        jvmFlags = listOf(
            "-Xms256m",
            "-Xmx512m",
            "-XX:+UseG1GC",
            "-XX:MaxGCPauseMillis=100"
        )
        ports = listOf("9092")  // Для health check endpoint в будущем
        environment = mapOf(
            "SERVICE_NAME" to "arbitrage-scanner-kafka"
        )
        creationTime = "USE_CURRENT_TIMESTAMP"
    }
}
```

**Архитектурный слой:** Infrastructure - контейнеризация

**Критерии выполнения:**
- Jib plugin версии 3.4.0 применен в `build.gradle.kts`
- Базовый образ: eclipse-temurin:21-jre
- Поддержка multi-arch: arm64 и amd64
- Образ тагируется версией проекта и latest
- JVM флаги оптимизированы для production
- Установлен правильный mainClass

**Критерии приемки:**
- Проект компилируется командой `./gradlew :arbitrage-scanner-kafka:build`
- Docker образ собирается командой `./gradlew :arbitrage-scanner-kafka:jibDockerBuild`
- Образ создается с правильными тегами
- Команда `docker images | grep arbitrage-scanner-kafka` показывает созданный образ

**Зависимости:** Subtask-18

**Команды для проверки:**
```bash
./gradlew :arbitrage-scanner-kafka:build
./gradlew :arbitrage-scanner-kafka:jibDockerBuild
docker images | grep arbitrage-scanner-kafka
docker inspect arbitrage-scanner-kafka:latest | jq '.[0].Config.Env'
```

---

### Subtask-20: Интеграция с docker-compose.minimal.yml

**Цель и обоснование:**
Добавить сервис Kafka транспорта в docker-compose для возможности запуска полного стека приложения с зависимостями (Kafka, Fluent Bit).

**Описание:**
Обновить файл `docker-compose.minimal.yml`:

```yaml
services:
  # ... существующие сервисы ...

  arbitrage-scanner-kafka:
    image: arbitrage-scanner-kafka:${VERSION:-latest}
    container_name: arbitrage-kafka-app
    depends_on:
      - kafka
      - fluent-bit
    environment:
      - KAFKA_BOOTSTRAP_SERVERS=kafka:9092
      - LOGS_FB_HOSTS=fluent-bit
      - LOGS_FB_PORT=24224
      - SERVICE_NAME=arbitrage-scanner-kafka
    networks:
      - arbitrage-network
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "true"]  # Заглушка, будет заменена на реальный health check
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s
```

Создать скрипт `run-kafka-app.sh`:

```bash
#!/bin/bash
set -e

echo "Building Kafka application Docker image..."
./gradlew :arbitrage-scanner-kafka:jibDockerBuild

echo "Starting Kafka application with docker-compose..."
docker-compose -f docker-compose.minimal.yml up -d arbitrage-scanner-kafka

echo "Kafka application started successfully"
echo "Logs: docker-compose -f docker-compose.minimal.yml logs -f arbitrage-scanner-kafka"
```

**Архитектурный слой:** Infrastructure - оркестрация

**Критерии выполнения:**
- Сервис `arbitrage-scanner-kafka` добавлен в `docker-compose.minimal.yml`
- Настроены зависимости на `kafka` и `fluent-bit`
- Переменные окружения корректно передают параметры подключения
- Сервис подключен к сети `arbitrage-network`
- Настроен health check (временная заглушка)
- Создан скрипт `run-kafka-app.sh` с правами на выполнение

**Критерии приемки:**
- Приложение запускается через `./run-kafka-app.sh`
- Контейнер успешно подключается к Kafka
- Логи отправляются в Fluent Bit
- Команда `docker-compose -f docker-compose.minimal.yml ps` показывает running статус

**Зависимости:** Subtask-19

**Команды для проверки:**
```bash
chmod +x run-kafka-app.sh
./run-kafka-app.sh
docker-compose -f docker-compose.minimal.yml ps | grep kafka
docker-compose -f docker-compose.minimal.yml logs -f arbitrage-scanner-kafka | head -20
```

---

## Этап 9: Написание тестов

### Subtask-21: Unit тесты для KafkaJsonSerializer и KafkaJsonDeserializer

**Цель и обоснование:**
Обеспечить корректность сериализации/десериализации всех типов запросов и ответов через автоматизированные тесты.

**Описание:**
Создать файлы тестов:

1. `src/test/kotlin/com/arbitrage/scanner/kafka/KafkaJsonSerializerTest.kt`:
```kotlin
class KafkaJsonSerializerTest {
    private val json = Json { /* конфигурация */ }
    private val serializer = KafkaJsonSerializer<IResponse>(json)

    @Test
    fun `should serialize ArbitrageOpportunityReadResponse`() { /* ... */ }

    @Test
    fun `should serialize ArbitrageOpportunitySearchResponse`() { /* ... */ }

    @Test
    fun `should return null for null input`() { /* ... */ }

    @Test
    fun `should throw SerializationException on error`() { /* ... */ }
}
```

2. `src/test/kotlin/com/arbitrage/scanner/kafka/KafkaJsonDeserializerTest.kt`:
```kotlin
class KafkaJsonDeserializerTest {
    private val json = Json { /* конфигурация */ }
    private val deserializer = createRequestDeserializer(json)

    @Test
    fun `should deserialize ArbitrageOpportunityReadRequest`() { /* ... */ }

    @Test
    fun `should deserialize ArbitrageOpportunitySearchRequest`() { /* ... */ }

    @Test
    fun `should return null for null input`() { /* ... */ }

    @Test
    fun `should handle unknown fields gracefully`() { /* ... */ }

    @Test
    fun `should throw SerializationException on invalid JSON`() { /* ... */ }
}
```

**Архитектурный слой:** Infrastructure - тестирование сериализации

**Критерии выполнения:**
- Тесты для всех типов IRequest (READ, SEARCH)
- Тесты для всех типов IResponse
- Тесты обработки null значений
- Тесты обработки ошибок сериализации
- Тесты корректности JSON формата (полиморфизм)
- Тесты игнорирования неизвестных полей

**Критерии приемки:**
- Все тесты проходят командой `./gradlew :arbitrage-scanner-kafka:test`
- Coverage тестов для serializer/deserializer > 90%
- Тесты проверяют edge cases (null, пустые значения, некорректный JSON)

**Зависимости:** Subtask-9

**Команды для проверки:**
```bash
./gradlew :arbitrage-scanner-kafka:test --tests "*KafkaJsonSerializerTest"
./gradlew :arbitrage-scanner-kafka:test --tests "*KafkaJsonDeserializerTest"
./gradlew :arbitrage-scanner-kafka:test
```

---

### Subtask-22: Unit тесты для MessageProcessor

**Цель и обоснование:**
Проверить корректность обработки запросов MessageProcessor с использованием mock зависимостей, убедиться в идентичности логики с Ktor модулем.

**Описание:**
Создать файл `src/test/kotlin/com/arbitrage/scanner/processors/MessageProcessorTest.kt`:

```kotlin
class MessageProcessorTest {
    private val businessLogicProcessor = mockk<BusinessLogicProcessor>()
    private val kafkaProducerService = mockk<KafkaProducerService>()
    private val loggerProvider = mockk<ArbScanLoggerProvider>()
    private val json = Json { /* конфигурация */ }

    private val messageProcessor = MessageProcessor(
        businessLogicProcessor,
        kafkaProducerService,
        loggerProvider,
        json
    )

    @BeforeTest
    fun setup() {
        every { loggerProvider.logger(any<KClass<*>>()) } returns mockk(relaxed = true)
    }

    @Test
    fun `should process READ request successfully`() = runTest {
        // Given
        val request = ArbitrageOpportunityReadRequest(/* ... */)
        coEvery { businessLogicProcessor.exec(any()) } just Runs
        coEvery { kafkaProducerService.sendResponse(any(), any(), any()) } returns Result.success(Unit)

        // When
        val result = messageProcessor.processMessage("test-id", request, "test-topic")

        // Then
        assertTrue(result.isSuccess)
        coVerify { businessLogicProcessor.exec(any()) }
        coVerify { kafkaProducerService.sendResponse("test-id", any(), "test-topic") }
    }

    @Test
    fun `should process SEARCH request successfully`() { /* ... */ }

    @Test
    fun `should handle business logic errors`() { /* ... */ }

    @Test
    fun `should send error response on exception`() { /* ... */ }

    @Test
    fun `should add correlation ID to context`() { /* ... */ }
}
```

**Архитектурный слой:** Application - тестирование обработки запросов

**Критерии выполнения:**
- Тесты для READ и SEARCH запросов
- Тесты успешной обработки
- Тесты обработки ошибок бизнес-логики
- Тесты отправки ответов через producer
- Тесты проверки correlation ID в контексте
- Использование MockK для мокирования зависимостей
- Использование runTest для тестирования suspend функций

**Критерии приемки:**
- Все тесты проходят командой `./gradlew :arbitrage-scanner-kafka:test`
- Coverage для MessageProcessor > 85%
- Тесты проверяют корректность вызовов всех зависимостей

**Зависимости:** Subtask-16

**Команды для проверки:**
```bash
./gradlew :arbitrage-scanner-kafka:test --tests "*MessageProcessorTest"
./gradlew :arbitrage-scanner-kafka:test
```

---

### Subtask-23: Unit тесты для KafkaProperties и конфигурации

**Цель и обоснование:**
Проверить корректность загрузки конфигурации из файла и переменных окружения, убедиться в правильном парсинге всех параметров.

**Описание:**
Создать файл `src/test/kotlin/com/arbitrage/scanner/kafka/config/KafkaPropertiesTest.kt`:

```kotlin
class KafkaPropertiesTest {
    @Test
    fun `should load default configuration from file`() {
        // Given
        val config = loadKafkaProperties()

        // Then
        assertEquals("localhost:9092", config.bootstrapServers)
        assertEquals("arbitrage-scanner-consumer", config.consumer.groupId)
        assertEquals(listOf("arbitrage-scanner-requests"), config.consumer.topics)
        assertEquals("earliest", config.consumer.autoOffsetReset)
        assertEquals(false, config.consumer.enableAutoCommit)
        assertEquals("arbitrage-scanner-responses", config.producer.topicResponses)
        assertEquals("all", config.producer.acks)
        assertEquals(3, config.producer.retries)
    }

    @Test
    fun `should override bootstrap servers from environment`() {
        // Given
        val originalValue = System.getProperty("KAFKA_BOOTSTRAP_SERVERS")
        System.setProperty("KAFKA_BOOTSTRAP_SERVERS", "kafka:9093")

        try {
            // When
            val config = loadKafkaProperties()

            // Then
            assertEquals("kafka:9093", config.bootstrapServers)
        } finally {
            // Cleanup
            if (originalValue != null) {
                System.setProperty("KAFKA_BOOTSTRAP_SERVERS", originalValue)
            } else {
                System.clearProperty("KAFKA_BOOTSTRAP_SERVERS")
            }
        }
    }

    @Test
    fun `should load application properties`() {
        // Given
        val config = loadApplicationProperties()

        // Then
        assertEquals("arbitrage-scanner-kafka", config.name)
        assertEquals(30000, config.shutdownTimeout)
    }
}
```

**Архитектурный слой:** Infrastructure - тестирование конфигурации

**Критерии выполнения:**
- Тесты загрузки дефолтной конфигурации
- Тесты переопределения через environment variables
- Тесты корректности всех параметров
- Тесты обработки отсутствующих параметров (если применимо)
- Корректная очистка environment после тестов

**Критерии приемки:**
- Все тесты проходят командой `./gradlew :arbitrage-scanner-kafka:test`
- Тесты не влияют на другие тесты (изоляция)
- Конфигурация корректно загружается в различных сценариях

**Зависимости:** Subtask-7

**Команды для проверки:**
```bash
./gradlew :arbitrage-scanner-kafka:test --tests "*KafkaPropertiesTest"
./gradlew :arbitrage-scanner-kafka:test
```

---

### Subtask-24: Интеграционные тесты с Testcontainers

**Цель и обоснование:**
Создать интеграционные тесты, которые запускают реальный Kafka в Docker контейнере и проверяют end-to-end обработку сообщений.

**Описание:**
Создать файл `src/test/kotlin/com/arbitrage/scanner/integration/KafkaIntegrationTest.kt`:

```kotlin
@Testcontainers
class KafkaIntegrationTest {

    companion object {
        @Container
        val kafka = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"))
            .withExposedPorts(9093)
    }

    private lateinit var koinApp: KoinApplication
    private lateinit var kafkaProducerService: KafkaProducerService
    private lateinit var messageProcessor: MessageProcessor

    @BeforeTest
    fun setup() {
        // Настройка Koin с Testcontainers Kafka
        koinApp = startKoin {
            modules(
                module {
                    single {
                        KafkaProperties(
                            bootstrapServers = kafka.bootstrapServers,
                            consumer = /* ... */,
                            producer = /* ... */
                        )
                    }
                },
                jsonModule,
                loggingModule,
                businessLogicProcessorModule,
                kafkaProducerModule,
                messageProcessorModule
            )
        }

        kafkaProducerService = koinApp.koin.get()
        messageProcessor = koinApp.koin.get()
    }

    @AfterTest
    fun teardown() {
        koinApp.close()
    }

    @Test
    fun `should process READ request and send response to Kafka`() = runTest {
        // Given
        val correlationId = "test-correlation-id"
        val request = ArbitrageOpportunityReadRequest(/* ... */)
        val responseTopic = "test-responses"

        // Создание consumer для чтения ответа
        val consumer = createTestConsumer(kafka.bootstrapServers, responseTopic)

        // When
        messageProcessor.processMessage(correlationId, request, responseTopic)

        // Then
        val records = consumer.poll(Duration.ofSeconds(5))
        assertEquals(1, records.count())

        val record = records.first()
        assertEquals(correlationId, record.key())
        assertNotNull(record.value())
        assertTrue(record.value() is ArbitrageOpportunityReadResponse)

        consumer.close()
    }

    @Test
    fun `should process SEARCH request and send response to Kafka`() { /* ... */ }

    @Test
    fun `should handle deserialization errors gracefully`() { /* ... */ }

    private fun createTestConsumer(bootstrapServers: String, topic: String): KafkaConsumer<String, IResponse> {
        // Создание test consumer
    }
}
```

**Архитектурный слой:** Infrastructure - интеграционное тестирование

**Критерии выполнения:**
- Использование Testcontainers для запуска Kafka
- Тесты end-to-end обработки READ и SEARCH запросов
- Проверка корректности ответов в Kafka топике
- Тесты обработки ошибок десериализации
- Корректная очистка ресурсов после тестов

**Критерии приемки:**
- Все интеграционные тесты проходят командой `./gradlew :arbitrage-scanner-kafka:test`
- Тесты корректно запускают и останавливают Kafka контейнер
- Проверяется полный цикл: запрос → обработка → ответ в топике
- Тесты работают в CI/CD pipeline

**Зависимости:** Subtask-22

**Команды для проверки:**
```bash
./gradlew :arbitrage-scanner-kafka:test --tests "*KafkaIntegrationTest"
./gradlew :arbitrage-scanner-kafka:test
```

---

### Subtask-25: Тест конфигурации Koin модулей

**Цель и обоснование:**
Проверить, что все Koin модули корректно настроены и все зависимости могут быть резолвлены без ошибок.

**Описание:**
Создать файл `src/test/kotlin/com/arbitrage/scanner/KoinModulesTest.kt`:

```kotlin
class KoinModulesTest {

    @Test
    fun `should start Koin with all modules`() {
        val koinApp = startKoin {
            modules(allModules)
        }

        try {
            // Проверка резолва всех основных компонентов
            assertNotNull(koinApp.koin.get<KafkaProperties>())
            assertNotNull(koinApp.koin.get<ApplicationProperties>())
            assertNotNull(koinApp.koin.get<Json>())
            assertNotNull(koinApp.koin.get<ArbScanLoggerProvider>())
            assertNotNull(koinApp.koin.get<BusinessLogicProcessor>())
            assertNotNull(koinApp.koin.get<KafkaProducerService>())
            assertNotNull(koinApp.koin.get<MessageProcessor>())
            assertNotNull(koinApp.koin.get<KafkaConsumerService>())
            assertNotNull(koinApp.koin.get<KafkaApplication>())
        } finally {
            koinApp.close()
        }
    }

    @Test
    fun `should inject correct logger instances`() {
        val koinApp = startKoin {
            modules(allModules)
        }

        try {
            val loggerProvider = koinApp.koin.get<ArbScanLoggerProvider>()
            val logger = loggerProvider.logger("Test")

            assertNotNull(logger)
        } finally {
            koinApp.close()
        }
    }

    @Test
    fun `should create singleton instances`() {
        val koinApp = startKoin {
            modules(allModules)
        }

        try {
            val producer1 = koinApp.koin.get<KafkaProducerService>()
            val producer2 = koinApp.koin.get<KafkaProducerService>()

            assertSame(producer1, producer2, "KafkaProducerService should be singleton")
        } finally {
            koinApp.close()
        }
    }
}
```

**Архитектурный слой:** Infrastructure - тестирование DI

**Критерии выполнения:**
- Тест успешного старта Koin с всеми модулями
- Тесты резолва всех основных компонентов
- Тесты singleton scope для сервисов
- Тесты корректности инъекции логгеров

**Критерии приемки:**
- Все тесты проходят командой `./gradlew :arbitrage-scanner-kafka:test`
- Koin успешно резолвит все зависимости
- Нет циклических зависимостей

**Зависимости:** Subtask-18

**Команды для проверки:**
```bash
./gradlew :arbitrage-scanner-kafka:test --tests "*KoinModulesTest"
./gradlew :arbitrage-scanner-kafka:test
```

---

## Этап 10: Документация

### Subtask-26: Создание README.md для модуля arbitrage-scanner-kafka

**Цель и обоснование:**
Создать полную документацию модуля для упрощения onboarding новых разработчиков и использования модуля.

**Описание:**
Создать файл `arbitrage-scanner/arbitrage-scanner-kafka/README.md` со следующими разделами:

```markdown
# Arbitrage Scanner - Kafka Transport

## Описание
Асинхронный транспорт для Arbitrage Scanner на базе Apache Kafka, обеспечивающий обработку запросов через систему сообщений.

## Требования
- Java 21+
- Apache Kafka 3.6+
- Docker (опционально)

## Конфигурация
### Параметры Kafka
- KAFKA_BOOTSTRAP_SERVERS - адреса Kafka brokers (по умолчанию: localhost:9092)
- Другие параметры...

### Структура топиков
- arbitrage-scanner-requests - входящие запросы
- arbitrage-scanner-responses - исходящие ответы

## Запуск
### Локально
```bash
./gradlew :arbitrage-scanner-kafka:run
```

### Docker
```bash
./gradlew :arbitrage-scanner-kafka:jibDockerBuild
docker-compose -f docker-compose.minimal.yml up arbitrage-scanner-kafka
```

## Примеры использования
### Отправка READ запроса
[код примера]

### Чтение ответа
[код примера]

## Архитектура
[Описание архитектуры модуля]

## Troubleshooting
[Типичные проблемы и решения]
```

**Архитектурный слой:** Documentation

**Критерии выполнения:**
- README.md создан со всеми основными разделами
- Документация описывает конфигурацию, запуск, примеры
- Приведены примеры кода для использования
- Описаны troubleshooting сценарии
- Форматирование Markdown корректное

**Критерии приемки:**
- README.md корректно отображается на GitHub
- Новый разработчик может запустить модуль по документации
- Примеры кода работают корректно

**Зависимости:** Subtask-20

**Команды для проверки:**
```bash
cat arbitrage-scanner/arbitrage-scanner-kafka/README.md
```

---

### Subtask-27: Обновление корневого CLAUDE.md с командами Kafka модуля

**Цель и обоснование:**
Добавить информацию о новом модуле в главный файл инструкций для Claude Code, чтобы AI ассистент знал о доступных командах и особенностях модуля.

**Описание:**
Обновить файл `CLAUDE.md`:

1. В раздел "Основные команды для разработки" добавить:
```markdown
### Kafka Transport
```bash
./gradlew :arbitrage-scanner-kafka:build    # Собрать Kafka модуль
./gradlew :arbitrage-scanner-kafka:test     # Запустить тесты
./gradlew :arbitrage-scanner-kafka:run      # Запустить приложение локально

# Docker сборка
./gradlew :arbitrage-scanner-kafka:jibDockerBuild  # Собрать Docker образ

# Запуск через docker-compose
./run-kafka-app.sh  # Собрать образ и запустить через docker-compose
```
```

2. Добавить раздел "Выбор между Ktor и Kafka транспортом":
```markdown
## Когда использовать Ktor vs Kafka

### Ktor (HTTP/REST)
- Синхронные запросы с немедленным ответом
- Простая интеграция с веб-клиентами
- Отладка через Postman/Swagger

### Kafka (Messaging)
- Асинхронная обработка запросов
- Высокая пропускная способность
- Decoupling между клиентами и серверами
- Event-driven архитектура
```

**Архитектурный слой:** Documentation

**Критерии выполнения:**
- Команды Kafka модуля добавлены в CLAUDE.md
- Добавлен раздел сравнения Ktor vs Kafka
- Форматирование соответствует существующему стилю
- Команды актуальны и корректны

**Критерии приемки:**
- Файл CLAUDE.md корректно форматирован
- Все команды работают корректно
- Claude Code может использовать новые команды

**Зависимости:** Subtask-26

**Команды для проверки:**
```bash
cat CLAUDE.md | grep -A 10 "arbitrage-scanner-kafka"
```

---

### Subtask-28: Обновление ARCHITECTURE.md с описанием Kafka транспорта

**Цель и обоснование:**
Документировать архитектурные решения Kafka транспорта в общей архитектурной документации проекта для сохранения знаний и упрощения поддержки.

**Описание:**
Обновить файл `ARCHITECTURE.md`:

1. В раздел "Presentation Layer" добавить упоминание Kafka модуля:
```markdown
**Модули**: `arbitrage-scanner-ktor`, `arbitrage-scanner-kafka`, `arbitrage-scanner-api-v1`

**Kafka Transport (arbitrage-scanner-kafka)**:
- Асинхронная обработка запросов через Apache Kafka
- Request-Reply Pattern через топики
- Переиспользование бизнес-логики из core модулей
```

2. Добавить раздел "Kafka Transport Architecture":
```markdown
## Kafka Transport Architecture

### Request-Reply Pattern

```
Client → arbitrage-scanner-requests topic → Kafka Consumer →
MessageProcessor → Business Logic → Response →
arbitrage-scanner-responses topic → Client
```

### Структура Kafka сообщений

**Key**: correlation-id (UUID)
**Value**: JSON сериализованный IRequest/IResponse
**Headers**:
- `correlation-id`: для трейсинга запросов
- `reply-to`: топик для ответа
- `timestamp`: время создания запроса

### Компоненты

1. **KafkaConsumerService**: Чтение запросов из топика
2. **KafkaProducerService**: Отправка ответов в топик
3. **MessageProcessor**: Адаптация для бизнес-логики
4. **KafkaJsonSerializer/Deserializer**: Сериализация сообщений
```

3. Обновить диаграмму компонентов с добавлением Kafka модуля.

**Архитектурный слой:** Documentation

**Критерии выполнения:**
- ARCHITECTURE.md обновлен с описанием Kafka транспорта
- Добавлена информация о Request-Reply Pattern
- Документирована структура сообщений
- Обновлена компонентная диаграмма
- Стиль документации соответствует существующему

**Критерии приемки:**
- Файл ARCHITECTURE.md корректно форматирован
- Описание архитектуры полное и понятное
- Диаграммы актуальны

**Зависимости:** Subtask-27

**Команды для проверки:**
```bash
cat ARCHITECTURE.md | grep -A 20 "Kafka Transport"
```

---

### Subtask-29: Создание примеров использования Kafka транспорта

**Цель и обоснование:**
Предоставить рабочие примеры для отправки запросов в Kafka и чтения ответов, что упростит интеграцию клиентов с сервисом.

**Описание:**
Создать директорию `examples/kafka-client/` с примерами:

1. `send-read-request.sh`:
```bash
#!/bin/bash
# Отправка READ запроса в Kafka топик

kafka-console-producer --bootstrap-server localhost:9092 \
  --topic arbitrage-scanner-requests \
  --property "parse.key=true" \
  --property "key.separator=:" << EOF
test-correlation-id:{"type":"READ","id":"test-id-123"}
EOF
```

2. `consume-responses.sh`:
```bash
#!/bin/bash
# Чтение ответов из Kafka топика

kafka-console-consumer --bootstrap-server localhost:9092 \
  --topic arbitrage-scanner-responses \
  --from-beginning \
  --property print.key=true \
  --property key.separator=":"
```

3. `kotlin-client-example.kt`:
```kotlin
// Пример Kotlin клиента для отправки запросов
fun main() {
    val producer = createKafkaProducer()
    val request = ArbitrageOpportunityReadRequest(id = "test-id")

    producer.send(
        ProducerRecord(
            "arbitrage-scanner-requests",
            "correlation-id-123",
            request
        )
    )

    producer.close()
}
```

4. `README.md` в директории examples с описанием каждого примера.

**Архитектурный слой:** Documentation - примеры использования

**Критерии выполнения:**
- Созданы shell скрипты для отправки запросов и чтения ответов
- Создан пример Kotlin клиента
- Примеры снабжены комментариями
- README.md описывает каждый пример и как его использовать
- Скрипты имеют права на выполнение

**Критерии приемки:**
- Все примеры работают корректно
- Скрипты успешно отправляют/читают сообщения из Kafka
- Kotlin пример компилируется и работает
- README.md в директории examples полный и понятный

**Зависимости:** Subtask-28

**Команды для проверки:**
```bash
chmod +x examples/kafka-client/*.sh
cd examples/kafka-client && ./send-read-request.sh
cd examples/kafka-client && ./consume-responses.sh &
```

---

## Этап 11: Финальная интеграция и проверка

### Subtask-30: End-to-End тестирование через docker-compose

**Цель и обоснование:**
Проверить работоспособность всей системы в реалистичном окружении с реальным Kafka и всеми зависимостями.

**Описание:**
Создать скрипт `test-e2e.sh`:

```bash
#!/bin/bash
set -e

echo "=== Starting E2E Test for Kafka Transport ==="

# 1. Сборка и запуск всего стека
echo "Building and starting docker-compose stack..."
./gradlew :arbitrage-scanner-kafka:jibDockerBuild
docker-compose -f docker-compose.minimal.yml up -d

# Ожидание готовности сервисов
echo "Waiting for services to be ready..."
sleep 30

# 2. Отправка READ запроса
echo "Sending READ request..."
docker exec -i kafka kafka-console-producer \
  --bootstrap-server localhost:9092 \
  --topic arbitrage-scanner-requests \
  --property "parse.key=true" \
  --property "key.separator=:" << EOF
e2e-test-read:{"type":"READ","id":"test-opportunity-1"}
EOF

# 3. Чтение ответа (с timeout)
echo "Reading response..."
timeout 10 docker exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic arbitrage-scanner-responses \
  --from-beginning \
  --max-messages 1 \
  --property print.key=true \
  --property key.separator=":"

# 4. Отправка SEARCH запроса
echo "Sending SEARCH request..."
docker exec -i kafka kafka-console-producer \
  --bootstrap-server localhost:9092 \
  --topic arbitrage-scanner-requests \
  --property "parse.key=true" \
  --property "key.separator=:" << EOF
e2e-test-search:{"type":"SEARCH","filter":{"statusType":"ACTIVE"}}
EOF

# 5. Чтение ответа SEARCH
echo "Reading SEARCH response..."
timeout 10 docker exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic arbitrage-scanner-responses \
  --from-beginning \
  --max-messages 2 \
  --property print.key=true \
  --property key.separator=":"

# 6. Проверка логов
echo "Checking application logs..."
docker-compose -f docker-compose.minimal.yml logs arbitrage-scanner-kafka | tail -50

# 7. Остановка стека
echo "Stopping docker-compose stack..."
docker-compose -f docker-compose.minimal.yml down

echo "=== E2E Test Completed Successfully ==="
```

**Архитектурный слой:** Testing - end-to-end

**Критерии выполнения:**
- Скрипт `test-e2e.sh` создан и имеет права на выполнение
- Тестируется полный цикл: запуск → READ запрос → ответ → SEARCH запрос → ответ → остановка
- Проверяются логи приложения на наличие ошибок
- Корректная очистка ресурсов после теста

**Критерии приемки:**
- Скрипт выполняется без ошибок
- READ и SEARCH запросы обрабатываются успешно
- Ответы приходят в правильный топик
- Логи не содержат критичных ошибок
- Все контейнеры корректно останавливаются

**Зависимости:** Subtask-29

**Команды для проверки:**
```bash
chmod +x test-e2e.sh
./test-e2e.sh
```

---

### Subtask-31: Сравнительное тестирование Ktor vs Kafka модулей

**Цель и обоснование:**
Убедиться, что бизнес-логика обработки запросов идентична в обоих модулях и результаты обработки совпадают.

**Описание:**
Создать скрипт `compare-ktor-kafka.sh`:

```bash
#!/bin/bash
set -e

echo "=== Comparing Ktor and Kafka Transport Results ==="

# 1. Запуск Ktor приложения
echo "Starting Ktor application..."
./gradlew :arbitrage-scanner-ktor:run &
KTOR_PID=$!
sleep 10

# 2. Отправка READ запроса через HTTP
echo "Sending READ request via Ktor..."
KTOR_RESPONSE=$(curl -s -X POST http://localhost:8080/arbitrage-opportunity/read \
  -H "Content-Type: application/json" \
  -d '{"id":"test-comparison"}')

echo "Ktor Response: $KTOR_RESPONSE"

# 3. Остановка Ktor
kill $KTOR_PID

# 4. Запуск Kafka приложения
echo "Starting Kafka stack..."
./gradlew :arbitrage-scanner-kafka:jibDockerBuild
docker-compose -f docker-compose.minimal.yml up -d
sleep 30

# 5. Отправка идентичного READ запроса через Kafka
echo "Sending READ request via Kafka..."
docker exec -i kafka kafka-console-producer \
  --bootstrap-server localhost:9092 \
  --topic arbitrage-scanner-requests \
  --property "parse.key=true" \
  --property "key.separator=:" << EOF
test-comparison:{"type":"READ","id":"test-comparison"}
EOF

# 6. Чтение ответа из Kafka
KAFKA_RESPONSE=$(timeout 10 docker exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic arbitrage-scanner-responses \
  --from-beginning \
  --max-messages 1)

echo "Kafka Response: $KAFKA_RESPONSE"

# 7. Сравнение результатов
echo "Comparing responses..."
# Можно использовать jq для сравнения JSON

# 8. Остановка Kafka
docker-compose -f docker-compose.minimal.yml down

echo "=== Comparison Completed ==="
```

**Архитектурный слой:** Testing - интеграционное тестирование

**Критерии выполнения:**
- Скрипт отправляет идентичные запросы через Ktor и Kafka
- Сравниваются результаты обработки
- Проверяется идентичность структуры ответов
- Корректная очистка ресурсов

**Критерии приемки:**
- Результаты обработки идентичны для Ktor и Kafka
- Бизнес-логика работает одинаково
- Скрипт завершается успешно

**Зависимости:** Subtask-30

**Команды для проверки:**
```bash
chmod +x compare-ktor-kafka.sh
./compare-ktor-kafka.sh
```

---

### Subtask-32: Финальная проверка всех требований и чек-листа

**Цель и обоснование:**
Пройти по полному чек-листу готовности к production из плана и убедиться, что все требования выполнены.

**Описание:**
Выполнить проверку всех пунктов из "Контрольный список готовности к production":

1. Проверка компиляции и тестов:
```bash
./gradlew :arbitrage-scanner-kafka:build
./gradlew :arbitrage-scanner-kafka:test
./gradlew check
```

2. Проверка Docker образа:
```bash
./gradlew :arbitrage-scanner-kafka:jibDockerBuild
docker images | grep arbitrage-scanner-kafka
docker run --rm arbitrage-scanner-kafka:latest --help || true
```

3. Проверка конфигурации:
```bash
# Проверка загрузки из файла
./gradlew :arbitrage-scanner-kafka:run &
sleep 5
kill %1

# Проверка переопределения через ENV
KAFKA_BOOTSTRAP_SERVERS=custom:9092 ./gradlew :arbitrage-scanner-kafka:run &
sleep 5
kill %1
```

4. Проверка graceful shutdown:
```bash
./gradlew :arbitrage-scanner-kafka:run &
APP_PID=$!
sleep 10
kill -SIGTERM $APP_PID
wait $APP_PID
echo "Graceful shutdown completed"
```

5. Проверка логирования:
```bash
docker-compose -f docker-compose.minimal.yml up -d
sleep 30
docker-compose logs arbitrage-scanner-kafka | grep "BIZ"
docker-compose logs fluent-bit | grep "arbitrage-scanner-kafka"
docker-compose down
```

6. Создание отчета проверки в файле `CHECKLIST_RESULTS.md`.

**Архитектурный слой:** Testing - финальная проверка

**Критерии выполнения:**
- Все пункты чек-листа проверены
- Документирован результат каждой проверки
- Выявленные проблемы исправлены
- Создан отчет CHECKLIST_RESULTS.md

**Критерии приемки:**
- Все пункты чек-листа выполнены успешно
- Модуль готов к production развертыванию
- Документация актуальна
- Все тесты проходят

**Зависимости:** Subtask-31

**Команды для проверки:**
```bash
./gradlew :arbitrage-scanner-kafka:build
./gradlew :arbitrage-scanner-kafka:test
./gradlew check
cat tasks/20251003_kafka_transport/CHECKLIST_RESULTS.md
```

---

## Сводная статистика

**Всего задач:** 32

**Распределение по этапам:**
- Этап 1 (Структура модуля): 4 задачи
- Этап 2 (Конфигурация): 3 задачи
- Этап 3 (Сериализация): 2 задачи
- Этап 4 (Producer): 2 задачи
- Этап 5 (Consumer): 3 задачи
- Этап 6 (MessageProcessor): 2 задачи
- Этап 7 (Application): 2 задачи
- Этап 8 (Docker): 2 задачи
- Этап 9 (Тесты): 5 задач
- Этап 10 (Документация): 4 задачи
- Этап 11 (Интеграция): 3 задачи

**Распределение по архитектурным слоям:**
- Infrastructure: 15 задач
- Application: 3 задачи
- Presentation: 1 задача
- Testing: 8 задач
- Documentation: 5 задач

**Оценка времени:** 36-50 часов (согласно плану)

---

## Порядок выполнения

Задачи должны выполняться последовательно в указанном порядке, так как многие задачи имеют зависимости от предыдущих. Каждая задача должна быть полностью завершена (код написан, тесты пройдены, критерии приемки выполнены) перед переходом к следующей.

**Критические зависимости:**
- Subtask-1 → Subtask-2 → Subtask-3 → Subtask-4 (базовая структура)
- Subtask-4 → Subtask-5 → Subtask-6 → Subtask-7 (конфигурация)
- Subtask-7 → Subtask-8 → Subtask-9 (сериализация)
- Subtask-9 → Subtask-10 → Subtask-11 (producer)
- Subtask-11 → Subtask-12 → Subtask-13 → Subtask-14 (consumer)
- Subtask-14 → Subtask-15 → Subtask-16 (processor)
- Subtask-16 → Subtask-17 → Subtask-18 (application)
- Subtask-18 → Subtask-19 → Subtask-20 (docker)
- Все предыдущие → Subtask-21..25 (тесты)
- Subtask-20 → Subtask-26..29 (документация)
- Subtask-29 → Subtask-30 → Subtask-31 → Subtask-32 (финальная проверка)

---

## Примечания

- Каждая задача должна завершаться успешной компиляцией проекта
- После каждой задачи рекомендуется запускать все существующие тесты
- Commit следует делать после каждой завершенной задачи
- При возникновении блокеров задачу следует пометить соответствующим образом

---

**Статус документа:** Готов к выполнению
**Версия:** 1.0
**Дата создания:** 2025-10-03
