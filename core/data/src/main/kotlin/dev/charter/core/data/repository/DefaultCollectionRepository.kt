package dev.charter.core.data.repository

import dev.charter.core.common.async.SerialQueue
import dev.charter.core.common.dispatchers.IoDispatcher
import dev.charter.core.common.result.Result
import dev.charter.core.data.mapper.toCollectionEntity
import dev.charter.core.data.mapper.toModel
import dev.charter.core.database.dao.CollectionDao
import dev.charter.core.model.Bangumi
import dev.charter.core.model.CollectStatus
import dev.charter.core.model.CollectionEntry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultCollectionRepository
    @Inject
    constructor(
        private val collectionDao: CollectionDao,
        @IoDispatcher private val io: CoroutineDispatcher,
    ) : CollectionRepository {
        // Rapid status toggles must land in submission order, not interleave.
        private val writes = SerialQueue()

        override fun observeAll(): Flow<List<CollectionEntry>> =
            collectionDao.observeAll().map { rows -> rows.mapNotNull { it.toModel() } }

        override fun observeOne(bangumiId: Long): Flow<CollectionEntry?> =
            collectionDao.observeOne(bangumiId).map { it?.toModel() }

        override suspend fun setStatus(
            bangumi: Bangumi,
            status: CollectStatus,
        ): Result<Unit> =
            writes.run {
                withContext(io) {
                    storageResult {
                        collectionDao.upsert(bangumi.toCollectionEntity(status, System.currentTimeMillis()))
                    }
                }
            }

        override suspend fun remove(bangumiId: Long): Result<Unit> =
            writes.run {
                withContext(io) { storageResult { collectionDao.delete(bangumiId) } }
            }
    }
