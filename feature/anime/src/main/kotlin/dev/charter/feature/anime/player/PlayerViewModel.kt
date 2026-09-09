package dev.charter.feature.anime.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.charter.core.common.async.SingleFlight
import dev.charter.core.common.error.AppError
import dev.charter.core.common.result.onFailure
import dev.charter.core.common.result.onSuccess
import dev.charter.core.data.repository.BangumiRepository
import dev.charter.core.data.repository.HistoryRepository
import dev.charter.core.data.source.PlaySourceAggregator
import dev.charter.core.model.HistoryEntry
import dev.charter.feature.anime.common.userMessage
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel
    @Inject
    constructor(
        private val bangumiRepository: BangumiRepository,
        private val historyRepository: HistoryRepository,
        private val aggregator: PlaySourceAggregator,
    ) : ViewModel() {
        private data class Playback(
            val bangumiId: Long,
            val sourceName: String,
            val roadName: String,
            val episodeSort: Float,
        )

        private val playback = MutableStateFlow<Playback?>(null)
        private val loadError = MutableStateFlow<AppError?>(null)
        private val refreshFlight = SingleFlight()

        val uiState: StateFlow<PlayerUiState> =
            playback
                .filterNotNull()
                .flatMapLatest { initial ->
                    combine(
                        bangumiRepository.observeDetail(initial.bangumiId),
                        bangumiRepository.observeEpisodes(initial.bangumiId),
                        historyRepository.observeOne(initial.bangumiId),
                        playback,
                        loadError,
                    ) { bangumi, episodes, history, current, error ->
                        when {
                            current == null -> PlayerUiState.Loading
                            bangumi == null && error != null -> PlayerUiState.Error(error.userMessage())
                            bangumi == null -> PlayerUiState.Loading
                            else ->
                                PlayerUiState.Content(
                                    bangumi = bangumi,
                                    roads = aggregator.roadsFor(current.sourceName, episodes).toImmutableList(),
                                    selectedRoad = current.roadName,
                                    selectedSort = current.episodeSort,
                                    history = history,
                                )
                        }
                    }
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
                    initialValue = PlayerUiState.Loading,
                )

        fun setPlayback(
            bangumiId: Long,
            sourceName: String,
            roadName: String,
            episodeSort: Float,
        ) {
            val next = Playback(bangumiId, sourceName, roadName, episodeSort)
            val previous = playback.value
            if (previous == next) return
            playback.value = next
            if (previous?.bangumiId != bangumiId) {
                loadError.value = null
                refreshSubject(bangumiId)
            }
        }

        fun selectEpisode(sort: Float) {
            playback.update { it?.copy(episodeSort = sort) }
            recordWatch(sort, STARTED_RATIO)
        }

        fun selectRoad(roadName: String) {
            playback.update { it?.copy(roadName = roadName) }
        }

        fun markEpisodeFinished() {
            val current = playback.value ?: return
            recordWatch(current.episodeSort, FINISHED_RATIO)
        }

        fun retry() {
            playback.value?.bangumiId?.let { refreshSubject(it) }
        }

        private fun refreshSubject(bangumiId: Long) {
            viewModelScope.launch {
                refreshFlight
                    .run { bangumiRepository.refreshDetail(bangumiId) }
                    .onSuccess { loadError.value = null }
                    .onFailure { loadError.value = it }
            }
        }

        private fun recordWatch(
            sort: Float,
            ratio: Float,
        ) {
            val content = uiState.value as? PlayerUiState.Content ?: return
            val bangumi = content.bangumi
            val current = playback.value ?: return
            val episodeTitle =
                content.roads
                    .firstOrNull { it.name == current.roadName }
                    ?.episodes
                    ?.firstOrNull { it.sort == sort }
                    ?.title
            viewModelScope.launch {
                historyRepository.recordWatch(
                    HistoryEntry(
                        bangumiId = bangumi.id,
                        name = bangumi.name,
                        nameCn = bangumi.nameCn,
                        imageUrl = bangumi.imageUrl,
                        sourceName = current.sourceName,
                        roadName = current.roadName,
                        lastEpisodeSort = sort,
                        lastEpisodeName = episodeTitle,
                        progressRatio = ratio,
                        updatedAt = System.currentTimeMillis(),
                    ),
                )
            }
        }

        private companion object {
            const val SUBSCRIPTION_TIMEOUT_MS = 5_000L
            const val STARTED_RATIO = 0f
            const val FINISHED_RATIO = 1f
        }
    }
