plugins {
    alias(libs.plugins.build.plugin.jvm)
    alias(libs.plugins.kotlinx.serialization)
    application
}

application {
    mainClass.set("com.arbitrage.scanner.ApplicationKt")
}

dependencies {
    // Внутренние зависимости
    implementation(project(":arbitrage-scanner-common"))
    implementation(project(":arbitrage-scanner-api-v1"))
    implementation(project(":arbitrage-scanner-business-logic"))
    implementation(project(":arbitrage-scanner-libs:arbitrage-scanner-lib-logging-logback"))

    // Kafka
    implementation(libs.kafka.clients)

    // Kotlinx
    implementation(libs.kotlinx.serialization.json)

    // Config
    implementation(libs.hoplite.core)
    implementation(libs.hoplite.yaml)

    // Koin
    implementation(libs.koin.ktor)

    // Тестирование
    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.testcontainers.kafka)
    testImplementation(libs.testcontainers.junit)
}

tasks.test {
    useJUnitPlatform()
}