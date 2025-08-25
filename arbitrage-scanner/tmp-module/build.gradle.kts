plugins {
    alias(libs.plugins.build.plugin.multiplatform)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.datetime)
        }
        
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}