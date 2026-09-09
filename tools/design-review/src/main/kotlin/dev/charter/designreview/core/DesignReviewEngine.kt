package dev.charter.designreview.core

import kotlinx.serialization.json.Json
import java.io.File

/**
 * Runs a set of [Audit]s over every [SemanticsExport] found in an input
 * directory. The export file names the screenshot it belongs to, so the harness
 * and the audit task can evolve independently.
 */
class DesignReviewEngine(
    private val audits: List<Audit>,
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun run(inputDir: File): AuditReport {
        val samples =
            inputDir
                .walkTopDown()
                .filter { it.isFile && it.extension == "json" }
                .mapNotNull(::parse)
                .toList()

        val findings =
            samples.flatMap { sample ->
                audits.flatMap { audit -> audit.evaluate(sample) }
            }
        return AuditReport(findings = findings, screensAudited = samples.size)
    }

    private fun parse(file: File): ScreenSample? =
        runCatching {
            val export = json.decodeFromString(SemanticsExport.serializer(), file.readText())
            val png = File(file.parentFile, export.screenshot).canonicalFile
            ScreenSample(name = export.screen, screenshot = png, semantics = export)
        }.getOrNull()
}
