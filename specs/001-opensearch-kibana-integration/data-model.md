# Data Model: OpenSearch Indices and Log Structures

**Feature**: 001-opensearch-kibana-integration
**Date**: 2025-11-16
**Status**: Design Complete

## Обзор

Этот документ описывает структуру данных, хранящихся в OpenSearch: индексы, схемы документов логов и метрик, а также relationships между сущностями.

## Индексы OpenSearch

### 1. Logs Index: `fluentbit-YYYY.MM.DD`

**Назначение**: Хранение логов приложений arbitrage-scanner

**Naming pattern**: Daily rotation (например: `fluentbit-2025.11.16`)

**Settings**:
```json
{
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas": 0,
    "index.lifecycle.name": "7-day-retention",
    "refresh_interval": "1s"
  }
}
```

**Mapping**:
```json
{
  "mappings": {
    "properties": {
      "@timestamp": {
        "type": "date",
        "format": "strict_date_optional_time||epoch_millis"
      },
      "level": {
        "type": "keyword"
      },
      "service_name": {
        "type": "keyword"
      },
      "message": {
        "type": "text",
        "fields": {
          "keyword": {
            "type": "keyword",
            "ignore_above": 256
          }
        }
      },
      "logger": {
        "type": "keyword"
      },
      "thread": {
        "type": "keyword"
      },
      "context": {
        "type": "object",
        "enabled": true
      },
      "tags": {
        "type": "keyword"
      }
    }
  }
}
```

**Retention**: 7 дней (управляется ISM policy)

**Estimated size**: ~10-50MB/день для dev окружения

---

## Структуры документов

### Лог-запись (Log Entry)

Представляет единицу логирования из приложения.

**Fields**:

| Поле | Тип | Обязательное | Описание | Пример |
|------|-----|--------------|----------|--------|
| `@timestamp` | date | ✅ | Временная метка создания лога | `2025-11-16T10:30:45.123Z` |
| `level` | keyword | ✅ | Уровень серьёзности | `info`, `warn`, `error`, `debug` |
| `service_name` | keyword | ✅ | Имя сервиса-источника | `arbitrage-scanner`, `arbitrage-scanner-app-kafka` |
| `message` | text | ✅ | Текст сообщения лога | `Processing arbitrage opportunity for BTC/USDT` |
| `logger` | keyword | ❌ | Имя logger class | `com.education.arbitrage.ArbitrageService` |
| `thread` | keyword | ❌ | Thread name | `ktor-worker-1` |
| `context` | object | ❌ | Дополнительные контекстные поля | `{"userId": "123", "requestId": "abc"}` |
| `tags` | keyword[] | ❌ | Теги для категоризации | `["http", "database", "external-api"]` |

**Example document**:
```json
{
  "@timestamp": "2025-11-16T10:30:45.123Z",
  "level": "info",
  "service_name": "arbitrage-scanner",
  "message": "Processing arbitrage opportunity for BTC/USDT",
  "logger": "com.education.arbitrage.ArbitrageService",
  "thread": "ktor-worker-1",
  "context": {
    "pair": "BTC/USDT",
    "profit_percent": 1.5,
    "exchange_from": "Binance",
    "exchange_to": "OKX"
  },
  "tags": ["arbitrage", "trade"]
}
```

**Validation rules**:
- `@timestamp` ДОЛЖЕН быть в формате ISO 8601 или epoch milliseconds
- `level` ДОЛЖЕН быть одним из: `trace`, `debug`, `info`, `warn`, `error`, `fatal`
- `service_name` НЕ ДОЛЖЕН быть пустым
- `message` НЕ ДОЛЖЕН превышать 10KB (soft limit)

**Lifecycle**:
1. **Creation**: Генерируется приложением и отправляется в Fluent-bit
2. **Ingestion**: Fluent-bit парсит и отправляет в OpenSearch
3. **Storage**: Хранится в daily index
4. **Retention**: Автоматически удаляется через 7 дней ISM policy
5. **Archival**: Нет (dev окружение)

---

### HTTP Request Metric

Представляет метрику HTTP запроса к arbitrage-scanner.

**Fields**:

| Поле | Тип | Обязательное | Описание | Пример |
|------|-----|--------------|----------|--------|
| `@timestamp` | date | ✅ | Время запроса | `2025-11-16T10:30:45.123Z` |
| `service_name` | keyword | ✅ | Имя сервиса | `arbitrage-scanner` |
| `http_method` | keyword | ✅ | HTTP метод | `GET`, `POST`, `PUT`, `DELETE` |
| `http_path` | keyword | ✅ | URL path | `/api/v1/opportunities` |
| `http_status` | integer | ✅ | HTTP status code | `200`, `404`, `500` |
| `response_time_ms` | long | ✅ | Время обработки (мс) | `45` |
| `client_ip` | ip | ❌ | IP адрес клиента | `192.168.1.100` |
| `user_agent` | text | ❌ | User agent string | `Mozilla/5.0...` |
| `request_id` | keyword | ❌ | Уникальный ID запроса | `req-abc-123` |

**Example document**:
```json
{
  "@timestamp": "2025-11-16T10:30:45.123Z",
  "level": "info",
  "service_name": "arbitrage-scanner",
  "message": "HTTP GET /api/v1/opportunities 200 45ms",
  "http_method": "GET",
  "http_path": "/api/v1/opportunities",
  "http_status": 200,
  "response_time_ms": 45,
  "client_ip": "192.168.1.1",
  "user_agent": "curl/7.68.0",
  "request_id": "req-abc-123"
}
```

**Validation rules**:
- `http_method` ДОЛЖЕН быть одним из стандартных HTTP методов
- `http_status` ДОЛЖЕН быть в диапазоне 100-599
- `response_time_ms` ДОЛЖЕН быть положительным числом
- `client_ip` ДОЛЖЕН быть валидным IPv4 или IPv6 адресом (если присутствует)

**Aggregations для дашборда**:
1. **Total requests**: Count всех документов за период
2. **Requests by method**: Terms aggregation по `http_method`
3. **Requests by status code**: Terms aggregation по `http_status`
4. **Requests by endpoint**: Terms aggregation по `http_path`
5. **Average response time**: Avg aggregation по `response_time_ms`
6. **95th percentile response time**: Percentiles aggregation по `response_time_ms`
7. **Error rate**: Bucket aggregation для status >= 400

---

## ISM Policy: 7-day Retention

**Policy name**: `7-day-retention`

**Описание**: Автоматическое удаление индексов старше 7 дней

**Определение**:
```json
{
  "policy": {
    "policy_id": "7-day-retention",
    "description": "Delete indices older than 7 days",
    "default_state": "hot",
    "schema_version": 1,
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
        ],
        "transitions": []
      }
    ],
    "ism_template": [
      {
        "index_patterns": ["fluentbit-*"],
        "priority": 100
      }
    ]
  }
}
```

**Применение**: Автоматически применяется ко всем индексам с паттерном `fluentbit-*`

---

## Index Template

**Template name**: `fluentbit-template`

**Определение**:
```json
{
  "index_patterns": ["fluentbit-*"],
  "template": {
    "settings": {
      "number_of_shards": 1,
      "number_of_replicas": 0,
      "index.lifecycle.name": "7-day-retention",
      "refresh_interval": "1s"
    },
    "mappings": {
      "properties": {
        "@timestamp": {
          "type": "date",
          "format": "strict_date_optional_time||epoch_millis"
        },
        "level": {
          "type": "keyword"
        },
        "service_name": {
          "type": "keyword"
        },
        "message": {
          "type": "text",
          "fields": {
            "keyword": {
              "type": "keyword",
              "ignore_above": 256
            }
          }
        },
        "logger": {
          "type": "keyword"
        },
        "thread": {
          "type": "keyword"
        },
        "http_method": {
          "type": "keyword"
        },
        "http_path": {
          "type": "keyword"
        },
        "http_status": {
          "type": "integer"
        },
        "response_time_ms": {
          "type": "long"
        },
        "client_ip": {
          "type": "ip"
        }
      }
    }
  },
  "priority": 100
}
```

---

## Relationships

```
┌─────────────────────┐
│  Application        │
│  (arbitrage-scanner)│
└──────────┬──────────┘
           │ генерирует
           ▼
    ┌─────────────┐
    │  Log Entry  │
    │  (JSON)     │
    └──────┬──────┘
           │ отправляет (Forward protocol)
           ▼
    ┌──────────────┐
    │  Fluent-bit  │
    │  (collector) │
    └──────┬───────┘
           │ парсит и буферизует
           ▼
    ┌───────────────────┐
    │  OpenSearch       │
    │  (storage)        │
    │                   │
    │  Index:           │
    │  fluentbit-*      │
    └────────┬──────────┘
             │ читает через API
             ▼
    ┌──────────────────────┐
    │  OpenSearch Dashboards│
    │  (visualization)      │
    │                      │
    │  - Discover          │
    │  - Dashboards        │
    │  - Visualizations    │
    └──────────────────────┘
             │
             ▼
       User (DevOps/Dev)
```

---

## Storage Estimates

**Assumptions**:
- Average log entry size: 500 bytes
- Logs per second: 10 (dev workload)
- Logs per day: 864,000
- Daily index size: ~432MB uncompressed
- With OpenSearch compression: ~150MB per day
- 7 days retention: ~1GB total

**Disk space required**: 2GB (with margin)

---

## Query Patterns

### Поиск логов по уровню серьёзности
```json
{
  "query": {
    "term": {
      "level": "error"
    }
  },
  "sort": [
    { "@timestamp": "desc" }
  ]
}
```

### Поиск логов по сервису за последние 24 часа
```json
{
  "query": {
    "bool": {
      "must": [
        { "term": { "service_name": "arbitrage-scanner" } },
        {
          "range": {
            "@timestamp": {
              "gte": "now-24h"
            }
          }
        }
      ]
    }
  }
}
```

### Aggregation для метрик HTTP запросов
```json
{
  "aggs": {
    "requests_by_method": {
      "terms": {
        "field": "http_method",
        "size": 10
      }
    },
    "avg_response_time": {
      "avg": {
        "field": "response_time_ms"
      }
    },
    "error_rate": {
      "filters": {
        "filters": {
          "errors": {
            "range": {
              "http_status": {
                "gte": 400
              }
            }
          }
        }
      }
    }
  }
}
```

---

## Выводы

Модель данных спроектирована с учетом:
- Простоты структуры для development окружения
- Эффективного хранения и поиска логов
- Автоматического управления retention
- Поддержки метрик HTTP запросов для дашборда
- Минимального потребления ресурсов (1 shard, 0 replicas)

Все entity definitions готовы для использования в implementation tasks.
