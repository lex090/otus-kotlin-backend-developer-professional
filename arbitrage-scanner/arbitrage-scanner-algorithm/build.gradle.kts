plugins {
    alias(libs.plugins.build.plugin.multiplatform)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":arbitrage-scanner-common"))
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlin.cor)
                implementation(libs.uuid)
                implementation(libs.bignum)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                implementation(project(":arbitrage-scanner-stubs"))
            }
        }
    }
}
