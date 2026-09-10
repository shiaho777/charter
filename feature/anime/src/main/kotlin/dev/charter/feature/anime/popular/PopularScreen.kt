package dev.charter.feature.anime.popular

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.charter.core.designsystem.components.CharterLoadingIndicator
import dev.charter.core.designsystem.components.EmptyState
import dev.charter.core.designsystem.components.ErrorState
import dev.charter.core.designsystem.components.LoadingState
import dev.charter.core.designsystem.tokens.LocalSpacing
import dev.charter.core.designsystem.tokens.WindowSizeClass
import dev.charter.core.designsystem.tokens.currentWindowSizeClass
import dev.charter.core.model.Season
import dev.charter.feature.anime.components.AnimePosterGrid

private const val LOAD_MORE_THRESHOLD = 6

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PopularScreen(
    onOpenBangumi: (Long) -> Unit,
    onOpenSearch: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PopularViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val spacing = LocalSpacing.current
    val windowSizeClass = currentWindowSizeClass()
    var showSeasonSheet by rememberSaveable { mutableStateOf(false) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("推荐") },
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = onOpenSearch) {
                        Icon(Icons.Filled.Search, contentDescription = "搜索")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            SeasonBar(
                label = seasonLabel(state),
                onClick = { showSeasonSheet = true },
                modifier = Modifier.padding(horizontal = spacing.md, vertical = spacing.sm),
            )
            PopularBody(
                state = state,
                windowSizeClass = windowSizeClass,
                onOpenBangumi = onOpenBangumi,
                onRefresh = viewModel::refresh,
                onLoadMore = viewModel::loadMore,
            )
        }
    }

    if (showSeasonSheet) {
        val current = seasonOf(state)
        SeasonSheet(
            currentSeason = current,
            onDismiss = { showSeasonSheet = false },
            onSelect = {
                viewModel.selectSeason(it)
                showSeasonSheet = false
            },
        )
    }
}

@Composable
private fun SeasonBar(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        FilledTonalButton(onClick = onClick) {
            Text(label)
            Spacer(Modifier.width(LocalSpacing.current.xs))
            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null)
        }
        Spacer(Modifier.width(LocalSpacing.current.sm))
        Text(
            text = "当季新番",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PopularBody(
    state: PopularUiState,
    windowSizeClass: WindowSizeClass,
    onOpenBangumi: (Long) -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
) {
    val spacing = LocalSpacing.current
    // Adaptive poster columns: phones 3, tablets 5, desk-class 6 — the prototype's density ladder.
    val columns =
        when (windowSizeClass) {
            WindowSizeClass.Compact -> 3
            WindowSizeClass.Medium -> 5
            WindowSizeClass.Expanded -> 6
        }
    when (state) {
        PopularUiState.Loading -> LoadingState(label = "加载中…")
        is PopularUiState.Error ->
            ErrorState(
                title = "加载失败",
                message = state.message,
                retryLabel = "重试",
                onRetry = onRefresh,
            )
        is PopularUiState.Empty ->
            EmptyState(
                title = "「${state.season.label}」还没有条目",
                message = "换个季度看看，或下拉刷新重试。",
            )
        is PopularUiState.Content ->
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                val gridState = rememberLazyGridState()
                LaunchedEffect(gridState, state.items.size, state.canLoadMore, state.isLoadingMore) {
                    snapshotFlow {
                        gridState.layoutInfo.visibleItemsInfo
                            .lastOrNull()
                            ?.index ?: 0
                    }.collect { last ->
                        if (last >= state.items.size - LOAD_MORE_THRESHOLD) onLoadMore()
                    }
                }
                AnimePosterGrid(
                    items = state.items,
                    columns = columns,
                    onOpenBangumi = onOpenBangumi,
                    contentPadding = PaddingValues(spacing.md),
                    footer = {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            GridFooter(
                                isLoadingMore = state.isLoadingMore,
                                canLoadMore = state.canLoadMore,
                                total = state.items.size,
                            )
                        }
                    },
                )
            }
    }
}

@Composable
private fun GridFooter(
    isLoadingMore: Boolean,
    canLoadMore: Boolean,
    total: Int,
) {
    val spacing = LocalSpacing.current
    Row(
        Modifier.fillMaxWidth().padding(spacing.md).height(spacing.xxl),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when {
            isLoadingMore -> {
                CharterLoadingIndicator(indicatorSize = INDICATOR_SIZE, animated = true)
                Spacer(Modifier.width(spacing.sm))
                Text(
                    "加载中…",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            !canLoadMore && total > 0 ->
                Text(
                    text = "已显示全部 $total 部",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
        }
    }
}

private fun seasonLabel(state: PopularUiState): String = seasonOf(state).label

private fun seasonOf(state: PopularUiState) =
    when (state) {
        is PopularUiState.Content -> state.season
        is PopularUiState.Empty -> state.season
        is PopularUiState.Error -> state.season
        PopularUiState.Loading -> Season.current()
    }

private const val INDICATOR_SIZE = 24
