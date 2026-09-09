package dev.charter.feature.repos.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.charter.core.designsystem.components.EmptyState
import dev.charter.core.designsystem.components.LoadingState
import dev.charter.core.designsystem.tokens.LocalSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepoDetailScreen(
    repoId: Long,
    modifier: Modifier = Modifier,
    viewModel: RepoDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val spacing = LocalSpacing.current

    LaunchedEffect(repoId) {
        viewModel.setRepoId(repoId)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Repository") }) },
        modifier = modifier,
    ) { innerPadding ->
        when (val s = state) {
            RepoDetailUiState.Loading -> LoadingState(Modifier.padding(innerPadding))
            RepoDetailUiState.NotFound ->
                EmptyState(
                    title = "Repository not found",
                    message = "It may have been removed from the cache.",
                    modifier = Modifier.padding(innerPadding),
                )
            is RepoDetailUiState.Content ->
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(spacing.md),
                ) {
                    Text(
                        text = s.repo.fullName,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    s.repo.description?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(spacing.lg)) {
                        s.repo.language?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Text(
                            text = "★ ${s.repo.stars}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "⑂ ${s.repo.forks}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
        }
    }
}
