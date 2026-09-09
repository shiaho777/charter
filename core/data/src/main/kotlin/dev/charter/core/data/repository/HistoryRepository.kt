package dev.charter.core.data.repository

import dev.charter.core.common.result.Result
import dev.charter.core.model.HistoryEntry
import kotlinx.coroutines.flow.Flow

/** Watch history — one row per subject, newest watch wins. */
interface HistoryRepository {
    fun observeAll(): Flow<List<HistoryEntry>>

    fun observeOne(bangumiId: Long): Flow<HistoryEntry?>

    suspend fun recordWatch(entry: HistoryEntry): Result<Unit>

    suspend fun remove(bangumiId: Long): Result<Unit>

    suspend fun clearAll(): Result<Unit>
}
