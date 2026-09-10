package dev.charter.core.model

/**
 * One row of watch history: which subject, from which (demo) source and road,
 * last watched episode and how far into it the user got.
 */
data class HistoryEntry(
    val bangumiId: Long,
    val name: String,
    val nameCn: String?,
    val imageUrl: String?,
    val sourceName: String,
    val roadName: String,
    val lastEpisodeSort: Float,
    val lastEpisodeName: String?,
    /** 0f..1f — how much of [lastEpisodeSort] was watched. */
    val progressRatio: Float,
    val updatedAt: Long,
) {
    val displayName: String
        get() = nameCn?.takeIf { it.isNotBlank() } ?: name

    val isFinished: Boolean
        get() = progressRatio >= FINISHED_RATIO

    private companion object {
        const val FINISHED_RATIO = 0.99f
    }
}
