package dev.charter.gradle

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) =
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            // The "android" extension is registered under the concrete application/
            // library type, never as CommonExtension — look both up.
            val common =
                extensions.findByType(ApplicationExtension::class.java)
                    ?: extensions.findByType(LibraryExtension::class.java)
                    ?: error("charter.android.compose requires an Android plugin applied first")
            common.buildFeatures {
                compose = true
            }

            extensions.configure<ComposeCompilerGradlePluginExtension> {
                // Stability + metrics reports land in build/compose-reports per module.
                // CI reads them to fail on unstable list-item types (see ci.yml).
                reportsDestination.set(layout.buildDirectory.dir("compose-reports"))
                metricsDestination.set(layout.buildDirectory.dir("compose-metrics"))
            }

            dependencies {
                val bom = libs.findLibrary("compose-bom").get()
                add("implementation", platform(bom))
                add("implementation", libs.findBundle("compose").get())
                add("debugImplementation", libs.findLibrary("compose-ui-tooling").get())
            }
        }
}
