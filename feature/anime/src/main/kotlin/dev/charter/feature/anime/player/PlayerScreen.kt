package dev.charter.feature.anime.player

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.charter.core.designsystem.components.ErrorState
import dev.charter.core.designsystem.components.LoadingState
import dev.charter.core.designsystem.motion.pressableClickable
import dev.charter.core.designsystem.tokens.LocalSpacing
import dev.charter.core.designsystem.tokens.WindowSizeClass
import dev.charter.core.designsystem.tokens.currentWindowSizeClass
import dev.charter.core.model.HistoryEntry
import dev.charter.core.model.RoadEpisode
import dev.charter.feature.anime.components.SectionHeader

private const val VIDEO_ASPECT = 16f / 9f
private const val LANDSCAPE_VIDEO_WEIGHT = 1.6f
private val EPISODE_CELL_SIZE = 48.dp
private val PLAY_ICON_SIZE = 64.dp
private val WATCHED_ICON_SIZE = 14.dp
private val FINISHED_ICON_SIZE = 20.dp
private const val SEGMENT_SIZE = 12

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    bangumiId: Long,
    sourceName: String,
    roadName: String,
    episodeSort: Float,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    LaunchedEffect(bangumiId, sourceName, roadName, episodeSort) {
        viewModel.setPlayback(bangumiId, sourceName, roadName, episodeSort)
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val windowSizeClass = currentWindowSizeClass()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = (state as? PlayerUiState.Content)?.bangumi?.displayName ?: "播放",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { innerPadding ->
        when (val s = state) {
            PlayerUiState.Loading -> LoadingState(Modifier.padding(innerPadding), label = "加载中…")
            is PlayerUiState.Error ->
                ErrorState(
                    title = "加载失败",
                    message = s.message,
                    retryLabel = "重试",
                    onRetry = viewModel::retry,
                    modifier = Modifier.padding(innerPadding),
                )
            is PlayerUiState.Content ->
                if (windowSizeClass == WindowSizeClass.Compact) {
                    Column(Modifier.padding(innerPadding).fillMaxSize()) {
                        VideoPlaceholder(
                            content = s,
                            onMarkFinished = viewModel::markEpisodeFinished,
                            modifier = Modifier.fillMaxWidth().padding(LocalSpacing.current.sm),
                        )
                        EpisodePanel(
                            content = s,
                            onSelectRoad = viewModel::selectRoad,
                            onSelectEpisode = viewModel::selectEpisode,
                            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                        )
                    }
                } else {
                    Row(Modifier.padding(innerPadding).fillMaxSize()) {
                        VideoPlaceholder(
                            content = s,
                            onMarkFinished = viewModel::markEpisodeFinished,
                            modifier = Modifier.weight(LANDSCAPE_VIDEO_WEIGHT).padding(LocalSpacing.current.sm),
                        )
                        EpisodePanel(
                            content = s,
                            onSelectRoad = viewModel::selectRoad,
                            onSelectEpisode = viewModel::selectEpisode,
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .verticalScroll(rememberScrollState())
                                    .padding(LocalSpacing.current.sm),
                        )
                    }
                }
        }
    }
}

@Composable
private fun VideoPlaceholder(
    content: PlayerUiState.Content,
    onMarkFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val road = content.roads.firstOrNull { it.name == content.selectedRoad } ?: content.roads.firstOrNull()
    val episode = road?.episodes?.firstOrNull { it.sort == content.selectedSort }
    val finished =
        content.history?.let { it.lastEpisodeSort == content.selectedSort && it.isFinished } == true

    Box(
        modifier =
            modifier
                .aspectRatio(VIDEO_ASPECT)
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surfaceContainer),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
            modifier = Modifier.padding(spacing.md),
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(PLAY_ICON_SIZE),
            )
            Text(
                text = "演示占位播放器",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "${content.bangumi.displayName} · ${content.selectedRoad}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = episode?.let { "第${it.number}话 ${it.title}" } ?: "未选择剧集",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (finished) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(FINISHED_ICON_SIZE),
                    )
                    Spacer(Modifier.size(spacing.xs))
                    Text(
                        text = "本集已看完",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            } else {
                FilledTonalButton(onClick = onMarkFinished) {
                    Text("标记本集看完")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EpisodePanel(
    content: PlayerUiState.Content,
    onSelectRoad: (String) -> Unit,
    onSelectEpisode: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val road = content.roads.firstOrNull { it.name == content.selectedRoad } ?: content.roads.firstOrNull()
    val episodes = road?.episodes ?: emptyList()
    val segments = episodes.chunked(SEGMENT_SIZE)
    var segmentIndex by remember(road?.name) {
        mutableIntStateOf(segmentOf(episodes, content.selectedSort))
    }

    Column(modifier.padding(horizontal = spacing.sm)) {
        SectionHeader(title = "选集 · ${episodes.size} 话")
        Spacer(Modifier.height(spacing.sm))
        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            modifier = Modifier.horizontalScroll(rememberScrollState()),
        ) {
            content.roads.forEach { roadOption ->
                FilterChip(
                    selected = roadOption.name == road?.name,
                    onClick = { onSelectRoad(roadOption.name) },
                    label = { Text(roadOption.name) },
                )
            }
        }
        if (segments.size > 1) {
            Spacer(Modifier.height(spacing.sm))
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                modifier = Modifier.horizontalScroll(rememberScrollState()),
            ) {
                segments.forEachIndexed { index, segment ->
                    FilterChip(
                        selected = index == segmentIndex,
                        onClick = { segmentIndex = index },
                        label = { Text("${segment.first().number}-${segment.last().number}") },
                    )
                }
            }
        }
        Spacer(Modifier.height(spacing.md))
        val visible = segments.getOrNull(segmentIndex) ?: episodes
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            visible.forEach { episode ->
                EpisodeCell(
                    episode = episode,
                    selected = episode.sort == content.selectedSort,
                    watched = isWatched(episode, content),
                    onClick = { onSelectEpisode(episode.sort) },
                )
            }
        }
        Spacer(Modifier.height(spacing.lg))
    }
}

@Composable
private fun EpisodeCell(
    episode: RoadEpisode,
    selected: Boolean,
    watched: Boolean,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val container: Color
    val content: Color
    when {
        selected -> {
            container = colors.secondaryContainer
            content = colors.onSecondaryContainer
        }
        watched -> {
            container = colors.primaryContainer
            content = colors.onPrimaryContainer
        }
        else -> {
            container = colors.surfaceContainerLow
            content = colors.onSurface
        }
    }
    Box(
        modifier =
            Modifier
                .size(EPISODE_CELL_SIZE)
                .clip(MaterialTheme.shapes.medium)
                .background(container)
                .pressableClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = episode.number,
                style = MaterialTheme.typography.labelLarge,
                color = content,
            )
            if (watched && !selected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "已看",
                    tint = content,
                    modifier = Modifier.size(WATCHED_ICON_SIZE),
                )
            }
        }
    }
}

private fun isWatched(
    episode: RoadEpisode,
    content: PlayerUiState.Content,
): Boolean {
    val history: HistoryEntry = content.history ?: return false
    return episode.sort < history.lastEpisodeSort ||
        (episode.sort == history.lastEpisodeSort && history.isFinished)
}

private fun segmentOf(
    episodes: List<RoadEpisode>,
    selectedSort: Float,
): Int {
    val index = episodes.indexOfFirst { it.sort == selectedSort }
    if (index < 0) return 0
    return index / SEGMENT_SIZE
}
