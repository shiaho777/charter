package dev.charter.agentcontract

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection

open class AgentContractExtension(
    project: Project,
) {
    /** AGENTS.md files to parse. */
    val agentsFiles: ConfigurableFileCollection = project.objects.fileCollection()

    /** detekt config yml files to scan for active rules. */
    val detektConfigs: ConfigurableFileCollection = project.objects.fileCollection()

    /** Kotlin sources to scan for `konsist:` check classes. */
    val konsistSources: ConfigurableFileCollection = project.objects.fileCollection()

    /** CI workflow yml files to scan for `ci:` needles. */
    val ciWorkflows: ConfigurableFileCollection = project.objects.fileCollection()

    /** Fail the build when a claimed check is missing. Default true. */
    var failOnViolation: Boolean = true
}

class AgentContractPlugin : Plugin<Project> {
    override fun apply(project: Project) =
        with(project) {
            val extension = extensions.create("agentContract", AgentContractExtension::class.java, this)

            // Sensible defaults; the build script can add more via the extension.
            extension.agentsFiles.from(layout.projectDirectory.file("AGENTS.md"))
            extension.detektConfigs.from(layout.projectDirectory.file("config/detekt/detekt.yml"))
            extension.konsistSources.from(layout.projectDirectory.dir("architecture-tests/src"))
            extension.ciWorkflows.from(layout.projectDirectory.dir(".github/workflows"))

            val verify = tasks.register("verifyAgentContract", VerifyAgentContractTask::class.java)

            verify.configure {
                description = "Verifies every [machine] rule in AGENTS.md has a real, " +
                    "configured check behind it. Emits a badge + JSON report."
                group = "verification"

                agentsFiles.from(extension.agentsFiles)
                detektConfigs.from(extension.detektConfigs)
                konsistSources.from(extension.konsistSources)
                ciWorkflows.from(extension.ciWorkflows)
                failOnViolation.set(extension.failOnViolation)
                reportDir.set(layout.buildDirectory.dir("reports/agent-contract"))
            }

            plugins.withId("base") {
                tasks.named("check").configure { dependsOn(verify) }
            }
        }
}
