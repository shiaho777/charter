package dev.charter.core.data.repository

import dev.charter.core.common.result.Result
import dev.charter.core.model.Bangumi
import dev.charter.core.model.CollectStatus
import dev.charter.core.model.CollectionEntry
import kotlinx.coroutines.flow.Flow

/** The user's five-state collection (追番库). Local-only, no cloud sync. */
interface CollectionRepository {
    fun observeAll(): Flow<List<CollectionEntry>>

    fun observeOne(bangumiId: Long): Flow<CollectionEntry?>

    suspend fun setStatus(
        bangumi: Bangumi,
        status: CollectStatus,
    ): Result<Unit>

    suspend fun remove(bangumiId: Long): Result<Unit>
}
