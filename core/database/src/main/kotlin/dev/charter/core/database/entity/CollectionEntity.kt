package dev.charter.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The user's collection (追番). Display fields are denormalized on purpose:
 * the library must render offline without joining the bangumi cache.
 */
@Entity(tableName = "collection")
data class CollectionEntity(
    @PrimaryKey val bangumiId: Long,
    val name: String,
    val nameCn: String?,
    val imageUrl: String?,
    val ratingScore: Float?,
    /** CollectStatus.code — 1在看 2想看 3搁置 4看过 5抛弃. */
    val status: Int,
    val updatedAt: Long,
)
