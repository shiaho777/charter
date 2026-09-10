package dev.charter.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.charter.core.designsystem.tokens.LocalSpacing

private val STATE_MAX_WIDTH = 440.dp
private val MIN_TOUCH_TARGET = 48.dp
private const val COMPACT_BADGE_SIZE = 56
private const val COMPACT_BADGE_ICON_SIZE = 28

/**
 * Full-bleed empty state: shape badge, headline, supporting message, and an
 * optional action. Centers in the available space; content is clamped to
 * [STATE_MAX_WIDTH] so it never smears wide on tablets.
 *
 */
@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    StateScreen(
        badge = { StateBadge(icon = StateBadges.Empty) },
        title = title,
        message = message,
        modifier = modifier,
        actionLabel = actionLabel,
        onAction = onAction,
    )
}

/** Full-bleed error state: [EmptyState] anatomy with an error badge and retry. */
@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Something went wrong",
    retryLabel: String = "Retry",
) {
    StateScreen(
        badge = { StateBadge(icon = StateBadges.Error) },
        title = title,
        message = message,
        modifier = modifier,
        actionLabel = retryLabel,
        onAction = onRetry,
    )
}

@Composable
private fun StateScreen(
    badge: @Composable () -> Unit,
    title: String,
    message: String,
    modifier: Modifier,
    actionLabel: String?,
    onAction: (() -> Unit)?,
) {
    val spacing = LocalSpacing.current
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().widthIn(max = STATE_MAX_WIDTH),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            badge()
            Spacer(Modifier.height(spacing.lg))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier.semantics {
                        heading()
                        liveRegion = LiveRegionMode.Polite
                    },
            )
            Spacer(Modifier.height(spacing.xs))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.height(spacing.md))
                Button(
                    onClick = onAction,
                    modifier = Modifier.heightIn(min = MIN_TOUCH_TARGET),
                ) {
                    Text(actionLabel)
                }
            }
        }
    }
}

/** Compact row variant for embedding inside lists, grids, and slivers. */
@Composable
fun CompactState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = StateBadges.Empty,
) {
    val spacing = LocalSpacing.current
    Row(
        modifier = modifier.fillMaxWidth().padding(spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        StateBadge(icon = icon, size = COMPACT_BADGE_SIZE, iconSize = COMPACT_BADGE_ICON_SIZE)
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
