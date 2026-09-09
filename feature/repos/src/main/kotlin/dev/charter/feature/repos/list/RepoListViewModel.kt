package dev.charter.feature.repos.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.charter.core.common.async.SingleFlight
import dev.charter.core.data.repository.RepoRepository
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class RepoListViewModel
    @Inject
    constructor(
        private val repository: RepoRepository,
    ) : ViewModel() {
        private val _query = MutableStateFlow(DEFAULT_QUERY)
        val query: StateFlow<String> = _query.asStateFlow()

        private val isRefreshing = MutableStateFlow(false)

        // Double pull-to-refresh joins the in-flight fetch instead of
        private val refreshFlight = SingleFlight()

        /** Scroll position, restored when the user returns to this screen. */
        var scrollOffset: Int = 0

        val uiState: StateFlow<RepoListUiState> =
            _query
                .debounce(QUERY_DEBOUNCE_MS)
                .flatMapLatest { q ->
                    repository
                        .observeRepos(q)
                        .map { repos ->
                            if (repos.isEmpty()) {
                                RepoListUiState.Empty
                            } else {
                                RepoListUiState.Content(repos.toImmutableList())
                            }
                        }.onStart { emit(RepoListUiState.Loading) }
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
                    initialValue = RepoListUiState.Loading,
                )

        init {
            refresh()
        }

        fun onQueryChange(newQuery: String) {
            _query.value = newQuery.trim().ifBlank { DEFAULT_QUERY }
        }

        fun refresh() {
            viewModelScope.launch {
                isRefreshing.value = true
                refreshFlight.run {
                    repository.refresh(_query.value)
                }
                isRefreshing.value = false
            }
        }

        companion object {
            const val DEFAULT_QUERY = "kotlin compose"
            const val QUERY_DEBOUNCE_MS = 400L
            const val SUBSCRIPTION_TIMEOUT_MS = 5_000L
        }
    }
