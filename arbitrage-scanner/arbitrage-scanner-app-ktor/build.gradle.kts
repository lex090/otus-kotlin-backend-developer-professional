plugins {
    alias(libs.plugins.build.plugin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinx.serialization)
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
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
        image = "arbitrage-scanner-app-ktor"  // Имя образа
        tags = setOf(
            project.version.toString(),                  // Версия из gradle.properties
        )
    }

    // Настройки контейнера
    container {
        // Главный класс приложения - автоматически берется из application блока
        mainClass = application.mainClass.get()

        // Порты, которые будет слушать приложение
        ports = listOf("8080")

        // JVM параметры для оптимизации работы в контейнере
        jvmFlags = emptyList()

        // Переменные окружения
        environment = emptyMap()

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
    implementation(project(":arbitrage-scanner-libs:arbitrage-scanner-lib-logging-logback"))

    // Ktor
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.config.yaml)

    // Koin
    implementation(libs.koin.ktor)

    testImplementation(libs.kotlin.test)

    testImplementation(libs.ktor.server.test)
    testImplementation(libs.ktor.client.negotiation)
}