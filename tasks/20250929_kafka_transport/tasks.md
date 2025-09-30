# Декомпозиция задачи: Реализация модуля arbitrage-scanner-kafka

**Дата создания**: 2025-09-29
**Базовый план**: [plan.md](./plan.md)

---

## Группа 1: Настройка проекта и базовой инфраструктуры

### Subtask-1: Добавить модуль в settings.gradle.kts

**Статус**: [x]

**Описание**:
Зарегистрировать новый модуль `arbitrage-scanner-kafka` в системе сборки проекта.

**Цель и обоснование**:
Без регистрации модуля в settings.gradle.kts Gradle не сможет найти и собрать новый модуль. Это первый необходимый шаг для создания любого нового модуля в composite build структуре.

**Архитектурный слой**: Infrastructure (Build Configuration)

**Файлы для изменения**:
- `/Users/ilya/IdeaProjects/otus-kotlin-backend-developer-professional/arbitrage-scanner/settings.gradle.kts`

**Изменения**:
- Добавить строку `include("arbitrage-scanner-kafka")` после существующих `include()` директив

**Зависимости**: Нет

**Критерии выполнения**:
- В файл `arbitrage-scanner/settings.gradle.kts` добавлена строка `include("arbitrage-scanner-kafka")`
- Модуль отображается в списке проектов

**Критерии приемки**:
- Команда `./gradlew projects` показывает новый модуль `arbitrage-scanner-kafka` в списке
- Проект компилируется без ошибок: `./gradlew projects`
- Новый модуль виден в IDE после синхронизации Gradle

**Команды для проверки**:
```bash
./gradlew projects | grep arbitrage-scanner-kafka
```

---

### Subtask-2: Создать директорию и структуру модуля

**Статус**: [ ]

**Описание**:
Создать файловую структуру для нового модуля со стандартной организацией директорий для Kotlin/Gradle проекта.

**Цель и обоснование**:
Необходимо создать базовую структуру директорий согласно конвенциям Gradle и Kotlin проекта, чтобы файлы исходного кода, ресурсы и тесты находились в правильных местах.

**Архитектурный слой**: Infrastructure (Project Structure)

**Файлы для создания**:
- `/Users/ilya/IdeaProjects/otus-kotlin-backend-developer-professional/arbitrage-scanner/arbitrage-scanner-kafka/`
- `/Users/ilya/IdeaProjects/otus-kotlin-backend-developer-professional/arbitrage-scanner/arbitrage-scanner-kafka/src/main/kotlin/com/arbitrage/scanner/`
- `/Users/ilya/IdeaProjects/otus-kotlin-backend-developer-professional/arbitrage-scanner/arbitrage-scanner-kafka/src/main/resources/`
- `/Users/ilya/IdeaProjects/otus-kotlin-backend-developer-professional/arbitrage-scanner/arbitrage-scanner-kafka/src/test/kotlin/com/arbitrage/scanner/`
- `/Users/ilya/IdeaProjects/otus-kotlin-backend-developer-professional/arbitrage-scanner/arbitrage-scanner-kafka/docker/`

**Зависимости**: Subtask-1

**Критерии выполнения**:
- Созданы все необходимые директории для исходного кода
- Созданы директории для ресурсов (конфигурационные файлы)
- Созданы директории для тестов
- Создана директория для Docker конфигурации

**Критерии приемки**:
- Все директории существуют и имеют правильную структуру
- Структура соответствует стандартам Gradle/Kotlin проектов
- Директории видны в файловой системе

**Команды для проверки**:
```bash
ls -la arbitrage-scanner/arbitrage-scanner-kafka/src/main/kotlin/com/arbitrage/scanner/
ls -la arbitrage-scanner/arbitrage-scanner-kafka/src/main/resources/
ls -la arbitrage-scanner/arbitrage-scanner-kafka/src/test/kotlin/com/arbitrage/scanner/
ls -la arbitrage-scanner/arbitrage-scanner-kafka/docker/
```

---

### Subtask-3: Создать build.gradle.kts с зависимостями

**Статус**: [ ]

**Описание**:
Создать файл сборки модуля с применением необходимых плагинов и объявлением всех зависимостей (внутренних модулей, Kafka, Kotlinx, Koin, конфигурация, тестирование).

**Цель и обоснование**:
Build-скрипт определяет, как модуль будет собираться, какие библиотеки использовать и как запускаться. Это критический конфигурационный файл для работы модуля.

**Архитектурный слой**: Infrastructure (Build Configuration)

**Файлы для создания**:
- `/Users/ilya/IdeaProjects/otus-kotlin-backend-developer-professional/arbitrage-scanner/arbitrage-scanner-kafka/build.gradle.kts`

**Содержимое**:
```kotlin
plugins {
    alias(libs.plugins.build.plugin.jvm)
    alias(libs.plugins.kotlinx.serialization)
}

application {
    mainClass.set("com.arbitrage.scanner.ApplicationKt")
}

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
    implementation("io.insert-koin:koin-core:3.5.6")

    // Конфигурация
    implementation("com.sksamuel.hoplite:hoplite-core:2.9.0")
    implementation("com.sksamuel.hoplite:hoplite-yaml:2.9.0")

    // Тестирование
    testImplementation(libs.kotlin.test)
    testImplementation("org.testcontainers:testcontainers:1.20.4")
    testImplementation("org.testcontainers:kafka:1.20.4")
    testImplementation("io.insert-koin:koin-test:3.5.6")
}
```

**Зависимости**: Subtask-1, Subtask-2

**Критерии выполнения**:
- Файл `build.gradle.kts` создан в корне модуля
- Применены плагины: `build-plugin-jvm` и `kotlinx-serialization`
- Объявлен блок `application` с правильным `mainClass`
- Добавлены все необходимые зависимости (внутренние модули, Kafka, Kotlinx, Koin, Hoplite, Testcontainers)
- Версии зависимостей указаны корректно (используются версии из libs.versions.toml где возможно)

**Критерии приемки**:
- Проект успешно синхронизируется в IDE
- Команда `./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:dependencies` показывает все зависимости
- Все зависимости успешно резолвятся
- Проект компилируется без ошибок: `./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:build`

**Команды для проверки**:
```bash
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:dependencies
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:build
```

---

## Группа 2: Конфигурация приложения

### Subtask-4: Создать модели конфигурации

**Статус**: [ ]

**Описание**:
Создать data классы для типобезопасной конфигурации Kafka, логирования и других параметров приложения с использованием Kotlin data classes.

**Цель и обоснование**:
Типобезопасная конфигурация через data классы позволяет избежать ошибок с типами и получить проверку на этапе компиляции. Это соответствует принципу type-safety в Kotlin.

**Архитектурный слой**: Infrastructure (Configuration)

**Файлы для создания**:
- `/Users/ilya/IdeaProjects/otus-kotlin-backend-developer-professional/arbitrage-scanner/arbitrage-scanner-kafka/src/main/kotlin/com/arbitrage/scanner/config/KafkaConfiguration.kt`

**Содержимое**:
```kotlin
package com.arbitrage.scanner.config

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

**Зависимости**: Subtask-3

**Критерии выполнения**:
- Создан файл `KafkaConfiguration.kt` с data классами
- Все параметры Kafka конфигурации покрыты (bootstrap servers, topics, consumer/producer настройки)
- Параметры логирования определены
- Используются правильные типы для параметров (String, Boolean, Int, Map)

**Критерии приемки**:
- Проект компилируется: `./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:compileKotlin`
- Классы конфигурации доступны для использования
- Нет ошибок компиляции

**Команды для проверки**:
```bash
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:compileKotlin
```

---

### Subtask-5: Создать ConfigLoader с использованием Hoplite

**Статус**: [ ]

**Описание**:
Реализовать загрузчик конфигурации, который читает YAML файл и десериализует его в типобезопасные data классы.

**Цель и обоснование**:
ConfigLoader обеспечивает централизованную загрузку конфигурации из внешних файлов, что позволяет изменять параметры без перекомпиляции приложения.

**Архитектурный слой**: Infrastructure (Configuration)

**Файлы для создания**:
- `/Users/ilya/IdeaProjects/otus-kotlin-backend-developer-professional/arbitrage-scanner/arbitrage-scanner-kafka/src/main/kotlin/com/arbitrage/scanner/config/ConfigLoader.kt`

**Содержимое**:
```kotlin
package com.arbitrage.scanner.config

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addResourceSource

object ConfigLoader {
    fun load(): AppConfig {
        return ConfigLoaderBuilder.default()
            .addResourceSource("/application.yaml")
            .build()
            .loadConfigOrThrow<AppConfig>()
    }
}
```

**Зависимости**: Subtask-4

**Критерии выполнения**:
- Создан файл `ConfigLoader.kt` с object ConfigLoader
- Реализован метод `load()`, возвращающий `AppConfig`
- Используется Hoplite ConfigLoaderBuilder для загрузки из ресурсов
- Конфигурация загружается из `/application.yaml` из classpath

**Критерии приемки**:
- Проект компилируется: `./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:compileKotlin`
- ConfigLoader может быть проинстанцирован
- Нет ошибок компиляции

**Команды для проверки**:
```bash
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:compileKotlin
```

---

### Subtask-6: Создать application.yaml с конфигурацией

**Статус**: [ ]

**Описание**:
Создать YAML файл конфигурации со всеми необходимыми параметрами для Kafka и логирования.

**Цель и обоснование**:
YAML конфигурация позволяет управлять параметрами приложения без изменения кода. Структура YAML должна соответствовать созданным data классам конфигурации.

**Архитектурный слой**: Infrastructure (Configuration)

**Файлы для создания**:
- `/Users/ilya/IdeaProjects/otus-kotlin-backend-developer-professional/arbitrage-scanner/arbitrage-scanner-kafka/src/main/resources/application.yaml`

**Содержимое**:
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
    root: "INFO"
    com.arbitrage.scanner: "DEBUG"
    org.apache.kafka: "WARN"
```

**Зависимости**: Subtask-4, Subtask-5

**Критерии выполнения**:
- Создан файл `application.yaml` в директории resources
- YAML структура соответствует data классам конфигурации
- Указаны все необходимые параметры Kafka (bootstrap servers, topics, consumer/producer настройки)
- Указаны уровни логирования для различных пакетов
- Значения по умолчанию подходят для локальной разработки

**Критерии приемки**:
- YAML файл корректно форматирован (валидный YAML)
- ConfigLoader успешно загружает конфигурацию (будет проверено в следующих задачах)
- Файл находится в правильной директории resources

**Команды для проверки**:
```bash
cat arbitrage-scanner/arbitrage-scanner-kafka/src/main/resources/application.yaml
```

---

### Subtask-7: Создать unit тест для ConfigLoader

**Статус**: [ ]

**Описание**:
Написать тест, проверяющий корректность загрузки конфигурации из YAML файла в типобезопасные data классы.

**Цель и обоснование**:
Unit тест гарантирует, что конфигурация загружается корректно, все поля заполняются правильными значениями, и структура YAML соответствует ожидаемой.

**Архитектурный слой**: Infrastructure (Testing)

**Файлы для создания**:
- `/Users/ilya/IdeaProjects/otus-kotlin-backend-developer-professional/arbitrage-scanner/arbitrage-scanner-kafka/src/test/kotlin/com/arbitrage/scanner/config/ConfigLoaderTest.kt`

**Содержимое**:
```kotlin
package com.arbitrage.scanner.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class ConfigLoaderTest {

    @Test
    fun `should load configuration from application yaml`() {
        // When
        val config = ConfigLoader.load()

        // Then
        assertNotNull(config)
        assertNotNull(config.kafka)
        assertNotNull(config.logging)
    }

    @Test
    fun `should load kafka configuration correctly`() {
        // When
        val kafkaConfig = ConfigLoader.load().kafka

        // Then
        assertEquals("localhost:9092", kafkaConfig.bootstrapServers)
        assertEquals("arbitrage-scanner-group", kafkaConfig.groupId)
        assertEquals("arbitrage-scanner-requests", kafkaConfig.consumer.topic)
        assertEquals("arbitrage-scanner-responses", kafkaConfig.producer.topic)
        assertFalse(kafkaConfig.consumer.enableAutoCommit)
        assertEquals(3, kafkaConfig.consumer.concurrency)
    }

    @Test
    fun `should load logging configuration correctly`() {
        // When
        val loggingConfig = ConfigLoader.load().logging

        // Then
        assertEquals("INFO", loggingConfig.level["root"])
        assertEquals("DEBUG", loggingConfig.level["com.arbitrage.scanner"])
        assertEquals("WARN", loggingConfig.level["org.apache.kafka"])
    }
}
```

**Зависимости**: Subtask-5, Subtask-6

**Критерии выполнения**:
- Создан файл `ConfigLoaderTest.kt` с тестами
- Реализованы тесты для загрузки общей конфигурации
- Реализованы тесты для проверки Kafka конфигурации
- Реализованы тесты для проверки конфигурации логирования
- Используется Kotlin Test framework

**Критерии приемки**:
- Проект компилируется: `./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:compileTestKotlin`
- Все тесты проходят: `./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:test`
- Тесты проверяют корректность всех важных полей конфигурации

**Команды для проверки**:
```bash
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:test --tests "com.arbitrage.scanner.config.ConfigLoaderTest"
```

---

## Группа 3: Kafka Producer (отправка ответов)

### Subtask-8: Создать интерфейс ResponseProducer

**Статус**: [ ]

**Описание**:
Определить интерфейс для отправки Response сообщений, обеспечивая абстракцию над конкретной реализацией Kafka Producer.

**Цель и обоснование**:
Интерфейс позволяет следовать принципу Dependency Inversion (SOLID) - зависеть от абстракции, а не от конкретной реализации. Это упрощает тестирование и возможную замену реализации.

**Архитектурный слой**: Infrastructure (Kafka Integration)

**Файлы для создания**:
- `/Users/ilya/IdeaProjects/otus-kotlin-backend-developer-professional/arbitrage-scanner/arbitrage-scanner-kafka/src/main/kotlin/com/arbitrage/scanner/producers/ResponseProducer.kt`

**Содержимое**:
```kotlin
package com.arbitrage.scanner.producers

import com.arbitrage.scanner.api.v1.models.IResponse

interface ResponseProducer {
    /**
     * Отправляет ответ в Kafka топик
     * @param correlationId ID для сопоставления запроса и ответа
     * @param response ответ для отправки
     */
    suspend fun sendResponse(
        correlationId: String,
        response: IResponse
    )

    /**
     * Закрывает producer и освобождает ресурсы
     */
    suspend fun close()
}
```

**Зависимости**: Subtask-3

**Критерии выполнения**:
- Создан файл `ResponseProducer.kt` с интерфейсом
- Определен метод `sendResponse` с параметрами correlationId и response
- Определен метод `close` для освобождения ресурсов
- Методы помечены как `suspend` для поддержки корутин
- Используется тип `IResponse` из модуля `arbitrage-scanner-api-v1`

**Критерии приемки**:
- Проект компилируется: `./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:compileKotlin`
- Интерфейс доступен для реализации
- Нет ошибок компиляции

**Команды для проверки**:
```bash
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:compileKotlin
```

---

### Subtask-9: Реализовать KafkaResponseProducer

**Статус**: [ ]

**Описание**:
Реализовать класс, который отправляет Response сообщения в Kafka, включая сериализацию в JSON, установку заголовков и обработку ошибок.

**Цель и обоснование**:
Конкретная реализация Producer использует Apache Kafka клиент для отправки сообщений. Она инкапсулирует логику сериализации, формирования Kafka записей и обработки ошибок отправки.

**Архитектурный слой**: Infrastructure (Kafka Integration)

**Файлы для создания**:
- `/Users/ilya/IdeaProjects/otus-kotlin-backend-developer-professional/arbitrage-scanner/arbitrage-scanner-kafka/src/main/kotlin/com/arbitrage/scanner/producers/KafkaResponseProducer.kt`

**Содержимое**:
```kotlin
package com.arbitrage.scanner.producers

import com.arbitrage.scanner.api.v1.models.IResponse
import com.arbitrage.scanner.api.v1.toResponseJsonString
import com.arbitrage.scanner.config.ProducerConfig
import com.arbitrage.scanner.libs.logging.common.ArbScanLogWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig as ApacheProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.util.*

class KafkaResponseProducer(
    private val config: ProducerConfig,
    private val kafkaConfig: com.arbitrage.scanner.config.KafkaConfig,
    private val json: Json,
    private val logger: ArbScanLogWrapper
) : ResponseProducer {

    private val producer: KafkaProducer<String, String>

    init {
        val props = Properties().apply {
            put(ApacheProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.bootstrapServers)
            put(ApacheProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            put(ApacheProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            put(ApacheProducerConfig.ACKS_CONFIG, config.acks)
            put(ApacheProducerConfig.RETRIES_CONFIG, config.retries)
            put(ApacheProducerConfig.COMPRESSION_TYPE_CONFIG, config.compressionType)
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
                headers().add("response-type", response::class.simpleName?.toByteArray() ?: "Unknown".toByteArray())
                headers().add("timestamp", System.currentTimeMillis().toString().toByteArray())
            }

            producer.send(record).get() // Синхронная отправка для надежности

            logger.info(
                msg = "Response sent",
                marker = "KAFKA_PRODUCER",
                data = "correlationId=$correlationId, type=${response::class.simpleName}"
            )
        } catch (e: Exception) {
            logger.error(
                msg = "Failed to send response",
                marker = "KAFKA_PRODUCER",
                data = "correlationId=$correlationId",
                e = e
            )
            throw e
        }
    }

    override suspend fun close() = withContext(Dispatchers.IO) {
        producer.close()
        logger.info(msg = "Kafka producer closed", marker = "KAFKA_PRODUCER")
    }
}
```

**Зависимости**: Subtask-4, Subtask-8

**Критерии выполнения**:
- Создан файл `KafkaResponseProducer.kt` с реализацией интерфейса `ResponseProducer`
- Инициализация KafkaProducer с настройками из конфигурации
- Сериализация IResponse в JSON с использованием Kotlinx.serialization
- Формирование ProducerRecord с key (correlationId), value (JSON) и заголовками
- Заголовки включают: correlation-id, response-type, timestamp
- Синхронная отправка через `send().get()` для гарантии доставки
- Логирование успешной отправки и ошибок
- Корректное закрытие producer в методе close()
- Использование корутин с `withContext(Dispatchers.IO)` для блокирующих операций

**Критерии приемки**:
- Проект компилируется: `./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:compileKotlin`
- Класс реализует интерфейс ResponseProducer
- Используются правильные конфигурационные параметры
- Логирование работает корректно

**Команды для проверки**:
```bash
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:compileKotlin
```

---

### Subtask-10: Создать unit тесты для KafkaResponseProducer

**Статус**: [ ]

**Описание**:
Написать тесты для проверки корректности сериализации, формирования Kafka записей и обработки ошибок в Producer.

**Цель и обоснование**:
Unit тесты гарантируют, что Producer корректно сериализует сообщения, устанавливает заголовки и обрабатывает исключения. Это критично для надежности отправки ответов.

**Архитектурный слой**: Infrastructure (Testing)

**Файлы для создания**:
- `/Users/ilya/IdeaProjects/otus-kotlin-backend-developer-professional/arbitrage-scanner/arbitrage-scanner-kafka/src/test/kotlin/com/arbitrage/scanner/producers/KafkaResponseProducerTest.kt`

**Содержимое**:
```kotlin
package com.arbitrage.scanner.producers

import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityReadResponse
import com.arbitrage.scanner.api.v1.models.ResponseResult
import com.arbitrage.scanner.config.KafkaConfig
import com.arbitrage.scanner.config.ProducerConfig
import com.arbitrage.scanner.libs.logging.common.ArbScanLogWrapper
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.apache.kafka.clients.producer.MockProducer
import org.apache.kafka.common.serialization.StringSerializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class KafkaResponseProducerTest {

    private val json = Json {
        prettyPrint = false
        isLenient = true
    }

    private val mockLogger = object : ArbScanLogWrapper {
        override fun info(msg: String, marker: String?, data: Any?) {}
        override fun warn(msg: String, marker: String?, data: Any?, e: Throwable?) {}
        override fun error(msg: String, marker: String?, data: Any?, e: Throwable?) {}
        override fun debug(msg: String, marker: String?, data: Any?) {}
    }

    @Test
    fun `should serialize response to JSON correctly`() = runBlocking {
        // Given
        val response = ArbitrageOpportunityReadResponse(
            result = ResponseResult.SUCCESS,
            errors = emptyList()
        )

        // When
        val jsonString = json.encodeToString(
            com.arbitrage.scanner.api.v1.models.IResponse.serializer(),
            response
        )

        // Then
        assertNotNull(jsonString)
        assert(jsonString.contains("\"result\":\"SUCCESS\""))
    }

    // Примечание: Полноценное тестирование KafkaProducer требует MockProducer или Testcontainers
    // Эти тесты будут расширены в интеграционных тестах
}
```

**Зависимости**: Subtask-9

**Критерии выполнения**:
- Создан файл `KafkaResponseProducerTest.kt`
- Реализован тест для проверки сериализации Response в JSON
- Используется mock logger для избежания зависимости от реального логирования
- Тест проверяет корректность JSON структуры

**Критерии приемки**:
- Проект компилируется: `./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:compileTestKotlin`
- Тесты проходят: `./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:test`
- Тест покрывает базовую функциональность сериализации

**Команды для проверки**:
```bash
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:test --tests "com.arbitrage.scanner.producers.KafkaResponseProducerTest"
```

---

## Группа 4: Kafka Message Processing

### Subtask-11: Создать модель KafkaMessage

**Статус**: [ ]

**Описание**:
Создать data класс, представляющий обертку над Kafka сообщением с полями для correlation ID, типа запроса, timestamp и payload.

**Цель и обоснование**:
KafkaMessage инкапсулирует структуру Kafka сообщения, извлекая важные метаданные из заголовков и предоставляя типобезопасный доступ к ним. Это упрощает работу с сообщениями в обработчиках.

**Архитектурный слой**: Infrastructure (Kafka Integration)

**Файлы для создания**:
- `/Users/ilya/IdeaProjects/otus-kotlin-backend-developer-professional/arbitrage-scanner/arbitrage-scanner-kafka/src/main/kotlin/com/arbitrage/scanner/models/KafkaMessage.kt`

**Содержимое**:
```kotlin
package com.arbitrage.scanner.models

import org.apache.kafka.clients.consumer.ConsumerRecord

/**
 * Представление Kafka сообщения с извлеченными метаданными
 */
data class KafkaMessage(
    val correlationId: String,
    val requestType: String,
    val timestamp: Long,
    val payload: String
)

/**
 * Преобразует ConsumerRecord в KafkaMessage, извлекая данные из заголовков
 */
fun ConsumerRecord<String, String>.toKafkaMessage(): KafkaMessage {
    val correlationId = headers().lastHeader("correlation-id")
        ?.value()?.decodeToString() ?: key() ?: "unknown"

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

**Зависимости**: Subtask-3

**Критерии выполнения**:
- Создан файл `KafkaMessage.kt` с data классом
- Data класс содержит поля: correlationId, requestType, timestamp, payload
- Реализована extension функция `ConsumerRecord.toKafkaMessage()`
- Функция извлекает correlation-id из заголовков (fallback на key)
- Функция извлекает request-type из заголовков (fallback на "UNKNOWN")
- Функция извлекает timestamp из заголовков (fallback на текущее время)
- Payload берется из value() записи

**Критерии приемки**:
- Проект компилируется: `./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:compileKotlin`
- KafkaMessage может быть создан из ConsumerRecord
- Обработка fallback значений работает корректно

**Команды для проверки**:
```bash
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:compileKotlin
```

---

### Subtask-12: Создать KafkaContextProcessor

**Статус**: [ ]

**Описание**:
Адаптировать логику обработки контекста из Ktor модуля для работы в Kafka окружении без зависимости от ApplicationCall.

**Цель и обоснование**:
KafkaContextProcessor обеспечивает единый паттерн обработки контекста между HTTP и Kafka транспортами, переиспользуя логику логирования и обработки ошибок.

**Архитектурный слой**: Application Layer (Processing)

**Файлы для создания**:
- `/Users/ilya/IdeaProjects/otus-kotlin-backend-developer-professional/arbitrage-scanner/arbitrage-scanner-kafka/src/main/kotlin/com/arbitrage/scanner/processors/KafkaContextProcessor.kt`

**Содержимое**:
```kotlin
package com.arbitrage.scanner.processors

import com.arbitrage.scanner.business.logic.BusinessLogicProcessor
import com.arbitrage.scanner.common.models.Context
import com.arbitrage.scanner.common.models.State
import com.arbitrage.scanner.libs.logging.common.ArbScanLoggerProvider
import com.arbitrage.scanner.common.helpers.asError

/**
 * Обработка контекста для Kafka транспорта
 */
suspend fun processContext(
    context: Context,
    businessLogicProcessor: BusinessLogicProcessor,
    loggerProvider: ArbScanLoggerProvider
) {
    val logger = loggerProvider.logger("KafkaContextProcessor")

    try {
        logger.info(
            msg = "Request processing started",
            marker = "BIZ",
            data = "requestId=${context.requestId}, command=${context.command}"
        )

        businessLogicProcessor.exec(context)

        logger.info(
            msg = "Request processing completed",
            marker = "BIZ",
            data = "requestId=${context.requestId}, state=${context.state}"
        )

    } catch (throwable: Throwable) {
        logger.error(
            msg = "Request processing failed",
            marker = "BIZ",
            data = "requestId=${context.requestId}",
            e = throwable
        )
        context.state = State.FAILING
        context.internalErrors.add(throwable.asError())
    }
}
```

**Зависимости**: Subtask-3

**Критерии выполнения**:
- Создан файл `KafkaContextProcessor.kt`
- Реализована suspend функция `processContext`
- Функция принимает Context, BusinessLogicProcessor и ArbScanLoggerProvider
- Логирование начала обработки с информацией о requestId и command
- Вызов BusinessLogicProcessor.exec(context)
- Логирование завершения обработки с информацией о state
- Обработка исключений с добавлением ошибки в context.internalErrors
- Установка state = FAILING при ошибках

**Критерии приемки**:
- Проект компилируется: `./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:compileKotlin`
- Функция может быть вызвана с корректными параметрами
- Паттерн обработки соответствует существующему в Ktor модуле

**Команды для проверки**:
```bash
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:compileKotlin
```

---

### Subtask-13: Реализовать KafkaMessageProcessor

**Статус**: [ ]

**Описание**:
Создать процессор, который координирует десериализацию запроса, создание контекста, вызов бизнес-логики и отправку ответа.

**Цель и обоснование**:
KafkaMessageProcessor - это центральный компонент обработки сообщений. Он связывает воедино десериализацию, бизнес-логику и отправку ответов, обеспечивая полный цикл обработки запроса.

**Архитектурный слой**: Application Layer (Processing)

**Файлы для создания**:
- `/Users/ilya/IdeaProjects/otus-kotlin-backend-developer-professional/arbitrage-scanner/arbitrage-scanner-kafka/src/main/kotlin/com/arbitrage/scanner/processors/KafkaMessageProcessor.kt`

**Содержимое**:
```kotlin
package com.arbitrage.scanner.processors

import com.arbitrage.scanner.api.v1.fromRequestJsonString
import com.arbitrage.scanner.api.v1.mappers.fromTransport
import com.arbitrage.scanner.api.v1.mappers.toTransport
import com.arbitrage.scanner.api.v1.models.IRequest
import com.arbitrage.scanner.api.v1.models.IResponse
import com.arbitrage.scanner.business.logic.BusinessLogicProcessor
import com.arbitrage.scanner.common.models.Context
import com.arbitrage.scanner.common.models.RequestId
import com.arbitrage.scanner.common.models.Timestamp
import com.arbitrage.scanner.libs.logging.common.ArbScanLoggerProvider
import com.arbitrage.scanner.models.KafkaMessage
import com.arbitrage.scanner.producers.ResponseProducer
import kotlinx.serialization.json.Json

/**
 * Процессор Kafka сообщений - координирует весь цикл обработки
 */
class KafkaMessageProcessor(
    private val businessLogicProcessor: BusinessLogicProcessor,
    private val responseProducer: ResponseProducer,
    private val json: Json,
    private val loggerProvider: ArbScanLoggerProvider
) {
    private val logger = loggerProvider.logger(KafkaMessageProcessor::class)

    suspend fun processMessage(message: KafkaMessage) {
        try {
            logger.debug(
                msg = "Starting message processing",
                marker = "KAFKA_PROCESSOR",
                data = "correlationId=${message.correlationId}"
            )

            // 1. Десериализация запроса
            val request = deserializeRequest(message.payload)

            // 2. Создание и заполнение контекста
            val context = createContext(message)
            request.fromTransport(context)

            // 3. Обработка бизнес-логики
            processContext(
                context = context,
                businessLogicProcessor = businessLogicProcessor,
                loggerProvider = loggerProvider
            )

            // 4. Преобразование в Response
            val response = context.toTransport()

            // 5. Отправка ответа
            responseProducer.sendResponse(message.correlationId, response)

            logger.info(
                msg = "Message processed successfully",
                marker = "KAFKA_PROCESSOR",
                data = "correlationId=${message.correlationId}"
            )

        } catch (e: Exception) {
            logger.error(
                msg = "Failed to process message",
                marker = "KAFKA_PROCESSOR",
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
        try {
            // Создать response с общей ошибкой
            val errorResponse = createErrorResponse(error)
            responseProducer.sendResponse(correlationId, errorResponse)
        } catch (e: Exception) {
            logger.error(
                msg = "Failed to send error response",
                marker = "KAFKA_PROCESSOR",
                data = "correlationId=$correlationId",
                e = e
            )
        }
    }

    private fun createErrorResponse(error: Throwable): IResponse {
        // Упрощенная реализация - создает базовый error response
        // TODO: Реализовать создание правильного типа response с ошибками
        return com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityReadResponse(
            result = com.arbitrage.scanner.api.v1.models.ResponseResult.ERROR,
            errors = listOf(
                com.arbitrage.scanner.api.v1.models.Error(
                    code = "PROCESSING_ERROR",
                    group = "internal",
                    field = "",
                    message = error.message ?: "Unknown error"
                )
            )
        )
    }
}
```

**Зависимости**: Subtask-9, Subtask-11, Subtask-12

**Критерии выполнения**:
- Создан файл `KafkaMessageProcessor.kt` с классом
- Конструктор принимает BusinessLogicProcessor, ResponseProducer, Json, ArbScanLoggerProvider
- Реализован метод `processMessage(message: KafkaMessage)`
- Последовательность обработки: десериализация → создание контекста → fromTransport → бизнес-логика → toTransport → отправка ответа
- Десериализация использует `json.fromRequestJsonString<IRequest>`
- Контекст создается с RequestId из correlationId и Timestamp
- Используется `request.fromTransport(context)` для заполнения контекста
- Используется `processContext` для вызова бизнес-логики
- Используется `context.toTransport()` для создания response
- Обработка исключений с отправкой error response
- Логирование на всех этапах

**Критерии приемки**:
- Проект компилируется: `./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:compileKotlin`
- Класс корректно связывает все компоненты обработки
- Логика обработки полная (от сообщения до ответа)

**Команды для проверки**:
```bash
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:compileKotlin
```

---

### Subtask-14: Создать unit тесты для KafkaMessageProcessor

**Статус**: [ ]

**Описание**:
Написать тесты для проверки корректности обработки сообщений, включая успешные сценарии и обработку ошибок.

**Цель и обоснование**:
Unit тесты гарантируют, что процессор корректно координирует все этапы обработки, правильно обрабатывает исключения и взаимодействует с зависимостями.

**Архитектурный слой**: Application Layer (Testing)

**Файлы для создания**:
- `/Users/ilya/IdeaProjects/otus-kotlin-backend-developer-professional/arbitrage-scanner/arbitrage-scanner-kafka/src/test/kotlin/com/arbitrage/scanner/processors/KafkaMessageProcessorTest.kt`

**Содержимое**:
```kotlin
package com.arbitrage.scanner.processors

import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityReadRequest
import com.arbitrage.scanner.business.logic.BusinessLogicProcessor
import com.arbitrage.scanner.common.models.Context
import com.arbitrage.scanner.libs.logging.common.ArbScanLoggerProvider
import com.arbitrage.scanner.libs.logging.common.ArbScanLogWrapper
import com.arbitrage.scanner.models.KafkaMessage
import com.arbitrage.scanner.producers.ResponseProducer
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertNotNull

class KafkaMessageProcessorTest {

    private val json = Json {
        prettyPrint = false
        isLenient = true
        ignoreUnknownKeys = true
    }

    private val mockLogger = object : ArbScanLogWrapper {
        override fun info(msg: String, marker: String?, data: Any?) {}
        override fun warn(msg: String, marker: String?, data: Any?, e: Throwable?) {}
        override fun error(msg: String, marker: String?, data: Any?, e: Throwable?) {}
        override fun debug(msg: String, marker: String?, data: Any?) {}
    }

    private val mockLoggerProvider = object : ArbScanLoggerProvider {
        override fun logger(loggerId: String) = mockLogger
        override fun logger(clazz: kotlin.reflect.KClass<*>) = mockLogger
    }

    private val mockBusinessLogic = object : BusinessLogicProcessor {
        override suspend fun exec(ctx: Context) {
            // Simple stub implementation
        }
    }

    private var capturedCorrelationId: String? = null
    private val mockProducer = object : ResponseProducer {
        override suspend fun sendResponse(correlationId: String, response: com.arbitrage.scanner.api.v1.models.IResponse) {
            capturedCorrelationId = correlationId
        }
        override suspend fun close() {}
    }

    @Test
    fun `should process valid read request message`() = runBlocking {
        // Given
        val processor = KafkaMessageProcessor(
            businessLogicProcessor = mockBusinessLogic,
            responseProducer = mockProducer,
            json = json,
            loggerProvider = mockLoggerProvider
        )

        val request = ArbitrageOpportunityReadRequest(id = "test-123")
        val requestJson = json.encodeToString(
            com.arbitrage.scanner.api.v1.models.IRequest.serializer(),
            request
        )

        val message = KafkaMessage(
            correlationId = "test-correlation-id",
            requestType = "READ",
            timestamp = System.currentTimeMillis(),
            payload = requestJson
        )

        // When
        processor.processMessage(message)

        // Then
        assertNotNull(capturedCorrelationId)
        kotlin.test.assertEquals("test-correlation-id", capturedCorrelationId)
    }

    // Дополнительные тесты будут добавлены по мере развития
}
```

**Зависимости**: Subtask-13

**Критерии выполнения**:
- Создан файл `KafkaMessageProcessorTest.kt`
- Реализован тест для обработки валидного read request
- Используются mock объекты для зависимостей (BusinessLogicProcessor, ResponseProducer, Logger)
- Тест проверяет, что ответ отправляется с правильным correlationId
- Используется Kotlin Test framework

**Критерии приемки**:
- Проект компилируется: `./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:compileTestKotlin`
- Тесты проходят: `./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:test`
- Тест покрывает основной happy path сценарий

**Команды для проверки**:
```bash
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:test --tests "com.arbitrage.scanner.processors.KafkaMessageProcessorTest"
```

---

## Группа 5: Kafka Consumer (прием запросов)

### Subtask-15: Реализовать KafkaRequestConsumer

**Статус**: [ ]

**Описание**:
Создать компонент, который подписывается на Kafka топик, получает сообщения, обрабатывает их через KafkaMessageProcessor и управляет offset'ами.

**Цель и обоснование**:
KafkaRequestConsumer - это входная точка для получения запросов из Kafka. Он управляет жизненным циклом Consumer, параллельной обработкой сообщений и гарантирует корректный commit offset'ов.

**Архитектурный слой**: Infrastructure (Kafka Integration)

**Файлы для создания**:
- `/Users/ilya/IdeaProjects/otus-kotlin-backend-developer-professional/arbitrage-scanner/arbitrage-scanner-kafka/src/main/kotlin/com/arbitrage/scanner/consumers/KafkaRequestConsumer.kt`

**Содержимое**:
```kotlin
package com.arbitrage.scanner.consumers

import com.arbitrage.scanner.config.ConsumerConfig
import com.arbitrage.scanner.config.KafkaConfig
import com.arbitrage.scanner.libs.logging.common.ArbScanLogWrapper
import com.arbitrage.scanner.models.toKafkaMessage
import com.arbitrage.scanner.processors.KafkaMessageProcessor
import kotlinx.coroutines.*
import org.apache.kafka.clients.consumer.ConsumerConfig as ApacheConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.errors.WakeupException
import org.apache.kafka.common.serialization.StringDeserializer
import java.time.Duration
import java.util.*

/**
 * Kafka Consumer для приема запросов из топика
 */
class KafkaRequestConsumer(
    private val config: ConsumerConfig,
    private val kafkaConfig: KafkaConfig,
    private val messageProcessor: KafkaMessageProcessor,
    private val logger: ArbScanLogWrapper
) {

    private val consumer: KafkaConsumer<String, String>
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    @Volatile
    private var isRunning = false

    init {
        val props = Properties().apply {
            put(ApacheConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.bootstrapServers)
            put(ApacheConsumerConfig.GROUP_ID_CONFIG, kafkaConfig.groupId)
            put(ApacheConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            put(ApacheConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            put(ApacheConsumerConfig.AUTO_OFFSET_RESET_CONFIG, config.autoOffsetReset)
            put(ApacheConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, config.enableAutoCommit)
            put(ApacheConsumerConfig.MAX_POLL_RECORDS_CONFIG, config.maxPollRecords)
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
            marker = "KAFKA_CONSUMER",
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
                    marker = "KAFKA_CONSUMER",
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
            marker = "KAFKA_CONSUMER",
            data = "workerId=$workerId, correlationId=${message.correlationId}"
        )

        messageProcessor.processMessage(message)
    }

    suspend fun shutdown() {
        logger.info(msg = "Shutting down Kafka consumer", marker = "KAFKA_CONSUMER")

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

        logger.info(msg = "Kafka consumer shut down", marker = "KAFKA_CONSUMER")
    }
}
```

**Зависимости**: Subtask-4, Subtask-11, Subtask-13

**Критерии выполнения**:
- Создан файл `KafkaRequestConsumer.kt` с классом
- Инициализация KafkaConsumer с настройками из конфигурации
- Подписка на топик из конфигурации
- Реализован метод `start()` для запуска обработки
- Создание N корутин для параллельной обработки (N = config.concurrency)
- Реализован `consumeLoop` для каждого worker'а
- Polling сообщений с коротким таймаутом (100ms)
- Обработка каждой записи через KafkaMessageProcessor
- Manual commit offset'ов после обработки batch
- Обработка WakeupException для graceful shutdown
- Реализован метод `shutdown()` с корректным завершением
- Логирование на всех этапах
- Использование корутин с SupervisorJob для изоляции ошибок

**Критерии приемки**:
- Проект компилируется: `./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:compileKotlin`
- Consumer корректно подписывается на топик
- Параллельная обработка работает (concurrency > 1)
- Graceful shutdown реализован корректно

**Команды для проверки**:
```bash
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:compileKotlin
```

---

## Группа 6: Dependency Injection и Application

### Subtask-16: Создать Koin конфигурацию

**Статус**: [ ]

**Описание**:
Настроить Koin DI модули для всех компонентов приложения: конфигурация, JSON, бизнес-логика, логирование, Kafka компоненты.

**Цель и обоснование**:
Koin DI обеспечивает централизованное управление зависимостями, упрощает тестирование и следует принципу Dependency Inversion. Все компоненты должны быть зарегистрированы в DI контейнере.

**Архитектурный слой**: Infrastructure (Dependency Injection)

**Файлы для создания**:
- `/Users/ilya/IdeaProjects/otus-kotlin-backend-developer-professional/arbitrage-scanner/arbitrage-scanner-kafka/src/main/kotlin/com/arbitrage/scanner/KoinConfiguration.kt`

**Содержимое**:
```kotlin
package com.arbitrage.scanner

import com.arbitrage.scanner.business.logic.BusinessLogicProcessor
import com.arbitrage.scanner.business.logic.BusinessLogicProcessorSimpleImpl
import com.arbitrage.scanner.config.AppConfig
import com.arbitrage.scanner.config.ConfigLoader
import com.arbitrage.scanner.config.KafkaConfig
import com.arbitrage.scanner.consumers.KafkaRequestConsumer
import com.arbitrage.scanner.libs.logging.common.ArbScanLoggerProvider
import com.arbitrage.scanner.libs.logging.logback.arbScanLoggerLogback
import com.arbitrage.scanner.processors.KafkaMessageProcessor
import com.arbitrage.scanner.producers.KafkaResponseProducer
import com.arbitrage.scanner.producers.ResponseProducer
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Модуль конфигурации
 */
val configModule = module {
    single { ConfigLoader.load() }
    single { get<AppConfig>().kafka }
}

/**
 * Модуль JSON сериализации
 */
val jsonModule = module {
    single {
        Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        }
    }
}

/**
 * Модуль бизнес-логики
 */
val businessLogicModule = module {
    singleOf(::BusinessLogicProcessorSimpleImpl) bind BusinessLogicProcessor::class
}

/**
 * Модуль логирования
 */
val loggingModule = module {
    single { ArbScanLoggerProvider(::arbScanLoggerLogback) }
}

/**
 * Модуль Kafka компонентов
 */
val kafkaModule = module {
    single<ResponseProducer> {
        KafkaResponseProducer(
            config = get<KafkaConfig>().producer,
            kafkaConfig = get(),
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
            kafkaConfig = get(),
            messageProcessor = get(),
            logger = get<ArbScanLoggerProvider>().logger(KafkaRequestConsumer::class)
        )
    }
}

/**
 * Все модули приложения
 */
val allModules = listOf(
    configModule,
    jsonModule,
    businessLogicModule,
    loggingModule,
    kafkaModule
)
```

**Зависимости**: Subtask-5, Subtask-9, Subtask-13, Subtask-15

**Критерии выполнения**:
- Создан файл `KoinConfiguration.kt` с модулями
- Создан `configModule` с загрузкой конфигурации
- Создан `jsonModule` с настройкой Json serializer
- Создан `businessLogicModule` с BusinessLogicProcessor
- Создан `loggingModule` с ArbScanLoggerProvider
- Создан `kafkaModule` с ResponseProducer, KafkaMessageProcessor, KafkaRequestConsumer
- Создан список `allModules` со всеми модулями
- Используется Koin DSL (module, single, singleOf, bind)
- Все зависимости корректно связаны

**Критерии приемки**:
- Проект компилируется: `./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:compileKotlin`
- Koin модули корректно определены
- Все зависимости могут быть разрешены

**Команды для проверки**:
```bash
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:compileKotlin
```

---

### Subtask-17: Создать главный класс приложения (Application.kt)

**Статус**: [ ]

**Описание**:
Создать класс KafkaApplication с lifecycle management (start, shutdown) и функцию main для запуска приложения.

**Цель и обоснование**:
Application класс управляет жизненным циклом приложения, координирует запуск и завершение компонентов, регистрирует shutdown hooks для graceful shutdown.

**Архитектурный слой**: Infrastructure (Application)

**Файлы для создания**:
- `/Users/ilya/IdeaProjects/otus-kotlin-backend-developer-professional/arbitrage-scanner/arbitrage-scanner-kafka/src/main/kotlin/com/arbitrage/scanner/Application.kt`

**Содержимое**:
```kotlin
package com.arbitrage.scanner

import com.arbitrage.scanner.consumers.KafkaRequestConsumer
import com.arbitrage.scanner.libs.logging.common.ArbScanLoggerProvider
import com.arbitrage.scanner.libs.logging.common.ArbScanLogWrapper
import com.arbitrage.scanner.producers.ResponseProducer
import kotlinx.coroutines.runBlocking
import org.koin.core.context.startKoin

/**
 * Главный класс Kafka приложения
 */
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
            msg = "Arbitrage Scanner Kafka Application started successfully",
            marker = "APP"
        )
    }

    suspend fun shutdown() {
        logger.info(
            msg = "Shutting down Arbitrage Scanner Kafka Application",
            marker = "APP"
        )

        try {
            // 1. Остановить consumer (прекратить прием новых сообщений)
            consumer.shutdown()

            // 2. Закрыть producer (отправить все pending сообщения)
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

/**
 * Точка входа в приложение
 */
fun main() {
    // Инициализация Koin
    val koinApp = startKoin {
        printLogger()
        modules(allModules)
    }
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

**Зависимости**: Subtask-15, Subtask-16

**Критерии выполнения**:
- Создан файл `Application.kt`
- Создан класс `KafkaApplication` с методами `start()` и `shutdown()`
- Реализована функция `main()`
- Инициализация Koin с `startKoin` и регистрацией модулей
- Получение зависимостей из Koin контейнера
- Регистрация shutdown hook для graceful shutdown
- Запуск consumer через `consumer.start()`
- Ожидание завершения через `Thread.currentThread().join()`
- Логирование на всех этапах (start, shutdown, errors)
- Graceful shutdown: сначала consumer, потом producer

**Критерии приемки**:
- Проект компилируется: `./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:build`
- Приложение может быть запущено: `./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:run`
- Shutdown hook корректно завершает приложение
- Логирование работает на всех этапах

**Команды для проверки**:
```bash
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:build
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:run
```

---

### Subtask-18: Создать logback.xml конфигурацию

**Статус**: [ ]

**Описание**:
Создать конфигурационный файл для Logback с настройкой уровней логирования, форматов и appender'ов.

**Цель и обоснование**:
Logback конфигурация определяет, как будут логироваться сообщения, куда они будут направляться (консоль, файлы) и какой формат использовать.

**Архитектурный слой**: Infrastructure (Logging)

**Файлы для создания**:
- `/Users/ilya/IdeaProjects/otus-kotlin-backend-developer-professional/arbitrage-scanner/arbitrage-scanner-kafka/src/main/resources/logback.xml`

**Содержимое**:
```xml
<configuration>
    <!-- Console appender -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- Root logger -->
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
    </root>

    <!-- Application logger -->
    <logger name="com.arbitrage.scanner" level="DEBUG" additivity="false">
        <appender-ref ref="CONSOLE"/>
    </logger>

    <!-- Kafka client logger -->
    <logger name="org.apache.kafka" level="WARN" additivity="false">
        <appender-ref ref="CONSOLE"/>
    </logger>

    <!-- Koin logger -->
    <logger name="org.koin" level="INFO" additivity="false">
        <appender-ref ref="CONSOLE"/>
    </logger>
</configuration>
```

**Зависимости**: Subtask-2

**Критерии выполнения**:
- Создан файл `logback.xml` в директории resources
- Настроен console appender с читаемым форматом
- Настроен root logger с уровнем INFO
- Настроен logger для пакета приложения с уровнем DEBUG
- Настроен logger для Kafka клиента с уровнем WARN
- Настроен logger для Koin с уровнем INFO

**Критерии приемки**:
- XML корректно форматирован (валидный Logback XML)
- Logback успешно парсит конфигурацию при запуске
- Логи выводятся в консоль с правильным форматом

**Команды для проверки**:
```bash
cat arbitrage-scanner/arbitrage-scanner-kafka/src/main/resources/logback.xml
```

---

## Группа 7: Docker и локальное окружение

### Subtask-19: Создать docker-compose для локального Kafka

**Статус**: [ ]

**Описание**:
Создать Docker Compose файл для запуска Kafka и Zookeeper локально для разработки и тестирования.

**Цель и обоснование**:
Docker Compose позволяет разработчикам быстро поднять локальное Kafka окружение без ручной установки. Это критично для локальной разработки и тестирования.

**Архитектурный слой**: Infrastructure (Development Environment)

**Файлы для создания**:
- `/Users/ilya/IdeaProjects/otus-kotlin-backend-developer-professional/arbitrage-scanner/arbitrage-scanner-kafka/docker/docker-compose-kafka.yml`

**Содержимое**:
```yaml
version: '3.8'

services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.8.0
    container_name: arbitrage-scanner-zookeeper
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - "2181:2181"
    networks:
      - kafka-network

  kafka:
    image: confluentinc/cp-kafka:7.8.0
    container_name: arbitrage-scanner-kafka
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
    networks:
      - kafka-network

networks:
  kafka-network:
    driver: bridge
```

**Зависимости**: Subtask-2

**Критерии выполнения**:
- Создан файл `docker-compose-kafka.yml` в директории docker
- Определен сервис Zookeeper с правильными настройками
- Определен сервис Kafka с зависимостью от Zookeeper
- Kafka настроен на порт 9092
- Включено автоматическое создание топиков
- Определена сеть для связи между сервисами
- Используются актуальные версии образов Confluent Platform

**Критерии приемки**:
- Docker Compose файл корректно форматирован (валидный YAML)
- Kafka и Zookeeper запускаются: `docker-compose -f arbitrage-scanner/arbitrage-scanner-kafka/docker/docker-compose-kafka.yml up -d`
- Kafka доступен на localhost:9092
- Топики могут быть созданы автоматически

**Команды для проверки**:
```bash
docker-compose -f arbitrage-scanner/arbitrage-scanner-kafka/docker/docker-compose-kafka.yml up -d
docker-compose -f arbitrage-scanner/arbitrage-scanner-kafka/docker/docker-compose-kafka.yml ps
docker exec -it arbitrage-scanner-kafka kafka-topics --list --bootstrap-server localhost:9092
docker-compose -f arbitrage-scanner/arbitrage-scanner-kafka/docker/docker-compose-kafka.yml down
```

---

## Группа 8: Интеграционное тестирование

### Subtask-20: Создать интеграционный тест с Testcontainers

**Статус**: [ ]

**Описание**:
Написать интеграционный тест, который запускает реальный Kafka в Docker контейнере, отправляет запрос и проверяет получение ответа.

**Цель и обоснование**:
Интеграционные тесты с Testcontainers проверяют работу всей системы в условиях, максимально приближенных к реальным. Это гарантирует, что все компоненты корректно взаимодействуют друг с другом.

**Архитектурный слой**: Application Layer (Testing)

**Файлы для создания**:
- `/Users/ilya/IdeaProjects/otus-kotlin-backend-developer-professional/arbitrage-scanner/arbitrage-scanner-kafka/src/test/kotlin/com/arbitrage/scanner/KafkaApplicationIntegrationTest.kt`

**Содержимое**:
```kotlin
package com.arbitrage.scanner

import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityReadRequest
import com.arbitrage.scanner.api.v1.models.ArbitrageOpportunityReadResponse
import com.arbitrage.scanner.api.v1.models.IRequest
import com.arbitrage.scanner.api.v1.models.IResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName
import java.time.Duration
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class KafkaApplicationIntegrationTest {

    private val json = Json {
        prettyPrint = false
        isLenient = true
        ignoreUnknownKeys = true
    }

    @Test
    fun `should process read request and send response through Kafka`() = runBlocking {
        // Given: Start Kafka container
        val kafka = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.8.0"))
        kafka.start()

        try {
            val bootstrapServers = kafka.bootstrapServers

            // Create topics
            createTopic(bootstrapServers, "arbitrage-scanner-requests")
            createTopic(bootstrapServers, "arbitrage-scanner-responses")

            // Create test producer
            val testProducer = createTestProducer(bootstrapServers)

            // Create test consumer
            val testConsumer = createTestConsumer(bootstrapServers)
            testConsumer.subscribe(listOf("arbitrage-scanner-responses"))

            // When: Send request
            val correlationId = UUID.randomUUID().toString()
            val request = ArbitrageOpportunityReadRequest(id = "test-id-123")

            sendTestRequest(testProducer, correlationId, request)

            // Then: Receive response
            val response = withTimeout(10000) {
                receiveTestResponse(testConsumer, correlationId)
            }

            assertNotNull(response)
            assertTrue(response is ArbitrageOpportunityReadResponse)

            testProducer.close()
            testConsumer.close()

        } finally {
            kafka.stop()
        }
    }

    private fun createTopic(bootstrapServers: String, topicName: String) {
        // Simplified - in real test would use AdminClient
        // For now, rely on auto-create-topics
    }

    private fun createTestProducer(bootstrapServers: String): KafkaProducer<String, String> {
        val props = Properties().apply {
            put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
        }
        return KafkaProducer(props)
    }

    private fun createTestConsumer(bootstrapServers: String): KafkaConsumer<String, String> {
        val props = Properties().apply {
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
            put(ConsumerConfig.GROUP_ID_CONFIG, "test-consumer-group")
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        }
        return KafkaConsumer(props)
    }

    private fun sendTestRequest(
        producer: KafkaProducer<String, String>,
        correlationId: String,
        request: IRequest
    ) {
        val jsonValue = json.encodeToString(IRequest.serializer(), request)

        val record = ProducerRecord<String, String>(
            "arbitrage-scanner-requests",
            correlationId,
            jsonValue
        ).apply {
            headers().add("correlation-id", correlationId.toByteArray())
            headers().add("request-type", "READ".toByteArray())
            headers().add("timestamp", System.currentTimeMillis().toString().toByteArray())
        }

        producer.send(record).get()
    }

    private suspend fun receiveTestResponse(
        consumer: KafkaConsumer<String, String>,
        expectedCorrelationId: String
    ): IResponse? {
        repeat(100) { // Try 100 times with delay
            val records = consumer.poll(Duration.ofMillis(100))

            for (record in records) {
                val correlationId = record.headers().lastHeader("correlation-id")
                    ?.value()?.decodeToString()

                if (correlationId == expectedCorrelationId) {
                    return json.decodeFromString(IResponse.serializer(), record.value())
                }
            }

            delay(100)
        }

        return null
    }
}
```

**Зависимости**: Subtask-17

**Критерии выполнения**:
- Создан файл `KafkaApplicationIntegrationTest.kt`
- Использован Testcontainers для запуска Kafka
- Реализован тест отправки read request и получения response
- Создаются test producer и consumer
- Отправка запроса с правильными заголовками
- Получение ответа с проверкой correlation ID
- Проверка типа ответа (ArbitrageOpportunityReadResponse)
- Корректное завершение контейнеров и ресурсов

**Критерии приемки**:
- Проект компилируется: `./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:compileTestKotlin`
- Тест проходит: `./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:test`
- Testcontainers корректно запускает и останавливает Kafka
- Запрос и ответ проходят через Kafka

**Команды для проверки**:
```bash
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:test --tests "com.arbitrage.scanner.KafkaApplicationIntegrationTest"
```

---

## Группа 9: Документация

### Subtask-21: Создать README.md для модуля

**Статус**: [ ]

**Описание**:
Создать документацию с инструкциями по установке, настройке, запуску и использованию Kafka модуля.

**Цель и обоснование**:
README предоставляет разработчикам и пользователям полную информацию о том, как работать с модулем. Это критично для onboarding новых разработчиков и эксплуатации системы.

**Архитектурный слой**: Documentation

**Файлы для создания**:
- `/Users/ilya/IdeaProjects/otus-kotlin-backend-developer-professional/arbitrage-scanner/arbitrage-scanner-kafka/README.md`

**Содержимое**:
```markdown
# Arbitrage Scanner Kafka Transport

Асинхронный транспортный модуль для Arbitrage Scanner, использующий Apache Kafka для обработки запросов и ответов через message broker.

## Описание

Модуль обеспечивает альтернативный транспортный механизм для Arbitrage Scanner, реализуя Request-Reply паттерн поверх Apache Kafka. Это позволяет использовать асинхронную обработку запросов с преимуществами Kafka: масштабируемость, надежность, распределенность.

## Архитектура

```
Client → Kafka (requests topic) → Consumer → Message Processor → Business Logic → Producer → Kafka (responses topic) → Client
```

### Основные компоненты

- **KafkaRequestConsumer** - принимает запросы из топика
- **KafkaMessageProcessor** - координирует обработку сообщений
- **KafkaResponseProducer** - отправляет ответы в топик
- **BusinessLogicProcessor** - переиспользуется из core модуля

## Требования

- Java 21
- Apache Kafka 2.8+ (для production)
- Docker и Docker Compose (для локальной разработки)

## Быстрый старт

### 1. Запуск локального Kafka

```bash
docker-compose -f docker/docker-compose-kafka.yml up -d
```

### 2. Создание топиков (опционально, если auto-create отключен)

```bash
docker exec -it arbitrage-scanner-kafka kafka-topics --create \
  --topic arbitrage-scanner-requests \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 1

docker exec -it arbitrage-scanner-kafka kafka-topics --create \
  --topic arbitrage-scanner-responses \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 1
```

### 3. Запуск приложения

```bash
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:run
```

## Конфигурация

Конфигурация находится в `src/main/resources/application.yaml`.

### Основные параметры

```yaml
kafka:
  bootstrap-servers: "localhost:9092"  # Адрес Kafka брокера
  group-id: "arbitrage-scanner-group"  # Consumer group ID

  consumer:
    topic: "arbitrage-scanner-requests"  # Топик для запросов
    concurrency: 3                       # Количество параллельных обработчиков

  producer:
    topic: "arbitrage-scanner-responses" # Топик для ответов
    acks: "all"                          # Уровень гарантий доставки
```

## Отправка запросов

### Через kafka-console-producer

```bash
docker exec -it arbitrage-scanner-kafka kafka-console-producer \
  --topic arbitrage-scanner-requests \
  --bootstrap-server localhost:9092 \
  --property "parse.key=true" \
  --property "key.separator=:"

# В интерактивном режиме:
test-id:{"requestType":"read","id":"123"}
```

### Через Kafka Producer (Kotlin)

```kotlin
val producer = KafkaProducer<String, String>(props)
val correlationId = UUID.randomUUID().toString()
val request = ArbitrageOpportunityReadRequest(id = "123")

val record = ProducerRecord(
    "arbitrage-scanner-requests",
    correlationId,
    json.encodeToString(IRequest.serializer(), request)
).apply {
    headers().add("correlation-id", correlationId.toByteArray())
    headers().add("request-type", "READ".toByteArray())
}

producer.send(record).get()
```

## Получение ответов

```bash
docker exec -it arbitrage-scanner-kafka kafka-console-consumer \
  --topic arbitrage-scanner-responses \
  --bootstrap-server localhost:9092 \
  --from-beginning \
  --property "print.key=true" \
  --property "print.headers=true"
```

## Тестирование

### Unit тесты

```bash
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:test
```

### Интеграционные тесты

```bash
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:test --tests "*IntegrationTest"
```

## Команды для разработки

```bash
# Сборка модуля
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:build

# Запуск приложения
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:run

# Проверка кода
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:check

# Просмотр зависимостей
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:dependencies
```

## Структура проекта

```
arbitrage-scanner-kafka/
├── src/
│   ├── main/
│   │   ├── kotlin/com/arbitrage/scanner/
│   │   │   ├── Application.kt              # Точка входа
│   │   │   ├── KoinConfiguration.kt        # DI настройка
│   │   │   ├── config/                     # Конфигурация
│   │   │   ├── consumers/                  # Kafka Consumer
│   │   │   ├── producers/                  # Kafka Producer
│   │   │   ├── processors/                 # Обработка сообщений
│   │   │   └── models/                     # Модели данных
│   │   └── resources/
│   │       ├── application.yaml            # Конфигурация
│   │       └── logback.xml                 # Логирование
│   └── test/                               # Тесты
├── docker/
│   └── docker-compose-kafka.yml            # Локальный Kafka
└── build.gradle.kts
```

## Troubleshooting

### Проблема: Приложение не может подключиться к Kafka

**Решение**: Убедитесь, что Kafka запущен и доступен на указанном адресе:

```bash
docker ps | grep kafka
telnet localhost 9092
```

### Проблема: Сообщения не обрабатываются

**Решение**: Проверьте логи приложения и consumer group:

```bash
docker exec -it arbitrage-scanner-kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --group arbitrage-scanner-group \
  --describe
```

## Ссылки

- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [Testcontainers Kafka Module](https://www.testcontainers.org/modules/kafka/)
```

**Зависимости**: Subtask-17, Subtask-19

**Критерии выполнения**:
- Создан файл `README.md` в корне модуля
- Описание модуля и его назначения
- Раздел с архитектурой и компонентами
- Инструкции по быстрому старту
- Описание конфигурации с примерами
- Примеры отправки запросов (console и code)
- Примеры получения ответов
- Команды для разработки и тестирования
- Структура проекта
- Раздел Troubleshooting
- Ссылки на внешнюю документацию

**Критерии приемки**:
- README содержит всю необходимую информацию для начала работы
- Примеры кода корректны и работают
- Markdown корректно форматирован
- Инструкции актуальны и полны

**Команды для проверки**:
```bash
cat arbitrage-scanner/arbitrage-scanner-kafka/README.md
```

---

### Subtask-22: Обновить общую документацию проекта

**Статус**: [ ]

**Описание**:
Добавить информацию о Kafka модуле в ARCHITECTURE.md и CLAUDE.md в корне проекта.

**Цель и обоснование**:
Общая документация проекта должна отражать новый модуль, чтобы разработчики понимали архитектуру системы в целом и могли использовать правильные команды.

**Архитектурный слой**: Documentation

**Файлы для изменения**:
- `/Users/ilya/IdeaProjects/otus-kotlin-backend-developer-professional/ARCHITECTURE.md`
- `/Users/ilya/IdeaProjects/otus-kotlin-backend-developer-professional/CLAUDE.md`

**Изменения в ARCHITECTURE.md**:

Добавить раздел после описания arbitrage-scanner-ktor:

```markdown
### Kafka Transport Layer (arbitrage-scanner-kafka)

**Модуль**: `arbitrage-scanner-kafka`

**Ответственность**:
- Асинхронная обработка запросов через Apache Kafka
- Реализация Request-Reply паттерна поверх message broker
- Прием запросов из Kafka топика
- Отправка ответов в Kafka топик

**Ключевые компоненты**:
- `KafkaRequestConsumer` - прием сообщений из топика
- `KafkaResponseProducer` - отправка ответов в топик
- `KafkaMessageProcessor` - координация обработки
- `KafkaContextProcessor` - адаптация processContext для Kafka
- Configuration models (AppConfig, KafkaConfig, etc.)

**Архитектурные особенности**:
- Переиспользование BusinessLogicProcessor из core модуля
- Переиспользование мапперов fromTransport/toTransport
- Manual offset management для надежности
- Parallel processing через корутины
- Graceful shutdown
```

**Изменения в CLAUDE.md**:

Добавить команды в раздел "Основные команды для разработки":

```markdown
### Kafka модуль

```bash
# Сборка
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:build

# Запуск
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:run

# Тестирование
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:test

# Локальный Kafka
docker-compose -f arbitrage-scanner/arbitrage-scanner-kafka/docker/docker-compose-kafka.yml up -d
docker-compose -f arbitrage-scanner/arbitrage-scanner-kafka/docker/docker-compose-kafka.yml down
```
```

**Зависимости**: Subtask-21

**Критерии выполнения**:
- Добавлен раздел о Kafka модуле в ARCHITECTURE.md
- Описаны ответственность и ключевые компоненты
- Добавлены команды для работы с Kafka модулем в CLAUDE.md
- Документация согласована с существующей структурой

**Критерии приемки**:
- ARCHITECTURE.md содержит информацию о Kafka модуле
- CLAUDE.md содержит команды для работы с Kafka модулем
- Markdown корректно форматирован
- Документация актуальна

**Команды для проверки**:
```bash
grep -A 10 "arbitrage-scanner-kafka" ARCHITECTURE.md
grep -A 10 "Kafka модуль" CLAUDE.md
```

---

## Группа 10: Финальная проверка и оптимизация

### Subtask-23: Выполнить полное тестирование системы

**Статус**: [ ]

**Описание**:
Провести комплексное ручное и автоматизированное тестирование всей системы: unit тесты, интеграционные тесты, ручное тестирование с реальным Kafka.

**Цель и обоснование**:
Финальное тестирование гарантирует, что все компоненты работают корректно как по отдельности, так и вместе. Это последняя проверка перед финализацией задачи.

**Архитектурный слой**: Testing (All Layers)

**Задачи**:
1. Запустить все unit тесты
2. Запустить все интеграционные тесты
3. Запустить локальный Kafka
4. Запустить приложение
5. Отправить test запросы (read, search)
6. Проверить логи приложения
7. Проверить graceful shutdown
8. Проверить обработку ошибок

**Зависимости**: Все предыдущие задачи

**Критерии выполнения**:
- Все unit тесты проходят успешно
- Все интеграционные тесты проходят успешно
- Приложение запускается без ошибок
- Kafka consumer успешно подключается
- Запросы обрабатываются корректно
- Ответы отправляются с правильными correlation ID
- Логирование работает на всех уровнях
- Graceful shutdown завершает приложение корректно
- Ошибки обрабатываются и логируются

**Критерии приемки**:
- Команда `./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:test` успешна (exit code 0)
- Команда `./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:build` успешна (exit code 0)
- Приложение запускается и работает стабильно
- Ручное тестирование подтверждает корректность работы

**Команды для проверки**:
```bash
# Unit и интеграционные тесты
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:test

# Полная сборка
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:build

# Проверка всего проекта
./gradlew check

# Запуск локального окружения
docker-compose -f arbitrage-scanner/arbitrage-scanner-kafka/docker/docker-compose-kafka.yml up -d

# Запуск приложения
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:run

# В другом терминале: отправка тестового запроса
docker exec -it arbitrage-scanner-kafka kafka-console-producer \
  --topic arbitrage-scanner-requests \
  --bootstrap-server localhost:9092 \
  --property "parse.key=true" \
  --property "key.separator=:"
# Ввести: test-id:{"requestType":"read","id":"test-123"}

# Проверка ответов
docker exec -it arbitrage-scanner-kafka kafka-console-consumer \
  --topic arbitrage-scanner-responses \
  --bootstrap-server localhost:9092 \
  --from-beginning
```

---

### Subtask-24: Code review и рефакторинг

**Статус**: [ ]

**Описание**:
Провести ревью всего написанного кода, проверить соответствие стандартам проекта, оптимизировать код, добавить недостающие комментарии.

**Цель и обоснование**:
Code review обеспечивает качество кода, соответствие best practices и архитектурным принципам проекта. Это критично для поддерживаемости и развития системы.

**Архитектурный слой**: All Layers

**Задачи**:
1. Проверить соответствие naming conventions
2. Проверить наличие необходимых комментариев
3. Проверить error handling во всех компонентах
4. Проверить корректность использования корутин
5. Оптимизировать imports (удалить неиспользуемые)
6. Проверить code style
7. Улучшить читаемость кода где необходимо
8. Добавить KDoc комментарии для публичных API

**Зависимости**: Subtask-23

**Критерии выполнения**:
- Все классы и функции имеют понятные имена
- Публичные API имеют KDoc комментарии
- Error handling присутствует во всех критических местах
- Корутины используются корректно (правильные dispatchers)
- Нет неиспользуемых imports
- Code style соответствует проекту
- Код легко читается и понимается

**Критерии приемки**:
- Code style check проходит: `./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:check`
- Код проходит review (самостоятельный или peer review)
- Нет очевидных проблем с качеством кода
- Все публичные API задокументированы

**Команды для проверки**:
```bash
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:check
```

---

## Итоговая статистика

**Всего подзадач**: 24

**Группировка по типам**:
- Настройка проекта: 3 задачи (Subtask 1-3)
- Конфигурация: 4 задачи (Subtask 4-7)
- Producer: 3 задачи (Subtask 8-10)
- Message Processing: 4 задачи (Subtask 11-14)
- Consumer: 1 задача (Subtask 15)
- DI и Application: 3 задачи (Subtask 16-18)
- Docker: 1 задача (Subtask 19)
- Интеграционное тестирование: 1 задача (Subtask 20)
- Документация: 2 задачи (Subtask 21-22)
- Финализация: 2 задачи (Subtask 23-24)

**Зависимости**:
- Линейные зависимости внутри каждой группы
- Некоторая параллельность между группами возможна

**Порядок выполнения** (оптимальный):
1. Группа 1 (Subtask 1-3): Базовая настройка проекта
2. Группа 2 (Subtask 4-7): Конфигурация приложения
3. Параллельно:
   - Группа 3 (Subtask 8-10): Producer
   - Начало Группы 4 (Subtask 11-12): Message models и processor
4. Группа 4 (Subtask 13-14): Message Processor с зависимостью от Producer
5. Группа 5 (Subtask 15): Consumer
6. Группа 6 (Subtask 16-18): DI и Application
7. Группа 7 (Subtask 19): Docker окружение
8. Группа 8 (Subtask 20): Интеграционные тесты
9. Группа 9 (Subtask 21-22): Документация
10. Группа 10 (Subtask 23-24): Финализация

---

## Примечания

- Все файловые пути указаны как абсолютные
- Используются существующие конвенции проекта (package naming, структура)
- Следование SOLID принципам и Clean Architecture
- Переиспользование существующих компонентов (BusinessLogicProcessor, мапперы)
- Корутины для асинхронности
- Manual offset management для надежности
- Graceful shutdown для корректного завершения
- Comprehensive logging на всех уровнях
- Testcontainers для интеграционных тестов