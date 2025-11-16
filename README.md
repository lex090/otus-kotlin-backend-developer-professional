# Название проекта - Arbitrage Scanner

## Краткое описание проекта

Проект Arbitrage Scanner предназначен для поиска арбитражных возможностей на CEX и DEX криптовалютных биржах.

### Основные задачи сервиса

1. Сканирование текущих цен криптовалютных пар с подключенных бирж.
2. Выявление арбитражных возможностей.
3. Оповещение пользователей о найденных возможностях.

### Типы арбитража

1. **Простой арбитраж** - покупка и продажа одной и той же криптовалюты на разных биржах.

## Инфраструктура и сервисы

### Порты

| Сервис | Порт | Описание |
|--------|------|----------|
| Arbitrage Scanner | 8080 | REST API приложения |
| OpenSearch | 9200 | Поисковый движок для логов и метрик |
| OpenSearch Dashboards | 5601 | Веб-интерфейс для визуализации логов ([Setup Guide](docs/monitoring/opensearch-kibana-setup.md)) |
| Fluent-bit (metrics) | 2020 | Health check и метрики сборщика логов |

### Мониторинг и логирование

Для централизованного сбора логов и мониторинга используется стек OpenSearch + OpenSearch Dashboards:

- **OpenSearch** - хранение и индексация логов приложения
- **OpenSearch Dashboards** - визуализация логов и метрик через веб-интерфейс
- **Fluent-bit** - сбор и маршрутизация логов

Подробная инструкция по настройке: [OpenSearch и Kibana Setup](docs/monitoring/opensearch-kibana-setup.md)

## Документация

1. [**Целевая аудитория**](docs/01-biz/01-audience.md)
2. [**MVP описание**](docs/01-biz/02-mvp-description.md)
3. **Варианты отображения главного экрана**
    1. [Вариант 1](docs/02-ui/03-main-screen-ascii-v1.md)
    2. [Вариант 2](docs/02-ui/03-main-screen-ascii-v2.md)
    3. [Вариант 3](docs/02-ui/03-main-screen-ascii-v3.md)
    4. [Вариант 4](docs/02-ui/03-main-screen-ascii-v4.md)
4. **API**
    1. [Arbitrage opportunity](docs/03-architecture/api/01-arbitrage-opportunity.md)
5. **C4**
    1. [C1](docs/03-architecture/c4/01-c1-context.drawio.svg)
    2. [C2](docs/03-architecture/c4/02-c2-containers.drawio.svg)
    3. [C3-ArbitrageOpportunityService](docs/03-architecture/c4/03-c3-ArbitrageOpportunityService-component.drawio.svg)
    4. [C3-CexMarketDataSnapshotService](docs/03-architecture/c4/03-c3-CexMarketDataSnapshotService-component.drawio.svg)
6. **Мониторинг**
    1. [OpenSearch и Kibana Setup](docs/monitoring/opensearch-kibana-setup.md)



