rootProject.name = "arbitrage-scanner"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

pluginManagement {
    includeBuild("../build-logic")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include("arbitrage-scanner-api-v1")
include("arbitrage-scanner-common")
include("arbitrage-scanner-ktor")
include("arbitrage-scanner-stubs")