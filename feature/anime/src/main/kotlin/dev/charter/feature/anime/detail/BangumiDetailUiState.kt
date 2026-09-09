package dev.charter.feature.anime.detail

import dev.charter.core.model.Bangumi
import dev.charter.core.model.CollectionEntry
import dev.charter.core.model.Episode
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/** Single immutable UI state — the UDF contract every feature screen follows. */
sealed interface BangumiDetailUiState {
    data object Loading : BangumiDetailUiState

    data class Content(
        val bangumi: Bangumi,
        val episodes: ImmutableList<Episode>,
        val collection: CollectionEntry?,
        val isCollectionPending: Boolean = false,
    ) : BangumiDetailUiState

    data class Error(
        val message: String,
    ) : BangumiDetailUiState

    companion object {
        val EmptyEpisodes: ImmutableList<Episode> = persistentListOf()
    }
}
