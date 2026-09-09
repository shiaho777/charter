package dev.charter.core.designsystem.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.toPath
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph

/**
 * The Charter loading indicator: a morphing walk through expressive
 * MaterialShapes (cookie → pill → sunny → …), rotating as it goes.
 *
 * `animated = false` renders the start polygon as a clipped silhouette —
 * screenshot goldens and previews use this; motion quality is verified
 * on-device (playbooks/new-ui-screen.md). One component serves both,
 * resolving the determinism tension. (The static branch also sidesteps a
 * Robolectric rendering gap: Morph-to-Path fills don't rasterize there,
 * while polygon clip+fill does.)
 *
 */
@Composable
fun CharterLoadingIndicator(
    modifier: Modifier = Modifier,
    indicatorSize: Int = INDICATOR_SIZE,
    color: Color = MaterialTheme.colorScheme.primary,
    animated: Boolean = true,
) {
    if (!animated) {
        // Static frame: the morph's start shape, clipped and filled.
        // Visually identical to animation frame 0.
        Box(
            modifier =
                modifier
                    .size(indicatorSize.dp)
                    .clip(Shapes.first().toShape())
                    .background(color)
                    .clearAndSetSemantics { contentDescription = "Loading" },
        )
        return
    }

    val transition = rememberInfiniteTransition(label = "loading")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(MORPH_CYCLE_MS, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
        label = "progress",
    )

    // One Path instance per indicator, rewritten each frame.
    val path = remember { Path() }

    Box(
        modifier =
            modifier
                .size(indicatorSize.dp)
                .clearAndSetSemantics { contentDescription = "Loading" }
                .drawBehind {
                    val cycle = progress * Shapes.size
                    val index = cycle.toInt() % Shapes.size
                    val fraction = cycle - cycle.toInt()
                    Morphs[index].toPath(fraction, path)
                    // Path is in unit space; fit the diagonal so rotation stays
                    // inside bounds, and pivot the transform at the center.
                    val s = size.minDimension / ROOT_2
                    withTransform({
                        translate(size.width / 2f, size.height / 2f)
                        rotate(progress * FULL_ROTATION)
                        scale(s, s)
                        translate(-UNIT_CENTER, -UNIT_CENTER)
                    }) {
                        drawPath(path, color)
                    }
                },
    )
}

private val Shapes =
    listOf(
        MaterialShapes.SoftBurst,
        MaterialShapes.Cookie9Sided,
        MaterialShapes.Pill,
        MaterialShapes.Sunny,
        MaterialShapes.Cookie4Sided,
        MaterialShapes.Oval,
        MaterialShapes.Cookie7Sided,
    )

/** Pre-built morph pairs: shape[i] → shape[i+1], closing the loop. */
private val Morphs =
    List(Shapes.size) { i ->
        Morph(Shapes[i], Shapes[(i + 1) % Shapes.size])
    }

private const val INDICATOR_SIZE = 48
private const val MORPH_CYCLE_MS = 4550
private const val FULL_ROTATION = 360f
private const val ROOT_2 = 1.41421356f
private const val UNIT_CENTER = 0.5f
