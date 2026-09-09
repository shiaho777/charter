package dev.charter.designreview.audits

import dev.charter.designreview.core.Audit
import dev.charter.designreview.core.Finding
import dev.charter.designreview.core.ScreenSample
import dev.charter.designreview.core.Severity

/**
 * Distilled from the ui_ux_engineering accessibility rules: every interactive
 * element must present a touch target of at least 48 x 48 dp.
 */
class TouchTargetAudit(
    private val minSizeDp: Float = 48f,
) : Audit {
    override val id = "touch-targets"

    override fun evaluate(sample: ScreenSample): List<Finding> {
        val nodes = sample.semantics?.nodes ?: return emptyList()
        return nodes
            .filter { it.clickable && (it.width < minSizeDp || it.height < minSizeDp) }
            .map { node ->
                Finding(
                    audit = id,
                    severity = Severity.ERROR,
                    screen = sample.name,
                    message =
                        "clickable node ${node.contentDescription ?: node.text ?: "<unlabeled>"} " +
                            "is ${fmt(node.width)}x${fmt(node.height)}dp (minimum ${fmt(minSizeDp)}dp)",
                )
            }
    }

    private fun fmt(v: Float): String = if (v == v.toLong().toFloat()) v.toLong().toString() else "%.1f".format(v)
}
