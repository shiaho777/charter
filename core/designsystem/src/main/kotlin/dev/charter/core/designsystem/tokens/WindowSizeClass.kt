package dev.charter.core.designsystem.tokens

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Window size classes per Google's adaptive guidance:
 * https://developer.android.com/develop/ui/compose/layouts/adaptive/use-window-size-classes
 *
 * Compact covers most phones in portrait; medium kicks in on tablets and
 * landscape phones; expanded is desk-class widths.
 *
 */
enum class WindowSizeClass(
    val minWidth: Dp,
) {
    Compact(minWidth = 0.dp),
    Medium(minWidth = 600.dp),
    Expanded(minWidth = 840.dp),
}

/** The current [WindowSizeClass], from the shortest window dimension. */
@Composable
fun currentWindowSizeClass(): WindowSizeClass {
    val configuration = LocalConfiguration.current
    val width = configuration.screenWidthDp.dp
    return when {
        width >= WindowSizeClass.Expanded.minWidth -> WindowSizeClass.Expanded
        width >= WindowSizeClass.Medium.minWidth -> WindowSizeClass.Medium
        else -> WindowSizeClass.Compact
    }
}
