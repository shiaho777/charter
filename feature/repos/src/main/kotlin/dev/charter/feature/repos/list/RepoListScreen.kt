package dev.charter.feature.repos.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.charter.core.designsystem.components.EmptyState
import dev.charter.core.designsystem.components.ErrorState
import dev.charter.core.designsystem.components.LoadingState
import dev.charter.core.designsystem.motion.pressableClickable
import dev.charter.core.designsystem.tokens.LocalSpacing
import dev.charter.core.designsystem.tokens.WindowSizeClass
import dev.charter.core.designsystem.tokens.currentWindowSizeClass
import dev.charter.core.model.Repo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepoListScreen(
    onOpenRepo: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RepoListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val spacing = LocalSpacing.current
    val windowSizeClass = currentWindowSizeClass()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Charter") },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.md, vertical = spacing.sm),
                singleLine = true,
                label = { Text("Search GitHub repositories") },
            )

            when (val s = state) {
                RepoListUiState.Loading -> LoadingState()
                is RepoListUiState.Error ->
                    ErrorState(
                        message = s.message,
                        onRetry = viewModel::refresh,
                    )
                RepoListUiState.Empty ->
                    EmptyState(
                        title = "No repositories yet",
                        message = "Pull down to refresh, or try another query.",
                    )
                is RepoListUiState.Content ->
                    PullToRefreshBox(
                        isRefreshing = s.isRefreshing,
                        onRefresh = viewModel::refresh,
                    ) {
                        // Adaptive columns: 2 on phones, more as width grows.
                        val columns =
                            when (windowSizeClass) {
                                WindowSizeClass.Compact -> 2
                                WindowSizeClass.Medium -> 3
                                WindowSizeClass.Expanded -> 4
                            }
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(columns),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(spacing.md),
                            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                            verticalArrangement = Arrangement.spacedBy(spacing.sm),
                        ) {
                            items(
                                items = s.repos,
                                key = { it.id },
                            ) { repo ->
                                RepoCard(
                                    repo = repo,
                                    onClick = { onOpenRepo(repo.id) },
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
private fun RepoCard(
    repo: Repo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // pressableClickable = uniform press feedback (0.96 dip, no-bounce spring);
    // Card(onClick=) hardcodes ripple, so the clickable is owned here.
    Card(
        shape = MaterialTheme.shapes.large,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
        modifier =
            modifier
                .fillMaxWidth()
                .pressableClickable(onClick = onClick),
    ) {
        Column(Modifier.padding(LocalSpacing.current.md)) {
            Text(
                text = repo.fullName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            repo.description?.let {
                Spacer(Modifier.height(LocalSpacing.current.xs))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
            }
            Spacer(Modifier.height(LocalSpacing.current.sm))
            Row(verticalAlignment = Alignment.CenterVertically) {
                repo.language?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = "★ ${repo.stars}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
