plugins {
    alias(libs.plugins.build.plugin.multiplatform)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.arbitrageScannerCommon)
                implementation(libs.uuid)
                implementation(libs.cache4k)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                implementation(projects.arbitrageScannerStubs)
                implementation(projects.arbitrageScannerRepoTests)
            }
        }
    }
}
