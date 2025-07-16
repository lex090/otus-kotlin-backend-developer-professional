plugins {
    kotlin("jvm") apply false
}

group = "com.education.project"
version = "1.0-SNAPSHOT"

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    group = project.group
    version = project.version
}
