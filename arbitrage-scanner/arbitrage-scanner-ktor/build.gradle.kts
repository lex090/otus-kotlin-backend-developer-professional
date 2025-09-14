plugins {
    alias(libs.plugins.build.plugin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinx.serialization)
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

ktor {
    docker {
        localImageName.set(project.name)
        imageTag.set(project.version.toString())
        jreVersion.set(JavaVersion.VERSION_21)
        portMappings.set(
            listOf(
                io.ktor.plugin.features.DockerPortMapping(
                    8080,
                    8080,
                    io.ktor.plugin.features.DockerPortMappingProtocol.TCP
                )
            )
        )
    }
}

jib {
    from {
        image = "eclipse-temurin:21-jre"
        platforms {
            platform {
                architecture = "arm64"
                os = "linux"
            }
            platform {
                architecture = "amd64"
                os = "linux"
            }
        }
    }
    container.mainClass = application.mainClass.get()
}

dependencies {

    implementation(project(":arbitrage-scanner-common"))
    implementation(project(":arbitrage-scanner-api-v1"))
    implementation(project(":arbitrage-scanner-business-logic"))

    // Ktor
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.config.yaml)

    // Koin
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)

    // Logging
    implementation(libs.logback.classic)
    implementation(libs.logback.more.appenders)
    implementation(libs.fluent.logger)

    testImplementation(libs.kotlin.test)

    testImplementation(libs.ktor.server.test)
    testImplementation(libs.ktor.client.negotiation)
}