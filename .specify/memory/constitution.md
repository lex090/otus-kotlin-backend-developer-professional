# Arbitrage Scanner Constitution

<!--
  SYNC IMPACT REPORT
  ==================
  Version Change: Initial → 1.0.0
  Type: MAJOR (Initial constitution establishment)
  Date: 2025-10-26

  Modified Principles: N/A (initial creation)
  Added Sections:
    - Core Principles (I-VII)
    - Technical Standards
    - Development Workflow
    - Governance

  Removed Sections: N/A

  Templates Requiring Updates:
    ✅ .specify/templates/plan-template.md - Constitution Check section compatible
    ✅ .specify/templates/spec-template.md - Requirements align with principles
    ✅ .specify/templates/tasks-template.md - Task categorization aligns with testing/modular principles

  Follow-up TODOs: None
-->

## Core Principles

### I. Модульная Архитектура (Modular Architecture)

**Правило**: Каждая функциональность должна быть выделена в отдельный модуль с четкой границей ответственности. Модули ДОЛЖНЫ быть независимо тестируемыми, иметь явные зависимости и минимальное связывание.

**Обоснование**: Мультимодульная структура с composite builds (build-logic, arbitrage-scanner) требует четкого разделения ответственности. Это обеспечивает:
- Независимую разработку и тестирование модулей
- Переиспользование кода между платформами (JVM, Linux x64, macOS ARM64/x64)
- Упрощенную поддержку и масштабирование системы

**Требования**:
- Используйте кастомные build плагины (BuildPluginJvm, BuildPluginMultiplatform) для стандартизации конфигурации
- Модули должны иметь явные API границы
- Зависимости между модулями должны быть односторонними (без циклических зависимостей)
- Общая логика выносится в common-модули с поддержкой мультиплатформенности

### II. Тестирование на Всех Уровнях (Comprehensive Testing)

**Правило**: Код ДОЛЖЕН быть покрыт тестами на всех уровнях: unit, integration, contract. Тесты ДОЛЖНЫ запускаться на всех поддерживаемых платформах (JVM, Linux x64, macOS ARM64/x64) для мультиплатформенных модулей.

**Обоснование**: Критическая важность надежности для финтех-приложения, работающего с реальными деньгами и криптовалютными биржами. Мультиплатформенная природа проекта требует проверки на всех таргетах.

**Требования**:
- Unit тесты для бизнес-логики в каждом модуле
- Integration тесты для взаимодействия модулей
- Contract тесты для API endpoints
- Используйте `./gradlew :arbitrage-scanner:testAll` для запуска всех тестов
- Для мультиплатформенных модулей: `jvmTest`, `linuxX64Test`, `macosArm64Test`
- Используйте `allTests` task для агрегированных отчетов
- Обязательный запуск `./gradlew check` перед коммитом

### III. Производительность и Масштабируемость (Performance & Scalability)

**Правило**: Система ДОЛЖНА обеспечивать низкую латентность (<100ms для API, <1 сек для уведомлений) и высокую пропускную способность (до 10000 запросов/сек для алготрейдеров). Код ДОЛЖЕН быть оптимизирован для конкурентного выполнения.

**Обоснование**: Арбитражные возможности существуют миллисекунды. Задержки приводят к упущенной прибыли. Целевая аудитория включает алготрейдеров и квантовые фонды с требованиями к высокой производительности.

**Требования**:
- Используйте Kotlin Coroutines (1.10.1) для асинхронных операций
- Минимизируйте блокирующие операции
- Оптимизируйте сериализацию/десериализацию (Kotlinx Serialization 1.9.0)
- Мониторинг производительности критических операций
- Нагрузочное тестирование для API endpoints

### IV. Наблюдаемость (Observability)

**Правило**: Все критические операции ДОЛЖНЫ логироваться. Логи ДОЛЖНЫ быть структурированными, включать контекст (correlation ID, user ID, timestamp) и уровень важности. Система ДОЛЖНА предоставлять метрики для мониторинга здоровья.

**Обоснование**: Финтех-приложение требует детального аудита операций для отладки, безопасности и комплаенса. Распределенная архитектура (Ktor app, Kafka app) требует трассировки запросов.

**Требования**:
- Используйте модуль `arbitrage-scanner-lib-logging` для мультиплатформенного логирования
- Используйте `arbitrage-scanner-lib-logging-logback` для JVM приложений
- Структурированные логи в JSON формате для production
- Логирование всех операций с биржами (API calls, responses, errors)
- Метрики для отслеживания латентности, throughput, error rate
- Distributed tracing для микросервисной архитектуры

### V. API-First Подход (API-First Design)

**Правило**: Все публичные интерфейсы ДОЛЖНЫ быть спроектированы через OpenAPI спецификации до начала реализации. API ДОЛЖНЫ быть версионируемыми, обратно совместимыми и документированными.

**Обоснование**: Поддержка алготрейдеров требует стабильного, документированного API. Интеграция с множеством бирж требует четких контрактов. OpenAPI обеспечивает автогенерацию клиентов и серверного кода.

**Требования**:
- OpenAPI спецификации в модуле `arbitrage-scanner-api-v1`
- Используйте OpenAPI Tools Generator для кодогенерации
- Версионирование API (v1, v2, ...) в пути или заголовках
- Contract тесты для проверки соответствия реализации спецификации
- Swagger/OpenAPI UI для документации endpoints

### VI. Управление Конфигурацией (Configuration Management)

**Правило**: Конфигурация ДОЛЖНА быть отделена от кода. Секреты (API keys, credentials) НИКОГДА не должны коммититься в репозиторий. Конфигурация ДОЛЖНА поддерживать разные окружения (dev, staging, production).

**Обоснование**: Безопасность API ключей от бирж критична. Разные окружения требуют разных конфигураций (endpoints, rate limits, feature flags).

**Требования**:
- Используйте environment variables для конфигурации
- Файлы `.env` в `.gitignore`
- Koin (3.5.6) для dependency injection с environment-specific конфигурациями
- Валидация конфигурации при старте приложения
- Документированные примеры конфигурации (`.env.example`)

### VII. Простота и Ясность (Simplicity & Clarity)

**Правило**: Код ДОЛЖЕН быть простым, понятным и следовать принципу YAGNI (You Aren't Gonna Need It). Сложность ДОЛЖНА быть обоснована бизнес-требованиями. Используйте идиоматичный Kotlin код.

**Обоснование**: Проект разрабатывается в рамках обучающего курса OTUS. Простота облегчает понимание, поддержку и расширение системы. Избыточная сложность увеличивает вероятность ошибок.

**Требования**:
- Следуйте Kotlin coding conventions
- Используйте Kotlin language features (data classes, sealed classes, extension functions)
- Избегайте преждевременной оптимизации
- Документируйте сложные алгоритмы и бизнес-логику
- Code review обязателен для всех изменений

## Technical Standards

### Технологический Стек

**ОБЯЗАТЕЛЬНЫЕ ВЕРСИИ**:
- Kotlin 2.2.0 (language version 2.2)
- Java 21 (toolchain и compiler)
- Gradle с Kotlin DSL и composite builds

**ОСНОВНЫЕ БИБЛИОТЕКИ**:
- Ktor 3.2.3 - веб-фреймворк
- Koin 3.5.6 - dependency injection
- Kotlinx Serialization 1.9.0 - сериализация
- Kotlinx Coroutines 1.10.1 - асинхронность
- JUnit Platform с Kotlin Test - тестирование

**ПОДДЕРЖИВАЕМЫЕ ПЛАТФОРМЫ** (для мультиплатформенных модулей):
- JVM (основной таргет)
- Linux x64
- macOS ARM64 (Apple Silicon)
- macOS x64 (Intel)

### Build System Conventions

**Именование Таргетов**:
- Используйте префикс `:arbitrage-scanner:` для команд подпроектов из корня
- Правильно: `./gradlew :arbitrage-scanner:arbitrage-scanner-common:build`
- Неправильно: `./gradlew arbitrage-scanner-common:build`

**Типы Модулей**:
- JVM-only модули: `alias(libs.plugins.build.plugin.jvm)`
- Мультиплатформенные модули: `alias(libs.plugins.build.plugin.multiplatform)`

**Source Sets для Мультиплатформенных Модулей**:
- `commonMain` - общий код для всех платформ
- `commonTest` - общие тесты
- `jvmMain`, `linuxX64Main`, `macosArm64Main` - платформенно-специфичный код

### Docker и Deployment

- Используйте Jib плагин для создания Docker образов: `./gradlew :arbitrage-scanner:jibDockerBuildAll`
- Shadow плагин для fat JAR: `./gradlew :arbitrage-scanner:arbitrage-scanner-app-ktor:shadowJar`
- Оптимизированные образы с multi-stage builds
- Health checks для Kubernetes/Docker deployments

## Development Workflow

### Pre-Commit Checklist

1. Запустить `./gradlew check` - ВСЕ проверки должны пройти
2. Запустить тесты для измененных модулей
3. Для мультиплатформенных модулей - проверить на всех таргетах
4. Проверить, что конфигурационные изменения не содержат секретов
5. Code review обязателен для всех PR

### Branching Strategy

- `main` - production-ready код
- `develop` - интеграционная ветка для разработки
- `feature/###-feature-name` - ветки для новых фич (по номеру задачи)
- `bugfix/###-bug-description` - ветки для исправлений

### Commit Messages

- Используйте conventional commits формат: `type(scope): description`
- Типы: `feat`, `fix`, `refactor`, `test`, `docs`, `build`, `ci`
- Примеры:
  - `feat(api): add arbitrage opportunity endpoint`
  - `fix(common): handle null price in calculation`
  - `test(business-logic): add integration tests for scanner`

### Pull Request Process

1. Создать PR с описанием изменений
2. Code review от минимум одного разработчика
3. Все CI проверки должны пройти (build, test, lint)
4. Squash commits перед merge (опционально)
5. Обновить документацию при необходимости

## Governance

### Amendment Procedure

Изменения в Constitution требуют:
1. Обоснование необходимости изменения
2. Обсуждение в команде (для учебного проекта - с преподавателем)
3. Документирование в Sync Impact Report
4. Обновление зависимых артефактов (шаблонов, документации)
5. Инкремент версии согласно семантическому версионированию

### Versioning Policy

- **MAJOR**: Несовместимые изменения в управлении (удаление/переопределение принципов)
- **MINOR**: Добавление новых принципов или существенное расширение руководства
- **PATCH**: Уточнения формулировок, исправление опечаток, несемантические улучшения

### Compliance Review

- Все PR/code reviews ДОЛЖНЫ проверять соответствие принципам Constitution
- Нарушения принципов ДОЛЖНЫ быть обоснованы в "Complexity Tracking" секции plan.md
- Необоснованная сложность ДОЛЖНА быть отклонена в code review
- Регулярный аудит кодовой базы на соответствие принципам (quarterly для продакшена)

### Runtime Development Guidance

Для повседневной разработки используйте:
- `CLAUDE.md` - руководство по разработке для AI ассистентов
- `docs/` - проектная документация (бизнес, UI, архитектура)
- `.specify/templates/` - шаблоны для спецификаций, планов, задач

**Version**: 1.0.0 | **Ratified**: 2025-10-26 | **Last Amended**: 2025-10-26
