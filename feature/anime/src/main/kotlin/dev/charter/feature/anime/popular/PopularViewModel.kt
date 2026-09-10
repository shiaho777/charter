package dev.charter.feature.anime.popular

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.charter.core.common.async.SingleFlight
import dev.charter.core.common.error.AppError
import dev.charter.core.common.result.onFailure
import dev.charter.core.common.result.onSuccess
import dev.charter.core.data.repository.BangumiRepository
import dev.charter.core.model.Season
import dev.charter.feature.anime.common.userMessage
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PopularViewModel
    @Inject
    constructor(
        private val repository: BangumiRepository,
    ) : ViewModel() {
        private val season = MutableStateFlow(Season.current())
        private val isRefreshing = MutableStateFlow(false)
        private val isLoadingMore = MutableStateFlow(false)
        private val canLoadMore = MutableStateFlow(true)
        private val loadError = MutableStateFlow<AppError?>(null)
        private val nextPage = MutableStateFlow(0)
        private val hasLoaded = MutableStateFlow(false)

        // Double pull-to-refresh joins the in-flight fetch instead of duplicating it.
        private val refreshFlight = SingleFlight()

        val uiState: StateFlow<PopularUiState> =
            combine(
                season,
                season.flatMapLatest { repository.observeSeason(it) },
                isRefreshing,
                isLoadingMore,
                canLoadMore,
            ) { current, items, refreshing, loadingMore, more ->
                PopularSnapshot(current, items, refreshing, loadingMore, more)
            }.combine(combine(loadError, hasLoaded) { error, loaded -> error to loaded }) { snapshot, errorAndLoaded ->
                snapshot.toUiState(errorAndLoaded.first, errorAndLoaded.second)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
                initialValue = PopularUiState.Loading,
            )

        init {
            loadPage(reset = true)
        }

        fun selectSeason(newSeason: Season) {
            if (season.value == newSeason) return
            season.value = newSeason
            loadError.value = null
            loadPage(reset = true)
        }

        fun refresh() {
            loadPage(reset = true)
        }

        fun loadMore() {
            if (!canLoadMore.value || isLoadingMore.value || isRefreshing.value) return
            loadPage(reset = false)
        }

        private fun loadPage(reset: Boolean) {
            // Flags flip synchronously so the season switch never flashes Empty.
            if (reset) isRefreshing.value = true else isLoadingMore.value = true
            viewModelScope.launch {
                if (reset) {
                    refreshFlight.run { fetchPage(reset = true) }
                } else {
                    fetchPage(reset = false)
                }
                isRefreshing.value = false
                isLoadingMore.value = false
                hasLoaded.value = true
            }
        }

        private suspend fun fetchPage(reset: Boolean) {
            val page = if (reset) 0 else nextPage.value
            repository
                .loadSeasonPage(season.value, page)
                .onSuccess { fetched ->
                    loadError.value = null
                    nextPage.value = page + 1
                    canLoadMore.value = fetched >= BangumiRepository.PAGE_SIZE
                }.onFailure { error ->
                    loadError.value = error
                    if (reset) canLoadMore.value = false
                }
        }

        private data class PopularSnapshot(
            val season: Season,
            val items: List<dev.charter.core.model.Bangumi>,
            val isRefreshing: Boolean,
            val isLoadingMore: Boolean,
            val canLoadMore: Boolean,
        )

        private fun PopularSnapshot.toUiState(
            error: AppError?,
            loaded: Boolean,
        ): PopularUiState =
            when {
                items.isNotEmpty() ->
                    PopularUiState.Content(
                        items = items.toImmutableList(),
                        season = season,
                        isRefreshing = isRefreshing,
                        isLoadingMore = isLoadingMore,
                        canLoadMore = canLoadMore,
                    )
                error != null && loaded -> PopularUiState.Error(error.userMessage(), season)
                !loaded || isRefreshing || isLoadingMore -> PopularUiState.Loading
                else -> PopularUiState.Empty(season)
            }

        private companion object {
            const val SUBSCRIPTION_TIMEOUT_MS = 5_000L
        }
    }
