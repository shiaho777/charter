package dev.charter.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "episodes",
    indices = [Index("subjectId")],
)
data class EpisodeEntity(
    @PrimaryKey val id: Long,
    val subjectId: Long,
    val sort: Float,
    val type: Int,
    val name: String?,
    val nameCn: String?,
    val cachedAt: Long,
)
