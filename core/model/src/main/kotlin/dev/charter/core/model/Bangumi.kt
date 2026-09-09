package dev.charter.core.model

/**
 * An anime subject as the app understands it. Mirrors the Bangumi open API
 * payloads (lite shapes from calendar/search, full shape from subject detail);
 * DTO/entity mapping lives in core:data.
 */
data class Bangumi(
    val id: Long,
    val name: String,
    val nameCn: String?,
    val summary: String?,
    /** ISO air date (`2026-07-06`); null when the payload omits it. */
    val airDate: String?,
    /** Bangumi weekday number: 1=Monday … 7=Sunday. Detail payloads lack it. */
    val airWeekday: Int?,
    /** Site-wide rank; present on calendar payloads and detail ratings. */
    val rank: Int?,
    val imageUrl: String?,
    val tags: List<String>,
    val rating: BangumiRating?,
) {
    /** Chinese title when usable, original name otherwise — the display rule. */
    val displayName: String
        get() = nameCn?.takeIf { it.isNotBlank() } ?: name
}

data class BangumiRating(
    val score: Float?,
    val total: Int,
    /** Detail payloads only: rank derived from the rating. */
    val rank: Int?,
    /** Detail payloads only: vote counts for scores 1..10, empty for lite shapes. */
    val counts: List<Int>,
)
