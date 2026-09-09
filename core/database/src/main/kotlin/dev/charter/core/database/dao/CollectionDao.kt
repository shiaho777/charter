package dev.charter.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.charter.core.database.entity.CollectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {
    @Query("SELECT * FROM collection ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<CollectionEntity>>

    @Query("SELECT * FROM collection WHERE bangumiId = :bangumiId")
    fun observeOne(bangumiId: Long): Flow<CollectionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CollectionEntity)

    @Query("DELETE FROM collection WHERE bangumiId = :bangumiId")
    suspend fun delete(bangumiId: Long)
}
