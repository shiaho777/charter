package dev.charter.feature.anime.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.charter.core.common.error.AppError
import dev.charter.core.common.result.onFailure
import dev.charter.core.data.repository.HistoryRepository
import dev.charter.core.model.HistoryEntry
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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel
    @Inject
    constructor(
        private val repository: HistoryRepository,
    ) : ViewModel() {
        private val isManaging = MutableStateFlow(false)
        private val selectedIds = MutableStateFlow<Set<Long>>(emptySet())
        private val reloadKey = MutableStateFlow(0)

        private val _messages = MutableSharedFlow<String>(extraBufferCapacity = MESSAGE_BUFFER)
        val messages: SharedFlow<String> = _messages.asSharedFlow()

        val uiState: StateFlow<HistoryUiState> =
            reloadKey
                .flatMapLatest {
                    combine(repository.observeAll(), isManaging, selectedIds) { entries, managing, selected ->
                        if (entries.isEmpty()) {
                            HistoryUiState.Empty
                        } else {
                            HistoryUiState.Content(
                                entries = entries.toImmutableList(),
                                isManaging = managing,
                                selectedIds = selected.toList().toImmutableList(),
                            )
                        }
                    }
                }.catch { e ->
                    emit(HistoryUiState.Error(AppError.fromThrowable(e).userMessage()))
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
                    initialValue = HistoryUiState.Loading,
                )

        fun toggleManaging() {
            isManaging.value = !isManaging.value
            selectedIds.value = emptySet()
        }

        fun toggleSelected(bangumiId: Long) {
            selectedIds.value =
                if (bangumiId in selectedIds.value) {
                    selectedIds.value - bangumiId
                } else {
                    selectedIds.value + bangumiId
                }
        }

        fun removeSelected() {
            val ids = selectedIds.value.toList()
            viewModelScope.launch {
                ids.forEach { id ->
                    repository.remove(id).onFailure { _messages.tryEmit(it.userMessage()) }
                }
                selectedIds.value = emptySet()
                isManaging.value = false
            }
        }

        fun remove(entry: HistoryEntry) {
            viewModelScope.launch {
                repository.remove(entry.bangumiId).onFailure { _messages.tryEmit(it.userMessage()) }
            }
        }

        fun clearAll() {
            viewModelScope.launch {
                repository.clearAll().onFailure { _messages.tryEmit(it.userMessage()) }
            }
        }

        fun retry() {
            reloadKey.value += 1
        }

        private companion object {
            const val SUBSCRIPTION_TIMEOUT_MS = 5_000L
            const val MESSAGE_BUFFER = 1
        }
    }
