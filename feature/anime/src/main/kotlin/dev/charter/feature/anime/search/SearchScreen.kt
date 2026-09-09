package dev.charter.feature.anime.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.charter.core.designsystem.components.EmptyState
import dev.charter.core.designsystem.components.ErrorState
import dev.charter.core.designsystem.components.LoadingState
import dev.charter.core.designsystem.tokens.LocalSpacing
import dev.charter.core.designsystem.tokens.WindowSizeClass
import dev.charter.core.designsystem.tokens.currentWindowSizeClass
import dev.charter.core.model.SearchHistoryEntry
import dev.charter.feature.anime.components.AnimePosterGrid
import dev.charter.feature.anime.components.SectionHeader
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onOpenBangumi: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val keyboard = LocalSoftwareKeyboardController.current

    Scaffold(modifier = modifier) { innerPadding ->
        Column(Modifier.padding(innerPadding).fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(
                    horizontal = LocalSpacing.current.sm,
                    vertical = LocalSpacing.current.sm,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
                OutlinedTextField(
                    value = query,
                    onValueChange = viewModel::onQueryChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    placeholder = { Text("搜索番剧名称") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotBlank()) {
                            IconButton(onClick = viewModel::clearQuery) {
                                Icon(Icons.Filled.Close, contentDescription = "清空")
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
                    shape = MaterialTheme.shapes.extraLarge,
                )
            }
            SearchBody(state = state, onOpenBangumi = onOpenBangumi, viewModel = viewModel)
        }
    }
}

@Composable
private fun SearchBody(
    state: SearchUiState,
    onOpenBangumi: (Long) -> Unit,
    viewModel: SearchViewModel,
) {
    val spacing = LocalSpacing.current
    val windowSizeClass = currentWindowSizeClass()
    val columns =
        when (windowSizeClass) {
            WindowSizeClass.Compact -> 3
            WindowSizeClass.Medium -> 5
            WindowSizeClass.Expanded -> 6
        }
    when (state) {
        is SearchUiState.Idle -> SearchHistoryPane(state.history, viewModel)
        is SearchUiState.Loading -> LoadingState(label = "搜索中…")
        is SearchUiState.Content ->
            Column(Modifier.fillMaxSize()) {
                Text(
                    text = "「${state.query}」共 ${state.items.size} 条结果",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = spacing.md, vertical = spacing.xs),
                )
                AnimePosterGrid(
                    items = state.items,
                    columns = columns,
                    onOpenBangumi = onOpenBangumi,
                    contentPadding = PaddingValues(spacing.md),
                )
            }
        is SearchUiState.Empty ->
            EmptyState(
                title = "没有找到「${state.query}」",
                message = "换个关键词试试，或检查拼写。",
            )
        is SearchUiState.Error ->
            ErrorState(
                title = "搜索失败",
                message = state.message,
                retryLabel = "重试",
                onRetry = viewModel::retry,
            )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchHistoryPane(
    history: ImmutableList<SearchHistoryEntry>,
    viewModel: SearchViewModel,
) {
    val spacing = LocalSpacing.current
    var managing by rememberSaveable { mutableStateOf(false) }
    if (history.isEmpty()) {
        EmptyState(
            title = "搜索番剧",
            message = "输入关键词开始搜索；也可以在推荐页按季度浏览。",
        )
        return
    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = spacing.md),
    ) {
        Spacer(Modifier.height(spacing.sm))
        SectionHeader(
            title = "搜索历史",
            trailing = {
                Row {
                    TextButton(onClick = { managing = !managing }) {
                        Text(if (managing) "完成" else "管理")
                    }
                    TextButton(onClick = viewModel::clearHistory) { Text("清空") }
                }
            },
        )
        Spacer(Modifier.height(spacing.sm))
        if (managing) {
            history.forEach { entry ->
                ListItem(
                    headlineContent = { Text(entry.keyword) },
                    trailingContent = {
                        IconButton(onClick = { viewModel.removeHistory(entry.keyword) }) {
                            Icon(Icons.Filled.Close, contentDescription = "删除「${entry.keyword}」")
                        }
                    },
                )
            }
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                verticalArrangement = Arrangement.spacedBy(spacing.sm),
            ) {
                history.forEach { entry ->
                    AssistChip(
                        onClick = { viewModel.onQueryChange(entry.keyword) },
                        label = { Text(entry.keyword) },
                    )
                }
            }
        }
        Spacer(Modifier.height(spacing.lg))
    }
}
