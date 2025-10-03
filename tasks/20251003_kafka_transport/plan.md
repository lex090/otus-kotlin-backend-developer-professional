# План реализации асинхронного транспорта Kafka для Arbitrage Scanner

## Контекст задачи

Необходимо создать новый модуль `arbitrage-scanner-kafka`, который будет обеспечивать асинхронную обработку запросов через Apache Kafka. Модуль должен повторять функциональность существующего синхронного HTTP-транспорта `arbitrage-scanner-ktor`, но работать через систему сообщений Kafka.

### Анализ существующего Ktor модуля

**Структура модуля arbitrage-scanner-ktor:**
- Использует Ktor Server с Netty engine
- Конфигурируется через application.yaml
- Применяет Koin для Dependency Injection
- Обрабатывает два типа запросов: READ и SEARCH
- Использует общую бизнес-логику через BusinessLogicProcessor
- Сериализует данные в JSON через kotlinx.serialization
- Логирование через arbitrage-scanner-lib-logging-logback

**Ключевые компоненты:**
1. **Application.kt** - точка входа с конфигурацией модулей
2. **KoinConfiguration.kt** - настройка DI контейнера (JSON, BusinessLogicProcessor, Logger)
3. **RoutingV1.kt** - маршруты для обработки запросов
4. **RequestProcessor.kt** - обработка запросов с типизацией
5. **ContextProcessor.kt** - основная логика обработки контекста
6. **Mappers** (из api-v1):
   - FromTransportMappers - преобразование запросов в доменную модель
   - ToTransportMappers - преобразование доменной модели в ответы

**Поток обработки запроса в Ktor:**
```
HTTP Request → receiveRequest → fromTransport → Context →
BusinessLogicProcessor.exec → toTransport → HTTP Response
```

---

## Архитектурные решения для Kafka транспорта

### 1. Паттерн взаимодействия

**Request-Reply Pattern через Kafka:**
- Клиент отправляет запрос в топик `arbitrage-scanner-requests`
- Kafka Consumer читает сообщения из топика запросов
- Обрабатывает запрос через существующий BusinessLogicProcessor
- Kafka Producer отправляет ответ в топик `arbitrage-scanner-responses`
- Используется correlation ID для связывания запросов и ответов

### 2. Структура топиков

**Топики:**
- `arbitrage-scanner-requests` - входящие запросы (READ/SEARCH)
- `arbitrage-scanner-responses` - исходящие ответы
- Dead Letter Queue (опционально): `arbitrage-scanner-requests-dlq` - для проблемных сообщений

### 3. Формат сообщений

**Структура Kafka сообщения:**
- **Key**: correlation-id (UUID) - для маршрутизации и отслеживания
- **Value**: JSON сериализованный IRequest/IResponse
- **Headers**:
  - `command-type`: READ | SEARCH
  - `reply-to`: имя топика для ответа (по умолчанию `arbitrage-scanner-responses`)
  - `timestamp`: время создания запроса

### 4. Переиспользование существующих компонентов

Модуль Kafka будет использовать:
- ✅ `arbitrage-scanner-common` - доменные модели и Context
- ✅ `arbitrage-scanner-api-v1` - transport модели и mappers
- ✅ `arbitrage-scanner-business-logic` - BusinessLogicProcessor
- ✅ `arbitrage-scanner-lib-logging-logback` - логирование

---

## Этапы реализации

### Этап 1: Создание структуры модуля

**Цель:** Создать базовую структуру модуля arbitrage-scanner-kafka с необходимыми конфигурационными файлами.

**Действия:**
1. Создать директорию `arbitrage-scanner/arbitrage-scanner-kafka/`
2. Создать `build.gradle.kts` с конфигурацией:
   - Применить `build.plugin.jvm`
   - Добавить application plugin с mainClass
   - Настроить Jib для Docker образов
   - Добавить зависимости:
     - Kafka clients (org.apache.kafka:kafka-clients)
     - Kotlinx coroutines
     - Kotlinx serialization
     - Koin для DI
     - Внутренние модули (common, api-v1, business-logic, libs)
3. Добавить модуль в `settings.gradle.kts` корневого проекта
4. Создать структуру пакетов:
   ```
   src/main/kotlin/com/arbitrage/scanner/
   ├── Application.kt
   ├── KafkaConfiguration.kt
   ├── KoinConfiguration.kt
   ├── kafka/
   │   ├── KafkaConsumerService.kt
   │   ├── KafkaProducerService.kt
   │   └── config/
   │       └── KafkaProperties.kt
   └── processors/
       └── MessageProcessor.kt
   ```

**Критерии готовности:**
- Модуль успешно компилируется через `./gradlew :arbitrage-scanner-kafka:build`
- Структура пакетов создана и соответствует принципам проекта

---

### Этап 2: Настройка конфигурации Kafka

**Цель:** Создать конфигурационный слой для подключения к Kafka и управления топиками.

**Действия:**
1. Создать `src/main/resources/application.conf` с HOCON конфигурацией:
   ```hocon
   kafka {
     bootstrap.servers = "localhost:9092"
     bootstrap.servers = ${?KAFKA_BOOTSTRAP_SERVERS}

     consumer {
       group.id = "arbitrage-scanner-consumer"
       topics = ["arbitrage-scanner-requests"]
       auto.offset.reset = "earliest"
       enable.auto.commit = false
     }

     producer {
       topic.responses = "arbitrage-scanner-responses"
       acks = "all"
       retries = 3
     }
   }

   application {
     name = "arbitrage-scanner-kafka"
     shutdown.timeout = 30000
   }
   ```

2. Создать data class `KafkaProperties` для типобезопасной работы с конфигурацией:
   - Bootstrap servers
   - Consumer properties (group.id, topics, auto.offset.reset)
   - Producer properties (acks, retries, compression)
   - Загрузка из HOCON через Typesafe Config

3. Настроить Koin модуль для конфигурации:
   ```kotlin
   val kafkaConfigModule = module {
       single { loadKafkaProperties() }
       single { createKafkaConsumerConfig(get()) }
       single { createKafkaProducerConfig(get()) }
   }
   ```

**Критерии готовности:**
- Конфигурация загружается из файла и environment variables
- KafkaProperties успешно создается через Koin
- Конфигурация поддерживает переопределение через ENV переменные

---

### Этап 3: Реализация Kafka Producer

**Цель:** Создать сервис для отправки ответов в Kafka топик.

**Действия:**
1. Создать интерфейс `KafkaProducerService`:
   ```kotlin
   interface KafkaProducerService {
       suspend fun sendResponse(
           correlationId: String,
           response: IResponse,
           replyToTopic: String
       ): Result<Unit>
       fun close()
   }
   ```

2. Реализовать `KafkaProducerServiceImpl`:
   - Инициализация KafkaProducer с конфигурацией для JSON сериализации
   - Использование StringSerializer для ключа
   - Использование JsonSerializer для значения (IResponse)
   - Асинхронная отправка через корутины с обработкой ошибок
   - Логирование успешной отправки и ошибок
   - Graceful shutdown в методе close()

3. Добавить в Koin модуль:
   ```kotlin
   val kafkaProducerModule = module {
       single<KafkaProducerService> {
           KafkaProducerServiceImpl(
               config = get(),
               json = get(),
               logger = get()
           )
       }
   }
   ```

**Технические детали:**
- Использовать `ProducerRecord` с correlation ID как ключ
- Добавлять headers: timestamp, command-type
- Обработка callback через suspendCoroutine для интеграции с корутинами
- Retry механизм через конфигурацию producer (retries=3)

**Критерии готовности:**
- Producer успешно отправляет сообщения в тестовый топик
- Обрабатываются ошибки отправки с логированием
- Close корректно завершает работу producer

---

### Этап 4: Реализация Kafka Consumer

**Цель:** Создать сервис для чтения запросов из Kafka топика.

**Действия:**
1. Создать интерфейс `KafkaConsumerService`:
   ```kotlin
   interface KafkaConsumerService {
       suspend fun start()
       fun stop()
   }
   ```

2. Реализовать `KafkaConsumerServiceImpl`:
   - Инициализация KafkaConsumer с подпиской на топик запросов
   - Использование StringDeserializer для ключа
   - Кастомный JsonDeserializer для IRequest с обработкой полиморфной десериализации
   - Бесконечный цикл poll() в корутине
   - Обработка каждого сообщения через MessageProcessor
   - Manual commit после успешной обработки
   - Обработка исключений с отправкой в DLQ
   - Graceful shutdown при остановке

3. Реализовать механизм обработки сообщений:
   - Извлечение correlation ID из ключа сообщения
   - Извлечение command-type из headers
   - Извлечение reply-to топика из headers (fallback на дефолтный)
   - Передача в MessageProcessor для обработки
   - Commit offset только после успешной обработки

4. Добавить в Koin модуль:
   ```kotlin
   val kafkaConsumerModule = module {
       single<KafkaConsumerService> {
           KafkaConsumerServiceImpl(
               config = get(),
               json = get(),
               messageProcessor = get(),
               logger = get()
           )
       }
   }
   ```

**Технические детали:**
- Poll timeout: 1000ms для баланса между latency и CPU
- Max poll records: 100 для batch обработки
- Session timeout: 30s для detection сбоев
- Heartbeat interval: 10s для monitoring жизнеспособности
- Enable auto commit: false для контроля над commit

**Критерии готовности:**
- Consumer успешно читает сообщения из топика
- Обрабатываются все типы запросов (READ, SEARCH)
- Graceful shutdown корректно закрывает consumer
- Offset commit происходит только после успешной обработки

---

### Этап 5: Реализация процессора сообщений

**Цель:** Адаптировать логику обработки запросов из Ktor модуля для работы с Kafka.

**Действия:**
1. Создать `MessageProcessor`:
   ```kotlin
   class MessageProcessor(
       private val businessLogicProcessor: BusinessLogicProcessor,
       private val kafkaProducerService: KafkaProducerService,
       private val loggerProvider: ArbScanLoggerProvider,
       private val json: Json
   ) {
       suspend fun processMessage(
           correlationId: String,
           request: IRequest,
           replyToTopic: String
       ): Result<Unit>
   }
   ```

2. Реализовать логику обработки аналогично ContextProcessor из Ktor:
   - Создание Context с startTimestamp
   - Заполнение Context через fromTransport(request)
   - Логирование начала обработки
   - Вызов businessLogicProcessor.exec(context)
   - Логирование завершения обработки
   - Конвертация context.toTransport() в IResponse
   - Отправка ответа через kafkaProducerService
   - Обработка исключений с добавлением ошибок в context

3. Обеспечить идентичность обработки с Ktor модулем:
   - Использовать те же mappers (fromTransport/toTransport)
   - Использовать тот же BusinessLogicProcessor
   - Логировать те же события с marker "BIZ"
   - Обрабатывать ошибки аналогичным образом

4. Добавить в Koin модуль:
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
   ```

**Критерии готовности:**
- Обработка запросов идентична Ktor модулю
- Все ошибки корректно обрабатываются и логируются
- Ответы успешно отправляются в топик responses

---

### Этап 6: Реализация Application и lifecycle management

**Цель:** Создать главный класс приложения с управлением жизненным циклом.

**Действия:**
1. Создать `Application.kt`:
   ```kotlin
   class KafkaApplication(
       private val kafkaConsumerService: KafkaConsumerService,
       private val kafkaProducerService: KafkaProducerService,
       private val logger: ArbScanLogWrapper
   ) {
       suspend fun start()
       fun stop()
   }

   suspend fun main() {
       val koinApp = startKoin { modules(allModules) }
       val app = koinApp.koin.get<KafkaApplication>()

       Runtime.getRuntime().addShutdownHook(thread(start = false) {
           app.stop()
       })

       app.start()
   }
   ```

2. Реализовать lifecycle management:
   - Инициализация всех сервисов через Koin
   - Запуск KafkaConsumerService в отдельной корутине
   - Обработка сигналов завершения (SIGTERM, SIGINT)
   - Graceful shutdown: stop consumer → flush producer → close all
   - Логирование событий lifecycle

3. Настроить Koin для всего приложения:
   ```kotlin
   val allModules = listOf(
       jsonModule,
       businessLogicProcessorModule,
       loggingModule,
       kafkaConfigModule,
       kafkaProducerModule,
       kafkaConsumerModule,
       messageProcessorModule,
       applicationModule
   )
   ```

4. Обновить `build.gradle.kts`:
   - Установить mainClass для application plugin
   - Настроить Jib для создания Docker образа

**Критерии готовности:**
- Приложение запускается через `./gradlew :arbitrage-scanner-kafka:run`
- Graceful shutdown работает корректно
- Все зависимости корректно инжектятся через Koin

---

### Этап 7: Настройка JSON сериализации/десериализации

**Цель:** Обеспечить корректную сериализацию и десериализацию Kafka сообщений.

**Действия:**
1. Создать `KafkaJsonSerializer`:
   ```kotlin
   class KafkaJsonSerializer<T>(private val json: Json) : Serializer<T> {
       override fun serialize(topic: String, data: T): ByteArray
   }
   ```

2. Создать `KafkaJsonDeserializer`:
   ```kotlin
   class KafkaJsonDeserializer<T>(
       private val json: Json,
       private val deserializer: DeserializationStrategy<T>
   ) : Deserializer<T> {
       override fun deserialize(topic: String, data: ByteArray): T
   }
   ```

3. Настроить конфигурацию сериализаторов для Kafka producer/consumer:
   - Producer: value.serializer = KafkaJsonSerializer<IResponse>
   - Consumer: value.deserializer = KafkaJsonDeserializer<IRequest>
   - Использовать тот же Json config, что и в Ktor модуле

4. Обработать ошибки десериализации:
   - Логировать проблемные сообщения
   - Отправлять в DLQ (Dead Letter Queue)
   - Продолжать обработку следующих сообщений

**Критерии готовности:**
- Сериализация/десериализация работает для всех типов IRequest/IResponse
- Полиморфная сериализация обрабатывается корректно
- Ошибки десериализации не останавливают consumer

---

### Этап 8: Docker конфигурация и интеграция

**Цель:** Настроить Docker образ и интеграцию с docker-compose.minimal.yml.

**Действия:**
1. Настроить Jib в `build.gradle.kts`:
   ```kotlin
   jib {
       from {
           image = "eclipse-temurin:21-jre"
           platforms {
               platform { architecture = "arm64"; os = "linux" }
               platform { architecture = "amd64"; os = "linux" }
           }
       }
       to {
           image = "arbitrage-scanner-kafka"
           tags = setOf(project.version.toString())
       }
       container {
           mainClass = "com.arbitrage.scanner.ApplicationKt"
           ports = listOf("9092") // для monitoring/health check
           jvmFlags = emptyList()
           environment = emptyMap()
       }
   }
   ```

2. Обновить `docker-compose.minimal.yml`:
   - Добавить сервис `arbitrage-scanner-kafka`:
     ```yaml
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
     ```

3. Создать скрипт для локального запуска:
   - `./gradlew :arbitrage-scanner-kafka:jibDockerBuild`
   - `docker-compose -f docker-compose.minimal.yml up arbitrage-scanner-kafka`

**Критерии готовности:**
- Docker образ успешно собирается через Jib
- Приложение запускается в Docker с подключением к Kafka
- Логи отправляются в Fluent Bit
- Kafka топики автоматически создаются при старте

---

### Этап 9: Написание тестов

**Цель:** Обеспечить надежность модуля через автоматизированное тестирование.

**Действия:**
1. **Unit тесты для компонентов:**
   - `MessageProcessorTest` - тестирование обработки сообщений с mock зависимостями
   - `KafkaJsonSerializerTest` - тестирование сериализации различных типов
   - `KafkaJsonDeserializerTest` - тестирование десериализации и обработки ошибок

2. **Интеграционные тесты с Testcontainers:**
   - Создать `KafkaIntegrationTest`:
     ```kotlin
     @Testcontainers
     class KafkaIntegrationTest {
         @Container
         val kafka = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"))

         @Test
         fun `should process READ request and send response`()

         @Test
         fun `should process SEARCH request and send response`()

         @Test
         fun `should handle deserialization errors gracefully`()
     }
     ```

3. **Тесты конфигурации:**
   - `KafkaPropertiesTest` - загрузка конфигурации из разных источников
   - `KoinModulesTest` - проверка корректности DI конфигурации

4. Добавить зависимости в `build.gradle.kts`:
   ```kotlin
   testImplementation(libs.kotlin.test)
   testImplementation(libs.testcontainers.kafka)
   testImplementation(libs.kotlinx.coroutines.test)
   testImplementation(libs.mockk)
   ```

**Критерии готовности:**
- Все unit тесты проходят успешно
- Интеграционные тесты с Testcontainers работают
- Coverage тестов > 80% для критичных компонентов
- Тесты запускаются через `./gradlew :arbitrage-scanner-kafka:test`

---

### Этап 10: Мониторинг и Health Checks

**Цель:** Добавить возможности мониторинга состояния приложения.

**Действия:**
1. Создать `HealthCheckService`:
   - Проверка подключения к Kafka
   - Проверка доступности топиков
   - Метрики обработки сообщений (throughput, latency, errors)

2. Добавить HTTP endpoint для health check (опционально):
   - Использовать Ktor server на отдельном порту (8081)
   - Endpoint `/health` - статус приложения
   - Endpoint `/metrics` - метрики Prometheus format

3. Добавить логирование метрик:
   - Количество обработанных сообщений
   - Среднее время обработки
   - Количество ошибок
   - Размер lag consumer group

**Критерии готовности:**
- Health check endpoint отвечает корректно
- Метрики логируются регулярно
- Можно отслеживать состояние приложения через логи

---

### Этап 11: Документация

**Цель:** Создать полную документацию для использования и поддержки модуля.

**Действия:**
1. Создать `README.md` в `arbitrage-scanner-kafka/`:
   - Описание модуля и его назначения
   - Требования и зависимости
   - Конфигурация (переменные окружения, application.conf)
   - Запуск локально и через Docker
   - Примеры использования (отправка запросов в Kafka)
   - Troubleshooting

2. Обновить корневой `CLAUDE.md`:
   - Добавить команды для работы с Kafka модулем
   - Описать отличия от Ktor модуля
   - Указать use cases для выбора между Ktor и Kafka

3. Обновить `ARCHITECTURE.md`:
   - Добавить Kafka транспорт в архитектурную диаграмму
   - Описать паттерн Request-Reply через Kafka
   - Документировать структуру топиков и сообщений

4. Создать примеры использования:
   - Скрипт для отправки тестовых запросов в Kafka
   - Пример консьюмера для чтения ответов
   - Docker compose конфигурация для полного стека

**Критерии готовности:**
- Документация полная и актуальная
- Новый разработчик может запустить модуль по документации
- Примеры работают корректно

---

### Этап 12: Финальная интеграция и тестирование

**Цель:** Проверить работоспособность всей системы в комплексе.

**Действия:**
1. **End-to-End тестирование:**
   - Запустить полный стек через docker-compose.minimal.yml
   - Отправить READ запрос в топик requests
   - Проверить получение ответа в топике responses
   - Отправить SEARCH запрос и проверить результат
   - Проверить обработку ошибочных запросов

2. **Нагрузочное тестирование:**
   - Отправить batch из 1000 сообщений
   - Проверить throughput и latency
   - Проверить корректность обработки всех сообщений
   - Проверить отсутствие memory leaks

3. **Сравнение с Ktor модулем:**
   - Запустить идентичные запросы через Ktor и Kafka
   - Сравнить результаты обработки
   - Убедиться в идентичности бизнес-логики

4. **Проверка resilience:**
   - Симулировать недоступность Kafka
   - Проверить graceful shutdown
   - Проверить recovery после перезапуска

**Критерии готовности:**
- Все E2E тесты проходят успешно
- Результаты обработки идентичны Ktor модулю
- Производительность приемлема для production
- Система устойчива к сбоям

---

## Зависимости и технологический стек

### Основные библиотеки

```kotlin
dependencies {
    // Внутренние модули
    implementation(project(":arbitrage-scanner-common"))
    implementation(project(":arbitrage-scanner-api-v1"))
    implementation(project(":arbitrage-scanner-business-logic"))
    implementation(project(":arbitrage-scanner-libs:arbitrage-scanner-lib-logging-logback"))

    // Kafka
    implementation("org.apache.kafka:kafka-clients:3.6.1")

    // Kotlinx
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Configuration
    implementation("com.typesafe:config:1.4.3")

    // Koin
    implementation("io.insert-koin:koin-core:3.5.6")

    // Testing
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.testcontainers:kafka:1.19.7")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    testImplementation("io.mockk:mockk:1.13.10")
}
```

### Версии зависимостей для libs.versions.toml

```toml
[versions]
kafka-clients = "3.6.1"
typesafe-config = "1.4.3"
testcontainers = "1.19.7"
mockk = "1.13.10"

[libraries]
kafka-clients = { module = "org.apache.kafka:kafka-clients", version.ref = "kafka-clients" }
typesafe-config = { module = "com.typesafe:config", version.ref = "typesafe-config" }
testcontainers-kafka = { module = "org.testcontainers:kafka", version.ref = "testcontainers" }
mockk = { module = "io.mockk:mockk", version.ref = "mockk" }
```

---

## Потенциальные риски и способы решения

### Риск 1: Сложности с полиморфной сериализацией

**Проблема:** Kotlinx.serialization может иметь проблемы с десериализацией sealed классов в Kafka deserializer.

**Решение:**
- Использовать явную регистрацию полиморфных serializers
- Добавить дискриминатор в JSON для типов IRequest/IResponse
- Протестировать все типы запросов/ответов в unit тестах

### Риск 2: Проблемы с graceful shutdown

**Проблема:** Kafka consumer может не успеть обработать сообщения при завершении.

**Решение:**
- Установить shutdown timeout в конфигурации
- Использовать wakeup() для прерывания poll()
- Обрабатывать WakeupException корректно
- Flush producer перед закрытием

### Риск 3: Дублирование сообщений

**Проблема:** При сбое после обработки, но до commit, сообщение обработается повторно.

**Решение:**
- Реализовать idempotency через correlation ID
- Использовать enable.idempotence=true для producer
- Документировать поведение для пользователей API

### Риск 4: Производительность

**Проблема:** Латентность может быть выше, чем у HTTP транспорта.

**Решение:**
- Настроить batch processing (max.poll.records)
- Использовать compression (lz4, snappy)
- Оптимизировать размер сообщений
- Провести нагрузочное тестирование

### Риск 5: Сложность отладки

**Проблема:** Асинхронность усложняет troubleshooting.

**Решение:**
- Добавить детальное логирование с correlation ID
- Использовать Kafka UI для мониторинга топиков
- Создать инструменты для replay сообщений
- Документировать типичные проблемы и решения

---

## Контрольный список готовности к production

- [ ] Модуль успешно компилируется и проходит все тесты
- [ ] Docker образ собирается через Jib
- [ ] Конфигурация загружается из файла и ENV переменных
- [ ] Kafka consumer корректно читает и обрабатывает сообщения
- [ ] Kafka producer отправляет ответы в правильный топик
- [ ] Бизнес-логика идентична Ktor модулю
- [ ] Graceful shutdown работает корректно
- [ ] Логирование настроено и отправляется в Fluent Bit
- [ ] Unit и интеграционные тесты покрывают основной функционал
- [ ] E2E тесты с реальным Kafka проходят
- [ ] Документация создана и актуальна
- [ ] Примеры использования работают
- [ ] Health checks реализованы
- [ ] Обработка ошибок покрыта тестами
- [ ] Нагрузочное тестирование проведено
- [ ] Code review выполнен

---

## Ожидаемые результаты

После выполнения всех этапов:

1. **Функциональность:**
   - Полнофункциональный Kafka транспорт для Arbitrage Scanner
   - Поддержка всех операций из Ktor модуля (READ, SEARCH)
   - Идентичная бизнес-логика обработки запросов

2. **Качество:**
   - Покрытие тестами > 80%
   - Документированный и поддерживаемый код
   - Соответствие архитектурным принципам проекта

3. **Готовность к production:**
   - Docker образ готов к развертыванию
   - Конфигурируется через environment variables
   - Graceful shutdown и error handling
   - Мониторинг и логирование

4. **Интеграция:**
   - Работает в составе docker-compose стека
   - Интегрируется с существующей инфраструктурой (Kafka, Fluent Bit)
   - Примеры использования для клиентов

---

## Примерная оценка времени

| Этап | Описание | Оценка времени |
|------|----------|----------------|
| 1 | Создание структуры модуля | 1-2 часа |
| 2 | Настройка конфигурации Kafka | 2-3 часа |
| 3 | Реализация Kafka Producer | 3-4 часа |
| 4 | Реализация Kafka Consumer | 4-6 часов |
| 5 | Реализация процессора сообщений | 2-3 часа |
| 6 | Application и lifecycle management | 2-3 часа |
| 7 | JSON сериализация/десериализация | 3-4 часа |
| 8 | Docker конфигурация | 2-3 часа |
| 9 | Написание тестов | 6-8 часов |
| 10 | Мониторинг и Health Checks | 3-4 часа |
| 11 | Документация | 2-3 часа |
| 12 | Финальная интеграция и тестирование | 4-6 часов |
| **Итого** | | **36-50 часов** |

---

## Заключение

Данный план обеспечивает пошаговую реализацию Kafka транспорта с максимальным переиспользованием существующих компонентов проекта. Модуль будет полностью совместим с текущей архитектурой, использовать те же бизнес-логику и доменные модели, что и Ktor модуль, но предоставит асинхронный способ взаимодействия через систему сообщений Apache Kafka.
