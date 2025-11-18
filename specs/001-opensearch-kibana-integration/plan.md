# Implementation Plan: OpenSearch и Kibana для мониторинга логов и метрик

**Branch**: `001-opensearch-kibana-integration` | **Date**: 2025-11-16 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/001-opensearch-kibana-integration/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

Добавление OpenSearch и Kibana в существующую Docker Compose конфигурацию для централизованного сбора, хранения и визуализации логов приложения arbitrage-scanner. Fluent-bit будет перенаправлять логи из приложения в OpenSearch вместо stdout, а Kibana предоставит веб-интерфейс для поиска логов и отображения метрик HTTP запросов. Система включает базовую аутентификацию, health checks для всех компонентов мониторинга и документацию по первоначальной настройке.

## Technical Context

**Language/Version**: YAML (Docker Compose v3.8+), Fluent-bit конфигурация
**Primary Dependencies**:
- OpenSearch 2.x
- Kibana (совместимая с OpenSearch 2.x)
- Fluent-bit 3.0
- Docker Compose

**Storage**:
- OpenSearch персистентное хранилище (Docker volume)
- Retention: 7 дней с автоматической ротацией индексов

**Testing**:
- Интеграционное тестирование через docker-compose up
- Проверка health checks через docker-compose ps
- Функциональное тестирование через Kibana UI

**Target Platform**: Docker контейнеризированное окружение (Linux/macOS)

**Project Type**: Infrastructure/DevOps конфигурация для существующего Kotlin backend приложения

**Performance Goals**:
- Логи появляются в Kibana за ≤5 секунд
- Поиск логов за 7 дней ≤10 секунд
- Обновление метрик ≤10 секунд
- Фильтрация логов ≤5 секунд

**Constraints**:
- Общее потребление RAM ≤4GB для всего docker-compose окружения
- Memory buffer Fluent-bit: 64MB
- Development окружение (не production)

**Scale/Scope**:
- 1 application (arbitrage-scanner) + infrastructure services
- ~6 Docker сервисов в docker-compose
- Поддержка нескольких источников логов (arbitrage-scanner, arbitrage-scanner-app-kafka)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### Применимость принципов конституции

**Примечание**: Данная задача касается инфраструктурной конфигурации (Docker Compose, Fluent-bit), а не разработки Kotlin кода. Многие принципы конституции не применяются напрямую к YAML конфигурациям.

| Принцип | Применимость | Статус | Комментарий |
|---------|--------------|--------|-------------|
| I. Качество кода | ⚠️ Частично | ✅ PASS | YAML конфигурации будут структурированы и читаемы. Применим принцип минимальной избыточности |
| II. Тестирование | ✅ Применимо | ✅ PASS | Интеграционное тестирование через docker-compose up. Health checks для контроля состояния |
| III. Логирование и наблюдаемость | ✅ Применимо | ✅ PASS | Это сама система логирования - напрямую соответствует принципу. Health checks обеспечат наблюдаемость |
| IV. Согласованность UX | ❌ Не применимо | N/A | Нет API или пользовательского интерфейса в самой конфигурации |
| Технологические стандарты | ⚠️ Частично | ✅ PASS | Используем стандартный стек: Docker, Docker Compose. Новые технологии (OpenSearch/Kibana) для инфраструктуры |
| Процесс разработки | ✅ Применимо | ✅ PASS | Будет выполнен ./gradlew check перед коммитом. Документация настройки будет добавлена |
| Критические ошибки | ✅ Применимо | ✅ PASS | IO операции (логирование) будут мониториться через health checks |

### Выводы

✅ **GATE PASSED**: Нарушений NON-NEGOTIABLE принципов нет.

Все применимые принципы конституции соблюдены:
- Тестирование: интеграционные тесты + health checks
- Логирование: система сама является решением для логирования и наблюдаемости
- Процесс: стандартные проверки перед коммитом + документация
- Качество: структурированные YAML конфигурации без избыточности

## Project Structure

### Documentation (this feature)

```text
specs/[###-feature]/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

**Примечание**: Эта задача касается инфраструктурных конфигураций, а не исходного кода приложения.

```text
# Конфигурационные файлы (изменяемые)
docker-compose.minimal.yml       # Добавление OpenSearch и Kibana сервисов

config/
└── fluent-bit/
    └── fluent-bit.conf         # Обновление для отправки в OpenSearch

# Документация (новые файлы)
docs/
└── monitoring/
    └── opensearch-kibana-setup.md  # Инструкция по настройке index pattern и дашборда
```

**Structure Decision**:
Эта задача не требует создания новых модулей Kotlin приложения. Все изменения касаются:
1. Обновления существующего `docker-compose.minimal.yml` - добавление OpenSearch и Kibana сервисов
2. Модификации `config/fluent-bit/fluent-bit.conf` - настройка output в OpenSearch
3. Создания документации для первоначальной настройки Kibana

Код Kotlin приложения (arbitrage-scanner) остается без изменений.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| [e.g., 4th project] | [current need] | [why 3 projects insufficient] |
| [e.g., Repository pattern] | [specific problem] | [why direct DB access insufficient] |
