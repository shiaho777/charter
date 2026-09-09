package dev.charter.feature.anime.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.charter.core.common.result.Result
import dev.charter.core.data.repository.BangumiRepository
import dev.charter.core.data.repository.SearchHistoryRepository
import dev.charter.core.model.SearchHistoryEntry
import dev.charter.feature.anime.common.userMessage
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Search-as-you-type: debounce covers input bursts, and collectLatest gives
 * latest-wins semantics — a slow old query is cancelled when a newer one
 * settles, so stale results never overwrite fresh ones.
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel
    @Inject
    constructor(
        private val bangumiRepository: BangumiRepository,
        private val searchHistoryRepository: SearchHistoryRepository,
    ) : ViewModel() {
        private val _query = MutableStateFlow("")
        val query: StateFlow<String> = _query.asStateFlow()

        private val _uiState = MutableStateFlow(SearchUiState.Initial)
        val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

        private val history = MutableStateFlow<List<SearchHistoryEntry>>(emptyList())

        init {
            viewModelScope.launch {
                searchHistoryRepository.observeRecent().collect { entries ->
                    history.value = entries
                    _uiState.update { current ->
                        if (current is SearchUiState.Idle) {
                            current.copy(history = entries.toImmutableList())
                        } else {
                            current
                        }
                    }
                }
            }
            viewModelScope.launch {
                _query
                    .debounce(QUERY_DEBOUNCE_MS)
                    .collectLatest { settled -> runSearch(settled) }
            }
        }

        fun onQueryChange(value: String) {
            _query.value = value
        }

        fun clearQuery() {
            _query.value = ""
        }

        fun retry() {
            val current = _query.value
            if (current.isNotBlank()) {
                viewModelScope.launch { runSearch(current) }
            }
        }

        fun removeHistory(keyword: String) {
            viewModelScope.launch { searchHistoryRepository.remove(keyword) }
        }

        fun clearHistory() {
            viewModelScope.launch { searchHistoryRepository.clear() }
        }

        private suspend fun runSearch(settledQuery: String) {
            if (settledQuery.isBlank()) {
                _uiState.value = SearchUiState.Idle(history.value.toImmutableList())
            } else {
                searchHistoryRepository.add(settledQuery)
                _uiState.value = SearchUiState.Loading(settledQuery)
                performSearch(settledQuery)?.let { _uiState.value = it }
            }
        }

        /** Returns null when a newer query settled mid-search — stale results are dropped. */
        private suspend fun performSearch(settledQuery: String): SearchUiState? {
            val result = bangumiRepository.refreshSearch(settledQuery)
            if (_query.value != settledQuery) return null
            val failure = (result as? Result.Failure)?.error
            return if (failure != null) {
                SearchUiState.Error(failure.userMessage(), settledQuery)
            } else {
                val items = bangumiRepository.observeSearch(settledQuery).first()
                if (items.isEmpty()) {
                    SearchUiState.Empty(settledQuery)
                } else {
                    SearchUiState.Content(settledQuery, items.toImmutableList())
                }
            }
        }

        private companion object {
            const val QUERY_DEBOUNCE_MS = 400L
        }
    }
