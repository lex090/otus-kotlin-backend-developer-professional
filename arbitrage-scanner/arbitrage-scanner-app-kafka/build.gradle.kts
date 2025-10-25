plugins {
    application
    alias(libs.plugins.build.plugin.jvm)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.jib)
}

application {
    mainClass.set("com.arbitrage.scanner.app.kafka.AppKafkaMainKt")
}

jib {
    from {
        image = "eclipse-temurin:21-jre"
        platforms {
            platform {
                architecture = "arm64"
                os = "linux"
            }
            platform {
                architecture = "amd64"
                os = "linux"
            }
        }
    }

    // Целевой образ - имя и теги
    to {
        image = "arbitrage-scanner-app-kafka"  // Имя образа
        tags = setOf(
            project.version.toString(),                  // Версия из gradle.properties
            "latest"
        )
    }

    // Настройки контейнера
    container {
        // Главный класс приложения - автоматически берется из application блока
        mainClass = application.mainClass.get()

        // JVM параметры для оптимизации работы в контейнере
        jvmFlags = emptyList()

        // Переменные окружения
        environment = mapOf(
            "KAFKA_HOST" to "kafka",
            "KAFKA_PORT" to "9092",
            "KAFKA_IN_TOPIC" to "arbitrage-opportunities-in",
            "KAFKA_OUT_TOPIC" to "arbitrage-opportunities-out",
            "KAFKA_GROUP_ID" to "arbitrage-scanner-group"
        )

        // Метки для организации и мониторинга
        labels = emptyMap()

        // Рабочая директория в контейнере
        workingDirectory = "/app"

        // Формат времени создания образа
        creationTime = "USE_CURRENT_TIMESTAMP"
    }
}

dependencies {
    implementation(project(":arbitrage-scanner-app-common"))
    implementation(project(":arbitrage-scanner-common"))
    implementation(project(":arbitrage-scanner-api-v1"))
    implementation(project(":arbitrage-scanner-business-logic"))

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)

    // Hoplite - Configuration library
    implementation(libs.hoplite.core)
    implementation(libs.hoplite.yaml)

    // Kafka
    implementation(libs.kafka.clients)

    // Koin
    implementation(libs.koin.core)

    // Logging
    implementation(project(":arbitrage-scanner-libs:arbitrage-scanner-lib-logging"))
    implementation(project(":arbitrage-scanner-libs:arbitrage-scanner-lib-logging-logback"))

    // Test
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotlinx.coroutines.test)
}
