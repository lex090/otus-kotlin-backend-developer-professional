plugins {
    application
    alias(libs.plugins.build.plugin.jvm)
    alias(libs.plugins.kotlinx.serialization)
}

application {
    mainClass.set("com.arbitrage.scanner.kafka.AppKafkaMainKt")
}

dependencies {
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
}
