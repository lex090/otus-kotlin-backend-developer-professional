# Подзадачи для реализации Kafka транспорта

## Subtask-1: Создание модуля arbitrage-scanner-kafka
**Статус:** ❌ Не выполнена
**Цель и обоснование:** Создать базовую структуру нового модуля для Kafka транспорта, аналогичного существующему модулю arbitrage-scanner-ktor
**Описание:** Создать новый модуль arbitrage-scanner-kafka в составе arbitrage-scanner с базовой структурой пакетов и конфигурацией сборки
**Архитектурный слой:** Infrastructure
**Критерии выполнения:**
- Создан модуль `arbitrage-scanner-kafka/` с правильной структурой пакетов
- Настроен `build.gradle.kts` с базовыми зависимостями (Kotlin, корутины)
- Модуль добавлен в `settings.gradle.kts`
- Создана базовая структура пакетов: `config/`, `consumers/`, `producers/`, `processors/`, `serialization/`, `monitoring/`

**Критерии приемки:**
- Проект компилируется командой `./gradlew :arbitrage-scanner-kafka:build`
- Все существующие тесты проходят
- Модуль корректно отображается в списке проектов `./gradlew projects`

**Зависимости:** Нет

**Команды для проверки:**
- `./gradlew projects`
- `./gradlew :arbitrage-scanner-kafka:build`

## Subtask-2: Добавление Kafka зависимостей в build.gradle.kts
**Статус:** ❌ Не выполнена
**Цель и обоснование:** Настроить все необходимые зависимости для работы с Apache Kafka в Kotlin Multiplatform проекте
**Описание:** Добавить зависимости Apache Kafka Client, kotlinx-coroutines-jdk8 и вспомогательные библиотеки в build конфигурацию
**Архитектурный слой:** Infrastructure
**Критерии выполнения:**
- Добавлена зависимость `org.apache.kafka:kafka-clients:3.7.0`
- Добавлена зависимость `org.jetbrains.kotlinx:kotlinx-coroutines-jdk8`
- Добавлены тестовые зависимости для embedded Kafka
- Настроен application plugin для запуска приложения
- Настроен shadow plugin для создания fat JAR

**Критерии приемки:**
- Проект компилируется командой `./gradlew :arbitrage-scanner-kafka:build`
- Все зависимости корректно разрешаются
- Задача `run` доступна для модуля

**Зависимости:** Subtask-1

**Команды для проверки:**
- `./gradlew :arbitrage-scanner-kafka:dependencies`
- `./gradlew :arbitrage-scanner-kafka:build`

## Subtask-3: Создание конфигурационных классов для Kafka Producer
**Статус:** ❌ Не выполнена
**Цель и обоснование:** Создать конфигурацию для Kafka Producer с оптимальными настройками производительности и надежности
**Описание:** Создать класс KafkaProducerConfig со всеми необходимыми настройками для отправки ответов в response topics
**Архитектурный слой:** Infrastructure
**Критерии выполнения:**
- Создан файл `config/KafkaProducerConfig.kt`
- Настроены базовые свойства producer'а (bootstrap.servers, key.serializer, value.serializer)
- Настроены параметры надежности (acks=all, retries, idempotence)
- Настроены параметры производительности (batch.size, linger.ms, compression.type)
- Добавлена возможность конфигурации через external properties

**Критерии приемки:**
- Проект компилируется без ошибок
- Конфигурация содержит все критически важные параметры
- Параметры могут быть переопределены через внешние конфигурационные файлы

**Зависимости:** Subtask-2

**Команды для проверки:**
- `./gradlew :arbitrage-scanner-kafka:compileKotlin`

## Subtask-4: Создание конфигурационных классов для Kafka Consumer
**Статус:** ❌ Не выполнена
**Цель и обоснование:** Создать конфигурацию для Kafka Consumer с настройками для обработки request topics
**Описание:** Создать класс KafkaConsumerConfig с оптимальными настройками для consumer'ов запросов
**Архитектурный слой:** Infrastructure
**Критерии выполнения:**
- Создан файл `config/KafkaConsumerConfig.kt`
- Настроены базовые свойства consumer'а (bootstrap.servers, group.id, key.deserializer, value.deserializer)
- Настроены параметры обработки (auto.offset.reset, enable.auto.commit, max.poll.records)
- Настроены параметры session management (session.timeout.ms, heartbeat.interval.ms)
- Добавлена поддержка множественных consumer groups для разных типов запросов

**Критерии приемки:**
- Проект компилируется без ошибок
- Конфигурация поддерживает различные стратегии offset management
- Параметры оптимизированы для request-reply паттерна

**Зависимости:** Subtask-2

**Команды для проверки:**
- `./gradlew :arbitrage-scanner-kafka:compileKotlin`

## Subtask-5: Создание конфигурации Kafka Topics
**Статус:** ❌ Не выполнена
**Цель и обоснование:** Определить структуру topics для request-reply паттерна с правильными названиями и настройками
**Описание:** Создать класс TopicsConfig с константами для всех используемых topics и их конфигурацией
**Архитектурный слой:** Infrastructure
**Критерии выполнения:**
- Создан файл `config/TopicsConfig.kt`
- Определены константы для request topics: `arbitrage-opportunities-read-requests`, `arbitrage-opportunities-search-requests`
- Определены константы для response topics: `arbitrage-opportunities-read-responses`, `arbitrage-opportunities-search-responses`
- Добавлены настройки topics (partitions, replication factor)
- Создан mapping между типами запросов и соответствующими topics

**Критерии приемки:**
- Проект компилируется без ошибок
- Topic названия соответствуют REST API endpoints
- Настройки topics оптимизированы для production использования

**Зависимости:** Subtask-2

**Команды для проверки:**
- `./gradlew :arbitrage-scanner-kafka:compileKotlin`

## Subtask-6: Реализация JSON сериализации для Kafka сообщений
**Статус:** ❌ Не выполнена
**Цель и обоснование:** Создать сериализаторы/десериализаторы для преобразования IRequest/IResponse объектов в JSON для отправки через Kafka
**Описание:** Создать KafkaJsonSerializer и KafkaJsonDeserializer с использованием существующей kotlinx.serialization конфигурации
**Архитектурный слой:** Infrastructure
**Критерии выполнения:**
- Создан файл `serialization/KafkaJsonSerializer.kt`
- Создан файл `serialization/KafkaJsonDeserializer.kt`
- Переиспользованы существующие сериализаторы из api-v1 модуля
- Добавлена обработка ошибок сериализации/десериализации
- Поддержка полиморфной сериализации через sealed classes

**Критерии приемки:**
- Проект компилируется без ошибок
- Сериализаторы корректно обрабатывают все типы IRequest и IResponse
- Обработаны edge cases (null values, неизвестные типы)

**Зависимости:** Subtask-2

**Команды для проверки:**
- `./gradlew :arbitrage-scanner-kafka:compileKotlin`

## Subtask-7: Создание конфигурационного файла application.yaml
**Статус:** ❌ Не выполнена
**Цель и обоснование:** Создать внешний конфигурационный файл для настройки Kafka подключения и параметров приложения
**Описание:** Создать application.yaml с настройками Kafka broker'ов, topics, consumer/producer параметров
**Архитектурный слой:** Infrastructure
**Критерии выполнения:**
- Создан файл `src/main/resources/application.yaml`
- Добавлены настройки Kafka bootstrap servers
- Добавлены настройки consumer и producer групп
- Добавлены настройки логирования
- Добавлена возможность переопределения через environment variables

**Критерии приемки:**
- Конфигурация корректно загружается приложением
- Все параметры имеют разумные default значения
- Поддерживается конфигурация для dev/test/prod окружений

**Зависимости:** Subtask-3, Subtask-4, Subtask-5

**Команды для проверки:**
- `./gradlew :arbitrage-scanner-kafka:build`

## Subtask-8: Создание ResponseProducer для отправки ответов
**Статус:** ❌ Не выполнена
**Цель и обоснование:** Реализовать компонент для отправки response сообщений в соответствующие response topics
**Описание:** Создать класс ResponseProducer с методами для отправки различных типов ответов с правильными headers
**Архитектурный слой:** Infrastructure
**Критерии выполнения:**
- Создан файл `producers/ResponseProducer.kt`
- Реализованы методы sendReadResponse() и sendSearchResponse()
- Добавлена обработка correlation-id для связи request-response
- Добавлены message headers (correlation-id, request-type, timestamp)
- Реализован graceful shutdown producer'а

**Критерии приемки:**
- Проект компилируется без ошибок
- Producer корректно отправляет сообщения в нужные topics
- Headers устанавливаются корректно для каждого сообщения

**Зависимости:** Subtask-3, Subtask-5, Subtask-6

**Команды для проверки:**
- `./gradlew :arbitrage-scanner-kafka:compileKotlin`

## Subtask-9: Создание ArbitrageOpportunityReadConsumer
**Статус:** ❌ Не выполнена
**Цель и обоснование:** Реализовать consumer для обработки запросов чтения арбитражных возможностей из read-requests topic
**Описание:** Создать класс ArbitrageOpportunityReadConsumer с корутинами для асинхронной обработки сообщений
**Архитектурный слой:** Infrastructure
**Критерии выполнения:**
- Создан файл `consumers/ArbitrageOpportunityReadConsumer.kt`
- Реализован polling loop с корутинами
- Добавлена обработка correlation-id из message headers
- Реализовано преобразование Kafka message в ArbitrageOpportunityReadRequest
- Добавлена обработка ошибок и логирование

**Критерии приемки:**
- Проект компилируется без ошибок
- Consumer корректно подписывается на read-requests topic
- Сообщения корректно десериализуются в domain объекты

**Зависимости:** Subtask-4, Subtask-5, Subtask-6

**Команды для проверки:**
- `./gradlew :arbitrage-scanner-kafka:compileKotlin`

## Subtask-10: Создание ArbitrageOpportunitySearchConsumer
**Статус:** ❌ Не выполнена
**Цель и обоснование:** Реализовать consumer для обработки запросов поиска арбитражных возможностей из search-requests topic
**Описание:** Создать класс ArbitrageOpportunitySearchConsumer аналогично ReadConsumer для search операций
**Архитектурный слой:** Infrastructure
**Критерии выполнения:**
- Создан файл `consumers/ArbitrageOpportunitySearchConsumer.kt`
- Реализован polling loop с корутинами для search-requests topic
- Добавлена обработка correlation-id и message headers
- Реализовано преобразование в ArbitrageOpportunitySearchRequest
- Добавлены аналогичные error handling и logging механизмы

**Критерии приемки:**
- Проект компилируется без ошибок
- Consumer корректно подписывается на search-requests topic
- Поддерживается параллельная обработка с ReadConsumer

**Зависимости:** Subtask-4, Subtask-5, Subtask-6

**Команды для проверки:**
- `./gradlew :arbitrage-scanner-kafka:compileKotlin`

## Subtask-11: Создание KafkaMessageProcessor для обработки контекста
**Статус:** ❌ Не выполнена
**Цель и обоснование:** Создать центральный компонент для обработки Kafka сообщений с интеграцией существующих Context и mappers
**Описание:** Реализовать KafkaMessageProcessor который связывает consumer'ы, бизнес-логику и producer'ы
**Архитектурный слой:** Application
**Критерии выполнения:**
- Создан файл `processors/KafkaMessageProcessor.kt`
- Реализованы методы processReadRequest() и processSearchRequest()
- Интегрированы fromTransport mappers из существующих модулей
- Интегрирован BusinessLogicProcessor из business-logic модуля
- Интегрированы toTransport mappers для создания ответов
- Добавлена обработка ошибок на всех этапах pipeline

**Критерии приемки:**
- Проект компилируется без ошибок
- Processor корректно вызывает всю цепочку обработки
- Интеграция с существующими компонентами работает без изменений в них

**Зависимости:** Subtask-8, Subtask-9, Subtask-10

**Команды для проверки:**
- `./gradlew :arbitrage-scanner-kafka:compileKotlin`

## Subtask-12: Создание KafkaConfiguration для DI контейнера
**Статус:** ❌ Не выполнена
**Цель и обоснование:** Настроить Dependency Injection контейнер аналогично существующему Ktor модулю
**Описание:** Создать Koin конфигурацию для всех Kafka компонентов с переиспользованием существующих сервисов
**Архитектурный слой:** Infrastructure
**Критерии выполнения:**
- Создан файл `KafkaConfiguration.kt`
- Добавлены beans для всех Kafka компонентов (producers, consumers, processors)
- Переиспользованы beans из business-logic модуля (BusinessLogicProcessor)
- Переиспользованы beans из libs модуля (ArbScanLoggerProvider)
- Настроена инициализация всех конфигурационных компонентов

**Критерии приемки:**
- Проект компилируется без ошибок
- Все зависимости корректно резолвятся через Koin
- DI конфигурация не конфликтует с существующими модулями

**Зависимости:** Subtask-11

**Команды для проверки:**
- `./gradlew :arbitrage-scanner-kafka:compileKotlin`

## Subtask-13: Создание главного класса Application.kt
**Статус:** ❌ Не выполнена
**Цель и обоснование:** Создать точку входа в Kafka приложение с инициализацией всех компонентов и graceful shutdown
**Описание:** Реализовать main функцию с запуском consumer'ов, настройкой DI и обработкой системных сигналов
**Архитектурный слой:** Infrastructure
**Критерии выполнения:**
- Создан файл `Application.kt` с main функцией
- Инициализирован Koin DI контейнер
- Запущены все consumer'ы в отдельных корутинах
- Реализован graceful shutdown hook
- Добавлено логирование startup/shutdown событий
- Обработаны исключения на уровне приложения

**Критерии приемки:**
- Приложение запускается командой `./gradlew :arbitrage-scanner-kafka:run`
- Graceful shutdown работает корректно при получении SIGTERM
- Все компоненты инициализируются в правильном порядке

**Зависимости:** Subtask-12

**Команды для проверки:**
- `./gradlew :arbitrage-scanner-kafka:run`

## Subtask-14: Создание KafkaHealthCheck для мониторинга
**Статус:** ❌ Не выполнена
**Цель и обоснование:** Реализовать health check компонент для отслеживания состояния Kafka подключений
**Описание:** Создать класс для проверки доступности Kafka broker'ов и состояния consumer/producer компонентов
**Архитектурный слой:** Infrastructure
**Критерии выполнения:**
- Создан файл `monitoring/KafkaHealthCheck.kt`
- Реализована проверка подключения к Kafka broker'ам
- Добавлена проверка состояния consumer groups
- Реализована проверка producer availability
- Добавлено периодическое выполнение health checks

**Критерии приемки:**
- Проект компилируется без ошибок
- Health check корректно определяет состояние Kafka компонентов
- Результаты проверки логируются с соответствующими уровнями

**Зависимости:** Subtask-8, Subtask-9, Subtask-10

**Команды для проверки:**
- `./gradlew :arbitrage-scanner-kafka:compileKotlin`

## Subtask-15: Создание KafkaMetrics для мониторинга производительности
**Статус:** ❌ Не выполнена
**Цель и обоснование:** Реализовать сбор и логирование метрик производительности Kafka операций
**Описание:** Создать систему метрик для отслеживания latency, throughput, errors и других KPIs
**Архитектурный слой:** Infrastructure
**Критерии выполнения:**
- Создан файл `monitoring/KafkaMetrics.kt`
- Добавлены метрики producer'а (send latency, batch size, error rate)
- Добавлены метрики consumer'а (poll latency, processing time, lag)
- Реализован периодический вывод метрик в логи
- Добавлены JVM метрики для memory usage

**Критерии приемки:**
- Проект компилируется без ошибок
- Метрики корректно собираются и выводятся
- Не влияет на производительность основной обработки

**Зависимости:** Subtask-14

**Команды для проверки:**
- `./gradlew :arbitrage-scanner-kafka:compileKotlin`

## Subtask-16: Создание unit тестов для сериализации
**Статус:** ❌ Не выполнена
**Цель и обоснование:** Обеспечить корректность работы JSON сериализации/десериализации для всех типов сообщений
**Описание:** Создать comprehensive тесты для KafkaJsonSerializer и KafkaJsonDeserializer
**Архитектурный слой:** Infrastructure
**Критерии выполнения:**
- Создан файл `src/test/kotlin/SerializationTest.kt`
- Тесты для всех типов IRequest (ArbitrageOpportunityReadRequest, ArbitrageOpportunitySearchRequest)
- Тесты для всех типов IResponse с корректной полиморфной сериализацией
- Тесты для edge cases (null values, malformed JSON, unknown types)
- Тесты roundtrip сериализации (serialize -> deserialize -> equals)

**Критерии приемки:**
- Все тесты проходят командой `./gradlew :arbitrage-scanner-kafka:test`
- Покрытие тестами сериализации составляет 100%
- Тесты выполняются быстро и не требуют внешних зависимостей

**Зависимости:** Subtask-6

**Команды для проверки:**
- `./gradlew :arbitrage-scanner-kafka:test`

## Subtask-17: Создание unit тестов для KafkaMessageProcessor
**Статус:** ❌ Не выполнена
**Цель и обоснование:** Протестировать корректность обработки сообщений и интеграции с бизнес-логикой
**Описание:** Создать изолированные тесты для KafkaMessageProcessor с mock dependencies
**Архитектурный слой:** Application
**Критерии выполнения:**
- Создан файл `src/test/kotlin/KafkaMessageProcessorTest.kt`
- Тесты для processReadRequest() и processSearchRequest()
- Mock'и для BusinessLogicProcessor, ResponseProducer
- Тесты обработки ошибок на каждом этапе pipeline
- Тесты correlation-id propagation

**Критерии приемки:**
- Все тесты проходят без внешних зависимостей
- Тестируются все ветки выполнения включая error cases
- Mock'и корректно верифицируют взаимодействие с зависимостями

**Зависимости:** Subtask-11

**Команды для проверки:**
- `./gradlew :arbitrage-scanner-kafka:test`

## Subtask-18: Создание интеграционных тестов с embedded Kafka
**Статус:** ❌ Не выполнена
**Цель и обоснование:** Протестировать end-to-end функциональность Kafka транспорта с реальными Kafka операциями
**Описание:** Создать интеграционные тесты с использованием embedded Kafka для полного тестирования request-reply flow
**Архитектурный слой:** Infrastructure
**Критерии выполнения:**
- Создан файл `src/test/kotlin/EmbeddedKafkaTest.kt`
- Настроен embedded Kafka server для тестов
- Тесты полного request-reply цикла для read и search операций
- Тесты обработки множественных сообщений
- Тесты graceful shutdown и error recovery

**Критерии приемки:**
- Все интеграционные тесты проходят изолированно
- Тесты не зависят от внешних Kafka серверов
- Performance тесты показывают приемлемую latency

**Зависимости:** Subtask-13, Subtask-17

**Команды для проверки:**
- `./gradlew :arbitrage-scanner-kafka:test`

## Subtask-19: Настройка shadow plugin для создания fat JAR
**Статус:** ❌ Не выполнена
**Цель и обоснование:** Обеспечить возможность создания standalone JAR файла для deployment аналогично Ktor модулю
**Описание:** Настроить shadow plugin с правильной конфигурацией main class и исключений
**Архитектурный слой:** Infrastructure
**Критерии выполнения:**
- Shadow plugin корректно настроен в build.gradle.kts
- Указан правильный main class для JAR манифеста
- Исключены конфликтующие зависимости и дубликаты
- Fat JAR содержит все необходимые зависимости

**Критерии приемки:**
- Fat JAR создается командой `./gradlew :arbitrage-scanner-kafka:shadowJar`
- JAR запускается автономно: `java -jar arbitrage-scanner-kafka-1.0-SNAPSHOT-all.jar`
- Размер JAR разумный (не избыточный)

**Зависимости:** Subtask-13

**Команды для проверки:**
- `./gradlew :arbitrage-scanner-kafka:shadowJar`
- `java -jar arbitrage-scanner-kafka/build/libs/arbitrage-scanner-kafka-1.0-SNAPSHOT-all.jar`

## Subtask-20: Создание тестов для конфигурационных компонентов
**Статус:** ❌ Не выполнена
**Цель и обоснование:** Обеспечить корректность загрузки и применения конфигурации Kafka компонентов
**Описание:** Создать тесты для всех конфигурационных классов и их интеграции
**Архитектурный слой:** Infrastructure
**Критерии выполнения:**
- Тесты для KafkaProducerConfig с различными настройками
- Тесты для KafkaConsumerConfig с различными consumer groups
- Тесты загрузки конфигурации из application.yaml
- Тесты override конфигурации через environment variables

**Критерии приемки:**
- Все конфигурационные тесты проходят
- Тестируется валидация некорректных конфигураций
- Покрытие конфигурационного кода минимум 90%

**Зависимости:** Subtask-3, Subtask-4, Subtask-5, Subtask-7

**Команды для проверки:**
- `./gradlew :arbitrage-scanner-kafka:test`

## Subtask-21: Тестирование совместимости с существующими API моделями
**Статус:** ❌ Не выполнена
**Цель и обоснование:** Убедиться, что Kafka транспорт полностью совместим с существующими API моделями из arbitrage-scanner-api-v1
**Описание:** Создать compatibility тесты между Kafka и HTTP транспортами для одинаковых операций
**Архитектурный слой:** Integration
**Критерии выполнения:**
- Тесты идентичности JSON сериализации между HTTP и Kafka транспортами
- Тесты совместимости всех Request/Response типов
- Тесты обработки одинаковых stub данных
- Верификация одинаковых результатов бизнес-логики

**Критерии приемки:**
- Результаты обработки через Kafka идентичны HTTP транспорту
- Совместимость подтверждена для всех endpoint'ов
- Нет регрессий в существующей функциональности

**Зависимости:** Subtask-18

**Команды для проверки:**
- `./gradlew :arbitrage-scanner-kafka:test`
- `./gradlew :arbitrage-scanner-ktor:test` (для сравнения)

## Subtask-22: Оптимизация производительности Kafka конфигурации
**Статус:** ❌ Не выполнена
**Цель и обоснование:** Настроить оптимальные параметры Kafka для production использования с балансом latency/throughput
**Описание:** Провести performance tuning конфигурации producer/consumer с benchmarking
**Архитектурный слой:** Infrastructure
**Критерии выполнения:**
- Оптимизированы параметры producer (batch.size, linger.ms, compression.type)
- Оптимизированы параметры consumer (max.poll.records, fetch.min.bytes)
- Добавлены комментарии к критическим параметрам с обоснованием
- Созданы конфигурации для dev/test/prod окружений

**Критерии приемки:**
- Latency обработки сообщения не превышает 100ms в тестах
- Throughput поддерживает минимум 1000 сообщений/сек
- Memory footprint приемлем для production deployment

**Зависимости:** Subtask-18, Subtask-15

**Команды для проверки:**
- `./gradlew :arbitrage-scanner-kafka:test`
- Performance тесты в embedded Kafka

## Subtask-23: Создание документации README для модуля
**Статус:** ❌ Не выполнена
**Цель и обоснование:** Предоставить разработчикам четкую документацию по использованию и настройке Kafka модуля
**Описание:** Создать подробную документацию с примерами конфигурации и инструкциями по запуску
**Архитектурный слой:** Documentation
**Критерии выполнения:**
- Создан README.md в корне модуля arbitrage-scanner-kafka
- Описаны все способы запуска приложения
- Приведены примеры конфигурации для разных окружений
- Описана архитектура модуля и его интеграция с остальными
- Добавлены примеры Kafka topic структуры и message format

**Критерии приемки:**
- Документация позволяет новому разработчику настроить и запустить модуль
- Все примеры конфигурации валидны и протестированы
- Описаны troubleshooting scenarios и их решения

**Зависимости:** Subtask-19, Subtask-22

**Команды для проверки:**
- Следование инструкциям из README должно приводить к успешному запуску

## Subtask-24: Обновление ARCHITECTURE.md с описанием Kafka модуля
**Статус:** ❌ Не выполнена
**Цель и обоснование:** Интегрировать описание нового Kafka транспорта в общую архитектурную документацию проекта
**Описание:** Расширить существующую ARCHITECTURE.md добавлением раздела о Kafka транспорте с диаграммами
**Архитектурный слой:** Documentation
**Критерии выполнения:**
- Добавлен раздел "Kafka Transport Layer" в ARCHITECTURE.md
- Описана интеграция с существующими компонентами
- Добавлены диаграммы request-reply flow через Kafka
- Обновлена компонентная диаграмма с включением Kafka модуля
- Описаны архитектурные паттерны, используемые в Kafka модуле

**Критерии приемки:**
- Документация отражает реальную реализацию
- Диаграммы корректны и читаемы
- Описание соответствует архитектурным принципам проекта

**Зависимости:** Subtask-23

**Команды для проверки:**
- Ревью архитектурной документации

## Subtask-25: Финальная интеграция и проверка всех компонентов
**Статус:** ❌ Не выполнена
**Цель и обоснование:** Провести полную интеграционную проверку всего Kafka модуля и его взаимодействия с остальными компонентами
**Описание:** Выполнить end-to-end тестирование, проверку производительности и готовности к production
**Архитектурный слой:** Integration
**Критерии выполнения:**
- Проведено полное end-to-end тестирование всех операций
- Проверена интеграция со всеми существующими модулями
- Выполнены stress-тесты и проверка memory leaks
- Проверен graceful shutdown при различных нагрузках
- Валидирована корректность всех метрик и логов

**Критерии приемки:**
- Все тесты проходят: `./gradlew :arbitrage-scanner-kafka:check`
- Приложение стабильно работает под нагрузкой минимум 1 час
- Нет memory leaks или connection leaks
- Graceful shutdown работает во всех сценариях
- Функциональная эквивалентность с Ktor модулем подтверждена

**Зависимости:** Все предыдущие подзадачи (Subtask-1 до Subtask-24)

**Команды для проверки:**
- `./gradlew :arbitrage-scanner-kafka:check`
- `./gradlew :arbitrage-scanner-kafka:run`
- `./gradlew :arbitrage-scanner-kafka:shadowJar`
- Stress-тестирование в dev окружении