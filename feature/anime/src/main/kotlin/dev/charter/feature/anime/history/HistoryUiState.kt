package dev.charter.feature.anime.history

import dev.charter.core.model.HistoryEntry
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/** Single immutable UI state — the UDF contract every feature screen follows. */
sealed interface HistoryUiState {
    data object Loading : HistoryUiState

    data class Content(
        val entries: ImmutableList<HistoryEntry>,
        val isManaging: Boolean = false,
        val selectedIds: ImmutableList<Long> = persistentListOf(),
    ) : HistoryUiState

    data object Empty : HistoryUiState

    data class Error(
        val message: String,
    ) : HistoryUiState
}
