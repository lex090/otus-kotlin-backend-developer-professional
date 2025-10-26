plugins {
    alias(libs.plugins.kotlin.jvm) apply false
}

group = "com.education.project"
version = "1.0-SNAPSHOT"

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    group = project.group
    version = project.version
}

// Централизованные таски для сборки и тестирования всех модулей
tasks.register("buildAll") {
    description = "Build all modules in the project"
    group = "Custom project tasks"

    dependsOn(gradle.includedBuild("arbitrage-scanner").task(":buildAll"))
}

tasks.register("testAll") {
    description = "Run all tests in all modules"
    group = "Custom project tasks"

    dependsOn(gradle.includedBuild("arbitrage-scanner").task(":testAll"))
}

tasks.register("cleanAll") {
    description = "Clean all modules in the project"
    group = "Custom project tasks"

    dependsOn(gradle.includedBuild("arbitrage-scanner").task(":cleanAll"))
}

tasks.register("checkAll") {
    description = "Check all modules (build + test)"
    group = "Custom project tasks"

    dependsOn(gradle.includedBuild("arbitrage-scanner").task(":checkAll"))
}

// Команда запуска ./gradlew jibDockerBuildAll --no-daemon
tasks.register("jibDockerBuildAll") {
    description = "Build Docker images using Jib for all modules that support it."
    group = "Custom project tasks"

    dependsOn(gradle.includedBuild("arbitrage-scanner").task(":jibDockerBuildAll"))
}
