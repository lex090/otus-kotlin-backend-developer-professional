plugins {
    alias(libs.plugins.build.plugin.jvm)
}

dependencies {
    implementation(projects.arbitrageScannerCommon)
    implementation(projects.arbitrageScannerRepoInmemory)
    implementation(libs.uuid)
    implementation(libs.bignum)
    implementation(libs.kotlinx.coroutines.core)

    // Exposed ORM
    api(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.dao)

    // PostgreSQL & Connection Pool
    implementation(libs.postgresql)
    implementation(libs.hikari)

    // Liquibase
    implementation(libs.liquibase.core)

    // Test dependencies
    testImplementation(projects.arbitrageScannerRepoTests)
    testImplementation(projects.arbitrageScannerStubs)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.junit)
}
