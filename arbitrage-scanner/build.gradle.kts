group = "com.arbitrage.scanner"
version = "1.0-SNAPSHOT"

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
}

ext {
    val specDir = layout.projectDirectory.dir("../specs")
    set("arbitrage-scanner-api-v1", specDir.file("arbitrage-scanner-api-v1.yaml").toString())
}

// Агрегирующие таски для подпроектов
tasks.register("buildAll") {
    description = "Build all arbitrage-scanner modules"
    group = "Custom project tasks"

    subprojects {
        tasks.findByName("build")?.let { this@register.dependsOn(it) }
    }
}

tasks.register("cleanAll") {
    description = "Clean all arbitrage-scanner modules"
    group = "Custom project tasks"

    subprojects {
        tasks.findByName("clean")?.let { this@register.dependsOn(it) }
    }
}

tasks.register("checkAll") {
    description = "Check all arbitrage-scanner modules"
    group = "Custom project tasks"

    subprojects {
        tasks.findByName("check")?.let { this@register.dependsOn(it) }
    }
}

tasks.register("testAll") {
    description = "Run all tests in arbitrage-scanner modules"
    group = "Custom project tasks"

    subprojects {
        val testTask = tasks.findByName("allTests") ?: tasks.findByName("test")
        testTask?.let { this@register.dependsOn(it) }
    }
}

tasks.register("jibDockerBuildAll") {
    description = "Build Docker images using Jib for all modules that support it. ./gradlew jibDockerBuildAll --no-daemon"
    group = "Custom project tasks"

    subprojects {
        tasks.findByName("jibDockerBuild")?.let { this@register.dependsOn(it) }
    }
}
