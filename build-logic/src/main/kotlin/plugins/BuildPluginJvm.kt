package plugins

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.repositories
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension

@Suppress("unused")
class BuildPluginJvm : Plugin<Project> {

    override fun apply(project: Project) = with(project) {
        val libs = project.the<LibrariesForLibs>()

        val kotlinJvmPluginId = libs.plugins.kotlin.jvm.get().pluginId

        pluginManager.apply(kotlinJvmPluginId)

        plugins.withId(kotlinJvmPluginId) {
            extensions.configure<KotlinJvmExtension> {
                jvmToolchain {
                    languageVersion.set(JavaLanguageVersion.of(libs.versions.jvm.language.get()))
                }
            }
        }

        group = rootProject.group
        version = rootProject.version

        repositories {
            mavenCentral()
        }
    }
}
