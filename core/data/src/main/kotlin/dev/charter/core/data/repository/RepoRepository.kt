package dev.charter.core.data.repository

import dev.charter.core.common.result.Result
import dev.charter.core.model.Repo
import kotlinx.coroutines.flow.Flow

interface RepoRepository {
    /** Offline-first stream: emits cached rows immediately, then refreshes. */
    fun observeRepos(query: String): Flow<List<Repo>>

    fun observeRepo(id: Long): Flow<Repo?>

    /** Refresh from network and upsert into the cache. */
    suspend fun refresh(query: String): Result<Unit>
}
