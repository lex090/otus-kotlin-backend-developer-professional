# Docker окружение для Arbitrage Scanner Kafka

Данный Docker Compose файл настраивает полное Kafka окружение для разработки и тестирования модуля `arbitrage-scanner-kafka`.

## Компоненты

### 1. Zookeeper
- **Порт**: 2181
- **Образ**: confluentinc/cp-zookeeper:7.5.0
- **Назначение**: Координатор для Kafka кластера

### 2. Kafka Broker
- **Порты**:
  - 9092 - для подключения с хоста
  - 9093 - JMX метрики
- **Образ**: confluentinc/cp-kafka:7.5.0
- **Назначение**: Основной Kafka брокер

### 3. Kafka UI
- **Порт**: 8080
- **Образ**: provectuslabs/kafka-ui:latest
- **Назначение**: Веб-интерфейс для мониторинга и управления Kafka
- **URL**: http://localhost:8080

### 4. Kafka Init
- **Назначение**: Автоматическое создание топиков при запуске

## Создаваемые топики

1. **arbitrage-scanner-in** (входящие запросы)
   - Партиции: 3
   - Replication factor: 1

2. **arbitrage-scanner-out** (исходящие ответы)
   - Партиции: 3
   - Replication factor: 1

## Быстрый старт

### Запуск окружения

```bash
# Из директории docker/
docker-compose up -d

# Или из корня модуля
docker-compose -f docker/docker-compose.yml up -d
```

### Проверка статуса

```bash
# Проверить запущенные контейнеры
docker-compose ps

# Просмотр логов
docker-compose logs -f kafka
```

### Остановка окружения

```bash
# Остановить контейнеры
docker-compose down

# Остановить и удалить volumes
docker-compose down -v
```

## Использование Kafka UI

После запуска откройте браузер по адресу: http://localhost:8080

В UI вы можете:
- Просматривать список топиков
- Отправлять тестовые сообщения
- Просматривать содержимое сообщений
- Управлять consumer groups
- Мониторить производительность

## Подключение приложения

После запуска Docker окружения, приложение может подключиться к Kafka по адресу: **localhost:9092**

Пример конфигурации в `application.yml`:

```yaml
kafka:
  bootstrapServers: "localhost:9092"
  groupId: "arbitrage-scanner-group"
  inTopic: "arbitrage-scanner-in"
  outTopic: "arbitrage-scanner-out"
```

## Полезные команды

### Просмотр топиков

```bash
docker exec arbitrage-kafka kafka-topics \
  --bootstrap-server localhost:9092 \
  --list
```

### Отправка тестового сообщения

```bash
docker exec -it arbitrage-kafka kafka-console-producer \
  --bootstrap-server localhost:9092 \
  --topic arbitrage-scanner-in
```

### Чтение сообщений из топика

```bash
docker exec arbitrage-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic arbitrage-scanner-out \
  --from-beginning
```

### Описание топика

```bash
docker exec arbitrage-kafka kafka-topics \
  --bootstrap-server localhost:9092 \
  --describe \
  --topic arbitrage-scanner-in
```

### Просмотр consumer groups

```bash
docker exec arbitrage-kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --list
```

## Troubleshooting

### Kafka не запускается

1. Проверьте, что порты 9092, 2181, 8080 свободны:
```bash
lsof -i :9092
lsof -i :2181
lsof -i :8080
```

2. Проверьте логи:
```bash
docker-compose logs kafka
```

### Топики не создаются

Проверьте логи контейнера kafka-init:
```bash
docker-compose logs kafka-init
```

### Очистка данных

Если нужно полностью очистить все данные Kafka:
```bash
docker-compose down -v
docker-compose up -d
```

## Требования

- Docker 20.10+
- Docker Compose 2.0+
- Свободные порты: 2181, 8080, 9092, 9093

## Производительность

Текущая конфигурация оптимизирована для разработки. Для production использования рекомендуется:
- Увеличить количество брокеров
- Увеличить replication factor
- Настроить retention policies
- Добавить мониторинг (Prometheus + Grafana)

## Дополнительная информация

- [Kafka Documentation](https://kafka.apache.org/documentation/)
- [Kafka UI GitHub](https://github.com/provectus/kafka-ui)
- [Confluent Docker Images](https://docs.confluent.io/platform/current/installation/docker/image-reference.html)