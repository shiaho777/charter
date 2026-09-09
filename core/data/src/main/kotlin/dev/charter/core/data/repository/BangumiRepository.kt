package dev.charter.core.data.repository

import dev.charter.core.common.result.Result
import dev.charter.core.model.Bangumi
import dev.charter.core.model.CalendarDay
import dev.charter.core.model.Episode
import dev.charter.core.model.Season
import kotlinx.coroutines.flow.Flow

/**
 * Bangumi metadata: season catalog, weekly calendar, search, detail+episodes.
 * Offline-first like [RepoRepository]: flows read the Room cache, refreshes
 * are write-through network calls.
 */
interface BangumiRepository {
    fun observeSeason(season: Season): Flow<List<Bangumi>>

    /**
     * Fetches page [page] (0-based) of the season catalog and appends it to
     * the cache. The returned count is the page size actually fetched — when
     * it drops below [PAGE_SIZE] there is no next page.
     */
    suspend fun loadSeasonPage(
        season: Season,
        page: Int,
    ): Result<Int>

    /** Always seven days (Monday first), some possibly empty. */
    fun observeCalendar(): Flow<List<CalendarDay>>

    suspend fun refreshCalendar(): Result<Unit>

    fun observeSearch(query: String): Flow<List<Bangumi>>

    /** Replaces the cached results for [query]; returns the server's total. */
    suspend fun refreshSearch(query: String): Result<Int>

    fun observeDetail(id: Long): Flow<Bangumi?>

    fun observeEpisodes(id: Long): Flow<List<Episode>>

    /**
     * Refreshes subject and episodes. Episode failure is non-fatal: the
     * subject still renders, the episode section stays from cache/empty.
     */
    suspend fun refreshDetail(id: Long): Result<Unit>

    companion object {
        const val PAGE_SIZE = 30
        const val SEARCH_LIMIT = 50
    }
}
