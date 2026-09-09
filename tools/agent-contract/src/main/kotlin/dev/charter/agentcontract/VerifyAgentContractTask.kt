package dev.charter.agentcontract

import dev.charter.agentcontract.report.ContractReporter
import dev.charter.agentcontract.rules.AgentRulesParser
import dev.charter.agentcontract.rules.CheckVerifier
import dev.charter.agentcontract.rules.RuleKind
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class VerifyAgentContractTask : DefaultTask() {
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFiles
    abstract val agentsFiles: ConfigurableFileCollection

    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFiles
    abstract val detektConfigs: ConfigurableFileCollection

    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFiles
    abstract val konsistSources: ConfigurableFileCollection

    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFiles
    abstract val ciWorkflows: ConfigurableFileCollection

    @get:Input
    abstract val failOnViolation: Property<Boolean>

    @get:OutputDirectory
    abstract val reportDir: DirectoryProperty

    init {
        // Task existence checks query the live task registry at execution time.
        notCompatibleWithConfigurationCache("AGENTS.md task: checks probe the live task registry")
    }

    @TaskAction
    fun verify() {
        val rules =
            expand(agentsFiles.files)
                .flatMap { AgentRulesParser.parse(it) }
        logger.lifecycle(
            "Parsed ${rules.count { it.kind == RuleKind.MACHINE }} machine rules and " +
                "${rules.count { it.kind == RuleKind.JUDGMENT }} judgment rules from " +
                "${expand(agentsFiles.files).size} AGENTS.md file(s).",
        )

        val verifier =
            CheckVerifier(
                detektConfigs = expand(detektConfigs.files),
                konsistSources = expand(konsistSources.files),
                ciWorkflows = expand(ciWorkflows.files),
                taskResolver = { path -> taskExists(path) },
            )
        val report = verifier.verify(rules)

        val dir = reportDir.get().asFile.also { it.mkdirs() }
        ContractReporter.writeJson(report, dir.resolve("agent-contract-report.json"))
        ContractReporter.writeBadge(report, dir.resolve("agent-rules-badge.json"))
        logger.lifecycle(ContractReporter.consoleSummary(report))

        if (report.violations.isNotEmpty() && failOnViolation.get()) {
            throw GradleException(
                "AGENTS.md claims ${report.violations.size} machine check(s) that do not exist: " +
                    report.violations.joinToString("; ") { v ->
                        "${v.rule.file}:${v.rule.line} missing " +
                            v.missingChecks.joinToString(", ") { it.toString() }
                    },
            )
        }
    }

    private fun expand(files: Set<File>): Set<File> =
        files
            .flatMap { f ->
                when {
                    f.isDirectory -> f.walkTopDown().filter { it.isFile }.toList()
                    f.isFile -> listOf(f)
                    else -> emptyList()
                }
            }.toSet()

    private fun taskExists(path: String): Boolean {
        if (!path.startsWith(":")) {
            return project.tasks.findByName(path) != null ||
                project.subprojects.any { it.tasks.findByName(path) != null }
        }
        val parts = path.removePrefix(":").split(":")
        val taskName = parts.last()
        val modulePath = ":" + parts.dropLast(1).joinToString(":")
        val module = if (modulePath == ":") project else project.findProject(modulePath)
        return module?.tasks?.findByName(taskName) != null
    }
}
