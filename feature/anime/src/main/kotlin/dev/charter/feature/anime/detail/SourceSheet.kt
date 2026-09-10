package dev.charter.feature.anime.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.charter.core.designsystem.components.CharterLoadingIndicator
import dev.charter.core.designsystem.motion.Motion
import dev.charter.core.designsystem.tokens.LocalSpacing
import dev.charter.core.model.Episode
import dev.charter.core.model.SourceResult
import dev.charter.core.model.SourceStatus

private val EPISODE_CELL_MIN = 64.dp
private val EPISODE_GRID_MAX_HEIGHT = 280.dp

/**
 * The prototype's signature interaction: every demo source searches on its
 * own, reports its own status, retries alone, and accepts its own keyword.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourceSheet(
    bangumiName: String,
    episodes: List<Episode>,
    onDismiss: () -> Unit,
    onPlay: (sourceName: String, roadName: String, episodeSort: Float) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SourceSheetViewModel = hiltViewModel(),
) {
    LaunchedEffect(bangumiName) { viewModel.start(bangumiName, episodes) }
    val results by viewModel.results.collectAsStateWithLifecycle()
    val spacing = LocalSpacing.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = modifier,
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = spacing.md).padding(bottom = spacing.xl),
        ) {
            Text(
                text = "选择播放源",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(spacing.xs))
            Text(
                text = "演示源仅用于展示多源检索交互，不连接任何第三方站点",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(spacing.md))
            results.forEach { result ->
                SourceRow(
                    result = result,
                    onRetry = { viewModel.retry(result.sourceName) },
                    onChangeKeyword = { viewModel.changeKeyword(result.sourceName, it) },
                    onPlay = onPlay,
                )
                Spacer(Modifier.height(spacing.sm))
            }
        }
    }
}

@Composable
private fun SourceRow(
    result: SourceResult,
    onRetry: () -> Unit,
    onChangeKeyword: (String) -> Unit,
    onPlay: (sourceName: String, roadName: String, episodeSort: Float) -> Unit,
) {
    val spacing = LocalSpacing.current
    var expanded by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf(false) }
    var keywordDraft by rememberSaveable(result.sourceName) { mutableStateOf(result.keyword) }
    var selectedRoad by remember(result.sourceName) { mutableIntStateOf(0) }

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(spacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = result.sourceName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(spacing.xs))
                    SourceStatusLine(result = result, onRetry = onRetry, onEditKeyword = { editing = true })
                }
                if (result.status == SourceStatus.SUCCESS) {
                    IconButton(onClick = { expanded = !expanded }) {
                        val arrow =
                            if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown
                        Icon(
                            imageVector = arrow,
                            contentDescription = if (expanded) "收起线路" else "展开线路与集数",
                        )
                    }
                }
            }
            if (editing) {
                Spacer(Modifier.height(spacing.sm))
                KeywordEditor(
                    draft = keywordDraft,
                    onDraftChange = { keywordDraft = it },
                    onConfirm = {
                        onChangeKeyword(keywordDraft)
                        editing = false
                    },
                    onCancel = {
                        keywordDraft = result.keyword
                        editing = false
                    },
                )
            }
            AnimatedVisibility(
                visible = expanded && result.status == SourceStatus.SUCCESS,
                enter = expandVertically(animationSpec = Motion.expandSize) + fadeIn(),
                exit = shrinkVertically(animationSpec = Motion.expandSize) + fadeOut(),
            ) {
                RoadAndEpisodePicker(
                    result = result,
                    selectedRoad = selectedRoad,
                    onSelectRoad = { selectedRoad = it },
                    onPlay = onPlay,
                )
            }
        }
    }
}

@Composable
private fun SourceStatusLine(
    result: SourceResult,
    onRetry: () -> Unit,
    onEditKeyword: () -> Unit,
) {
    val spacing = LocalSpacing.current
    when (result.status) {
        SourceStatus.PENDING, SourceStatus.SEARCHING ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                CharterLoadingIndicator(indicatorSize = STATUS_INDICATOR_SIZE)
                Spacer(Modifier.width(spacing.sm))
                Text(
                    text = "检索「${result.keyword}」中…",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        SourceStatus.FAILED ->
            Column {
                Text(
                    text = result.errorMessage ?: "检索失败",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.xs)) {
                    TextButton(onClick = onRetry) { Text("重试") }
                    TextButton(onClick = onEditKeyword) { Text("改关键词") }
                }
            }
        SourceStatus.SUCCESS -> {
            val episodeCount =
                result.roads
                    .firstOrNull()
                    ?.episodes
                    ?.size ?: 0
            Text(
                text = "${result.roads.size} 条线路 · $episodeCount 话",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun KeywordEditor(
    draft: String,
    onDraftChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LocalSpacing.current.xs),
    ) {
        OutlinedTextField(
            value = draft,
            onValueChange = onDraftChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            label = { Text("该源的搜索词") },
        )
        IconButton(onClick = onConfirm) {
            Icon(Icons.Filled.Check, contentDescription = "用该关键词重新检索")
        }
        IconButton(onClick = onCancel) {
            Icon(Icons.Filled.Close, contentDescription = "取消修改")
        }
    }
}

@Composable
private fun RoadAndEpisodePicker(
    result: SourceResult,
    selectedRoad: Int,
    onSelectRoad: (Int) -> Unit,
    onPlay: (sourceName: String, roadName: String, episodeSort: Float) -> Unit,
) {
    val spacing = LocalSpacing.current
    Column(Modifier.padding(top = spacing.sm)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            modifier = Modifier.horizontalScroll(rememberScrollState()),
        ) {
            result.roads.forEachIndexed { index, road ->
                FilterChip(
                    selected = index == selectedRoad,
                    onClick = { onSelectRoad(index) },
                    label = { Text(road.name) },
                )
            }
        }
        val road = result.roads.getOrNull(selectedRoad) ?: result.roads.firstOrNull() ?: return
        Spacer(Modifier.height(spacing.sm))
        LazyVerticalGrid(
            columns = GridCells.Adaptive(EPISODE_CELL_MIN),
            modifier = Modifier.heightIn(max = EPISODE_GRID_MAX_HEIGHT),
            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
            verticalArrangement = Arrangement.spacedBy(spacing.xs),
        ) {
            items(road.episodes, key = { "${result.sourceName}-${road.name}-${it.sort}" }) { episode ->
                SuggestionChip(
                    onClick = { onPlay(result.sourceName, road.name, episode.sort) },
                    label = { Text(episode.number) },
                )
            }
        }
    }
}

private const val STATUS_INDICATOR_SIZE = 16
