package dev.charter.feature.anime.timeline

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.charter.core.designsystem.components.EmptyState
import dev.charter.core.designsystem.components.ErrorState
import dev.charter.core.designsystem.components.LoadingState
import dev.charter.core.designsystem.motion.Transitions
import dev.charter.core.designsystem.tokens.LocalSpacing
import dev.charter.core.designsystem.tokens.WindowSizeClass
import dev.charter.core.designsystem.tokens.currentWindowSizeClass
import dev.charter.core.model.CalendarDay
import dev.charter.feature.anime.components.AnimePosterGrid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    onOpenBangumi: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TimelineViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val windowSizeClass = currentWindowSizeClass()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("时间表") },
                actions = {
                    val following =
                        (state as? TimelineUiState.Content)?.onlyFollowing
                            ?: (state as? TimelineUiState.Empty)?.onlyFollowing ?: false
                    IconButton(onClick = viewModel::toggleOnlyFollowing) {
                        val tint =
                            if (following) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        Icon(
                            imageVector = if (following) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "只看追番",
                            tint = tint,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            WeekdayTabs(state = state, onSelect = viewModel::selectDay)
            TimelineBody(
                state = state,
                windowSizeClass = windowSizeClass,
                onOpenBangumi = onOpenBangumi,
                onRefresh = viewModel::refresh,
            )
        }
    }
}

@Composable
private fun WeekdayTabs(
    state: TimelineUiState,
    onSelect: (Int) -> Unit,
) {
    val spacing = LocalSpacing.current
    val days = (state as? TimelineUiState.Content)?.days
    val selected =
        when (state) {
            is TimelineUiState.Content -> state.selectedWeekday
            is TimelineUiState.Empty -> state.selectedWeekday
            else -> CalendarDay.todayWeekday()
        }
    if (days == null) return
    ScrollableTabRow(
        selectedTabIndex = (selected - 1).coerceIn(0, days.lastIndex),
        edgePadding = spacing.md,
        divider = { },
    ) {
        days.forEach { day ->
            Tab(
                selected = day.weekday == selected,
                onClick = { onSelect(day.weekday) },
                text = { Text("${day.label} ${day.items.size}") },
            )
        }
    }
}

@Composable
private fun TimelineBody(
    state: TimelineUiState,
    windowSizeClass: WindowSizeClass,
    onOpenBangumi: (Long) -> Unit,
    onRefresh: () -> Unit,
) {
    val spacing = LocalSpacing.current
    val columns =
        when (windowSizeClass) {
            WindowSizeClass.Compact -> 2
            WindowSizeClass.Medium -> 4
            WindowSizeClass.Expanded -> 5
        }
    when (state) {
        TimelineUiState.Loading -> LoadingState(label = "加载中…")
        is TimelineUiState.Error ->
            ErrorState(
                title = "加载失败",
                message = state.message,
                retryLabel = "重试",
                onRetry = onRefresh,
            )
        is TimelineUiState.Empty ->
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                EmptyState(
                    title = if (state.onlyFollowing) "本周没有追番放送" else "本周时间表是空的",
                    message = if (state.onlyFollowing) "去推荐页收藏几部番，或关掉「只看追番」。" else "下拉刷新重试。",
                )
            }
        is TimelineUiState.Content -> {
            val day = state.days.firstOrNull { it.weekday == state.selectedWeekday } ?: state.days.first()
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                AnimatedContent(
                    targetState = day,
                    transitionSpec = { Transitions.sharedAxisX(forward = targetState.weekday > initialState.weekday) },
                    label = "timelineDay",
                    modifier = Modifier.fillMaxSize(),
                ) { current ->
                    if (current.items.isEmpty()) {
                        EmptyState(
                            title = if (state.onlyFollowing) "${current.label}没有追番放送" else "${current.label}没有放送条目",
                            message = "换一天看看。",
                        )
                    } else {
                        AnimePosterGrid(
                            items = current.items,
                            columns = columns,
                            onOpenBangumi = onOpenBangumi,
                            contentPadding = PaddingValues(spacing.md),
                        )
                    }
                }
            }
        }
    }
}
