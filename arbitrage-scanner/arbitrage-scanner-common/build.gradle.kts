plugins {
    alias(libs.plugins.build.plugin.multiplatform)
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(libs.bignum)
                implementation(project(":arbitrage-scanner-libs:arbitrage-scanner-lib-logging"))
            }
        }
    }
}