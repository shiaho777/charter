package dev.charter.feature.anime.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.charter.core.common.async.SingleFlight
import dev.charter.core.common.error.AppError
import dev.charter.core.common.result.onFailure
import dev.charter.core.common.result.onSuccess
import dev.charter.core.data.repository.BangumiRepository
import dev.charter.core.data.repository.CollectionRepository
import dev.charter.core.model.CollectStatus
import dev.charter.feature.anime.common.userMessage
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BangumiDetailViewModel
    @Inject
    constructor(
        private val bangumiRepository: BangumiRepository,
        private val collectionRepository: CollectionRepository,
    ) : ViewModel() {
        private val bangumiId = MutableStateFlow(UNSET_ID)
        private val loadError = MutableStateFlow<AppError?>(null)
        private val collectionPending = MutableStateFlow(false)
        private val refreshFlight = SingleFlight()

        /** One-shot user messages (collection write failures) — events, not state. */
        private val _messages = MutableSharedFlow<String>(extraBufferCapacity = MESSAGE_BUFFER)
        val messages: SharedFlow<String> = _messages.asSharedFlow()

        val uiState: StateFlow<BangumiDetailUiState> =
            bangumiId
                .filter { it != UNSET_ID }
                .flatMapLatest { id ->
                    combine(
                        bangumiRepository.observeDetail(id),
                        bangumiRepository.observeEpisodes(id),
                        collectionRepository.observeOne(id),
                        loadError,
                        collectionPending,
                    ) { detail, episodes, collection, error, pending ->
                        when {
                            detail == null && error != null -> BangumiDetailUiState.Error(error.userMessage())
                            detail == null -> BangumiDetailUiState.Loading
                            else ->
                                BangumiDetailUiState.Content(
                                    bangumi = detail,
                                    episodes = episodes.toImmutableList(),
                                    collection = collection,
                                    isCollectionPending = pending,
                                )
                        }
                    }
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
                    initialValue = BangumiDetailUiState.Loading,
                )

        fun setBangumiId(id: Long) {
            if (bangumiId.value == id) return
            bangumiId.value = id
            loadError.value = null
            refresh()
        }

        fun refresh() {
            val id = bangumiId.value
            if (id == UNSET_ID) return
            viewModelScope.launch {
                refreshFlight
                    .run { bangumiRepository.refreshDetail(id) }
                    .onSuccess { loadError.value = null }
                    .onFailure { error ->
                        // With cached detail on screen the failure stays silent;
                        // without cache the error state surfaces via loadError.
                        loadError.value = error
                    }
            }
        }

        fun setCollectStatus(status: CollectStatus) {
            val bangumi = (uiState.value as? BangumiDetailUiState.Content)?.bangumi ?: return
            collectionPending.value = true
            viewModelScope.launch {
                collectionRepository
                    .setStatus(bangumi, status)
                    .onFailure { _messages.tryEmit(it.userMessage()) }
                collectionPending.value = false
            }
        }

        fun removeCollection() {
            val bangumi = (uiState.value as? BangumiDetailUiState.Content)?.bangumi ?: return
            collectionPending.value = true
            viewModelScope.launch {
                collectionRepository
                    .remove(bangumi.id)
                    .onFailure { _messages.tryEmit(it.userMessage()) }
                collectionPending.value = false
            }
        }

        private companion object {
            const val UNSET_ID = 0L
            const val SUBSCRIPTION_TIMEOUT_MS = 5_000L
            const val MESSAGE_BUFFER = 1
        }
    }
