package dev.charter.core.data.repository

import dev.charter.core.common.result.Result
import dev.charter.core.model.SearchHistoryEntry
import kotlinx.coroutines.flow.Flow

/** Recent search keywords, most recent first, capped. */
interface SearchHistoryRepository {
    fun observeRecent(): Flow<List<SearchHistoryEntry>>

    suspend fun add(keyword: String): Result<Unit>

    suspend fun remove(keyword: String): Result<Unit>

    suspend fun clear(): Result<Unit>

    companion object {
        const val MAX_ENTRIES = 20
    }
}
