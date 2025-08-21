# CLAUDE.md

Этот файл содержит инструкции для Claude Code (claude.ai/code) при работе с кодом в данном репозитории.

## Важно: Язык общения

**Все ответы и комментарии должны быть на русском языке.**

## Структура проекта

Это мульти-модульный Kotlin проект для курса OTUS "Kotlin Backend Developer Professional". Проект использует Gradle composite builds с тремя основными компонентами:

- `lessons/` - Учебные модули с уроками курса (например, `m1l1-first`)
- `main-project/` - Основные модули приложения, включая мультиплатформенные модули
- `build-logic/` - Кастомные Gradle build плагины для стандартизированной конфигурации
- `arbitrage-scanner/` - Модуль приложения (пока пустой/заготовка)

## Система сборки

Проект использует Gradle с Kotlin DSL и кастомные build плагины, расположенные в `build-logic/`:

- `BuildPluginJvm` - Конфигурирует JVM-only Kotlin модули
- `BuildPluginMultiplatform` - Конфигурирует Kotlin Multiplatform модули с JVM, Linux и macOS таргетами

Каталог версий определен в `gradle/libs.versions.toml` с Kotlin 2.1.21 и Java 21.

## Основные команды для разработки

### Сборка
```bash
./gradlew build                    # Собрать все модули
./gradlew :lessons:build          # Собрать модули уроков
./gradlew :main-project:build     # Собрать модули основного проекта
```

### Тестирование
```bash
./gradlew test                    # Запустить все тесты
./gradlew :lessons:m1l1-first:test    # Запустить тесты конкретного урока
```

Используется фреймворк тестирования JUnit Platform с Kotlin Test.

### Запуск приложений
```bash
./gradlew :lessons:m1l1-first:run     # Запустить приложение урока
./gradlew :main-project:tmp-module:run # Запустить приложение основного проекта
```

## Архитектурные заметки

- Использует composite builds (`includeBuild`) для разделения ответственности между уроками, основным проектом и логикой сборки
- Кастомные build плагины стандартизируют конфигурацию Java 21 toolchain во всех модулях
- Мультиплатформенные модули поддерживают JVM, Linux x64, macOS ARM64 и macOS x64 таргеты
- В мультиплатформенных модулях включены прогрессивные возможности языка Kotlin
- Все модули используют общую группу `com.education.project` и версию `1.0-SNAPSHOT`

## Типы модулей

- **JVM модули**: Используют плагин `alias(libs.plugins.build.plugin.jvm)`
- **Мультиплатформенные модули**: Используют плагин `alias(libs.plugins.build.plugin.multiplatform)`
- **Модули уроков**: Расположены в `lessons/` с простой структурой и тестами
- **Модули приложений**: Поддерживают как commonMain, так и platform-специфичные source sets