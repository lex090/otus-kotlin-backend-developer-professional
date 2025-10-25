# Gradle Tasks Documentation

## Централизованные таски для управления проектом

Этот документ описывает добавленные gradle таски для централизованной сборки и тестирования всех модулей проекта.

Все пользовательские таски объединены в группу **"Custom project tasks"** для удобства навигации в IDE.

## Глобальные таски (корневой проект)

Запускаются из корня проекта:

### Сборка

```bash
./gradlew buildAll    # Собирает все модули в проекте (arbitrage-scanner и lessons)
./gradlew cleanAll    # Очищает все модули в проекте
```

### Тестирование

```bash
./gradlew testAll     # Запускает все тесты во всех модулях
./gradlew checkAll    # Выполняет полную проверку (сборка + тесты) всех модулей
```

## Специфичные таски для arbitrage-scanner

Запускаются с префиксом `:arbitrage-scanner:`:

### Сборка

```bash
./gradlew :arbitrage-scanner:buildApplications  # Собирает только приложения (ktor, kafka)
./gradlew :arbitrage-scanner:buildLibraries     # Собирает только библиотеки
```

### Тестирование

```bash
./gradlew :arbitrage-scanner:testJvmAll         # Запускает все JVM тесты
./gradlew :arbitrage-scanner:testApiModules     # Тестирует API и business-logic модули
./gradlew :arbitrage-scanner:allTests           # Запускает все тесты (JVM + Native)
```

## Примеры использования

### Полная сборка и тестирование проекта
```bash
./gradlew cleanAll buildAll testAll
```

### Быстрая проверка всего проекта
```bash
./gradlew checkAll
```

### Сборка только приложений arbitrage-scanner
```bash
./gradlew :arbitrage-scanner:buildApplications
```

### Тестирование только API модулей
```bash
./gradlew :arbitrage-scanner:testApiModules
```

## Полный список пользовательских тасок

### Корневой проект

| Таска | Описание |
|-------|----------|
| `buildAll` | Собирает все модули в проекте (arbitrage-scanner и lessons) |
| `cleanAll` | Очищает все модули в проекте |
| `testAll` | Запускает все тесты во всех модулях |
| `checkAll` | Выполняет полную проверку (сборка + тесты) всех модулей |

### Проект arbitrage-scanner

| Таска | Описание |
|-------|----------|
| `build` | Собирает все модули arbitrage-scanner |
| `clean` | Очищает все модули arbitrage-scanner |
| `check` | Проверяет все модули arbitrage-scanner |
| `allTests` | Запускает все тесты (JVM + Native) в arbitrage-scanner |
| `testJvmAll` | Запускает все JVM тесты в arbitrage-scanner |
| `buildApplications` | Собирает только приложения (ktor, kafka) |
| `buildLibraries` | Собирает только библиотеки (lib-cor, lib-logging, lib-logging-logback) |
| `testApiModules` | Тестирует API и business-logic модули |

## Просмотр доступных тасок

Для просмотра всех доступных пользовательских тасок:

```bash
./gradlew tasks --group "Custom project tasks"
```

Для просмотра всех тасок проекта:

```bash
./gradlew tasks --all
```

Для получения подробной информации о конкретной таске:

```bash
./gradlew help --task buildAll
```

## Структура проекта

- **arbitrage-scanner/** - основное приложение с 11 модулями:
  - API модули (api-v1, common, stubs, business-logic)
  - Приложения (app-ktor, app-kafka, app-common)
  - Библиотеки (lib-cor, lib-logging, lib-logging-logback)

- **lessons/** - учебные модули курса

- **build-logic/** - кастомные gradle плагины

## Использование в CI/CD

Централизованные таски идеально подходят для использования в пайплайнах CI/CD:

### GitHub Actions пример
```yaml
name: Build and Test

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
      - name: Build and test all modules
        run: ./gradlew checkAll
```

### GitLab CI пример
```yaml
stages:
  - build
  - test

build:
  stage: build
  script:
    - ./gradlew buildAll

test:
  stage: test
  script:
    - ./gradlew testAll
```

## Преимущества централизованных тасок

1. **Упрощение команд** - вместо длинных команд с указанием всех модулей
2. **Единая точка входа** - все основные операции доступны из корня проекта
3. **Гибкость** - можно запускать как все модули сразу, так и отдельные группы
4. **CI/CD готовность** - простая интеграция с системами непрерывной интеграции
5. **Организация** - все пользовательские таски в одной группе "Custom project tasks"

## Технические детали

### Организация тасок

Все пользовательские таски объединены в группу **"Custom project tasks"**, что обеспечивает:
- Удобную навигацию в IDE (IntelliJ IDEA, Android Studio)
- Легкий поиск нужных команд
- Разделение от стандартных Gradle тасок

### Механизм работы

Таски используют механизм composite builds Gradle для работы с включенными сборками:

**На уровне корневого проекта:**
```kotlin
gradle.includedBuild("arbitrage-scanner").task(":build")
gradle.includedBuild("lessons").task(":build")
```

**На уровне arbitrage-scanner:**
```kotlin
subprojects {
    val testTask = tasks.findByName("allTests") ?: tasks.findByName("test")
    testTask?.let { this@register.dependsOn(it) }
}
```

### Безопасность зависимостей

Используется безопасный подход к поиску тасок:
- `findByName()?.let { }` - предотвращает ошибки при отсутствии таски
- Избегается создание dummy-тасок, что предотвращает конфликты имен
- Поддержка как JVM, так и multiplatform модулей

## FAQ (Часто задаваемые вопросы)

### Как запустить только тесты приложения ktor?
```bash
./gradlew :arbitrage-scanner:arbitrage-scanner-app-ktor:test
```

### Как пересобрать проект с чистого листа?
```bash
./gradlew cleanAll buildAll
```

### Как запустить только JVM тесты без Native?
```bash
./gradlew :arbitrage-scanner:testJvmAll
```

### Где найти все пользовательские таски в IDE?
В Gradle панели (обычно справа) откройте:
`Tasks → Custom project tasks`

### Как добавить свою пользовательскую таску?
Добавьте таску в соответствующий `build.gradle.kts` файл и укажите группу:
```kotlin
tasks.register("myTask") {
    description = "My custom task"
    group = "Custom project tasks"
    // задача
}
```

### Почему некоторые таски не показываются при запуске `./gradlew tasks`?
По умолчанию отображаются только основные таски. Используйте `./gradlew tasks --all` для полного списка.

### Как узнать время выполнения тасок?
Добавьте флаг `--profile` к команде:
```bash
./gradlew buildAll --profile
```
Отчет будет сохранен в `build/reports/profile/`