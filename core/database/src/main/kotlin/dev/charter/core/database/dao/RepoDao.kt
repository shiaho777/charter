package dev.charter.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.charter.core.database.entity.RepoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RepoDao {
    @Query("SELECT * FROM repos WHERE `query` = :query ORDER BY stars DESC")
    fun observeRepos(query: String): Flow<List<RepoEntity>>

    @Query("SELECT * FROM repos WHERE id = :id")
    fun observeRepo(id: Long): Flow<RepoEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(repos: List<RepoEntity>)

    @Query("DELETE FROM repos WHERE `query` = :query")
    suspend fun deleteQuery(query: String)

    @Query("SELECT cachedAt FROM repos WHERE `query` = :query LIMIT 1")
    suspend fun cachedAt(query: String): Long?
}
