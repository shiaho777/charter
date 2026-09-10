package dev.charter.core.database.entity

import androidx.room.Entity

/**
 * Cached subject row. The same subject can be cached under several sources
 * (season list, calendar, a search query, its detail page), hence the
 * composite key; `sortIndex` preserves the server's list order per source.
 */
@Entity(tableName = "bangumi", primaryKeys = ["id", "source"])
data class BangumiEntity(
    val id: Long,
    val source: String,
    val name: String,
    val nameCn: String?,
    val summary: String?,
    val airDate: String?,
    val airWeekday: Int?,
    val rank: Int?,
    val imageUrl: String?,
    /** Top tag names, pipe-joined. */
    val tags: String?,
    val ratingScore: Float?,
    val ratingTotal: Int?,
    val ratingRank: Int?,
    /** Detail rows only: vote counts for scores 1..10, pipe-joined. */
    val ratingCounts: String?,
    val sortIndex: Int,
    val cachedAt: Long,
)
