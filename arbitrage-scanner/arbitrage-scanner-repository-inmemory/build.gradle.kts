plugins {
    alias(libs.plugins.build.plugin.multiplatform)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":arbitrage-scanner-common"))
                implementation(libs.bignum)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.atomicfu)
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
