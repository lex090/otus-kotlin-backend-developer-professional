plugins {
    alias(libs.plugins.build.plugin.multiplatform)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":arbitrage-scanner-common"))
                implementation(project(":arbitrage-scanner-stubs"))
                implementation(project(":arbitrage-scanner-libs:arbitrage-scanner-lib-logging"))
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlin.cor)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
            }
        }
    }
}