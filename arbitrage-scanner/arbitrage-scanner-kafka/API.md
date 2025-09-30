# Arbitrage Scanner Kafka API

Документация по форматам сообщений для Kafka транспорта Arbitrage Scanner.

## Оглавление

- [Общие принципы](#общие-принципы)
- [Correlation ID механизм](#correlation-id-механизм)
- [Заголовки сообщений](#заголовки-сообщений)
- [Формат запросов](#формат-запросов)
- [Формат ответов](#формат-ответов)
- [Обработка ошибок](#обработка-ошибок)
- [Примеры](#примеры)

## Общие принципы

### Топики

- **Входящий топик** (по умолчанию: `arbitrage-scanner-in`):
  - Принимает запросы от клиентов
  - Формат: JSON в теле сообщения + метаданные в заголовках

- **Исходящий топик** (по умолчанию: `arbitrage-scanner-out`):
  - Отправляет ответы клиентам
  - Формат: JSON в теле сообщения + метаданные в заголовках

### Сериализация

- **Ключ сообщения**: String (correlation ID)
- **Значение сообщения**: String (JSON)
- **Кодировка**: UTF-8

### Версионирование

API использует версию v1. Версия указывается в URL OpenAPI спецификации и влияет на структуру JSON.

## Correlation ID механизм

Для сопоставления запросов и ответов используется **Correlation ID**.

### Источники Correlation ID (в порядке приоритета)

1. **Заголовок `correlation-id`** - основной источник
2. **Ключ сообщения (key)** - fallback, если заголовок отсутствует
3. **"unknown"** - если ни заголовок, ни ключ не указаны

### Правила работы

- Клиент **ДОЛЖЕН** устанавливать уникальный correlation ID для каждого запроса
- Сервер **ГАРАНТИРУЕТ** возврат того же correlation ID в ответе
- Рекомендуется использовать UUID v4 для correlation ID

### Пример

**Запрос:**
```
Headers: correlation-id = "550e8400-e29b-41d4-a716-446655440000"
Key: "550e8400-e29b-41d4-a716-446655440000"
```

**Ответ:**
```
Headers: correlation-id = "550e8400-e29b-41d4-a716-446655440000"
Key: "550e8400-e29b-41d4-a716-446655440000"
```

## Заголовки сообщений

### Обязательные заголовки (запрос)

| Заголовок | Тип | Описание | Пример |
|-----------|-----|----------|--------|
| `correlation-id` | String | Уникальный ID для сопоставления запроса/ответа | `550e8400-e29b-41d4-a716-446655440000` |
| `request-type` | String | Тип операции (READ, SEARCH) | `READ` |
| `timestamp` | Long (String) | Временная метка создания запроса (Unix timestamp в мс) | `1234567890123` |

### Опциональные заголовки (запрос)

| Заголовок | Тип | Описание | Пример |
|-----------|-----|----------|--------|
| `client-id` | String | Идентификатор клиента | `mobile-app-v1.2.3` |
| `user-id` | String | Идентификатор пользователя | `user-12345` |
| `trace-id` | String | ID для distributed tracing | `trace-abc123` |

### Заголовки ответа

Сервер **копирует** все заголовки из запроса в ответ, добавляя:

| Заголовок | Тип | Описание | Пример |
|-----------|-----|----------|--------|
| `response-timestamp` | Long (String) | Временная метка формирования ответа | `1234567890456` |
| `processing-time-ms` | Long (String) | Время обработки запроса в мс | `123` |

## Формат запросов

### Базовая структура запроса

Все запросы содержат общие поля:

```json
{
  "requestType": "read|search",
  "requestId": "string",
  "debug": {
    "mode": "PROD|TEST|STUB",
    "stubs": "SUCCESS|NOT_FOUND|BAD_ID|VALIDATION_ERROR|SERVER_ERROR|NONE"
  }
}
```

### Поля запроса

| Поле | Тип | Обязательное | Описание |
|------|-----|--------------|----------|
| `requestType` | String | Да | Тип запроса: `"read"` или `"search"` |
| `requestId` | String | Да | Уникальный ID запроса (для логирования) |
| `debug` | Object | Нет | Настройки отладки |
| `debug.mode` | String | Нет | Режим работы: `PROD`, `TEST`, `STUB` |
| `debug.stubs` | String | Нет | Тип stub данных (только в режиме STUB) |

### READ запрос

Получение одной арбитражной возможности по ID.

#### Структура

```json
{
  "requestType": "read",
  "requestId": "req-001",
  "debug": {
    "mode": "STUB",
    "stubs": "SUCCESS"
  },
  "opportunityId": "string"
}
```

#### Дополнительные поля

| Поле | Тип | Обязательное | Описание |
|------|-----|--------------|----------|
| `opportunityId` | String | Да | Уникальный идентификатор арбитражной возможности |

#### Пример

```json
{
  "requestType": "read",
  "requestId": "550e8400-e29b-41d4-a716-446655440001",
  "debug": {
    "mode": "STUB",
    "stubs": "SUCCESS"
  },
  "opportunityId": "opp-btc-pancake-binance-20231015-001"
}
```

### SEARCH запрос

Поиск арбитражных возможностей с фильтрацией.

#### Структура

```json
{
  "requestType": "search",
  "requestId": "req-002",
  "debug": {
    "mode": "STUB",
    "stubs": "SUCCESS"
  },
  "filter": {
    "searchString": "string",
    "minProfitPercent": 0.0,
    "maxProfitPercent": 100.0,
    "tokenSymbol": "string",
    "dexName": "string",
    "cexName": "string",
    "status": "ACTIVE|EXPIRED|EXECUTED"
  }
}
```

#### Поля фильтра

| Поле | Тип | Обязательное | Описание |
|------|-----|--------------|----------|
| `searchString` | String | Нет | Поиск по названию токена или описанию |
| `minProfitPercent` | Double | Нет | Минимальный процент прибыли |
| `maxProfitPercent` | Double | Нет | Максимальный процент прибыли |
| `tokenSymbol` | String | Нет | Символ токена (BTC, ETH, и т.д.) |
| `dexName` | String | Нет | Название DEX (PancakeSwap, SushiSwap, 1inch) |
| `cexName` | String | Нет | Название CEX (Binance, OKX, Bybit) |
| `status` | String | Нет | Статус возможности: ACTIVE, EXPIRED, EXECUTED |

#### Пример

```json
{
  "requestType": "search",
  "requestId": "550e8400-e29b-41d4-a716-446655440002",
  "debug": {
    "mode": "STUB",
    "stubs": "SUCCESS"
  },
  "filter": {
    "minProfitPercent": 1.5,
    "tokenSymbol": "BTC",
    "dexName": "PancakeSwap",
    "status": "ACTIVE"
  }
}
```

## Формат ответов

### Базовая структура ответа

```json
{
  "responseType": "read|search",
  "requestId": "string",
  "result": "SUCCESS|ERROR",
  "errors": [
    {
      "code": "string",
      "group": "string",
      "field": "string",
      "message": "string"
    }
  ]
}
```

### Общие поля ответа

| Поле | Тип | Всегда присутствует | Описание |
|------|-----|---------------------|----------|
| `responseType` | String | Да | Тип ответа: `"read"` или `"search"` |
| `requestId` | String | Да | ID запроса (копия из запроса) |
| `result` | String | Да | Результат: `"SUCCESS"` или `"ERROR"` |
| `errors` | Array | Нет | Список ошибок (присутствует при result=ERROR) |

### READ ответ (успешный)

```json
{
  "responseType": "read",
  "requestId": "550e8400-e29b-41d4-a716-446655440001",
  "result": "SUCCESS",
  "arbitrageOpportunity": {
    "id": "opp-btc-pancake-binance-20231015-001",
    "opportunityType": "DEX_TO_CEX_SIMPLE|CEX_TO_DEX_SIMPLE",
    "tokenSymbol": "BTC",
    "profitPercent": 2.5,
    "profitAmountUsd": 125.50,
    "dexPrice": {
      "dexName": "PancakeSwap",
      "priceUsd": 45000.00,
      "liquidityUsd": 1000000.00,
      "timestamp": 1697385600000
    },
    "cexPrice": {
      "cexName": "Binance",
      "priceUsd": 46125.00,
      "timestamp": 1697385600000
    },
    "status": "ACTIVE|EXPIRED|EXECUTED",
    "createdAt": 1697385600000,
    "expiresAt": 1697385900000
  }
}
```

### SEARCH ответ (успешный)

```json
{
  "responseType": "search",
  "requestId": "550e8400-e29b-41d4-a716-446655440002",
  "result": "SUCCESS",
  "arbitrageOpportunities": [
    {
      "id": "opp-btc-pancake-binance-20231015-001",
      "opportunityType": "DEX_TO_CEX_SIMPLE",
      "tokenSymbol": "BTC",
      "profitPercent": 2.5,
      "profitAmountUsd": 125.50,
      "dexPrice": {
        "dexName": "PancakeSwap",
        "priceUsd": 45000.00,
        "liquidityUsd": 1000000.00,
        "timestamp": 1697385600000
      },
      "cexPrice": {
        "cexName": "Binance",
        "priceUsd": 46125.00,
        "timestamp": 1697385600000
      },
      "status": "ACTIVE",
      "createdAt": 1697385600000,
      "expiresAt": 1697385900000
    },
    {
      "id": "opp-eth-1inch-okx-20231015-002",
      "opportunityType": "DEX_TO_CEX_SIMPLE",
      "tokenSymbol": "ETH",
      "profitPercent": 1.8,
      "profitAmountUsd": 45.20,
      "dexPrice": {
        "dexName": "1inch",
        "priceUsd": 2500.00,
        "liquidityUsd": 500000.00,
        "timestamp": 1697385610000
      },
      "cexPrice": {
        "cexName": "OKX",
        "priceUsd": 2545.00,
        "timestamp": 1697385610000
      },
      "status": "ACTIVE",
      "createdAt": 1697385610000,
      "expiresAt": 1697385910000
    }
  ]
}
```

### Структура ArbitrageOpportunity

| Поле | Тип | Описание |
|------|-----|----------|
| `id` | String | Уникальный идентификатор |
| `opportunityType` | String | Тип: `DEX_TO_CEX_SIMPLE`, `CEX_TO_DEX_SIMPLE` |
| `tokenSymbol` | String | Символ токена (BTC, ETH, и т.д.) |
| `profitPercent` | Double | Процент прибыли |
| `profitAmountUsd` | Double | Сумма прибыли в USD |
| `dexPrice` | Object | Информация о цене на DEX |
| `dexPrice.dexName` | String | Название DEX |
| `dexPrice.priceUsd` | Double | Цена в USD |
| `dexPrice.liquidityUsd` | Double | Ликвидность в USD |
| `dexPrice.timestamp` | Long | Временная метка |
| `cexPrice` | Object | Информация о цене на CEX |
| `cexPrice.cexName` | String | Название CEX |
| `cexPrice.priceUsd` | Double | Цена в USD |
| `cexPrice.timestamp` | Long | Временная метка |
| `status` | String | Статус: `ACTIVE`, `EXPIRED`, `EXECUTED` |
| `createdAt` | Long | Дата создания (Unix timestamp в мс) |
| `expiresAt` | Long | Дата истечения (Unix timestamp в мс) |

## Обработка ошибок

### Формат ошибки

```json
{
  "responseType": "read|search",
  "requestId": "string",
  "result": "ERROR",
  "errors": [
    {
      "code": "not-found|validation-error|server-error",
      "group": "string",
      "field": "string",
      "message": "string"
    }
  ]
}
```

### Типы ошибок

| Код | Группа | Описание | HTTP эквивалент |
|-----|--------|----------|-----------------|
| `validation-error` | `validation` | Ошибка валидации входных данных | 400 Bad Request |
| `not-found` | `not-found` | Ресурс не найден | 404 Not Found |
| `server-error` | `internal` | Внутренняя ошибка сервера | 500 Internal Server Error |
| `parse-error` | `validation` | Ошибка парсинга JSON | 400 Bad Request |

### Примеры ошибок

#### Арбитражная возможность не найдена

```json
{
  "responseType": "read",
  "requestId": "550e8400-e29b-41d4-a716-446655440001",
  "result": "ERROR",
  "errors": [
    {
      "code": "not-found",
      "group": "not-found",
      "field": "opportunityId",
      "message": "Arbitrage opportunity with id 'opp-non-existent' not found"
    }
  ]
}
```

#### Ошибка валидации

```json
{
  "responseType": "search",
  "requestId": "550e8400-e29b-41d4-a716-446655440002",
  "result": "ERROR",
  "errors": [
    {
      "code": "validation-error",
      "group": "validation",
      "field": "minProfitPercent",
      "message": "minProfitPercent must be between 0 and 100"
    }
  ]
}
```

#### Ошибка парсинга JSON

```json
{
  "responseType": "read",
  "requestId": "unknown",
  "result": "ERROR",
  "errors": [
    {
      "code": "parse-error",
      "group": "validation",
      "field": "payload",
      "message": "Failed to parse JSON: Unexpected character..."
    }
  ]
}
```

#### Внутренняя ошибка сервера

```json
{
  "responseType": "read",
  "requestId": "550e8400-e29b-41d4-a716-446655440001",
  "result": "ERROR",
  "errors": [
    {
      "code": "server-error",
      "group": "internal",
      "field": "",
      "message": "An unexpected error occurred while processing the request"
    }
  ]
}
```

## Примеры

### Полный цикл READ запроса

#### 1. Отправка запроса в Kafka

**Топик:** `arbitrage-scanner-in`

**Заголовки:**
```
correlation-id: 550e8400-e29b-41d4-a716-446655440000
request-type: READ
timestamp: 1697385600000
```

**Ключ:** `550e8400-e29b-41d4-a716-446655440000`

**Тело:**
```json
{
  "requestType": "read",
  "requestId": "550e8400-e29b-41d4-a716-446655440001",
  "debug": {
    "mode": "STUB",
    "stubs": "SUCCESS"
  },
  "opportunityId": "opp-btc-pancake-binance-20231015-001"
}
```

#### 2. Получение ответа из Kafka

**Топик:** `arbitrage-scanner-out`

**Заголовки:**
```
correlation-id: 550e8400-e29b-41d4-a716-446655440000
request-type: READ
timestamp: 1697385600000
response-timestamp: 1697385600123
processing-time-ms: 123
```

**Ключ:** `550e8400-e29b-41d4-a716-446655440000`

**Тело:**
```json
{
  "responseType": "read",
  "requestId": "550e8400-e29b-41d4-a716-446655440001",
  "result": "SUCCESS",
  "arbitrageOpportunity": {
    "id": "opp-btc-pancake-binance-20231015-001",
    "opportunityType": "DEX_TO_CEX_SIMPLE",
    "tokenSymbol": "BTC",
    "profitPercent": 2.5,
    "profitAmountUsd": 125.50,
    "dexPrice": {
      "dexName": "PancakeSwap",
      "priceUsd": 45000.00,
      "liquidityUsd": 1000000.00,
      "timestamp": 1697385600000
    },
    "cexPrice": {
      "cexName": "Binance",
      "priceUsd": 46125.00,
      "timestamp": 1697385600000
    },
    "status": "ACTIVE",
    "createdAt": 1697385600000,
    "expiresAt": 1697385900000
  }
}
```

### Полный цикл SEARCH запроса

#### 1. Отправка запроса

**Топик:** `arbitrage-scanner-in`

**Заголовки:**
```
correlation-id: 550e8400-e29b-41d4-a716-446655440002
request-type: SEARCH
timestamp: 1697385700000
```

**Ключ:** `550e8400-e29b-41d4-a716-446655440002`

**Тело:**
```json
{
  "requestType": "search",
  "requestId": "550e8400-e29b-41d4-a716-446655440003",
  "debug": {
    "mode": "STUB",
    "stubs": "SUCCESS"
  },
  "filter": {
    "minProfitPercent": 1.5,
    "tokenSymbol": "BTC",
    "status": "ACTIVE"
  }
}
```

#### 2. Получение ответа

**Топик:** `arbitrage-scanner-out`

**Заголовки:**
```
correlation-id: 550e8400-e29b-41d4-a716-446655440002
request-type: SEARCH
timestamp: 1697385700000
response-timestamp: 1697385700234
processing-time-ms: 234
```

**Ключ:** `550e8400-e29b-41d4-a716-446655440002`

**Тело:**
```json
{
  "responseType": "search",
  "requestId": "550e8400-e29b-41d4-a716-446655440003",
  "result": "SUCCESS",
  "arbitrageOpportunities": [
    {
      "id": "opp-btc-pancake-binance-20231015-001",
      "opportunityType": "DEX_TO_CEX_SIMPLE",
      "tokenSymbol": "BTC",
      "profitPercent": 2.5,
      "profitAmountUsd": 125.50,
      "dexPrice": {
        "dexName": "PancakeSwap",
        "priceUsd": 45000.00,
        "liquidityUsd": 1000000.00,
        "timestamp": 1697385600000
      },
      "cexPrice": {
        "cexName": "Binance",
        "priceUsd": 46125.00,
        "timestamp": 1697385600000
      },
      "status": "ACTIVE",
      "createdAt": 1697385600000,
      "expiresAt": 1697385900000
    }
  ]
}
```

## Best Practices

### Для клиентов

1. **Всегда устанавливайте уникальный correlation ID**
   ```kotlin
   val correlationId = UUID.randomUUID().toString()
   ```

2. **Используйте таймауты для ожидания ответа**
   ```kotlin
   val timeout = Duration.ofSeconds(30)
   val response = consumer.poll(timeout)
   ```

3. **Обрабатывайте все типы ошибок**
   ```kotlin
   when (response.result) {
       "SUCCESS" -> handleSuccess(response)
       "ERROR" -> handleErrors(response.errors)
   }
   ```

4. **Логируйте request ID для отладки**
   ```kotlin
   logger.info("Sent request: requestId=${request.requestId}, correlationId=$correlationId")
   ```

5. **Используйте режим STUB для тестирования**
   ```kotlin
   val request = ReadRequest(
       requestType = "read",
       requestId = UUID.randomUUID().toString(),
       debug = Debug(mode = "STUB", stubs = "SUCCESS"),
       opportunityId = "test-id"
   )
   ```

### Для серверной стороны

1. **Всегда возвращайте correlation ID из запроса**
2. **Обрабатывайте исключения и возвращайте понятные ошибки**
3. **Логируйте время обработки запроса**
4. **Используйте idempotent обработку при возможности**
5. **Настройте мониторинг метрик Kafka**

## Версионирование API

Текущая версия: **v1**

При изменении формата API будет создана новая версия (v2), и старая версия будет поддерживаться в течение определенного периода (deprecated).

Клиенты должны быть готовы к:
- Добавлению новых опциональных полей
- Изменению порядка полей в JSON
- Добавлению новых типов enum значений

Критичные изменения (breaking changes) будут вводиться только в новых версиях API.

## Дополнительная информация

- [README.md](README.md) - Общая документация модуля
- [OpenAPI спецификация](../../arbitrage-scanner-api-v1/specs/specs-arbitrage-v1.yaml) - Полная спецификация API
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/) - Документация Kafka