plugins {
    alias(libs.plugins.build.plugin.jvm)
    alias(libs.plugins.kotlinx.serialization)
}

dependencies {
    implementation(project(":arbitrage-scanner-common"))

    implementation(libs.kotlinx.serialization.json)

    // Kafka
    implementation(libs.kafka.clients)

    // Test
    testImplementation(libs.kotlin.test)
}
