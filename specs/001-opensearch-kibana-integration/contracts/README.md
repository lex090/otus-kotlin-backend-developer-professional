# Contracts Directory

Эта директория содержит конфигурационные "контракты" для интеграции OpenSearch и Kibana.

## Файлы

### 1. docker-compose-opensearch.yml
**Назначение**: Полная конфигурация Docker Compose сервисов для добавления в `docker-compose.minimal.yml`

**Содержит**:
- OpenSearch сервис с настройками безопасности и health checks
- OpenSearch Dashboards для визуализации
- Обновленные зависимости для существующих сервисов
- Volume конфигурацию для персистентности
- Network конфигурацию

**Как использовать**: Скопировать содержимое в `docker-compose.minimal.yml` и адаптировать существующие сервисы

---

### 2. fluent-bit-opensearch.conf
**Назначение**: Конфигурация Fluent-bit для отправки логов в OpenSearch

**Содержит**:
- INPUT секцию для Forward protocol
- FILTER секции для парсинга и добавления метаданных
- OUTPUT секцию для OpenSearch с аутентификацией
- Memory buffer настройки (64MB)
- Retry logic

**Как использовать**: Заменить существующий `config/fluent-bit/fluent-bit.conf`

---

### 3. ism-policy.json
**Назначение**: ISM (Index State Management) политика для автоматического удаления старых индексов

**Содержит**:
- Policy definition для 7-дневного retention
- Автоматическое применение к индексам `fluentbit-*`
- Transition rules между состояниями

**Как применить**:
```bash
curl -X PUT "http://localhost:9200/_plugins/_ism/policies/7-day-retention" \
  -H 'Content-Type: application/json' \
  -u admin:Admin123! \
  -d @ism-policy.json
```

---

### 4. index-template.json
**Назначение**: Index template для автоматической конфигурации новых индексов логов

**Содержит**:
- Mapping определение для всех полей логов и метрик
- Settings (shards, replicas, compression)
- Автоматическое применение к `fluentbit-*` паттерну

**Как применить**:
```bash
curl -X PUT "http://localhost:9200/_index_template/fluentbit-template" \
  -H 'Content-Type: application/json' \
  -u admin:Admin123! \
  -d @index-template.json
```

---

## Порядок применения

1. **Обновить docker-compose.minimal.yml** используя `docker-compose-opensearch.yml`
2. **Обновить config/fluent-bit/fluent-bit.conf** используя `fluent-bit-opensearch.conf`
3. **Запустить окружение**: `docker-compose -f docker-compose.minimal.yml up -d`
4. **Дождаться healthy статуса** OpenSearch: `docker-compose ps`
5. **Применить index template**: используя curl команду выше
6. **Применить ISM policy**: используя curl команду выше
7. **Настроить Kibana**: следовать инструкциям в `quickstart.md`

---

## Credentials

**OpenSearch**:
- URL: http://localhost:9200
- Username: admin
- Password: Admin123!

**OpenSearch Dashboards**:
- URL: http://localhost:5601
- Используются те же credentials что и для OpenSearch

---

## Проверка работоспособности

### OpenSearch
```bash
curl -u admin:Admin123! http://localhost:9200/_cluster/health?pretty
```

Expected response: `"status" : "green"` или `"yellow"` (для single-node это нормально)

### Fluent-bit
```bash
curl http://localhost:2020/api/v1/health
```

Expected response: `{"status":"ok"}`

### OpenSearch Dashboards
```bash
curl http://localhost:5601/api/status
```

Expected response: JSON с `"overall":{"level":"available"}`

---

## Troubleshooting

### OpenSearch не стартует
- Проверить логи: `docker-compose logs opensearch`
- Убедиться что порт 9200 не занят: `lsof -i :9200`
- Проверить memory limits: OpenSearch требует минимум 512MB heap

### Fluent-bit не отправляет логи
- Проверить connectivity: `docker-compose exec fluent-bit curl -u admin:Admin123! http://opensearch:9200`
- Проверить логи Fluent-bit: `docker-compose logs fluent-bit`
- Временно включить stdout output для отладки

### OpenSearch Dashboards не доступен
- Убедиться что OpenSearch healthy: `docker-compose ps opensearch`
- Проверить логи: `docker-compose logs opensearch-dashboards`
- Проверить connectivity от Dashboards к OpenSearch

---

## Ресурсы

- [OpenSearch Documentation](https://opensearch.org/docs/latest/)
- [Fluent-bit OpenSearch Output](https://docs.fluentbit.io/manual/pipeline/outputs/opensearch)
- [ISM Policies Guide](https://opensearch.org/docs/latest/im-plugin/ism/index/)
