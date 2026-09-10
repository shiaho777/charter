package dev.charter.core.data.repository

import dev.charter.core.common.async.SerialQueue
import dev.charter.core.common.dispatchers.IoDispatcher
import dev.charter.core.common.result.Result
import dev.charter.core.data.mapper.toEntity
import dev.charter.core.data.mapper.toModel
import dev.charter.core.database.dao.HistoryDao
import dev.charter.core.model.HistoryEntry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultHistoryRepository
    @Inject
    constructor(
        private val historyDao: HistoryDao,
        @IoDispatcher private val io: CoroutineDispatcher,
    ) : HistoryRepository {
        // Progress ticks arrive fast; serialization keeps the row converging
        // to the last submitted write instead of the last scheduled one.
        private val writes = SerialQueue()

        override fun observeAll(): Flow<List<HistoryEntry>> =
            historyDao.observeAll().map { rows -> rows.map { it.toModel() } }

        override fun observeOne(bangumiId: Long): Flow<HistoryEntry?> =
            historyDao.observeOne(bangumiId).map { it?.toModel() }

        override suspend fun recordWatch(entry: HistoryEntry): Result<Unit> =
            writes.run {
                withContext(io) { storageResult { historyDao.upsert(entry.toEntity()) } }
            }

        override suspend fun remove(bangumiId: Long): Result<Unit> =
            writes.run {
                withContext(io) { storageResult { historyDao.delete(bangumiId) } }
            }

        override suspend fun clearAll(): Result<Unit> =
            writes.run {
                withContext(io) { storageResult { historyDao.deleteAll() } }
            }
    }
