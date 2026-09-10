package dev.charter.feature.anime.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.charter.core.common.error.AppError
import dev.charter.core.common.result.Result
import dev.charter.core.common.result.onFailure
import dev.charter.core.data.repository.CollectionRepository
import dev.charter.core.model.Bangumi
import dev.charter.core.model.BangumiRating
import dev.charter.core.model.CollectStatus
import dev.charter.core.model.CollectionEntry
import dev.charter.feature.anime.common.userMessage
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollectionViewModel
    @Inject
    constructor(
        private val repository: CollectionRepository,
    ) : ViewModel() {
        private val filter = MutableStateFlow<CollectStatus?>(null)
        private val pendingIds = MutableStateFlow<Set<Long>>(emptySet())
        private val reloadKey = MutableStateFlow(0)

        private val _messages = MutableSharedFlow<String>(extraBufferCapacity = MESSAGE_BUFFER)
        val messages: SharedFlow<String> = _messages.asSharedFlow()

        val uiState: StateFlow<CollectionUiState> =
            reloadKey
                .flatMapLatest {
                    combine(repository.observeAll(), filter, pendingIds) { entries, currentFilter, pending ->
                        when {
                            entries.isEmpty() -> CollectionUiState.Empty(currentFilter)
                            else -> {
                                val visible =
                                    if (currentFilter == null) {
                                        entries
                                    } else {
                                        entries.filter { it.status == currentFilter }
                                    }
                                CollectionUiState.Content(
                                    entries = visible.toImmutableList(),
                                    filter = currentFilter,
                                    pendingIds = pending.toList().toImmutableList(),
                                )
                            }
                        }
                    }
                }.catch { e ->
                    emit(CollectionUiState.Error(AppError.fromThrowable(e).userMessage()))
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
                    initialValue = CollectionUiState.Loading,
                )

        fun setFilter(status: CollectStatus?) {
            filter.value = status
        }

        fun setStatus(
            entry: CollectionEntry,
            status: CollectStatus,
        ) {
            write(entry.bangumiId) { repository.setStatus(entry.toBangumiStub(), status) }
        }

        fun remove(entry: CollectionEntry) {
            write(entry.bangumiId) { repository.remove(entry.bangumiId) }
        }

        fun retry() {
            reloadKey.value += 1
        }

        private fun write(
            bangumiId: Long,
            operation: suspend () -> Result<Unit>,
        ) {
            pendingIds.update { it + bangumiId }
            viewModelScope.launch {
                operation().onFailure { _messages.tryEmit(it.userMessage()) }
                pendingIds.update { it - bangumiId }
            }
        }

        private companion object {
            const val SUBSCRIPTION_TIMEOUT_MS = 5_000L
            const val MESSAGE_BUFFER = 1
        }
    }

/** Collection rows carry denormalized display fields; rebuild a stub for writes. */
private fun CollectionEntry.toBangumiStub() =
    Bangumi(
        id = bangumiId,
        name = name,
        nameCn = nameCn,
        summary = null,
        airDate = null,
        airWeekday = null,
        rank = null,
        imageUrl = imageUrl,
        tags = emptyList(),
        rating = ratingScore?.let { BangumiRating(it, 0, null, emptyList()) },
    )
