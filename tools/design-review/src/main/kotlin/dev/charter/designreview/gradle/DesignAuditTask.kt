package dev.charter.designreview.gradle

import dev.charter.designreview.audits.AccentHueAudit
import dev.charter.designreview.audits.ContrastAudit
import dev.charter.designreview.audits.TouchTargetAudit
import dev.charter.designreview.core.DesignReviewEngine
import dev.charter.designreview.core.Finding
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

abstract class DesignAuditTask : DefaultTask() {
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputDirectory
    abstract val inputDir: DirectoryProperty

    @get:OutputDirectory
    abstract val reportDir: DirectoryProperty

    @get:Input
    abstract val maxAccentHues: Property<Int>

    @get:Input
    abstract val minTouchTargetDp: Property<Float>

    @get:Input
    abstract val minContrastRatio: Property<Double>

    @get:Optional
    @get:Input
    abstract val failOnError: Property<Boolean>

    @TaskAction
    fun audit() {
        val engine =
            DesignReviewEngine(
                audits =
                    listOf(
                        AccentHueAudit(maxAccentHues = maxAccentHues.get()),
                        TouchTargetAudit(minSizeDp = minTouchTargetDp.get()),
                        ContrastAudit(minRatio = minContrastRatio.get()),
                    ),
            )
        val report = engine.run(inputDir.get().asFile)

        val dir = reportDir.get().asFile.also { it.mkdirs() }
        dir.resolve("design-review-report.json").writeText(toJson(report.findings, report.screensAudited))
        val summary =
            buildString {
                appendLine(
                    "designAudit: ${report.screensAudited} screen(s), " +
                        "${report.errors.size} error(s), ${report.warnings.size} warning(s)",
                )
                report.findings.forEach { f ->
                    appendLine("  [${f.severity}] ${f.audit} ${f.screen}: ${f.message}")
                }
            }
        logger.lifecycle(summary)
        if (summary.lines().size <= 2) println(summary.trim())

        if (report.errors.isNotEmpty() && failOnError.getOrElse(true)) {
            throw GradleException(
                "designAudit failed with ${report.errors.size} error(s). " +
                    "See ${dir.resolve("design-review-report.json")}",
            )
        }
    }

    private fun toJson(
        findings: List<Finding>,
        screens: Int,
    ): String {
        val sb = StringBuilder()
        sb.append("{\n  \"screensAudited\": ").append(screens).append(",\n  \"findings\": [\n")
        findings.forEachIndexed { i, f ->
            sb
                .append("    {\"audit\": \"")
                .append(f.audit)
                .append("\", \"severity\": \"")
                .append(f.severity)
                .append("\", \"screen\": \"")
                .append(esc(f.screen))
                .append("\", \"message\": \"")
                .append(esc(f.message))
                .append("\"}")
            if (i < findings.size - 1) sb.append(',')
            sb.append('\n')
        }
        sb.append("  ]\n}\n")
        return sb.toString()
    }

    private fun esc(s: String) = s.replace("\\", "\\\\").replace("\"", "\\\"")
}
