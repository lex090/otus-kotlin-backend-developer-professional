# Quick Start: OpenSearch и Kibana мониторинг

**Feature**: 001-opensearch-kibana-integration
**Цель**: Быстрый запуск системы централизованного логирования и мониторинга

## Предварительные требования

- Docker и Docker Compose установлены
- Минимум 4GB свободной RAM
- Порты 9200, 5601, 2020, 24224 свободны
- Базовое понимание Docker и логирования

## Шаг 1: Обновление Docker Compose конфигурации

### 1.1. Добавить OpenSearch и Dashboards сервисы

Открыть `docker-compose.minimal.yml` и добавить следующие сервисы:

```yaml
  # OpenSearch - поисковый движок для логов
  opensearch:
    image: opensearchproject/opensearch:2.11.1
    container_name: opensearch
    environment:
      - cluster.name=opensearch-cluster
      - node.name=opensearch-node1
      - discovery.type=single-node
      - bootstrap.memory_lock=true
      - "OPENSEARCH_JAVA_OPTS=-Xms512m -Xmx512m"
      - "DISABLE_INSTALL_DEMO_CONFIG=true"
      - "OPENSEARCH_INITIAL_ADMIN_PASSWORD=Admin123!"
      - plugins.security.ssl.http.enabled=false
      - plugins.security.ssl.transport.enabled=false
    ulimits:
      memlock:
        soft: -1
        hard: -1
      nofile:
        soft: 65536
        hard: 65536
    ports:
      - "9200:9200"
      - "9600:9600"
    volumes:
      - opensearch-data:/usr/share/opensearch/data
    mem_limit: 1.5g
    healthcheck:
      test: ["CMD-SHELL", "curl -u admin:Admin123! -f http://localhost:9200/_cluster/health || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 5
      start_period: 60s

  # OpenSearch Dashboards - UI для визуализации
  opensearch-dashboards:
    image: opensearchproject/opensearch-dashboards:2.11.1
    container_name: opensearch-dashboards
    depends_on:
      opensearch:
        condition: service_healthy
    environment:
      - 'OPENSEARCH_HOSTS=["http://opensearch:9200"]'
      - "DISABLE_SECURITY_DASHBOARDS_PLUGIN=false"
      - "SERVER_HOST=0.0.0.0"
    ports:
      - "5601:5601"
    mem_limit: 512m
    healthcheck:
      test: ["CMD-SHELL", "curl -f http://localhost:5601/api/status || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 5
      start_period: 60s
```

### 1.2. Обновить зависимости существующих сервисов

Обновить `fluent-bit` сервис:

```yaml
  fluent-bit:
    image: fluent/fluent-bit:3.0
    container_name: fluent-bit
    depends_on:
      opensearch:
        condition: service_healthy
    # ... остальная конфигурация
```

Обновить `arbitrage-scanner` сервис:

```yaml
  arbitrage-scanner:
    # ... другие настройки
    depends_on:
      postgres:
        condition: service_healthy
      liquibase:
        condition: service_completed_successfully
      fluent-bit:
        condition: service_started
      opensearch:
        condition: service_healthy
      kafka:
        condition: service_started
```

### 1.3. Добавить volume для OpenSearch

В секцию `volumes:` добавить:

```yaml
volumes:
  postgres-data:
  kafka-data:
  opensearch-data:  # <-- добавить эту строку
```

## Шаг 2: Обновление конфигурации Fluent-bit

Заменить содержимое `config/fluent-bit/fluent-bit.conf`:

```conf
[SERVICE]
    Flush         1
    Log_Level     info
    Daemon        off
    HTTP_Server   On
    HTTP_Listen   0.0.0.0
    HTTP_Port     2020
    storage.type  memory
    storage.metrics on

[INPUT]
    Name              forward
    Listen            0.0.0.0
    Port              24224
    Tag               app
    Mem_Buf_Limit     64MB

[FILTER]
    Name record_modifier
    Match app
    Record hostname ${HOSTNAME}
    Record cluster arbitrage-scanner-dev

[OUTPUT]
    Name  opensearch
    Match app
    Host  opensearch
    Port  9200
    Index fluentbit
    Type  _doc
    HTTP_User admin
    HTTP_Passwd Admin123!
    tls   Off
    tls.verify Off
    Suppress_Type_Name On
    Logstash_Format On
    Logstash_Prefix fluentbit
    Logstash_DateFormat %Y.%m.%d
    Retry_Limit 5
    Buffer_Size 5MB

# Опционально: для отладки
[OUTPUT]
    Name  stdout
    Match app
    Format json_lines
```

## Шаг 3: Запуск окружения

### 3.1. Запустить все сервисы

```bash
docker-compose -f docker-compose.minimal.yml up -d
```

### 3.2. Проверить статус сервисов

```bash
docker-compose -f docker-compose.minimal.yml ps
```

Ожидаемый вывод - все сервисы в состоянии `healthy` или `running`:
```
NAME                        STATUS
opensearch                  Up (healthy)
opensearch-dashboards       Up (healthy)
fluent-bit                  Up (healthy)
arbitrage-scanner           Up
...
```

### 3.3. Проверить логи при проблемах

```bash
# OpenSearch
docker-compose -f docker-compose.minimal.yml logs opensearch

# Fluent-bit
docker-compose -f docker-compose.minimal.yml logs fluent-bit

# OpenSearch Dashboards
docker-compose -f docker-compose.minimal.yml logs opensearch-dashboards
```

## Шаг 4: Настройка OpenSearch (через API)

### 4.1. Применить Index Template

```bash
curl -X PUT "http://localhost:9200/_index_template/fluentbit-template" \
  -H 'Content-Type: application/json' \
  -u admin:Admin123! \
  -d '{
    "index_patterns": ["fluentbit-*"],
    "template": {
      "settings": {
        "number_of_shards": 1,
        "number_of_replicas": 0,
        "refresh_interval": "1s"
      },
      "mappings": {
        "properties": {
          "@timestamp": {"type": "date"},
          "level": {"type": "keyword"},
          "service_name": {"type": "keyword"},
          "message": {
            "type": "text",
            "fields": {"keyword": {"type": "keyword"}}
          },
          "http_method": {"type": "keyword"},
          "http_status": {"type": "integer"},
          "response_time_ms": {"type": "long"}
        }
      }
    }
  }'
```

### 4.2. Применить ISM Policy для 7-дневного retention

```bash
curl -X PUT "http://localhost:9200/_plugins/_ism/policies/7-day-retention" \
  -H 'Content-Type: application/json' \
  -u admin:Admin123! \
  -d '{
    "policy": {
      "description": "Delete indices older than 7 days",
      "default_state": "hot",
      "states": [
        {
          "name": "hot",
          "transitions": [{
            "state_name": "delete",
            "conditions": {"min_index_age": "7d"}
          }]
        },
        {
          "name": "delete",
          "actions": [{"delete": {}}]
        }
      ],
      "ism_template": [{
        "index_patterns": ["fluentbit-*"],
        "priority": 100
      }]
    }
  }'
```

## Шаг 5: Настройка OpenSearch Dashboards (через UI)

### 5.1. Открыть OpenSearch Dashboards

Открыть в браузере: http://localhost:5601

**Credentials**:
- Username: `admin`
- Password: `Admin123!`

### 5.2. Создать Index Pattern

1. В левом меню выбрать **Management** → **Dashboards Management** → **Index Patterns**
2. Нажать **Create index pattern**
3. В поле "Index pattern name" ввести: `fluentbit-*`
4. Нажать **Next step**
5. В "Time field" выбрать: `@timestamp`
6. Нажать **Create index pattern**

### 5.3. Проверить логи в Discover

1. В левом меню выбрать **Discover**
2. Выбрать index pattern `fluentbit-*`
3. Установить временной диапазон (например, Last 15 minutes)
4. Вы должны увидеть логи из arbitrage-scanner

### 5.4. Создать дашборд для HTTP метрик

1. Перейти в **Dashboards** → **Create new dashboard**
2. Нажать **Create visualization**

#### Визуализация 1: Общее количество запросов

- Type: **Metric**
- Metric: **Count**
- Filter: `http_method:*` (чтобы отобрать только HTTP логи)
- Название: "Total HTTP Requests"

#### Визуализация 2: Запросы по методам

- Type: **Pie Chart**
- Buckets: Split slices
- Aggregation: **Terms**
- Field: `http_method.keyword`
- Название: "Requests by HTTP Method"

#### Визуализация 3: Среднее время ответа

- Type: **Metric**
- Metric: **Average**
- Field: `response_time_ms`
- Название: "Average Response Time (ms)"

#### Визуализация 4: Запросы во времени

- Type: **Line Chart**
- X-axis: **Date Histogram** on `@timestamp`
- Y-axis: **Count**
- Название: "Requests Over Time"

3. Сохранить дашборд как "HTTP Metrics Dashboard"

## Шаг 6: Генерация тестовых логов

### 6.1. Отправить тестовые HTTP запросы

```bash
# GET запросы
for i in {1..10}; do
  curl http://localhost:8080/api/v1/opportunities
  sleep 1
done

# POST запросы (если endpoint поддерживается)
for i in {1..5}; do
  curl -X POST http://localhost:8080/api/v1/opportunities \
    -H "Content-Type: application/json" \
    -d '{"test": "data"}'
  sleep 1
done
```

### 6.2. Проверить логи в Dashboards

1. Обновить страницу **Discover**
2. Установить временной диапазон "Last 5 minutes"
3. Применить filter: `http_method:*`
4. Вы должны увидеть свои HTTP запросы

### 6.3. Проверить дашборд метрик

1. Открыть созданный "HTTP Metrics Dashboard"
2. Обновить страницу
3. Должны появиться данные о запросах

## Шаг 7: Полезные запросы и фильтры

### Поиск ошибок

В Discover применить filter:
```
level:error OR http_status >= 400
```

### Поиск медленных запросов

В Discover применить filter:
```
response_time_ms >= 1000
```

### Фильтрация по сервису

В Discover применить filter:
```
service_name:"arbitrage-scanner"
```

### Поиск по тексту сообщения

В search bar ввести:
```
message:"arbitrage" AND level:info
```

## Шаг 8: Проверка работоспособности

### Checklist

- [ ] OpenSearch доступен: `curl -u admin:Admin123! http://localhost:9200/_cluster/health`
- [ ] Fluent-bit health check OK: `curl http://localhost:2020/api/v1/health`
- [ ] OpenSearch Dashboards доступен: http://localhost:5601
- [ ] Index pattern создан в Dashboards
- [ ] Логи видны в Discover
- [ ] Дашборд метрик создан и отображает данные
- [ ] Все Docker сервисы в состоянии healthy: `docker-compose ps`

## Troubleshooting

### OpenSearch не стартует

**Проблема**: Container постоянно перезапускается

**Решение**:
1. Проверить логи: `docker-compose logs opensearch`
2. Увеличить vm.max_map_count (Linux):
   ```bash
   sudo sysctl -w vm.max_map_count=262144
   ```
3. Проверить memory limits: OpenSearch требует минимум 512MB heap

### Логи не появляются в Dashboards

**Проблема**: Discover показывает "No results found"

**Решение**:
1. Проверить что Fluent-bit подключен к OpenSearch:
   ```bash
   docker-compose exec fluent-bit curl -u admin:Admin123! http://opensearch:9200
   ```
2. Проверить что индексы создаются:
   ```bash
   curl -u admin:Admin123! http://localhost:9200/_cat/indices?v
   ```
3. Проверить временной диапазон в Discover (установить Last 24 hours)

### OpenSearch Dashboards не доступен

**Проблема**: http://localhost:5601 не открывается

**Решение**:
1. Убедиться что OpenSearch healthy:
   ```bash
   docker-compose ps opensearch
   ```
2. Проверить логи Dashboards:
   ```bash
   docker-compose logs opensearch-dashboards
   ```
3. Дождаться полного запуска (может занять 1-2 минуты)

### Превышение memory limit

**Проблема**: Docker сервисы killed из-за OOM

**Решение**:
1. Проверить использование памяти:
   ```bash
   docker stats
   ```
2. Уменьшить heap size OpenSearch в docker-compose.yml:
   ```yaml
   OPENSEARCH_JAVA_OPTS: "-Xms256m -Xmx256m"
   ```
3. Остановить неиспользуемые сервисы

## Следующие шаги

1. **Кастомизация дашборда**: Добавить дополнительные визуализации
2. **Алерты**: Настроить alerting для критических ошибок (требует OpenSearch Alerting plugin)
3. **Index aliases**: Создать aliases для упрощения запросов
4. **Saved searches**: Сохранить часто используемые поисковые запросы
5. **Export/Import**: Экспортировать дашборды для версионирования

## Полезные ссылки

- OpenSearch API: http://localhost:9200
- OpenSearch Dashboards: http://localhost:5601
- Fluent-bit metrics: http://localhost:2020/api/v1/metrics/prometheus
- Arbitrage Scanner: http://localhost:8080

## Credentials summary

| Сервис | URL | Username | Password |
|--------|-----|----------|----------|
| OpenSearch | http://localhost:9200 | admin | Admin123! |
| OpenSearch Dashboards | http://localhost:5601 | admin | Admin123! |
| Fluent-bit (metrics) | http://localhost:2020 | - | - |

---

**Готово!** Система централизованного логирования и мониторинга настроена и готова к использованию.

При возникновении проблем обратитесь к `contracts/README.md` для дополнительной информации.
