package dev.charter.feature.repos.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.charter.core.data.repository.RepoRepository
import dev.charter.core.model.Repo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface RepoDetailUiState {
    data object Loading : RepoDetailUiState

    data class Content(
        val repo: Repo,
    ) : RepoDetailUiState

    data object NotFound : RepoDetailUiState
}

@HiltViewModel
class RepoDetailViewModel
    @Inject
    constructor(
        private val repository: RepoRepository,
    ) : ViewModel() {
        private val repoId = MutableStateFlow(-1L)

        fun setRepoId(id: Long) {
            repoId.value = id
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        val uiState: StateFlow<RepoDetailUiState> =
            repoId
                .flatMapLatest { id ->
                    repository.observeRepo(id).map { repo ->
                        if (repo == null) RepoDetailUiState.NotFound else RepoDetailUiState.Content(repo)
                    }
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = RepoDetailUiState.Loading,
                )
    }
