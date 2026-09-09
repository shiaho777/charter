package dev.charter.designreview.audits

import dev.charter.designreview.core.Audit
import dev.charter.designreview.core.Finding
import dev.charter.designreview.core.ScreenSample
import dev.charter.designreview.core.Severity
import javax.imageio.ImageIO
import kotlin.math.floor

/**
 * Distilled from the ui_ux_engineering "hierarchy from type, not new colors"
 * rule: a screen with more than a handful of accent hues reads as garish.
 *
 * Heuristic: count hue buckets (30° each) that hold at least [minShare] of all
 * visibly saturated pixels. Neutrals (low saturation / near-black / near-white)
 * are ignored — they are the surface language, not accents.
 */
class AccentHueAudit(
    private val maxAccentHues: Int = 3,
    private val minSaturation: Float = 0.40f,
    private val minValue: Float = 0.25f,
    private val minShare: Float = 0.015f,
) : Audit {
    override val id = "accent-hues"

    override fun evaluate(sample: ScreenSample): List<Finding> {
        val png = sample.screenshot.takeIf { it.isFile } ?: return emptyList()
        val img = ImageIO.read(png) ?: return emptyList()

        val buckets = IntArray(12)
        var saturated = 0
        // Sample every 4th pixel: stable enough for hue census, 16x faster.
        var y = 0
        while (y < img.height) {
            var x = 0
            while (x < img.width) {
                val rgb = img.getRGB(x, y)
                val r = (rgb shr 16) and 0xFF
                val g = (rgb shr 8) and 0xFF
                val b = rgb and 0xFF
                val max = maxOf(r, g, b) / 255f
                val min = minOf(r, g, b) / 255f
                val sat = if (max == 0f) 0f else (max - min) / max
                if (sat >= minSaturation && max >= minValue) {
                    saturated++
                    val hue = hueDegrees(r / 255f, g / 255f, b / 255f, max, min)
                    buckets[floor(hue / 30f).toInt().coerceIn(0, 11)]++
                }
                x += 4
            }
            y += 4
        }

        if (saturated == 0) return emptyList()
        val used = buckets.count { it >= saturated * minShare }
        return if (used > maxAccentHues) {
            listOf(
                Finding(
                    audit = id,
                    severity = Severity.WARNING,
                    screen = sample.name,
                    message =
                        "$used accent hue families on screen (limit $maxAccentHues); " +
                            "hierarchy should come from type/weight/elevation, not new colors",
                ),
            )
        } else {
            emptyList()
        }
    }

    private fun hueDegrees(
        r: Float,
        g: Float,
        b: Float,
        max: Float,
        min: Float,
    ): Float {
        val d = max - min
        if (d == 0f) return 0f
        val h =
            when (max) {
                r -> ((g - b) / d) % 6f
                g -> ((b - r) / d) + 2f
                else -> ((r - g) / d) + 4f
            }
        var deg = h * 60f
        if (deg < 0) deg += 360f
        return deg
    }
}
