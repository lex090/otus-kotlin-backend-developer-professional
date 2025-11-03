plugins {
    alias(libs.plugins.build.plugin.multiplatform)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(libs.bignum)
                implementation(projects.arbitrageScannerLibs.arbitrageScannerLibLogging)
            }
        }
    }
}