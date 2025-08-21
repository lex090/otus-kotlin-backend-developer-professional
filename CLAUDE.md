# CLAUDE.md

Этот файл содержит инструкции для Claude Code (claude.ai/code) при работе с кодом в данном репозитории.

## Важно: Язык общения

**Все ответы и комментарии должны быть на русском языке.**

## Структура проекта

Это мульти-модульный Kotlin проект для курса OTUS "Kotlin Backend Developer Professional". Проект использует Gradle composite builds с тремя основными компонентами:

- `lessons/` - Учебные модули с уроками курса (например, `m1l1-first`)
- `arbitrage-scanner/` - Основное приложение для поиска арбитражных возможностей между DEX и CEX биржами
- `build-logic/` - Кастомные Gradle build плагины для стандартизированной конфигурации

## Система сборки

Проект использует Gradle с Kotlin DSL и кастомные build плагины, расположенные в `build-logic/`:

- `BuildPluginJvm` - Конфигурирует JVM-only Kotlin модули
- `BuildPluginMultiplatform` - Конфигурирует Kotlin Multiplatform модули с JVM, Linux и macOS таргетами

Каталог версий определен в `gradle/libs.versions.toml` с Kotlin 2.1.21 и Java 21.

## Основные команды для разработки

### Сборка
```bash
./gradlew build                        # Собрать все модули
./gradlew :lessons:build              # Собрать модули уроков
./gradlew :arbitrage-scanner:build    # Собрать модули arbitrage-scanner
```

### Тестирование
```bash
./gradlew test                        # Запустить все тесты
./gradlew :lessons:m1l1-first:test    # Запустить тесты конкретного урока
```

Используется фреймворк тестирования JUnit Platform с Kotlin Test.

### Запуск приложений
```bash
./gradlew :lessons:m1l1-first:run            # Запустить приложение урока
./gradlew :arbitrage-scanner:tmp-module:run  # Запустить arbitrage scanner
```

## Описание основного проекта

**Arbitrage Scanner** - система для поиска арбитражных возможностей между децентрализованными (DEX) и централизованными (CEX) криптовалютными биржами в сети Binance Smart Chain.

### Функциональность
- Мониторинг цен токенов на DEX (PancakeSwap, SushiSwap, 1inch) и CEX (Binance, OKX, Bybit) биржах
- Автоматическое выявление ценовых расхождений между биржами
- Расчет прибыльности с учетом комиссий и газа
- Система уведомлений о найденных арбитражных возможностях

## Архитектурные заметки

- Использует composite builds (`includeBuild`) для разделения ответственности между уроками, arbitrage-scanner и логикой сборки
- Кастомные build плагины стандартизируют конфигурацию Java 21 toolchain во всех модулях
- Мультиплатформенные модули поддерживают JVM, Linux x64, macOS ARM64 и macOS x64 таргеты
- В мультиплатформенных модулях включены прогрессивные возможности языка Kotlin
- Все модули используют общую группу `com.education.project` и версию `1.0-SNAPSHOT`

## Типы модулей

- **JVM модули**: Используют плагин `alias(libs.plugins.build.plugin.jvm)`
- **Мультиплатформенные модули**: Используют плагин `alias(libs.plugins.build.plugin.multiplatform)`
- **Модули уроков**: Расположены в `lessons/` с простой структурой и тестами
- **Модули приложений**: Поддерживают как commonMain, так и platform-специфичные source sets