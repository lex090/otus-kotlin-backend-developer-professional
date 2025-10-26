plugins {
    alias(libs.plugins.build.plugin.multiplatform)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":arbitrage-scanner-common"))
                implementation(project(":arbitrage-scanner-business-logic"))
                implementation(project(":arbitrage-scanner-libs:arbitrage-scanner-lib-logging"))
            }
        }
    }
}