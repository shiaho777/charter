package dev.charter.core.data.repository

import dev.charter.core.common.dispatchers.IoDispatcher
import dev.charter.core.common.result.Result
import dev.charter.core.data.mapper.toModel
import dev.charter.core.database.dao.SearchHistoryDao
import dev.charter.core.database.entity.SearchHistoryEntity
import dev.charter.core.model.SearchHistoryEntry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultSearchHistoryRepository
    @Inject
    constructor(
        private val searchHistoryDao: SearchHistoryDao,
        @IoDispatcher private val io: CoroutineDispatcher,
    ) : SearchHistoryRepository {
        override fun observeRecent(): Flow<List<SearchHistoryEntry>> =
            searchHistoryDao
                .observeRecent(SearchHistoryRepository.MAX_ENTRIES)
                .map { rows -> rows.map { it.toModel() } }

        override suspend fun add(keyword: String): Result<Unit> =
            withContext(io) {
                val trimmed = keyword.trim()
                if (trimmed.isEmpty()) {
                    Result.Success(Unit)
                } else {
                    storageResult {
                        searchHistoryDao.upsert(SearchHistoryEntity(trimmed, System.currentTimeMillis()))
                    }
                }
            }

        override suspend fun remove(keyword: String): Result<Unit> =
            withContext(io) { storageResult { searchHistoryDao.delete(keyword) } }

        override suspend fun clear(): Result<Unit> = withContext(io) { storageResult { searchHistoryDao.deleteAll() } }
    }
