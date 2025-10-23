plugins {
    alias(libs.plugins.build.plugin.multiplatform)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":arbitrage-scanner-common"))
                implementation(project(":arbitrage-scanner-stubs"))
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlin.cor)
            }
        }
    }
}