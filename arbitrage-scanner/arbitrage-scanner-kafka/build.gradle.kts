plugins {
    alias(libs.plugins.build.plugin.jvm)
    alias(libs.plugins.kotlinx.serialization)
}

dependencies {
    implementation(project(":arbitrage-scanner-common"))

    implementation(libs.kotlinx.serialization.json)

    // Hoplite - Configuration library
    implementation(libs.hoplite.core)
    implementation(libs.hoplite.yaml)

    // Kafka
    implementation(libs.kafka.clients)

    // Logging
    implementation(project(":arbitrage-scanner-libs:arbitrage-scanner-lib-logging"))
    implementation(project(":arbitrage-scanner-libs:arbitrage-scanner-lib-logging-logback"))

    // Test
    testImplementation(libs.kotlin.test)
}
