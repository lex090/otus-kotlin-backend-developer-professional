FROM eclipse-temurin:21-jre

WORKDIR /app

COPY build/libs/arbitrage-scanner-app-ktor-all.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
