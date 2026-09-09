package dev.charter.feature.anime.collection

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import dev.charter.core.designsystem.components.CharterLoadingIndicator
import dev.charter.core.designsystem.components.EmptyState
import dev.charter.core.designsystem.components.ErrorState
import dev.charter.core.designsystem.components.LoadingState
import dev.charter.core.designsystem.tokens.LocalSpacing
import dev.charter.core.model.CollectStatus
import dev.charter.core.model.CollectionEntry
import dev.charter.feature.anime.components.MediaRow
import dev.charter.feature.anime.components.MediaRowCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionScreen(
    onOpenBangumi: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CollectionViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val messages = viewModel.messages
    LaunchedEffect(messages) { messages.collect { snackbarHostState.showSnackbar(it) } }
    val spacing = LocalSpacing.current

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopAppBar(title = { Text("追番") }) },
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            FilterRow(
                filter = (state as? CollectionUiState.Content)?.filter ?: (state as? CollectionUiState.Empty)?.filter,
                onSelect = viewModel::setFilter,
            )
            when (val s = state) {
                CollectionUiState.Loading -> LoadingState(label = "加载中…")
                is CollectionUiState.Error ->
                    ErrorState(
                        title = "读取失败",
                        message = s.message,
                        retryLabel = "重试",
                        onRetry = viewModel::retry,
                    )
                is CollectionUiState.Empty ->
                    EmptyState(
                        title = if (s.filter == null) "还没有追任何番" else "「${s.filter.label}」分类下还没有条目",
                        message = if (s.filter == null) "去推荐页或时间表逛逛，在详情页点「追番」加入收藏。" else "换个分类看看，或长按条目调整分类。",
                    )
                is CollectionUiState.Content ->
                    if (s.entries.isEmpty()) {
                        EmptyState(
                            title = "「${s.filter?.label}」分类下还没有条目",
                            message = "换个分类看看，或长按条目调整分类。",
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(spacing.md),
                            verticalArrangement = Arrangement.spacedBy(spacing.sm),
                        ) {
                            items(s.entries, key = { it.bangumiId }) { entry ->
                                CollectionRow(
                                    entry = entry,
                                    isPending = entry.bangumiId in s.pendingIds,
                                    onClick = { onOpenBangumi(entry.bangumiId) },
                                    onEditStatus = viewModel,
                                    modifier = Modifier.animateItem(),
                                )
                            }
                        }
                    }
            }
        }
    }
}

@Composable
private fun FilterRow(
    filter: CollectStatus?,
    onSelect: (CollectStatus?) -> Unit,
) {
    val spacing = LocalSpacing.current
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = spacing.md, vertical = spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
    ) {
        FilterChip(selected = filter == null, onClick = { onSelect(null) }, label = { Text("全部") })
        CollectStatus.entries.forEach { status ->
            FilterChip(
                selected = filter == status,
                onClick = { onSelect(status) },
                label = { Text(status.label) },
            )
        }
    }
}

@Composable
private fun CollectionRow(
    entry: CollectionEntry,
    isPending: Boolean,
    onClick: () -> Unit,
    onEditStatus: CollectionViewModel,
    modifier: Modifier = Modifier,
) {
    var showDialog by remember(entry.bangumiId) { mutableStateOf(false) }
    val subtitle =
        buildString {
            entry.ratingScore?.let { append("★ $it · ") }
            append(entry.status.label)
        }
    MediaRowCard(
        row = MediaRow(imageUrl = entry.imageUrl, title = entry.displayName, subtitle = subtitle),
        onClick = onClick,
        onLongClick = { showDialog = true },
        modifier = modifier,
        trailing = {
            if (isPending) {
                CharterLoadingIndicator(indicatorSize = ROW_INDICATOR_SIZE)
            } else {
                AssistChip(onClick = { showDialog = true }, label = { Text(entry.status.label) })
            }
        },
    )
    if (showDialog) {
        StatusDialog(
            entry = entry,
            onDismiss = { showDialog = false },
            onSetStatus = { status ->
                onEditStatus.setStatus(entry, status)
                showDialog = false
            },
            onRemove = {
                onEditStatus.remove(entry)
                showDialog = false
            },
        )
    }
}

@Composable
private fun StatusDialog(
    entry: CollectionEntry,
    onDismiss: () -> Unit,
    onSetStatus: (CollectStatus) -> Unit,
    onRemove: () -> Unit,
) {
    val spacing = LocalSpacing.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("调整「${entry.displayName}」") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                CollectStatus.entries.forEach { status ->
                    TextButton(onClick = { onSetStatus(status) }, modifier = Modifier.fillMaxWidth()) {
                        Text(status.label)
                    }
                }
                Spacer(Modifier.height(spacing.xs))
                TextButton(onClick = onRemove, modifier = Modifier.fillMaxWidth()) {
                    Text("移除收藏", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = { },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}

private const val ROW_INDICATOR_SIZE = 20
