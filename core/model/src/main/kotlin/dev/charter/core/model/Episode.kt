package dev.charter.core.model

// The codes are the Bangumi API's episode-type protocol values, not tunables.
@Suppress("MagicNumber")
enum class EpisodeType(
    val code: Int,
) {
    MAIN(0),
    SPECIAL(1),
    OPENING(2),
    ENDING(3),
    UNKNOWN(-1),
    ;

    companion object {
        fun fromCode(code: Int): EpisodeType = entries.firstOrNull { it.code == code } ?: UNKNOWN
    }
}

/** One episode of a subject. `sort` is fractional in the Bangumi API (1, 1.5, 2…). */
data class Episode(
    val id: Long,
    val sort: Float,
    val type: EpisodeType,
    val name: String?,
    val nameCn: String?,
) {
    /** Episode number as viewers read it: `7`, not `7.0`. */
    val displayNumber: String
        get() = if (sort % 1f == 0f) sort.toInt().toString() else sort.toString()

    val displayName: String
        get() = nameCn?.takeIf { it.isNotBlank() } ?: name?.takeIf { it.isNotBlank() } ?: "第${displayNumber}话"
}
