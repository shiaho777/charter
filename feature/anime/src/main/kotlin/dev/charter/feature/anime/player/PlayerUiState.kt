package dev.charter.feature.anime.player

import dev.charter.core.model.Bangumi
import dev.charter.core.model.HistoryEntry
import dev.charter.core.model.Road
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/** Single immutable UI state — the UDF contract every feature screen follows. */
sealed interface PlayerUiState {
    data object Loading : PlayerUiState

    data class Content(
        val bangumi: Bangumi,
        val roads: ImmutableList<Road>,
        val selectedRoad: String,
        val selectedSort: Float,
        val history: HistoryEntry?,
    ) : PlayerUiState

    data class Error(
        val message: String,
    ) : PlayerUiState

    companion object {
        val EmptyRoads: ImmutableList<Road> = persistentListOf()
    }
}
