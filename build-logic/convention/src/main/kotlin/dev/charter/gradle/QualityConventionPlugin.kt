package dev.charter.gradle

import com.diffplug.gradle.spotless.SpotlessExtension
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * Quality gate wiring: detekt (+ Charter's own rule pack) and spotless.
 *
 * Applied to the root project; configures every subproject. Rules live in
 * config/detekt/detekt.yml — the single source of truth that
 * `verifyAgentContract` cross-checks AGENTS.md against.
 */
class QualityConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) =
        with(target) {
            with(pluginManager) {
                apply("io.gitlab.arturbosch.detekt")
                apply("com.diffplug.spotless")
            }

            extensions.configure<DetektExtension> {
                buildUponDefaultConfig = true
                allRules = false
                config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
                baseline = file("$rootDir/config/detekt/baseline.xml")
                source.setFrom(
                    files(rootDir).asFileTree.matching {
                        include("**/*.kt", "**/*.kts")
                        exclude("**/build/**", "**/resources/**", "**/.gradle/**")
                    },
                )
            }

            dependencies {
                "detektPlugins"(libs.findLibrary("detekt-formatting").get())
                // Charter's own rules (MotionSpecInline, NoGlobalScope, ...).
                "detektPlugins"("dev.charter:detekt-rules")
            }

            extensions.configure<SpotlessExtension> {
                kotlin {
                    target("**/*.kt")
                    targetExclude("**/build/**/*.kt")
                    ktlint(libs.findVersion("ktlint").get().toString())
                        .setEditorConfigPath("$rootDir/.editorconfig")
                }
                kotlinGradle {
                    target("**/*.gradle.kts")
                    ktlint(libs.findVersion("ktlint").get().toString())
                        .setEditorConfigPath("$rootDir/.editorconfig")
                }
                format("misc") {
                    target("**/*.md", "**/*.yml", "**/*.yaml", "**/*.toml", "**/.gitignore")
                    targetExclude("**/build/**", "**/.gradle/**")
                    trimTrailingWhitespace()
                    endWithNewline()
                }
            }
        }
}
