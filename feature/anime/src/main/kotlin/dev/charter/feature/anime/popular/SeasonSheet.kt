package dev.charter.feature.anime.popular

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.charter.core.designsystem.tokens.LocalSpacing
import dev.charter.core.model.Season

private const val RECENT_SEASONS = 8

/** Season picker — the prototype's quarter sheet, backed by real air-date filters. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SeasonSheet(
    currentSeason: Season,
    onDismiss: () -> Unit,
    onSelect: (Season) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = modifier,
    ) {
        Column(Modifier.padding(horizontal = spacing.md).padding(bottom = spacing.xl)) {
            Text(
                text = "选择季度",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(spacing.sm))
            Text(
                text = "按放送季度浏览番剧目录",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(spacing.md))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                Season.recent(RECENT_SEASONS).forEach { season ->
                    FilterChip(
                        selected = season == currentSeason,
                        onClick = { onSelect(season) },
                        label = { Text(season.label) },
                    )
                }
            }
        }
    }
}
