plugins {
    alias(libs.plugins.build.plugin.multiplatform)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(projects.arbitrageScannerCommon)
                api(projects.arbitrageScannerStubs)
                api(libs.kotlin.test)
                api(libs.kotlinx.coroutines.test)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        jvmMain {
            dependencies {
                api(kotlin("test-junit"))
            }
        }
    }
}
