package dev.charter.feature.anime.collection

import dev.charter.core.model.CollectStatus
import dev.charter.core.model.CollectionEntry
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/** Single immutable UI state — the UDF contract every feature screen follows. */
sealed interface CollectionUiState {
    data object Loading : CollectionUiState

    data class Content(
        val entries: ImmutableList<CollectionEntry>,
        val filter: CollectStatus?,
        /** Row-scoped in-flight markers: each row tracks its own write. */
        val pendingIds: ImmutableList<Long>,
    ) : CollectionUiState

    data class Empty(
        val filter: CollectStatus?,
    ) : CollectionUiState

    data class Error(
        val message: String,
    ) : CollectionUiState

    companion object {
        val EmptyEntries: ImmutableList<CollectionEntry> = persistentListOf()
        val NoPending: ImmutableList<Long> = persistentListOf()
    }
}
