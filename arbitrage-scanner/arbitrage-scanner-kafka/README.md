# Модуль arbitrage-scanner-kafka

Модуль для работы с Apache Kafka в проекте Arbitrage Scanner.

## Компоненты

### AppKafkaConsumer

Класс для инкапсуляции подключения к Kafka и управления считыванием сообщений.

### AppKafkaProducer

Класс для инкапсуляции подключения к Kafka и отправки сообщений.

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

---

## AppKafkaProducer

### Основные возможности

- ✅ Автоматическое подключение к Kafka на основе конфигурации
- ✅ Асинхронная и синхронная отправка сообщений
- ✅ Топик по умолчанию с возможностью переопределения
- ✅ Поддержка ключей сообщений
- ✅ Callback для обработки результатов отправки
- ✅ Обработка ошибок с логированием
- ✅ Реализация AutoCloseable для безопасной работы с ресурсами

### Использование

```kotlin
import com.arbitrage.scanner.kafka.AppKafkaProducer
import com.arbitrage.scanner.kafka.config.KafkaConfigLoader
import com.arbitrage.scanner.libs.logging.ArbScanLoggerProvider
import com.arbitrage.scanner.libs.logging.arbScanLoggerLogback

fun main() {
    // Загрузка конфигурации
    val config = KafkaConfigLoader.load()

    // Инициализация логирования
    val loggerProvider = ArbScanLoggerProvider(::arbScanLoggerLogback)

    // Создание producer (defaultTopic из config.outTopic)
    val producer = AppKafkaProducer(config, loggerProvider)

    try {
        // Асинхронная отправка
        producer.send("Мое сообщение")
        producer.send("Сообщение с ключом", key = "my-key")

        // Синхронная отправка (ожидание подтверждения)
        val metadata = producer.sendSync("Важное сообщение")
        println("Отправлено: offset=${metadata.offset()}")

        // Принудительная отправка всех буферизованных сообщений
        producer.flush()

    } finally {
        producer.close()
    }
}
```

### Кастомизация топика по умолчанию

```kotlin
fun main() {
    val config = KafkaConfigLoader.load()
    val loggerProvider = ArbScanLoggerProvider(::arbScanLoggerLogback)

    // Producer с кастомным топиком по умолчанию
    val producer = AppKafkaProducer(
        config = config,
        loggerProvider = loggerProvider,
        defaultTopic = "my-custom-topic"
    )

    // Отправка в топик по умолчанию
    producer.send("Сообщение в кастомный топик")

    // Отправка в другой топик
    producer.send(topic = "another-topic", message = "Сообщение в другой топик")

    producer.close()
}
```

### API

#### Конструктор

```kotlin
AppKafkaProducer(
    config: KafkaConfig,
    loggerProvider: ArbScanLoggerProvider,
    defaultTopic: String = config.outTopic
)
```

Параметры:
- `config` - конфигурация Kafka (см. KafkaConfig)
- `loggerProvider` - провайдер системы логирования приложения
- `defaultTopic` - топик по умолчанию для отправки (по умолчанию из `config.outTopic`)

#### Методы

##### send (асинхронная отправка)

```kotlin
fun send(
    message: String,
    key: String? = null
): Future<RecordMetadata>

fun send(
    topic: String,
    message: String,
    key: String? = null
): Future<RecordMetadata>
```

Асинхронная отправка сообщения. Возвращает Future для отслеживания результата.

Параметры:
- `topic` - топик для отправки (необязательно, используется defaultTopic)
- `message` - сообщение для отправки
- `key` - ключ сообщения (необязательно)

##### sendSync (синхронная отправка)

```kotlin
fun sendSync(
    message: String,
    key: String? = null
): RecordMetadata

fun sendSync(
    topic: String,
    message: String,
    key: String? = null
): RecordMetadata
```

Синхронная отправка сообщения с ожиданием подтверждения.

Параметры:
- `topic` - топик для отправки (необязательно, используется defaultTopic)
- `message` - сообщение для отправки
- `key` - ключ сообщения (необязательно)

Возвращает: `RecordMetadata` с информацией о отправленном сообщении

##### flush

```kotlin
fun flush()
```

Принудительная отправка всех буферизованных сообщений.

##### close

```kotlin
fun close()
```

Закрывает соединение с Kafka и освобождает ресурсы.

---

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

Готовые примеры использования находятся в `example/ConsumerExample.kt`:

- `ConsumerExample` - пример использования Consumer
- `ProducerExample` - пример использования Producer
- `ProducerConsumerExample` - пример совместного использования Producer и Consumer

Для запуска примеров:

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

### AppKafkaConsumer

Обрабатывает ошибки следующим образом:

1. **Ошибки в messageHandler**: логируются, но не прерывают работу consumer
2. **Ошибки подключения к Kafka**: прерывают работу и пробрасываются наружу
3. **Ошибки при закрытии**: логируются, но не пробрасываются

Пример:

```kotlin
consumer.subscribe { record ->
    try {
        processMessage(record.value())
    } catch (e: Exception) {
        logger.error(msg = "Ошибка обработки сообщения", e = e)
        // Consumer продолжит работу
    }
}
```

### AppKafkaProducer

Обрабатывает ошибки следующим образом:

1. **Ошибки при отправке**: логируются через callback, Future возвращается с ошибкой
2. **Ошибки при синхронной отправке**: пробрасываются наружу
3. **Ошибки при закрытии**: логируются, но не пробрасываются

Пример обработки ошибок при асинхронной отправке:

```kotlin
val future = producer.send("Мое сообщение")
try {
    val metadata = future.get() // Ожидание результата
    println("Отправлено: offset=${metadata.offset()}")
} catch (e: Exception) {
    logger.error(msg = "Ошибка при отправке", e = e)
}
```

## Зависимости

- `org.apache.kafka:kafka-clients` - клиент Kafka
- `arbitrage-scanner-lib-logging` - кастомная система логирования проекта
- `arbitrage-scanner-lib-logging-logback` - реализация логирования на базе Logback
- `com.sksamuel.hoplite:hoplite-core` - загрузка конфигурации
- `com.sksamuel.hoplite:hoplite-yaml` - поддержка YAML

## Лучшие практики

### Общие

1. **Всегда закрывайте ресурсы**: используйте `try-finally` или `use` блоки
2. **Обрабатывайте shutdown**: добавляйте shutdown hook для корректного завершения
3. **Логируйте ошибки**: используйте систему логирования приложения

### AppKafkaConsumer

1. **Настройте pollTimeout**: выбирайте значение в зависимости от вашего use case
2. **Не блокируйте обработку**: если обработка занимает много времени, используйте async processing
3. **Обрабатывайте исключения в handler**: чтобы consumer продолжил работу

### AppKafkaProducer

1. **Используйте flush()**: перед завершением работы для отправки всех буферизованных сообщений
2. **Выбирайте правильный метод отправки**:
   - `send()` - для высокой производительности (асинхронная отправка)
   - `sendSync()` - когда важно знать результат отправки немедленно
3. **Используйте ключи**: для партиционирования сообщений по определенной логике
4. **Обрабатывайте Future**: при асинхронной отправке для проверки успешности

## Структура модуля

```
arbitrage-scanner-kafka/
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── com/arbitrage/scanner/kafka/
│   │   │       ├── AppKafkaConsumer.kt       # Класс consumer для считывания
│   │   │       ├── AppKafkaProducer.kt       # Класс producer для отправки
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
│               ├── AppKafkaConsumerTest.kt   # Тесты для Consumer
│               └── AppKafkaProducerTest.kt   # Тесты для Producer
├── build.gradle.kts
└── README.md
```
