package dev.charter.feature.anime.timeline

import dev.charter.core.model.CalendarDay
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/** Single immutable UI state — the UDF contract every feature screen follows. */
sealed interface TimelineUiState {
    data object Loading : TimelineUiState

    data class Content(
        val days: ImmutableList<CalendarDay>,
        val selectedWeekday: Int,
        val onlyFollowing: Boolean,
        val isRefreshing: Boolean = false,
    ) : TimelineUiState

    data class Empty(
        val selectedWeekday: Int,
        val onlyFollowing: Boolean,
        val isRefreshing: Boolean = false,
    ) : TimelineUiState

    data class Error(
        val message: String,
    ) : TimelineUiState

    companion object {
        val EmptyDays: ImmutableList<CalendarDay> = persistentListOf()
    }
}
