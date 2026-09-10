package dev.charter.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import dev.charter.core.database.entity.EpisodeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EpisodeDao {
    @Query("SELECT * FROM episodes WHERE subjectId = :subjectId ORDER BY sort ASC")
    fun observeBySubject(subjectId: Long): Flow<List<EpisodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(rows: List<EpisodeEntity>)

    @Query("DELETE FROM episodes WHERE subjectId = :subjectId")
    suspend fun deleteBySubject(subjectId: Long)

    /** Full replace so removed/renumbered episodes never linger. */
    @Transaction
    suspend fun replaceAll(
        subjectId: Long,
        rows: List<EpisodeEntity>,
    ) {
        deleteBySubject(subjectId)
        upsertAll(rows)
    }
}
