package dev.charter.core.designsystem.motion

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

/**
 * The one motion vocabulary for Charter. Named specs, defined once, referenced
 * everywhere — call sites never write `spring(...)` (detekt: MotionSpecInline).
 *
 * Philosophy:
 * - Springs, not durations, for anything the finger or layout drives.
 * - No bounce on effects; a touch of life (0.82 damping) only where playfulness
 *   is the point (`snappyFloat`).
 * - Transitions fade through a 0.98 scale — subtle depth, never a slide show.
 */
object Motion {
    /** General-purpose value settling — calm, no overshoot. */
    val softFloat =
        spring<Float>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = 420f,
        )

    /** Playful elements — the one spec allowed a visible bounce. */
    val snappyFloat =
        spring<Float>(
            dampingRatio = 0.82f,
            stiffness = 900f,
        )

    val softDp =
        spring<Dp>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = 420f,
        )

    val softInt =
        spring<Int>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = 420f,
        )

    val softOffset =
        spring<IntOffset>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = 420f,
        )

    /** Content-driven size changes (list item growth, panel resize). */
    val contentSize =
        spring<IntSize>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow,
        )

    /** Expand/collapse of list rows and sheets. */
    val expandSize =
        spring<IntSize>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = 380f,
        )

    /** Segmented-control indicator settling — snappy, no bounce. */
    val indicator =
        spring<Float>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = 520f,
        )

    /** Pane split ratio (split-view scaffolds). */
    val paneRatio =
        spring<Float>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = 380f,
        )

    /** Press feedback: dip to [PressFeedback.PRESSED_SCALE] and spring back. */
    val press =
        spring<Float>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = 700f,
        )

    /** Sheet anchor settling — heavier, feels physical. */
    val sheetAnchor =
        spring<Float>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow,
        )

    internal val enterTween = tween<Float>(durationMillis = 220, easing = FastOutSlowInEasing)
    internal val exitTween = tween<Float>(durationMillis = 160, easing = FastOutSlowInEasing)
    internal val enterOffset =
        spring<IntOffset>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = 380f,
        )
    internal val exitOffset =
        spring<IntOffset>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = 480f,
        )
}
