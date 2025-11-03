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
include("arbitrage-scanner-stubs")
include("arbitrage-scanner-service-stub")
include("arbitrage-scanner-business-logic")
include("arbitrage-scanner-algorithm")
include("arbitrage-scanner-repo-inmemory")
include("arbitrage-scanner-repo-tests")
include("arbitrage-scanner-app-common")
include("arbitrage-scanner-app-kafka")
include("arbitrage-scanner-app-ktor")
include("arbitrage-scanner-libs:arbitrage-scanner-lib-logging")
include("arbitrage-scanner-libs:arbitrage-scanner-lib-logging-logback")