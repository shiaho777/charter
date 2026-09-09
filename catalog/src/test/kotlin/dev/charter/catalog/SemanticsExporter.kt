package dev.charter.catalog

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.onRoot
import kotlinx.serialization.Serializable
import java.io.File

/**
 * Exports the semantics facts pixels cannot express (clickability, text,
 * bounds) next to each screenshot. `designAudit` reads these JSON files and
 * runs the design-invariant detectors (touch targets, contrast) against the
 * referenced PNG.
 */
@Serializable
data class ExportedNode(
    val boundsInDp: List<Float>,
    val clickable: Boolean = false,
    val text: String? = null,
    val contentDescription: String? = null,
)

@Serializable
data class SemanticsExport(
    val screen: String,
    val screenshot: String,
    val density: Float,
    val nodes: List<ExportedNode>,
)

object SemanticsExporter {
    fun export(
        provider: SemanticsNodeInteractionsProvider,
        screen: String,
        screenshotRelativePath: String,
        density: Float,
        outFile: File,
    ) {
        val root = provider.onRoot().fetchSemanticsNode()
        val nodes = mutableListOf<ExportedNode>()
        collect(root, density, nodes)
        outFile.parentFile?.mkdirs()
        outFile.writeText(toJson(SemanticsExport(screen, screenshotRelativePath, density, nodes)))
    }

    private fun collect(
        node: SemanticsNode,
        density: Float,
        out: MutableList<ExportedNode>,
    ) {
        val bounds = node.boundsInRoot
        val clickable = node.config.getOrNull(SemanticsActions.OnClick) != null
        val text =
            node.config
                .getOrNull(SemanticsProperties.Text)
                ?.joinToString(" ") { it.text }
        val cd =
            node.config
                .getOrNull(SemanticsProperties.ContentDescription)
                ?.joinToString(" ")
        if (clickable || !text.isNullOrBlank() || !cd.isNullOrBlank()) {
            out.add(
                ExportedNode(
                    boundsInDp =
                        listOf(
                            bounds.left / density,
                            bounds.top / density,
                            bounds.right / density,
                            bounds.bottom / density,
                        ),
                    clickable = clickable,
                    text = text,
                    contentDescription = cd,
                ),
            )
        }
        node.children.forEach { collect(it, density, out) }
    }

    private fun toJson(export: SemanticsExport): String {
        val sb = StringBuilder()
        sb.append("{\n")
        sb.append("  \"screen\": \"").append(esc(export.screen)).append("\",\n")
        sb.append("  \"screenshot\": \"").append(esc(export.screenshot)).append("\",\n")
        sb.append("  \"density\": ").append(export.density).append(",\n")
        sb.append("  \"nodes\": [\n")
        export.nodes.forEachIndexed { i, n ->
            sb
                .append("    {\"boundsInDp\": [")
                .append(n.boundsInDp.joinToString(", ") { "%.1f".format(it) })
                .append("], \"clickable\": ")
                .append(n.clickable)
            n.text?.let { sb.append(", \"text\": \"").append(esc(it)).append('"') }
            n.contentDescription?.let { sb.append(", \"contentDescription\": \"").append(esc(it)).append('"') }
            sb.append("}").append(if (i < export.nodes.size - 1) "," else "").append('\n')
        }
        sb.append("  ]\n}\n")
        return sb.toString()
    }

    private fun esc(s: String) = s.replace("\\", "\\\\").replace("\"", "\\\"")
}
