package dev.charter.designreview.audits

import dev.charter.designreview.core.Audit
import dev.charter.designreview.core.Finding
import dev.charter.designreview.core.ScreenSample
import dev.charter.designreview.core.Severity
import javax.imageio.ImageIO
import kotlin.math.max
import kotlin.math.pow

/**
 * Distilled from the ui_ux_engineering accessibility rules: body text contrast
 * must be >= 4.5:1 (large text >= 3:1).
 *
 * Heuristic: within each text node's bounds, split pixels into the darkest and
 * lightest quartiles by luminance and treat those as glyph vs background. It is
 * deliberately conservative — real glyph/background pairs score far above the
 * threshold, so only genuinely muddy text falls below it.
 */
class ContrastAudit(
    private val minRatio: Double = 4.5,
) : Audit {
    override val id = "contrast"

    override fun evaluate(sample: ScreenSample): List<Finding> {
        val semantics = sample.semantics ?: return emptyList()
        val textNodes = semantics.nodes.filter { it.hasText }
        if (textNodes.isEmpty()) return emptyList()
        val img = sample.screenshot.takeIf { it.isFile }?.let(ImageIO::read) ?: return emptyList()
        val density = semantics.density.takeIf { it > 0f } ?: return emptyList()

        return textNodes.mapNotNull { node ->
            val l = (node.boundsInDp[0] * density).toInt().coerceIn(0, img.width - 1)
            val t = (node.boundsInDp[1] * density).toInt().coerceIn(0, img.height - 1)
            val r = (node.boundsInDp[2] * density).toInt().coerceIn(l + 1, img.width)
            val b = (node.boundsInDp[3] * density).toInt().coerceIn(t + 1, img.height)
            if (r - l < 4 || b - t < 4) return@mapNotNull null

            val lums = ArrayList<Float>((r - l) * (b - t))
            var y = t
            while (y < b) {
                var x = l
                while (x < r) {
                    val rgb = img.getRGB(x, y)
                    lums.add(relativeLuminance((rgb shr 16) and 0xFF, (rgb shr 8) and 0xFF, rgb and 0xFF))
                    x++
                }
                y++
            }
            lums.sort()
            val darkEnd = max(1, lums.size / 5)
            val lightStart = max(darkEnd, lums.size * 3 / 4)
            val dark = lums.subList(0, darkEnd).average()
            val light = lums.subList(lightStart, lums.size).average()
            val ratio = (light + 0.05) / (dark + 0.05)

            if (ratio < minRatio) {
                Finding(
                    audit = id,
                    severity = Severity.ERROR,
                    screen = sample.name,
                    message =
                        "text \"${node.text?.take(
                            32,
                        )}\" contrast %.2f:1 (minimum %.1f:1)".format(ratio, minRatio),
                )
            } else {
                null
            }
        }
    }

    private fun relativeLuminance(
        r: Int,
        g: Int,
        b: Int,
    ): Float {
        fun ch(c: Int): Double {
            val s = c / 255.0
            return if (s <= 0.03928) s / 12.92 else ((s + 0.055) / 1.055).pow(2.4)
        }
        return (0.2126 * ch(r) + 0.7152 * ch(g) + 0.0722 * ch(b)).toFloat()
    }
}
