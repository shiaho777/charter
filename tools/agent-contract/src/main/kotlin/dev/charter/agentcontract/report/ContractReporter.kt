package dev.charter.agentcontract.report

import dev.charter.agentcontract.rules.ContractReport
import dev.charter.agentcontract.rules.RuleKind
import java.io.File

/** Writes the verification report and a shields.io-compatible badge file. */
object ContractReporter {
    fun writeJson(
        report: ContractReport,
        file: File,
    ) {
        file.parentFile?.mkdirs()
        val sb = StringBuilder()
        sb.append("{\n")
        sb.append("  \"summary\": {\n")
        sb.append("    \"machineRules\": ").append(report.total).append(",\n")
        sb.append("    \"verified\": ").append(report.verified).append(",\n")
        sb.append("    \"percent\": ").append(report.percent).append(",\n")
        sb.append("    \"judgmentRules\": ").append(report.judgmentRules.size).append("\n")
        sb.append("  },\n")
        sb.append("  \"rules\": [\n")
        report.rules.forEachIndexed { i, v ->
            val rule = v.rule
            sb.append("    {\n")
            sb.append("      \"file\": ").append(jsonString(rule.file)).append(",\n")
            sb.append("      \"line\": ").append(rule.line).append(",\n")
            sb.append("      \"kind\": ").append(jsonString(rule.kind.name.lowercase())).append(",\n")
            sb.append("      \"description\": ").append(jsonString(rule.description)).append(",\n")
            if (rule.kind == RuleKind.MACHINE) {
                sb
                    .append("      \"checks\": [")
                    .append(rule.checks.joinToString(", ") { jsonString(it.toString()) })
                    .append("],\n")
                sb
                    .append("      \"missingChecks\": [")
                    .append(v.missingChecks.joinToString(", ") { jsonString(it.toString()) })
                    .append("],\n")
                sb.append("      \"verified\": ").append(v.ok).append("\n")
            } else {
                sb.append("      \"verified\": null\n")
            }
            sb.append("    }").append(if (i < report.rules.size - 1) "," else "").append('\n')
        }
        sb.append("  ]\n")
        sb.append("}\n")
        file.writeText(sb.toString())
    }

    /** shields.io endpoint schema — feed to https://img.shields.io/endpoint. */
    fun writeBadge(
        report: ContractReport,
        file: File,
    ) {
        file.parentFile?.mkdirs()
        val color =
            when {
                report.violations.isEmpty() -> "brightgreen"
                report.percent >= 80 -> "yellow"
                else -> "red"
            }
        val message = "${report.percent}%25 machine-enforced (${report.verified}/${report.total})"
        file.writeText(
            """
            {
              "schemaVersion": 1,
              "label": "agent rules",
              "message": "$message",
              "color": "$color"
            }
            """.trimIndent() + "\n",
        )
    }

    fun consoleSummary(report: ContractReport): String {
        val sb = StringBuilder()
        sb.appendLine("Agent contract report")
        sb.appendLine("=====================")
        report.rules.forEach { v ->
            val mark =
                when {
                    v.rule.kind == RuleKind.JUDGMENT -> "JUDGMENT"
                    v.ok -> "  OK   "
                    else -> " MISSING"
                }
            val checks = if (v.rule.checks.isEmpty()) "" else " (${v.rule.checks.joinToString(", ") { it.toString() }})"
            sb.appendLine("[$mark] ${v.rule.file}:${v.rule.line} ${v.rule.description}$checks")
        }
        sb.appendLine(
            "Machine-enforced: ${report.verified}/${report.total} (${report.percent}%). " +
                "Judgment rules: ${report.judgmentRules.size}.",
        )
        return sb.toString()
    }

    private fun jsonString(s: String): String =
        buildString {
            append('"')
            for (c in s) {
                when (c) {
                    '"' -> append("\\\"")
                    '\\' -> append("\\\\")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> append(c)
                }
            }
            append('"')
        }
}
