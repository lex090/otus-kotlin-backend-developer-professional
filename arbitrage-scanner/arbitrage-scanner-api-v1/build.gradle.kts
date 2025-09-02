import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    alias(libs.plugins.build.plugin.multiplatform)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.crowdproj.generator)
}

crowdprojGenerate {
    packageName.set("${project.group}.api.v1")
    inputSpec.set(rootProject.ext["arbitrage-scanner-api-v1"] as String)
}

kotlin {
    sourceSets {
        commonMain {
            kotlin.srcDir(layout.buildDirectory.dir("generate-resources/src/commonMain/kotlin"))
            dependencies {
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.arbitrage.scanner.common)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

tasks {
    val openApiGenerateTask: GenerateTask = getByName("openApiGenerate", GenerateTask::class) {
        outputDir.set(layout.buildDirectory.file("generate-resources").get().toString())
        finalizedBy("compileCommonMainKotlinMetadata")
    }
    filter { it.name.startsWith("compile") }.forEach {
        it.dependsOn(openApiGenerateTask)
    }
}