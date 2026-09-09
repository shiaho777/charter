package dev.charter.core.designsystem.motion

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/** Press feedback vocabulary. */
object PressFeedback {
    /** Elements dip to 96% while pressed — visible, but not theatrical. */
    const val PRESSED_SCALE = 0.96f
}

/**
 * Uniform press feedback: scales down while pressed, springs back on release.
 * [interactionSource] must be the same instance handed to the element's
 * clickable — see [pressableClickable] for the one-call version.
 */
@Composable
fun Modifier.pressScale(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = PressFeedback.PRESSED_SCALE,
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = Motion.press,
        label = "pressScale",
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/** Click + uniform press-scale feedback in one modifier. */
@Composable
fun Modifier.pressableClickable(onClick: () -> Unit): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    return this
        .pressScale(interactionSource)
        .clickable(
            interactionSource = interactionSource,
            indication = LocalIndication.current,
            onClick = onClick,
        )
}
