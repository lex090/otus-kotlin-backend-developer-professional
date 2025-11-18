# OpenSearch и Kibana: Настройка мониторинга логов и метрик

## Обзор

Этот документ описывает процесс настройки централизованного логирования и мониторинга для arbitrage-scanner с использованием OpenSearch и OpenSearch Dashboards (Kibana).

## Архитектура

```
arbitrage-scanner → Fluent-bit → OpenSearch → OpenSearch Dashboards
```

- **arbitrage-scanner**: Приложение, генерирующее логи
- **Fluent-bit**: Сборщик и маршрутизатор логов
- **OpenSearch**: Поисковый движок для хранения и индексации логов
- **OpenSearch Dashboards**: Веб-интерфейс для визуализации логов и метрик

## Предварительные требования

- Docker и Docker Compose установлены
- Минимум 4GB свободной RAM
- Порты 9200, 5601, 2020, 24224 свободны

## Шаг 1: Запуск окружения

### 1.1. Запустить все сервисы

```bash
docker-compose -f docker-compose.minimal.yml up -d
```

### 1.2. Проверить статус сервисов

```bash
docker-compose -f docker-compose.minimal.yml ps
```

Ожидаемый результат - все сервисы в состоянии `healthy` или `running`.

### 1.3. Дождаться готовности OpenSearch

OpenSearch требует ~60 секунд для полной инициализации. Проверить готовность:

```bash
curl http://localhost:9200/_cluster/health
```

Ожидаемый ответ:
```json
{
  "cluster_name": "opensearch-cluster",
  "status": "green",
  ...
}
```

## Шаг 2: Настройка индексов OpenSearch

### 2.1. Применить Index Template

Index template автоматически конфигурирует новые индексы `fluentbit-*`:

```bash
curl -X PUT "http://localhost:9200/_index_template/fluentbit-template" \
  -H 'Content-Type: application/json' \
  -u admin:Admin123! \
  -d @specs/001-opensearch-kibana-integration/contracts/index-template.json
```

### 2.2. Применить ISM Policy для 7-дневного retention

ISM (Index State Management) policy автоматически удаляет индексы старше 7 дней:

```bash
curl -X PUT "http://localhost:9200/_plugins/_ism/policies/7-day-retention" \
  -H 'Content-Type: application/json' \
  -u admin:Admin123! \
  -d @specs/001-opensearch-kibana-integration/contracts/ism-policy.json
```

### 2.3. Проверить применение policy

```bash
curl -u admin:Admin123! "http://localhost:9200/_plugins/_ism/policies/7-day-retention"
```

## Шаг 3: Настройка OpenSearch Dashboards

### 3.1. Открыть OpenSearch Dashboards

Открыть в браузере: http://localhost:5601

**Credentials**:
- Username: `admin`
- Password: `Admin123!`

### 3.2. Создать Index Pattern

1. В левом меню выбрать **Management** → **Dashboards Management** → **Index Patterns**
2. Нажать **Create index pattern**
3. В поле "Index pattern name" ввести: `fluentbit-*`
4. Нажать **Next step**
5. В "Time field" выбрать: `@timestamp`
6. Нажать **Create index pattern**

### 3.3. Проверить логи в Discover

1. В левом меню выбрать **Discover**
2. Выбрать index pattern `fluentbit-*`
3. Установить временной диапазон (например, Last 15 minutes)
4. Вы должны увидеть логи из arbitrage-scanner

## Шаг 4: Создание дашборда для HTTP метрик

**Важно**: HTTP метрики находятся в поле `message` в формате key=value. Для визуализации необходимо использовать фильтры по тексту или scripted fields.

### 4.1. Создать Saved Search для HTTP логов

1. Открыть **Discover**
2. Выбрать index pattern `fluentbit-*`
3. В search bar ввести: `message:http_method`
4. Нажать **Save** → Название: "HTTP Requests"

### 4.2. Создать новый Dashboard

1. Перейти в **Dashboard** → **Create new dashboard**
2. Нажать **Add an existing** → Выбрать saved search "HTTP Requests"
3. Нажать **Save** → Название: "HTTP Metrics Dashboard"

### 4.3. Добавить визуализацию: Общее количество запросов

1. В dashboard нажать **Edit**
2. Нажать **Create new** → **Aggregation based**
3. Выбрать **Metric**
4. В **Metrics**:
   - Aggregation: **Count**
5. В **Buckets** добавить **Split group**:
   - Aggregation: **Filters**
   - Filter 0: `message:http_method`
6. **Options** → Title: "Total HTTP Requests"
7. Нажать **Update** → **Save and return**

### 4.4. Добавить визуализацию: Запросы по методам

**Примечание**: Так как методы в текстовом поле, используем фильтры:

1. **Create new** → **Aggregation based** → **Pie**
2. **Metrics**: Count
3. **Buckets** → **Split slices**:
   - Aggregation: **Filters**
   - Filter 0 Label: "GET" Query: `message:"http_method=GET"`
   - Filter 1 Label: "POST" Query: `message:"http_method=POST"`
   - Filter 2 Label: "PUT" Query: `message:"http_method=PUT"`
   - Filter 3 Label: "DELETE" Query: `message:"http_method=DELETE"`
4. Title: "Requests by HTTP Method"
5. **Save and return**

### 4.5. Добавить визуализацию: Запросы во времени

1. **Create new** → **Aggregation based** → **Line**
2. **Metrics**: Count
3. **Buckets** → **X-axis**:
   - Aggregation: **Date Histogram**
   - Field: `@timestamp`
   - Minimum interval: `Auto`
4. **Buckets** → **Split series**:
   - Sub aggregation: **Filters**
   - Filter 0 Label: "HTTP Requests" Query: `message:http_method`
5. Title: "HTTP Requests Over Time"
6. **Save and return**

### 4.6. Добавить визуализацию: Статусы ответов

1. **Create new** → **Aggregation based** → **Pie**
2. **Metrics**: Count
3. **Buckets** → **Split slices**:
   - Aggregation: **Filters**
   - Filter 0 Label: "2xx Success" Query: `message:"http_status=2"`
   - Filter 1 Label: "4xx Client Error" Query: `message:"http_status=4"`
   - Filter 2 Label: "5xx Server Error" Query: `message:"http_status=5"`
4. Title: "Response Status Distribution"
5. **Save and return**

### 4.7. Настроить layout и автообновление

1. Перетащить визуализации для удобного расположения
2. Нажать **Save**
3. В правом верхнем углу нажать на иконку времени
4. Установить time range: **Last 15 minutes**
5. Нажать на иконку **Refresh** → Выбрать **Auto refresh: 10s**

### 4.8. Сохранить дашборд

1. Нажать **Save** в правом верхнем углу
2. Название должно быть: "HTTP Metrics Dashboard"
3. Поставить галочку "Store time with dashboard"
4. Нажать **Save**

## Шаг 5: Генерация тестовых логов

### 5.1. Отправить тестовые HTTP запросы

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

### 5.2. Проверить логи в Dashboards

1. Обновить страницу **Discover**
2. Установить временной диапазон "Last 5 minutes"
3. Применить filter: `http_method:*`
4. Логи должны появиться в течение 5 секунд

## Полезные запросы и фильтры

### Поиск ошибок

В Discover применить filter:
```
level:error OR http_status >= 400
```

### Поиск медленных запросов

```
response_time_ms >= 1000
```

### Фильтрация по сервису

```
service_name:"arbitrage-scanner"
```

### Поиск по тексту сообщения

```
message:"arbitrage" AND level:info
```

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

## Полезные запросы и фильтры

### Поиск HTTP логов

В Discover применить:
```
message:http_method
```

### Поиск логов с ошибками

```
level:error
```

### Поиск HTTP ошибок (4xx, 5xx)

```
message:"http_status=4" OR message:"http_status=5"
```

### Поиск логов определенного сервиса

```
cluster:arbitrage-scanner-dev
```

### Поиск по тексту сообщения

```
message:"arbitrage" AND level:info
```

### Фильтрация по временному диапазону

Используйте time picker в правом верхнем углу:
- Last 15 minutes
- Last 1 hour
- Last 24 hours
- Last 7 days
- Custom range

## Credentials Summary

**Важно**: Эти credentials используются только для development окружения. Для production необходимо изменить пароли!

| Сервис | URL | Username | Password | Назначение |
|--------|-----|----------|----------|------------|
| OpenSearch | http://localhost:9200 | admin | Admin123! | API доступ, curl запросы |
| OpenSearch Dashboards | http://localhost:5601 | admin | Admin123! | Веб-интерфейс для просмотра логов |
| Fluent-bit (metrics) | http://localhost:2020 | - | - | Health check и метрики |
| Arbitrage Scanner | http://localhost:8080 | - | - | Приложение |

## Проверка работоспособности

### Checklist

- [ ] OpenSearch доступен: `curl -u admin:Admin123! http://localhost:9200/_cluster/health`
- [ ] Fluent-bit health check OK: `curl http://localhost:2020/api/v1/health`
- [ ] OpenSearch Dashboards доступен: http://localhost:5601
- [ ] Index pattern создан в Dashboards
- [ ] Логи видны в Discover
- [ ] Дашборд метрик создан и отображает данные
- [ ] Все Docker сервисы в состоянии healthy: `docker-compose ps`

## Следующие шаги

1. **Кастомизация дашборда**: Добавить дополнительные визуализации
2. **Saved searches**: Сохранить часто используемые поисковые запросы
3. **Export/Import**: Экспортировать дашборды для версионирования

---

**Готово!** Система централизованного логирования и мониторинга настроена и готова к использованию.
