package dev.charter.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp

/**
 * Expressive shape badge for state screens: a MaterialShapes cookie with a
 * contrasting icon inside. Decorative by design — the state's text is the
 * semantic content, the badge is the visual anchor.
 *
 */
@Composable
fun StateBadge(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: Int = STATE_BADGE_SIZE,
    iconSize: Int = STATE_BADGE_ICON_SIZE,
) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier =
            modifier
                .size(size.dp)
                .clip(MaterialShapes.Cookie4Sided.toShape())
                .background(colors.secondaryContainer)
                .clearAndSetSemantics { },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.onSecondaryContainer,
            modifier = Modifier.size(iconSize.dp),
        )
    }
}

/** Pre-configured badge icons for the four screen states. */
object StateBadges {
    val Empty: ImageVector get() = Icons.Outlined.SearchOff
    val Error: ImageVector get() = Icons.Outlined.CloudOff
    val Favorite: ImageVector get() = Icons.Outlined.FavoriteBorder
}

private const val STATE_BADGE_SIZE = 88
private const val STATE_BADGE_ICON_SIZE = 36
