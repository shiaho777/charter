package dev.charter.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.charter.core.designsystem.motion.Motion

/**
 * M3 connected-button style segmented control: segments melt into each other,
 * the selected pill's inner corners expand from 8dp to the outer radius as
 * selection animates, and pressing sharpens the corner — the corner radius
 * is the state signal, not just a background fill.
 *
 */
@Composable
fun ConnectedTabs(
    labels: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(GROUP_HEIGHT.dp),
        horizontalArrangement = Arrangement.spacedBy(GROUP_GAP.dp),
    ) {
        labels.forEachIndexed { index, label ->
            val selection =
                animateFloatAsState(
                    targetValue = if (index == selected) 1f else 0f,
                    animationSpec = Motion.indicator,
                    label = "selection",
                ).value
            ConnectedSegment(
                label = label,
                selection = selection,
                isLeading = index == 0,
                isTrailing = index == labels.lastIndex,
                isSelected = index == selected,
                onTap = { onSelect(index) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ConnectedSegment(
    label: String,
    selection: Float,
    isLeading: Boolean,
    isTrailing: Boolean,
    isSelected: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressAnim =
        animateFloatAsState(
            targetValue = if (pressed) 1f else 0f,
            animationSpec = tween(PRESS_TWEEN_MS),
            label = "press",
        ).value

    val outer = (GROUP_HEIGHT / 2).dp
    val innerBase = INNER_CORNER.dp + (outer - INNER_CORNER.dp) * selection
    val inner = innerBase + (PRESSED_INNER_CORNER.dp - innerBase) * pressAnim * (1f - selection)
    val shape =
        RoundedCornerShape(
            topStart = if (isLeading) outer else inner,
            bottomStart = if (isLeading) outer else inner,
            topEnd = if (isTrailing) outer else inner,
            bottomEnd = if (isTrailing) outer else inner,
        )

    Box(
        modifier =
            modifier
                .height(GROUP_HEIGHT.dp)
                .minimumInteractiveComponentSize()
                .clip(shape)
                .background(
                    lerp(
                        colors.surfaceContainer,
                        colors.secondaryContainer,
                        selection,
                    ),
                ).clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Tab,
                    onClick = onTap,
                ).semantics {
                    selected = isSelected
                },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            maxLines = 1,
            style = MaterialTheme.typography.labelLarge,
            color = lerp(colors.onSurfaceVariant, colors.onSecondaryContainer, selection),
            modifier = Modifier.padding(horizontal = 12.dp),
        )
    }
}

private const val GROUP_HEIGHT = 40
private const val GROUP_GAP = 2
private const val INNER_CORNER = 8
private const val PRESSED_INNER_CORNER = 4
private const val PRESS_TWEEN_MS = 180
