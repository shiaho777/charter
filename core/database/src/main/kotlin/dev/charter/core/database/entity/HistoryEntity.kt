package dev.charter.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Watch history: one row per subject, always pointing at the last watch. */
@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey val bangumiId: Long,
    val name: String,
    val nameCn: String?,
    val imageUrl: String?,
    val sourceName: String,
    val roadName: String,
    val lastEpisodeSort: Float,
    val lastEpisodeName: String?,
    /** 0f..1f — how much of the last episode was watched. */
    val progressRatio: Float,
    val updatedAt: Long,
)
