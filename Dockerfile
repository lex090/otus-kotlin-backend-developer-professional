# Multi-stage build для полной сборки проекта внутри контейнера
FROM gradle:8.5-jdk21 AS build

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем Gradle wrapper и основные конфигурационные файлы
COPY gradle/ gradle/
COPY gradlew gradlew.bat ./
COPY build.gradle.kts gradle.properties ./

# Копируем файл settings.gradle.kts и исправляем его для сборки только нужных модулей
COPY settings.gradle.kts ./
RUN sed -i '/includeBuild("lessons")/d' settings.gradle.kts

# Копируем модули
COPY build-logic/ build-logic/
COPY arbitrage-scanner/ arbitrage-scanner/
COPY specs/ specs/

# Даем права на выполнение gradlew (на всякий случай)
RUN chmod +x gradlew

# Собираем только JVM компоненты, избегая multiplatform сборки
RUN ./gradlew :arbitrage-scanner:arbitrage-scanner-ktor:shadowJar \
    -x :arbitrage-scanner:arbitrage-scanner-common:commonizeNativeDistribution \
    -x :arbitrage-scanner:arbitrage-scanner-common:linuxX64Test \
    -x :arbitrage-scanner:arbitrage-scanner-common:macosArm64Test \
    -x :arbitrage-scanner:arbitrage-scanner-common:macosX64Test \
    -x :arbitrage-scanner:arbitrage-scanner-api-v1:commonizeNativeDistribution \
    --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Устанавливаем рабочую директорию
WORKDIR /app

# Создаем пользователя для безопасности
RUN addgroup -g 1001 -S appuser && \
    adduser -u 1001 -S appuser -G appuser

# Копируем собранный JAR из build stage
COPY --from=build /app/arbitrage-scanner/arbitrage-scanner-ktor/build/libs/*-all.jar app.jar

# Меняем владельца файла
RUN chown appuser:appuser app.jar

# Переключаемся на непривилегированного пользователя
USER appuser

# Открываем порт
EXPOSE 8080

# Настройки JVM для контейнера
ENV JVM_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Запускаем приложение
CMD ["sh", "-c", "java $JVM_OPTS -jar app.jar"]