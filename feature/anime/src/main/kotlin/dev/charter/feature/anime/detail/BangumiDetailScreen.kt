package dev.charter.feature.anime.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import dev.charter.core.designsystem.components.CharterLoadingIndicator
import dev.charter.core.designsystem.components.CompactState
import dev.charter.core.designsystem.components.ErrorState
import dev.charter.core.designsystem.components.LoadingState
import dev.charter.core.designsystem.components.StateBadges
import dev.charter.core.designsystem.tokens.LocalSpacing
import dev.charter.core.model.Bangumi
import dev.charter.core.model.CollectStatus
import dev.charter.core.model.CollectionEntry
import dev.charter.core.model.Episode
import dev.charter.feature.anime.components.SectionHeader

private val POSTER_WIDTH = 110.dp
private const val POSTER_RATIO = 5f / 7f
private const val SUMMARY_COLLAPSED_LINES = 4
private const val RATING_BAR_WIDTH = 24
private const val RATING_COUNT_WIDTH = 40

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BangumiDetailScreen(
    bangumiId: Long,
    onPlay: (bangumiId: Long, sourceName: String, roadName: String, episodeSort: Float) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BangumiDetailViewModel = hiltViewModel(),
) {
    LaunchedEffect(bangumiId) { viewModel.setBangumiId(bangumiId) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showSourceSheet by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val messages = viewModel.messages
    LaunchedEffect(messages) { messages.collect { snackbarHostState.showSnackbar(it) } }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = detailTitle(state),
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
        floatingActionButton = {
            if (state is BangumiDetailUiState.Content) {
                ExtendedFloatingActionButton(
                    onClick = { showSourceSheet = true },
                    icon = { Icon(Icons.Filled.PlayArrow, contentDescription = null) },
                    text = { Text("播放") },
                )
            }
        },
    ) { innerPadding ->
        when (val s = state) {
            BangumiDetailUiState.Loading -> LoadingState(Modifier.padding(innerPadding), label = "加载中…")
            is BangumiDetailUiState.Error ->
                ErrorState(
                    title = "加载失败",
                    message = s.message,
                    retryLabel = "重试",
                    onRetry = viewModel::refresh,
                    modifier = Modifier.padding(innerPadding),
                )
            is BangumiDetailUiState.Content ->
                DetailContent(
                    content = s,
                    modifier = Modifier.padding(innerPadding),
                    onCollectStatus = viewModel::setCollectStatus,
                    onRemoveCollection = viewModel::removeCollection,
                    onOpenSourceSheet = { showSourceSheet = true },
                )
        }
    }

    if (showSourceSheet) {
        val content = state as? BangumiDetailUiState.Content
        if (content != null) {
            SourceSheet(
                bangumiName = content.bangumi.displayName,
                episodes = content.episodes,
                onDismiss = { showSourceSheet = false },
                onPlay = { source, road, sort ->
                    showSourceSheet = false
                    onPlay(content.bangumi.id, source, road, sort)
                },
            )
        }
    }
}

private fun detailTitle(state: BangumiDetailUiState): String =
    (state as? BangumiDetailUiState.Content)?.bangumi?.displayName ?: "详情"

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailContent(
    content: BangumiDetailUiState.Content,
    modifier: Modifier = Modifier,
    onCollectStatus: (CollectStatus) -> Unit,
    onRemoveCollection: () -> Unit,
    onOpenSourceSheet: () -> Unit,
) {
    val spacing = LocalSpacing.current
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = spacing.xxl),
    ) {
        DetailHeader(
            bangumi = content.bangumi,
            collection = content.collection,
            isPending = content.isCollectionPending,
            onCollectStatus = onCollectStatus,
            onRemoveCollection = onRemoveCollection,
        )
        content.bangumi.summary?.let { SummarySection(it) }
        if (content.bangumi.tags.isNotEmpty()) {
            Column(Modifier.padding(horizontal = spacing.md, vertical = spacing.sm)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
                    content.bangumi.tags.forEach { tag ->
                        AssistChip(onClick = { }, label = { Text(tag) })
                    }
                }
            }
        }
        content.bangumi.rating?.let { rating ->
            if (rating.counts.size == RATING_BUCKETS && rating.total > 0) {
                Column(Modifier.padding(horizontal = spacing.md, vertical = spacing.sm)) {
                    SectionHeader(title = "评分分布")
                    Spacer(Modifier.height(spacing.sm))
                    RatingDistribution(counts = rating.counts)
                }
            }
        }
        Column(Modifier.padding(horizontal = spacing.md, vertical = spacing.sm)) {
            SectionHeader(title = "选集 · ${content.episodes.size} 话")
            Spacer(Modifier.height(spacing.sm))
            if (content.episodes.isEmpty()) {
                CompactState(
                    title = "暂无剧集信息",
                    message = "下拉数据稍后重试，或直接打开播放源。",
                    icon = StateBadges.Empty,
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    content.episodes.forEach { episode ->
                        SuggestionChip(
                            onClick = onOpenSourceSheet,
                            label = { Text(episode.displayNumber) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailHeader(
    bangumi: Bangumi,
    collection: CollectionEntry?,
    isPending: Boolean,
    onCollectStatus: (CollectStatus) -> Unit,
    onRemoveCollection: () -> Unit,
) {
    val spacing = LocalSpacing.current
    Row(Modifier.padding(spacing.md), horizontalArrangement = Arrangement.spacedBy(spacing.md)) {
        Box(
            Modifier
                .width(POSTER_WIDTH)
                .aspectRatio(POSTER_RATIO)
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        ) {
            if (bangumi.imageUrl != null) {
                AsyncImage(
                    model = bangumi.imageUrl,
                    contentDescription = bangumi.displayName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = bangumi.displayName,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (!bangumi.nameCn.isNullOrBlank() && bangumi.nameCn != bangumi.name) {
                Text(
                    text = bangumi.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.height(spacing.xs))
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                bangumi.rating?.score?.let {
                    Text(
                        text = "★ $it",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                bangumi.rank?.let {
                    Text(
                        text = "#$it",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            bangumi.airDate?.let {
                Text(
                    text = "放送：$it",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(spacing.sm))
            CollectButton(
                collection = collection,
                isPending = isPending,
                onCollectStatus = onCollectStatus,
                onRemoveCollection = onRemoveCollection,
            )
        }
    }
}

@Composable
private fun CollectButton(
    collection: CollectionEntry?,
    isPending: Boolean,
    onCollectStatus: (CollectStatus) -> Unit,
    onRemoveCollection: () -> Unit,
) {
    val spacing = LocalSpacing.current
    var menuExpanded by remember { mutableStateOf(false) }
    Box {
        Button(onClick = { menuExpanded = true }, enabled = !isPending) {
            if (isPending) {
                CharterLoadingIndicator(
                    indicatorSize = BUTTON_INDICATOR_SIZE,
                    color = LocalContentColor.current,
                )
                Spacer(Modifier.width(spacing.xs))
                Text("处理中…")
            } else {
                Text(collection?.status?.label ?: "追番")
                Spacer(Modifier.width(spacing.xs))
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null)
            }
        }
        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
            CollectStatus.entries.forEach { status ->
                DropdownMenuItem(
                    text = { Text(status.label) },
                    onClick = {
                        onCollectStatus(status)
                        menuExpanded = false
                    },
                    leadingIcon = {
                        if (collection?.status == status) Icon(Icons.Filled.Check, contentDescription = null)
                    },
                )
            }
            if (collection != null) {
                DropdownMenuItem(
                    text = {
                        Text("移除收藏", color = MaterialTheme.colorScheme.error)
                    },
                    onClick = {
                        onRemoveCollection()
                        menuExpanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun SummarySection(summary: String) {
    val spacing = LocalSpacing.current
    var expanded by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.padding(horizontal = spacing.md, vertical = spacing.sm)) {
        SectionHeader(title = "简介")
        Spacer(Modifier.height(spacing.xs))
        Text(
            text = summary,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = if (expanded) Int.MAX_VALUE else SUMMARY_COLLAPSED_LINES,
            overflow = TextOverflow.Ellipsis,
        )
        if (summary.length > SUMMARY_EXPAND_THRESHOLD) {
            TextButton(onClick = { expanded = !expanded }) {
                Text(if (expanded) "收起" else "展开")
            }
        }
    }
}

@Composable
private fun RatingDistribution(counts: List<Int>) {
    val spacing = LocalSpacing.current
    val max = (counts.maxOrNull() ?: 1).coerceAtLeast(1).toFloat()
    Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
        counts.indices.reversed().forEach { index ->
            val score = index + 1
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                Text(
                    text = "$score",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(RATING_BAR_WIDTH.dp),
                )
                LinearProgressIndicator(
                    progress = { counts[index] / max },
                    modifier = Modifier.weight(1f).height(spacing.sm),
                )
                Text(
                    text = "${counts[index]}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(RATING_COUNT_WIDTH.dp),
                )
            }
        }
    }
}

private const val RATING_BUCKETS = 10
private const val SUMMARY_EXPAND_THRESHOLD = 120
private const val BUTTON_INDICATOR_SIZE = 18
