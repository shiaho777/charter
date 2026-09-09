package dev.charter.feature.anime.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.charter.core.designsystem.components.EmptyState
import dev.charter.core.designsystem.components.ErrorState
import dev.charter.core.designsystem.components.LoadingState
import dev.charter.core.designsystem.tokens.LocalSpacing
import dev.charter.core.model.HistoryEntry
import dev.charter.feature.anime.components.MediaRow
import dev.charter.feature.anime.components.MediaRowCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onResumePlay: (bangumiId: Long, sourceName: String, roadName: String, episodeSort: Float) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val messages = viewModel.messages
    LaunchedEffect(messages) { messages.collect { snackbarHostState.showSnackbar(it) } }
    var showClearDialog by remember { mutableStateOf(false) }
    val hasEntries = state is HistoryUiState.Content

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            HistoryTopBar(
                hasEntries = hasEntries,
                isManaging = (state as? HistoryUiState.Content)?.isManaging == true,
                onBack = onBack,
                onToggleManaging = viewModel::toggleManaging,
                onClearAll = { showClearDialog = true },
            )
        },
    ) { innerPadding ->
        HistoryBody(
            state = state,
            viewModel = viewModel,
            onResumePlay = onResumePlay,
            modifier = Modifier.padding(innerPadding),
        )
    }

    if (showClearDialog) {
        ClearHistoryDialog(
            onDismiss = { showClearDialog = false },
            onConfirm = {
                viewModel.clearAll()
                showClearDialog = false
            },
        )
    }
}

@Composable
private fun HistoryBody(
    state: HistoryUiState,
    viewModel: HistoryViewModel,
    onResumePlay: (Long, String, String, Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    Column(modifier) {
        when (val s = state) {
            HistoryUiState.Loading -> LoadingState(label = "加载中…")
            is HistoryUiState.Error ->
                ErrorState(
                    title = "读取失败",
                    message = s.message,
                    retryLabel = "重试",
                    onRetry = viewModel::retry,
                )
            HistoryUiState.Empty ->
                EmptyState(
                    title = "还没有观看记录",
                    message = "在详情页选一个播放源开始观看，记录会出现在这里。",
                )
            is HistoryUiState.Content -> {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(spacing.md),
                    verticalArrangement = Arrangement.spacedBy(spacing.sm),
                ) {
                    items(s.entries, key = { it.bangumiId }) { entry ->
                        HistoryRow(
                            entry = entry,
                            isManaging = s.isManaging,
                            isSelected = entry.bangumiId in s.selectedIds,
                            onToggleSelected = { viewModel.toggleSelected(entry.bangumiId) },
                            onResume = {
                                onResumePlay(
                                    entry.bangumiId,
                                    entry.sourceName,
                                    entry.roadName,
                                    entry.lastEpisodeSort,
                                )
                            },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
                if (s.isManaging && s.selectedIds.isNotEmpty()) {
                    Button(
                        onClick = viewModel::removeSelected,
                        modifier = Modifier.fillMaxWidth().padding(spacing.md),
                    ) {
                        Text("删除所选（${s.selectedIds.size}）")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryTopBar(
    hasEntries: Boolean,
    isManaging: Boolean,
    onBack: () -> Unit,
    onToggleManaging: () -> Unit,
    onClearAll: () -> Unit,
) {
    TopAppBar(
        title = { Text("历史记录") },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
            }
        },
        actions = {
            if (hasEntries) {
                IconButton(onClick = onToggleManaging) {
                    Icon(
                        imageVector = if (isManaging) Icons.Filled.Check else Icons.Filled.Edit,
                        contentDescription = if (isManaging) "完成管理" else "管理",
                    )
                }
                IconButton(onClick = onClearAll) {
                    Icon(Icons.Filled.Delete, contentDescription = "清空全部")
                }
            }
        },
    )
}

@Composable
private fun ClearHistoryDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("清空观看历史？") },
        text = { Text("此操作不可撤销，所有观看记录与进度将被删除。") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("清空", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    )
}

@Composable
private fun HistoryRow(
    entry: HistoryEntry,
    isManaging: Boolean,
    isSelected: Boolean,
    onToggleSelected: () -> Unit,
    onResume: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val episodeLabel = entry.lastEpisodeName ?: "第${entry.lastEpisodeSort.toInt()}话"
    MediaRowCard(
        row =
            MediaRow(
                imageUrl = entry.imageUrl,
                title = entry.displayName,
                subtitle = "看到 $episodeLabel · ${entry.sourceName}",
                progress = entry.progressRatio,
            ),
        onClick = { if (isManaging) onToggleSelected() else onResume() },
        modifier = modifier,
        trailing = {
            if (isManaging) {
                Checkbox(checked = isSelected, onCheckedChange = { onToggleSelected() })
            } else {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}
