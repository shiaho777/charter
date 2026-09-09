package dev.charter.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.charter.core.database.entity.BangumiEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BangumiDao {
    @Query("SELECT * FROM bangumi WHERE source = :source ORDER BY sortIndex ASC")
    fun observeBySource(source: String): Flow<List<BangumiEntity>>

    @Query("SELECT * FROM bangumi WHERE id = :id AND source = 'detail'")
    fun observeDetail(id: Long): Flow<BangumiEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(rows: List<BangumiEntity>)

    @Query("SELECT COUNT(*) FROM bangumi WHERE source = :source")
    fun countBySource(source: String): Flow<Int>

    @Query("DELETE FROM bangumi WHERE source = :source")
    suspend fun deleteBySource(source: String)

    @Query("SELECT cachedAt FROM bangumi WHERE source = :source LIMIT 1")
    suspend fun cachedAt(source: String): Long?
}
