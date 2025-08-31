group = "com.arbitrage.scanner"
version = "1.0-SNAPSHOT"

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
}

ext {
    val specDir = layout.projectDirectory.dir("../specs")
    set("arbitrage-scanner-api-v1", specDir.file("arbitrage-scanner-api-v1.yaml").toString())
}
