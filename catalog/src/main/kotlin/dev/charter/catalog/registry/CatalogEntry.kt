package dev.charter.catalog.registry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.charter.core.designsystem.components.CharterLoadingIndicator
import dev.charter.core.designsystem.components.CompactState
import dev.charter.core.designsystem.components.ConnectedTabs
import dev.charter.core.designsystem.components.EmptyState
import dev.charter.core.designsystem.components.ErrorState
import dev.charter.core.designsystem.components.LoadingState
import dev.charter.core.designsystem.components.StateBadge
import dev.charter.core.designsystem.components.StateBadges
import dev.charter.core.designsystem.tokens.LocalSpacing

/**
 * Every design-system component registers here. The registry is the contract
 * the screenshot tests and designAudit iterate over — add a component to the
 * design system, add an entry here, and it automatically gets: a catalog page,
 * golden screenshots in light + dark, and design-invariant auditing.
 */
data class CatalogEntry(
    val name: String,
    val description: String,
    val content: @Composable () -> Unit,
)

val catalogEntries: List<CatalogEntry> =
    listOf(
        CatalogEntry(
            name = "LoadingState",
            description = "Morphing CharterLoadingIndicator + caption. The shape walk is the brand.",
        ) {
            LoadingState()
        },
        CatalogEntry(
            name = "CharterLoadingIndicator",
            description = "Static frame of the morphing indicator — what goldens capture.",
        ) {
            CharterLoadingIndicator(animated = false)
        },
        CatalogEntry(
            name = "ErrorState",
            description = "Shape badge + one primary action. Emphasis wins on position + size + color.",
        ) {
            ErrorState(message = "Network unavailable", onRetry = {})
        },
        CatalogEntry(
            name = "EmptyState",
            description = "Shape badge + headline + supporting message + next step.",
        ) {
            EmptyState(
                title = "No repositories yet",
                message = "Pull down to refresh, or try another query.",
                actionLabel = "Refresh",
                onAction = {},
            )
        },
        CatalogEntry(
            name = "CompactState",
            description = "Row variant for embedding inside lists, grids, and slivers.",
        ) {
            CompactState(
                title = "No results",
                message = "Try a different query.",
                icon = StateBadges.Empty,
            )
        },
        CatalogEntry(
            name = "ConnectedTabs",
            description = "Connected-button segmented control; corner radius is the state signal.",
        ) {
            var selected by remember { mutableIntStateOf(0) }
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = LocalSpacing.current.md),
                verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.md),
            ) {
                ConnectedTabs(
                    labels = listOf("概览", "吐槽", "角色"),
                    selected = selected,
                    onSelect = { selected = it },
                )
            }
        },
    )

@Composable
fun CatalogEntryCard(entry: CatalogEntry) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            text = entry.name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = LocalSpacing.current.md),
        )
        Text(
            text = entry.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier =
                Modifier.padding(
                    horizontal = LocalSpacing.current.md,
                    vertical = LocalSpacing.current.xs,
                ),
        )
        entry.content()
    }
}
