# Модуль arbitrage-scanner-kafka

Модуль для работы с Apache Kafka в проекте Arbitrage Scanner.

## Компоненты

### AppKafkaConsumer

Класс для инкапсуляции подключения к Kafka и управления считыванием сообщений.

#### Основные возможности

- ✅ Автоматическое подключение к Kafka на основе конфигурации
- ✅ Подписка на топики с гибкой обработкой сообщений
- ✅ Автоматическое коммитирование offset'ов
- ✅ Обработка ошибок с логированием
- ✅ Корректное завершение работы (graceful shutdown)
- ✅ Реализация AutoCloseable для безопасной работы с ресурсами

#### Использование

```kotlin
import com.arbitrage.scanner.kafka.AppKafkaConsumer
import com.arbitrage.scanner.kafka.config.KafkaConfigLoader
import com.arbitrage.scanner.libs.logging.ArbScanLoggerProvider
import com.arbitrage.scanner.libs.logging.arbScanLoggerLogback

fun main() {
    // Загрузка конфигурации
    val config = KafkaConfigLoader.load()

    // Инициализация логирования
    val loggerProvider = ArbScanLoggerProvider(::arbScanLoggerLogback)

    // Создание consumer (topics и pollTimeout настраиваются при создании)
    val consumer = AppKafkaConsumer(config, loggerProvider)

    try {
        // Подписка на топик с обработкой сообщений
        consumer.subscribe { record ->
            println("Получено сообщение: ${record.value()}")
            // Ваша логика обработки
        }
    } finally {
        consumer.close()
    }
}
```

#### Кастомизация топиков и timeout

```kotlin
import java.time.Duration

fun main() {
    val config = KafkaConfigLoader.load()
    val loggerProvider = ArbScanLoggerProvider(::arbScanLoggerLogback)

    // Подписка на несколько топиков с кастомным timeout
    val consumer = AppKafkaConsumer(
        config = config,
        loggerProvider = loggerProvider,
        topics = listOf("topic1", "topic2", "topic3"),
        pollTimeout = Duration.ofMillis(500)
    )

    consumer.subscribe { record ->
        println("Сообщение из ${record.topic()}: ${record.value()}")
    }
}
```

#### API

##### Конструктор

```kotlin
AppKafkaConsumer(
    config: KafkaConfig,
    loggerProvider: ArbScanLoggerProvider,
    topics: List<String> = listOf(config.inTopic),
    pollTimeout: Duration = Duration.ofSeconds(1)
)
```

Параметры:
- `config` - конфигурация Kafka (см. KafkaConfig)
- `loggerProvider` - провайдер системы логирования приложения
- `topics` - список топиков для подписки (по умолчанию из `config.inTopic`)
- `pollTimeout` - таймаут опроса Kafka (по умолчанию 1 секунда)

##### Методы

###### subscribe

```kotlin
fun subscribe(
    messageHandler: (ConsumerRecord<String, String>) -> Unit
)
```

Синхронная подписка на топики с блокирующим считыванием сообщений.

Топики и timeout настраиваются при создании экземпляра класса.

Параметры:
- `messageHandler` - функция-обработчик для каждого полученного сообщения

###### stop

```kotlin
fun stop()
```

Останавливает считывание сообщений. Consumer можно остановить из обработчика сообщений или из другого потока.

###### isRunning

```kotlin
fun isRunning(): Boolean
```

Проверяет, работает ли consumer в данный момент.

###### close

```kotlin
fun close()
```

Закрывает соединение с Kafka и освобождает ресурсы. Реализует интерфейс AutoCloseable.

## Конфигурация

Конфигурация загружается из `application.yaml`:

```yaml
kafka:
  host: localhost
  port: 9092
  inTopic: arbitrage-input
  outTopic: arbitrage-output
  groupId: arbitrage-scanner-consumer-group
```

### KafkaConfig

```kotlin
data class KafkaConfig(
    val host: String,           // Хост Kafka сервера
    val port: Int,              // Порт Kafka сервера
    val inTopic: String,        // Топик для чтения
    val outTopic: String,       // Топик для записи
    val groupId: String         // Consumer Group ID
)
```

## Примеры

Готовые примеры использования находятся в `example/ConsumerExample.kt`.

Для запуска примера:

```bash
cd arbitrage-scanner
../gradlew :arbitrage-scanner-kafka:run
```

## Тестирование

Тесты находятся в `src/test/kotlin/com/arbitrage/scanner/kafka/AppKafkaConsumerTest.kt`.

Для запуска тестов:

```bash
cd arbitrage-scanner
../gradlew :arbitrage-scanner-kafka:test
```

## Логирование

Модуль использует кастомную систему логирования проекта (`ArbScanLoggerProvider`) с Logback:

- `INFO` - информация о подключении, подписке и обработке сообщений
- `DEBUG` - детальная информация о количестве обработанных сообщений и каждом сообщении
- `ERROR` - ошибки при обработке сообщений

Пример инициализации логирования:

```kotlin
import com.arbitrage.scanner.libs.logging.ArbScanLoggerProvider
import com.arbitrage.scanner.libs.logging.arbScanLoggerLogback

val loggerProvider = ArbScanLoggerProvider(::arbScanLoggerLogback)
```

## Обработка ошибок

`AppKafkaConsumer` обрабатывает ошибки следующим образом:

1. **Ошибки в messageHandler**: логируются, но не прерывают работу consumer
2. **Ошибки подключения к Kafka**: прерывают работу и пробрасываются наружу
3. **Ошибки при закрытии**: логируются, но не пробрасываются

Пример обработки ошибок:

```kotlin
consumer.subscribe(topics = listOf(config.inTopic)) { record ->
    try {
        processMessage(record.value())
    } catch (e: Exception) {
        logger.error("Ошибка обработки сообщения", e)
        // Consumer продолжит работу
    }
}
```

## Зависимости

- `org.apache.kafka:kafka-clients` - клиент Kafka
- `arbitrage-scanner-lib-logging` - кастомная система логирования проекта
- `arbitrage-scanner-lib-logging-logback` - реализация логирования на базе Logback
- `com.sksamuel.hoplite:hoplite-core` - загрузка конфигурации
- `com.sksamuel.hoplite:hoplite-yaml` - поддержка YAML

## Лучшие практики

1. **Всегда закрывайте consumer**: используйте `try-finally` или `use` блоки
2. **Обрабатывайте shutdown**: добавляйте shutdown hook для корректного завершения
3. **Настройте pollTimeout**: выбирайте значение в зависимости от вашего use case
4. **Логируйте ошибки**: используйте логирование внутри messageHandler
5. **Не блокируйте обработку**: если обработка занимает много времени, используйте async processing

## Структура модуля

```
arbitrage-scanner-kafka/
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── com/arbitrage/scanner/kafka/
│   │   │       ├── AppKafkaConsumer.kt       # Основной класс consumer
│   │   │       ├── Utils.kt                  # Утилиты для создания consumer/producer
│   │   │       ├── config/
│   │   │       │   ├── KafkaConfig.kt        # Модель конфигурации
│   │   │       │   └── KafkaConfigLoader.kt  # Загрузчик конфигурации
│   │   │       └── example/
│   │   │           └── ConsumerExample.kt    # Примеры использования
│   │   └── resources/
│   │       └── application.yaml              # Конфигурация
│   └── test/
│       └── kotlin/
│           └── com/arbitrage/scanner/kafka/
│               └── AppKafkaConsumerTest.kt   # Тесты
├── build.gradle.kts
└── README.md
```
