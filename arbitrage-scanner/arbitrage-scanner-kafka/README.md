# Arbitrage Scanner Kafka Transport

Модуль для обработки запросов к Arbitrage Scanner через Apache Kafka в качестве транспортного слоя.

## Описание

Данный модуль предоставляет асинхронный Kafka-based транспорт для работы с Arbitrage Scanner. Он принимает запросы из Kafka топика, обрабатывает их через бизнес-логику и отправляет ответы обратно в другой Kafka топик.

### Основные возможности

- Асинхронная обработка запросов через Apache Kafka
- Поддержка операций READ и SEARCH для арбитражных возможностей
- Correlation ID для сопоставления запросов и ответов
- Гибкая конфигурация через YAML файлы
- Dependency Injection через Koin
- Интеграционные тесты с Testcontainers
- Docker окружение для разработки

## Архитектура

```
┌─────────────────┐         ┌──────────────────┐         ┌─────────────────┐
│                 │         │                  │         │                 │
│  Kafka IN       │────────▶│  Consumer        │────────▶│  Processor      │
│  Topic          │         │                  │         │                 │
│                 │         └──────────────────┘         └─────────────────┘
└─────────────────┘                                              │
                                                                 │
                                                                 ▼
┌─────────────────┐         ┌──────────────────┐         ┌─────────────────┐
│                 │         │                  │         │                 │
│  Kafka OUT      │◀────────│  Producer        │◀────────│  Business       │
│  Topic          │         │                  │         │  Logic          │
│                 │         └──────────────────┘         └─────────────────┘
└─────────────────┘
```

### Компоненты

#### 1. Consumer (`ArbitrageKafkaConsumer`)
- Подписывается на входящий Kafka топик
- Читает сообщения и извлекает метаданные из заголовков
- Передает сообщения процессору для обработки
- Управляет жизненным циклом consumer'а

#### 2. Processor (`KafkaMessageProcessor`, `KafkaContextProcessor`)
- `KafkaMessageProcessor` - обрабатывает входящие Kafka сообщения
- `KafkaContextProcessor` - конвертирует контекст бизнес-логики в ответы
- Маршрутизация запросов по типу (READ/SEARCH)
- Обработка ошибок и формирование ответов

#### 3. Producer (`KafkaResponseProducer`)
- Отправляет ответы в исходящий Kafka топик
- Сохраняет correlation ID для трассировки
- Настроен на надежную доставку (acks=all)

#### 4. Configuration (`KafkaConfig`, `ConfigLoader`)
- Загрузка конфигурации из YAML файлов
- Настройки Consumer и Producer
- Конфигурация топиков и параметров подключения

#### 5. Models (`KafkaMessage`)
- Модель для представления Kafka сообщений
- Извлечение метаданных из заголовков
- Fallback стратегии для отсутствующих данных

## Требования

- Java 21+
- Kotlin 2.2.0+
- Apache Kafka 3.8+
- Gradle 8.5+
- Docker (для локального окружения)

## Зависимости

Модуль использует следующие внутренние зависимости:
- `arbitrage-scanner-common` - общие модели и контракты
- `arbitrage-scanner-api-v1` - API модели версии 1
- `arbitrage-scanner-business-logic` - бизнес-логика обработки
- `arbitrage-scanner-lib-logging-logback` - логирование

Внешние библиотеки:
- Apache Kafka Clients 3.8.1
- Kotlinx Serialization 1.9.0
- Hoplite 2.7.5 (конфигурация)
- Koin 3.5.6 (DI)
- Testcontainers 1.20.4 (тестирование)

## Конфигурация

### Файл application.yml

Создайте файл `application.yml` в `src/main/resources`:

```yaml
kafka:
  bootstrapServers: "localhost:9092"

  consumer:
    groupId: "arbitrage-scanner-group"
    topics:
      - "arbitrage-scanner-in"
    autoOffsetReset: "earliest"
    enableAutoCommit: true
    autoCommitIntervalMs: 5000
    sessionTimeoutMs: 30000
    maxPollRecords: 500
    properties: {}

  producer:
    clientId: "arbitrage-scanner-producer"
    topics:
      responses: "arbitrage-scanner-out"
    acks: "all"
    retries: 3
    batchSize: 16384
    lingerMs: 10
    compressionType: "gzip"
    maxInFlightRequestsPerConnection: 5
    properties: {}
```

### Переменные окружения

Можно переопределить конфигурацию через переменные окружения:

```bash
export KAFKA_BOOTSTRAP_SERVERS="kafka-broker:9092"
export KAFKA_CONSUMER_GROUP_ID="custom-group"
export KAFKA_CONSUMER_TOPICS="custom-in-topic"
export KAFKA_PRODUCER_TOPICS_RESPONSES="custom-out-topic"
```

## Топики Kafka

### Входящий топик (по умолчанию: `arbitrage-scanner-in`)

Принимает запросы в формате JSON с заголовками:
- `correlation-id` - уникальный ID для сопоставления запроса/ответа
- `request-type` - тип запроса (READ, SEARCH)
- `timestamp` - временная метка создания запроса

### Исходящий топик (по умолчанию: `arbitrage-scanner-out`)

Отправляет ответы в формате JSON с теми же заголовками для корреляции.

## Запуск

### 1. Локальная разработка с Docker

#### Запуск Kafka окружения

```bash
cd docker
docker-compose up -d
```

Это запустит:
- Zookeeper (порт 2181)
- Kafka Broker (порт 9092)
- Kafka UI (порт 8080, http://localhost:8080)

#### Запуск приложения

```bash
# Из корня проекта
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:run

# Или с конкретной конфигурацией
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:run \
  -Dkafka.bootstrapServers=localhost:9092
```

### 2. Сборка JAR

```bash
# Сборка обычного JAR
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:build

# Сборка Fat JAR со всеми зависимостями
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:shadowJar
```

JAR файл будет создан в: `build/libs/arbitrage-scanner-kafka-1.0-SNAPSHOT-all.jar`

Запуск:
```bash
java -jar build/libs/arbitrage-scanner-kafka-1.0-SNAPSHOT-all.jar
```

### 3. Docker контейнер

```bash
# Сборка Docker образа через Jib
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:jibDockerBuild

# Запуск контейнера
docker run -e KAFKA_BOOTSTRAP_SERVERS=host.docker.internal:9092 \
  arbitrage-scanner-kafka:1.0-SNAPSHOT
```

## Примеры использования

### Отправка READ запроса

```bash
# Через Kafka console producer
docker exec -it arbitrage-kafka kafka-console-producer \
  --bootstrap-server localhost:9092 \
  --topic arbitrage-scanner-in \
  --property "parse.headers=true" \
  --property "headers.delimiter=|"

# Ввести (каждая строка - отдельное сообщение):
correlation-id:12345|request-type:READ|timestamp:1234567890
{"requestType":"read","requestId":"req-001","opportunityId":"opp-123","debug":{"mode":"STUB","stubs":"SUCCESS"}}
```

### Отправка SEARCH запроса

```bash
correlation-id:67890|request-type:SEARCH|timestamp:1234567890
{"requestType":"search","requestId":"req-002","filter":{"minProfitPercent":1.5,"tokenSymbol":"BTC"},"debug":{"mode":"STUB","stubs":"SUCCESS"}}
```

### Чтение ответов

```bash
docker exec arbitrage-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic arbitrage-scanner-out \
  --from-beginning \
  --property print.headers=true \
  --property print.timestamp=true
```

### Использование Kafka UI

Откройте http://localhost:8080 для:
- Просмотра топиков и сообщений
- Отправки тестовых сообщений через веб-интерфейс
- Мониторинга Consumer Groups
- Просмотра метрик

## Тестирование

### Запуск всех тестов

```bash
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:test
```

### Запуск интеграционных тестов

```bash
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:test \
  --tests "com.arbitrage.scanner.integration.*"
```

### Запуск unit тестов

```bash
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:test \
  --tests "com.arbitrage.scanner.*Test"
```

### Тесты с отчетом

```bash
./gradlew :arbitrage-scanner:arbitrage-scanner-kafka:test \
  :arbitrage-scanner:arbitrage-scanner-kafka:jacocoTestReport
```

Отчет будет в: `build/reports/jacoco/test/html/index.html`

## Мониторинг и отладка

### Логирование

Приложение использует Logback для логирования. Настройки в `src/main/resources/logback.xml`.

Уровни логирования можно изменить через переменные окружения:
```bash
export LOG_LEVEL=DEBUG
```

### Метрики Kafka

Consumer и Producer экспортируют JMX метрики на порт 9093.

Подключение через JConsole:
```bash
jconsole localhost:9093
```

### Health Check

Приложение логирует статус consumer'а каждые 30 секунд:
- Подключение к Kafka
- Статус подписки на топики
- Количество обработанных сообщений

## Troubleshooting

### Проблема: Consumer не получает сообщения

**Решение:**
1. Проверьте, что Kafka запущен: `docker ps | grep kafka`
2. Проверьте топики: `docker exec arbitrage-kafka kafka-topics --list --bootstrap-server localhost:9092`
3. Проверьте consumer group: `docker exec arbitrage-kafka kafka-consumer-groups --bootstrap-server localhost:9092 --group arbitrage-scanner-group --describe`
4. Проверьте логи приложения

### Проблема: Ошибки сериализации JSON

**Решение:**
1. Убедитесь, что запрос соответствует формату API (см. API.md)
2. Проверьте логи на наличие stack trace
3. Используйте `debug.mode=STUB` для тестирования без валидации

### Проблема: Медленная обработка

**Решение:**
1. Увеличьте `maxPollRecords` в конфигурации
2. Увеличьте количество партиций топика
3. Запустите несколько инстансов consumer'а (scaling)
4. Проверьте метрики JMX

### Проблема: Kafka недоступен

**Решение:**
1. Проверьте `bootstrapServers` в конфигурации
2. Проверьте сетевую доступность: `telnet localhost 9092`
3. Проверьте Docker сеть: `docker network inspect arbitrage-network`
4. Перезапустите контейнеры: `docker-compose restart`

## Production Considerations

Для production окружения рекомендуется:

### 1. Kafka кластер
- Минимум 3 брокера для отказоустойчивости
- Replication factor >= 3 для критичных топиков
- Мониторинг через Prometheus + Grafana

### 2. Безопасность
- Включить SSL/TLS для шифрования
- Настроить SASL для аутентификации
- Ограничить ACL для топиков

### 3. Производительность
- Настроить retention policies для топиков
- Использовать сжатие (gzip, lz4, zstd)
- Настроить batch размеры для producer'а
- Использовать несколько consumer инстансов

### 4. Мониторинг
- JMX метрики Kafka
- Application metrics (Micrometer)
- Distributed tracing (Jaeger, Zipkin)
- Логирование в ELK stack

### 5. Backup и Recovery
- Kafka MirrorMaker для репликации
- Регулярные бэкапы конфигурации
- Disaster recovery план

## Структура проекта

```
arbitrage-scanner-kafka/
├── build.gradle.kts              # Gradle конфигурация
├── README.md                      # Данный файл
├── API.md                         # Описание API
├── docker/                        # Docker окружение
│   ├── docker-compose.yml        # Kafka + Zookeeper + UI
│   └── README.md                 # Инструкции по Docker
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── com/arbitrage/scanner/
│   │   │       ├── Application.kt              # Main entry point
│   │   │       ├── KoinConfiguration.kt        # DI конфигурация
│   │   │       ├── config/                     # Конфигурация
│   │   │       │   ├── ConfigLoader.kt
│   │   │       │   └── KafkaConfig.kt
│   │   │       ├── consumers/                  # Kafka consumers
│   │   │       │   └── ArbitrageKafkaConsumer.kt
│   │   │       ├── producers/                  # Kafka producers
│   │   │       │   ├── ResponseProducer.kt
│   │   │       │   └── KafkaResponseProducer.kt
│   │   │       ├── processors/                 # Обработчики сообщений
│   │   │       │   ├── KafkaMessageProcessor.kt
│   │   │       │   └── KafkaContextProcessor.kt
│   │   │       └── models/                     # Модели данных
│   │   │           └── KafkaMessage.kt
│   │   └── resources/
│   │       ├── application.yml                 # Конфигурация приложения
│   │       └── logback.xml                     # Конфигурация логирования
│   └── test/
│       └── kotlin/
│           └── com/arbitrage/scanner/
│               ├── integration/                # Интеграционные тесты
│               │   └── KafkaIntegrationTest.kt
│               ├── producers/                  # Unit тесты
│               │   └── KafkaResponseProducerTest.kt
│               └── config/
│                   └── ConfigLoaderTest.kt
└── build/                                      # Build артефакты
```

## Дополнительная документация

- [API.md](API.md) - Подробное описание форматов запросов и ответов
- [docker/README.md](docker/README.md) - Инструкции по Docker окружению
- [ARCHITECTURE.md](../../ARCHITECTURE.md) - Общая архитектура проекта
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)

## Лицензия

Проект разработан в рамках курса OTUS "Kotlin Backend Developer Professional".

## Контакты

Для вопросов и предложений обращайтесь к команде разработки проекта Arbitrage Scanner.