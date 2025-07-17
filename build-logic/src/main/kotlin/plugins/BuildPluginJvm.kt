package plugins

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.repositories
import org.gradle.kotlin.dsl.the

@Suppress("unused")
class BuildPluginJvm : Plugin<Project> {

    override fun apply(project: Project) = with(project) {
        val libs = project.the<LibrariesForLibs>()

        pluginManager.apply(libs.plugins.kotlin.jvm.get().pluginId)

        group = rootProject.group
        version = rootProject.version

        repositories {
            mavenCentral()
        }
    }
}
