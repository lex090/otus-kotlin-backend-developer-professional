plugins {
    alias(libs.plugins.build.plugin.jvm)
}

dependencies {
    testImplementation(libs.kotlin.test)
}

tasks.test {
    useJUnitPlatform()
}