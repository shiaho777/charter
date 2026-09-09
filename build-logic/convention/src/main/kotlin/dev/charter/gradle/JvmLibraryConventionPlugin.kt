package dev.charter.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class JvmLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) =
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.jvm")
            configureKotlinJvmToolchain()

            dependencies {
                add("implementation", libs.findLibrary("kotlinx-coroutines-core").get())
            }
        }
}
