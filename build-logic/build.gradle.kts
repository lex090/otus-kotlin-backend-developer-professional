plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        create("buildPluginJvm") {
            id = "build-plugin-jvm"
            implementationClass = "BuildPluginJvm"
        }
    }
}

group = "com.education.project"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

    implementation(libs.kotlin.gradle.plugin.api)
}

