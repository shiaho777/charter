package dev.charter.designreview.core

import kotlinx.serialization.Serializable

/**
 * A screen under review: one screenshot plus the semantics facts exported
 * alongside it by the catalog's test harness. The PNG carries the visual truth,
 * the JSON carries what pixels cannot express (clickability, text presence).
 */
data class ScreenSample(
    val name: String,
    val screenshot: java.io.File,
    val semantics: SemanticsExport?,
)

@Serializable
data class SemanticsExport(
    val screen: String,
    val screenshot: String,
    val density: Float,
    val nodes: List<SemanticsNodeInfo> = emptyList(),
)

@Serializable
data class SemanticsNodeInfo(
    /** [left, top, right, bottom] in dp. */
    val boundsInDp: List<Float>,
    val clickable: Boolean = false,
    val text: String? = null,
    val contentDescription: String? = null,
) {
    val width: Float get() = boundsInDp.getOrElse(2) { 0f } - boundsInDp.getOrElse(0) { 0f }
    val height: Float get() = boundsInDp.getOrElse(3) { 0f } - boundsInDp.getOrElse(1) { 0f }
    val hasText: Boolean get() = !text.isNullOrBlank()
}

enum class Severity { ERROR, WARNING }

data class Finding(
    val audit: String,
    val severity: Severity,
    val screen: String,
    val message: String,
)

data class AuditReport(
    val findings: List<Finding>,
    val screensAudited: Int,
) {
    val errors: List<Finding> get() = findings.filter { it.severity == Severity.ERROR }
    val warnings: List<Finding> get() = findings.filter { it.severity == Severity.WARNING }
    val passed: Boolean get() = errors.isEmpty()
}

interface Audit {
    val id: String

    fun evaluate(sample: ScreenSample): List<Finding>
}
