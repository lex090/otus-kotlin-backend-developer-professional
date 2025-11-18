# Research: OpenSearch и Kibana интеграция

**Feature**: 001-opensearch-kibana-integration
**Date**: 2025-11-16
**Status**: Completed

## Обзор

Этот документ содержит результаты исследования технических решений для интеграции OpenSearch и Kibana в существующую Docker Compose конфигурацию проекта arbitrage-scanner.

## Исследуемые вопросы

### 1. Выбор версии OpenSearch и совместимость с Kibana

**Decision**: OpenSearch 2.11.1 + OpenSearch Dashboards 2.11.1

**Rationale**:
- OpenSearch 2.x - стабильная версия с долгосрочной поддержкой
- OpenSearch Dashboards (форк Kibana) полностью совместим с OpenSearch
- Версия 2.11.1 - последняя стабильная на момент разработки
- Официальная поддержка Docker образов
- Минимальные требования к ресурсам для dev окружения

**Alternatives considered**:
- ~~Kibana + Elasticsearch~~ - отклонено из-за лицензионных ограничений Elastic и усложнения стека
- ~~OpenSearch 1.x~~ - устаревшая версия, отсутствуют новые функции безопасности
- ~~OpenSearch 3.x (alpha)~~ - нестабильная версия, не рекомендуется для использования

### 2. Конфигурация Fluent-bit для отправки в OpenSearch

**Decision**: Использовать встроенный OpenSearch output plugin Fluent-bit

**Rationale**:
- Нативная поддержка OpenSearch в Fluent-bit 3.0+
- Автоматический retry и buffering
- Поддержка bulk API для эффективной отправки
- Простая конфигурация без дополнительных зависимостей

**Configuration template**:
```conf
[OUTPUT]
    Name  opensearch
    Match *
    Host  opensearch
    Port  9200
    Index fluentbit
    Type  _doc
    HTTP_User admin
    HTTP_Passwd admin
    tls   Off
    Suppress_Type_Name On
```

**Alternatives considered**:
- ~~HTTP output plugin~~ - требует ручного формирования запросов, более сложная конфигурация
- ~~Forward protocol to Logstash~~ - избыточный компонент для dev окружения
- ~~Kafka as intermediate buffer~~ - излишняя сложность для данного use case

### 3. Настройка безопасности OpenSearch (базовая аутентификация)

**Decision**: Использовать встроенный Security plugin OpenSearch с базовой аутентификацией

**Rationale**:
- Security plugin включен по умолчанию в OpenSearch 2.x
- Простая настройка через environment variables
- Достаточно для development окружения
- Совместимость с OpenSearch Dashboards из коробки

**Configuration**:
```yaml
environment:
  OPENSEARCH_INITIAL_ADMIN_PASSWORD: "StrongPassword123!"
  discovery.type: single-node
  plugins.security.disabled: false  # включено по умолчанию
```

**Alternatives considered**:
- ~~Полное отключение security~~ - небезопасно даже для dev, отклонено после уточнения требований
- ~~SAML/LDAP аутентификация~~ - избыточно для development окружения
- ~~mTLS сертификаты~~ - излишняя сложность для локальной разработки

### 4. Index Lifecycle Management (ILM) для 7-дневного retention

**Decision**: Использовать Index State Management (ISM) policies OpenSearch

**Rationale**:
- ISM - нативный механизм OpenSearch для управления жизненным циклом индексов
- Декларативная политика через JSON
- Автоматическая ротация и удаление старых индексов
- Низкие накладные расходы на ресурсы

**Policy template**:
```json
{
  "policy": {
    "description": "7 day retention policy",
    "default_state": "hot",
    "states": [
      {
        "name": "hot",
        "actions": [],
        "transitions": [
          {
            "state_name": "delete",
            "conditions": {
              "min_index_age": "7d"
            }
          }
        ]
      },
      {
        "name": "delete",
        "actions": [
          {
            "delete": {}
          }
        ]
      }
    ]
  }
}
```

**Alternatives considered**:
- ~~Curator~~ - устаревший инструмент, больше не рекомендуется
- ~~Manual cron job~~ - требует дополнительного контейнера и скриптов
- ~~Rollover based on size~~ - не подходит для time-based retention requirement

### 5. Health checks конфигурация

**Decision**: Использовать HTTP health check endpoints каждого сервиса

**Rationale**:
- OpenSearch предоставляет `/_cluster/health` endpoint
- Fluent-bit предоставляет HTTP server с `/api/v1/health` endpoint
- OpenSearch Dashboards предоставляет `/api/status` endpoint
- Docker встроенная поддержка HEALTHCHECK инструкции

**Templates**:

OpenSearch:
```yaml
healthcheck:
  test: ["CMD-SHELL", "curl -u admin:admin -f http://localhost:9200/_cluster/health || exit 1"]
  interval: 30s
  timeout: 10s
  retries: 5
```

Fluent-bit:
```yaml
healthcheck:
  test: ["CMD-SHELL", "curl -f http://localhost:2020/api/v1/health || exit 1"]
  interval: 30s
  timeout: 5s
  retries: 3
```

OpenSearch Dashboards:
```yaml
healthcheck:
  test: ["CMD-SHELL", "curl -u admin:admin -f http://localhost:5601/api/status || exit 1"]
  interval: 30s
  timeout: 10s
  retries: 5
```

**Alternatives considered**:
- ~~TCP socket check~~ - менее надежный, не проверяет реальную работоспособность приложения
- ~~External monitoring service~~ - избыточно для Docker Compose окружения
- ~~Script-based health checks~~ - более сложная поддержка, требует монтирования скриптов

### 6. Memory buffer sizing для Fluent-bit

**Decision**: 64MB memory buffer с настройкой Mem_Buf_Limit

**Rationale**:
- 64MB достаточно для буферизации ~5-10 минут логов при нормальной нагрузке
- Баланс между надежностью и использованием памяти
- Укладывается в общий лимит 4GB RAM для всего окружения
- Стандартная практика для development окружений

**Configuration**:
```conf
[SERVICE]
    storage.type memory
    storage.metrics on

[INPUT]
    Name              forward
    Listen            0.0.0.0
    Port              24224
    Mem_Buf_Limit     64MB
```

**Alternatives considered**:
- ~~32MB~~ - слишком мало для потенциальных всплесков логирования
- ~~128MB+~~ - избыточно для dev окружения, съедает много RAM
- ~~Filesystem buffering~~ - отклонено после уточнения (выбран memory buffer)

### 7. Index pattern и naming strategy

**Decision**: Daily index rotation с паттерном `fluentbit-YYYY.MM.DD`

**Rationale**:
- Упрощает управление retention (удаление индексов старше 7 дней)
- Стандартная практика для time-series данных
- Оптимальный баланс между количеством индексов и размером каждого
- Совместимо с ISM policies для автоматического удаления

**Index template**:
```json
{
  "index_patterns": ["fluentbit-*"],
  "template": {
    "settings": {
      "number_of_shards": 1,
      "number_of_replicas": 0,
      "index.lifecycle.name": "7-day-retention"
    }
  }
}
```

**Alternatives considered**:
- ~~Hourly rotation~~ - слишком много мелких индексов, усложняет управление
- ~~Weekly rotation~~ - затрудняет точное удаление по 7-дневному retention
- ~~Single index~~ - невозможно эффективно удалять старые данные

### 8. Resource limits для Docker сервисов

**Decision**: Установить явные memory limits для OpenSearch и Dashboards

**Rationale**:
- Предотвращение OOM killer
- Гарантия укладывания в общий лимит 4GB
- Predictable resource allocation

**Рекомендуемые limits**:
```yaml
# OpenSearch
mem_limit: 1.5g
environment:
  - "OPENSEARCH_JAVA_OPTS=-Xms512m -Xmx512m"

# OpenSearch Dashboards
mem_limit: 512m

# Fluent-bit
mem_limit: 256m
```

**Total allocation**: ~2.3GB для новых сервисов + ~1.7GB для существующих = 4GB

**Alternatives considered**:
- ~~Без limits~~ - риск OOM и непредсказуемое поведение
- ~~Более высокие limits~~ - не укладываются в requirement 4GB
- ~~CPU limits~~ - не критично для dev окружения

## Best Practices

### OpenSearch в Docker

1. **Single-node cluster для dev**: `discovery.type: single-node`
2. **Отключить production checks**: `DISABLE_INSTALL_DEMO_CONFIG=true`
3. **Persistent volumes**: Обязательно для сохранения данных
4. **Network mode**: bridge network для изоляции

### Fluent-bit конфигурация

1. **Structured logging**: Использовать JSON формат для логов
2. **Retry logic**: Настроить Retry_Limit для opensearch output
3. **Monitoring**: Включить HTTP server для метрик Fluent-bit
4. **Buffering**: Memory buffer с явным размером для предсказуемости

### Security

1. **Изменить дефолтный пароль**: Использовать OPENSEARCH_INITIAL_ADMIN_PASSWORD
2. **TLS для production**: Отключен только для dev (tls Off)
3. **Network isolation**: Все сервисы в одной Docker network

### Performance

1. **Shards configuration**: 1 shard для dev (низкая нагрузка)
2. **Replicas**: 0 replicas для single-node setup
3. **Refresh interval**: Дефолтный 1s подходит для dev
4. **Bulk size**: Дефолтные настройки Fluent-bit (достаточно)

## Риски и митигация

| Риск | Вероятность | Влияние | Митигация |
|------|-------------|---------|-----------|
| OOM из-за превышения 4GB | Средняя | Высокое | Явные memory limits на все сервисы |
| Потеря данных при сбое OpenSearch | Низкая | Среднее | Memory buffer в Fluent-bit + acceptable для dev |
| Медленный запуск всех сервисов | Высокая | Низкое | Правильный depends_on порядок + health checks |
| Переполнение диска | Средняя | Среднее | ISM policy с 7-дневным retention |
| Проблемы совместимости версий | Низкая | Высокое | Использовать matching versions (2.11.1) |

## Выводы

Все технические решения выбраны с учетом:
- Development окружения (не production)
- Ограничения RAM ≤4GB
- Простоты настройки и поддержки
- Соблюдения требований спецификации
- Best practices индустрии

Решения готовы к имплементации в Phase 1 (Design & Contracts).
