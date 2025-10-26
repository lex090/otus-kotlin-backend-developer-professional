rootProject.name = "otus-kotlin-backend-developer-professional"

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

includeBuild("build-logic")
includeBuild("arbitrage-scanner")