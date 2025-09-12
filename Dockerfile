# Multi-stage build с копированием всего проекта
FROM gradle:8.5-jdk21 AS build

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем весь проект целиком
COPY . .

# Даем права на выполнение gradlew
RUN chmod +x gradlew

# Собираем shadow JAR для arbitrage-scanner-ktor, исключая problematic multiplatform задачи
RUN ./gradlew :arbitrage-scanner:arbitrage-scanner-ktor:shadowJar \
    -x :arbitrage-scanner:arbitrage-scanner-common:commonizeNativeDistribution \
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