package dev.charter.agentcontract.rules

import org.gradle.api.Project
import java.io.File

/**
 * Verifies that every check a rule claims actually exists somewhere it can run.
 *
 * Check kinds:
 * - `detekt:RuleId` — RuleId appears in a detekt config yml with `active: true`
 * - `konsist:ClassName` — a Kotlin file declaring `class ClassName` exists
 * - `task:taskPath` — a Gradle task with that name/path is registered
 * - `ci:needle` — the needle string appears in a workflow yml
 */
class CheckVerifier(
    private val detektConfigs: Set<File>,
    private val konsistSources: Set<File>,
    private val ciWorkflows: Set<File>,
    private val taskResolver: (String) -> Boolean,
) {
    fun verify(report: List<AgentRule>): ContractReport {
        val verifications =
            report.map { rule ->
                if (rule.kind == RuleKind.JUDGMENT) {
                    RuleVerification(rule, emptyList(), emptyList())
                } else {
                    val (ok, missing) = rule.checks.partition { exists(it) }
                    RuleVerification(rule, ok, missing)
                }
            }
        return ContractReport(verifications)
    }

    private fun exists(ref: CheckRef): Boolean =
        when (ref.type) {
            "detekt" -> detektRuleActive(ref.target)
            "konsist" -> konsistClassExists(ref.target)
            "task" -> taskResolver(ref.target)
            "ci" -> ciContains(ref.target)
            else -> false
        }

    private fun detektRuleActive(ruleId: String): Boolean = detektConfigs.any { cfg -> ruleActiveInConfig(cfg, ruleId) }

    /** A rule is active if its block contains `active: true` under the right key. */
    private fun ruleActiveInConfig(
        cfg: File,
        ruleId: String,
    ): Boolean {
        if (!cfg.isFile) return false
        var inRule = false
        var ruleIndent = -1
        var active = false
        cfg.forEachLine { raw ->
            val trimmed = raw.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#")) return@forEachLine
            val indent = raw.takeWhile { it == ' ' }.length
            when {
                trimmed.startsWith("$ruleId:") -> {
                    inRule = true
                    ruleIndent = indent
                }
                inRule && indent <= ruleIndent && trimmed.endsWith(":") -> inRule = false
                inRule && indent > ruleIndent && trimmed.startsWith("active:") && trimmed.contains("true") ->
                    active = true
            }
        }
        return active
    }

    private fun konsistClassExists(className: String): Boolean =
        konsistSources.any { src ->
            src.isFile && src.readText().contains(Regex("""class\s+$className\b"""))
        }

    private fun ciContains(needle: String): Boolean =
        ciWorkflows.any { wf -> wf.isFile && wf.readText().contains(needle) }

    companion object {
        fun fromProject(
            project: Project,
            detektConfigs: Set<File>,
            konsistSources: Set<File>,
            ciWorkflows: Set<File>,
        ): CheckVerifier =
            CheckVerifier(
                detektConfigs = detektConfigs,
                konsistSources = konsistSources,
                ciWorkflows = ciWorkflows,
                taskResolver = { path -> taskExists(project, path) },
            )

        private fun taskExists(
            root: Project,
            path: String,
        ): Boolean {
            if (path.startsWith(":")) {
                val parts = path.removePrefix(":").split(":")
                val taskName = parts.last()
                val modulePath = ":" + parts.dropLast(1).joinToString(":")
                val module = if (modulePath == ":") root else root.findProject(modulePath)
                return module?.tasks?.findByName(taskName) != null
            }
            if (root.tasks.findByName(path) != null) return true
            return root.subprojects.any { it.tasks.findByName(path) != null }
        }
    }
}
