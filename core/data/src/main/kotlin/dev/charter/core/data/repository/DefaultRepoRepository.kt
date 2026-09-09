package dev.charter.core.data.repository

import dev.charter.core.common.dispatchers.IoDispatcher
import dev.charter.core.common.result.Result
import dev.charter.core.common.result.map
import dev.charter.core.data.mapper.toEntity
import dev.charter.core.data.mapper.toModel
import dev.charter.core.database.dao.RepoDao
import dev.charter.core.model.Repo
import dev.charter.core.network.GitHubNetworkDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * The Room database is the single source of truth; the network is a write-through
 * refresh channel. UI reads this flow and never talks to the network directly.
 */
class DefaultRepoRepository
    @Inject
    constructor(
        private val network: GitHubNetworkDataSource,
        private val repoDao: RepoDao,
        @IoDispatcher private val io: CoroutineDispatcher,
    ) : RepoRepository {
        override fun observeRepos(query: String): Flow<List<Repo>> =
            repoDao.observeRepos(query).map { entities -> entities.map { it.toModel() } }

        override fun observeRepo(id: Long): Flow<Repo?> = repoDao.observeRepo(id).map { it?.toModel() }

        override suspend fun refresh(query: String): Result<Unit> =
            withContext(io) {
                network.searchRepositories(query).map { response ->
                    val now = System.currentTimeMillis()
                    repoDao.upsertAll(response.items.map { it.toEntity(query = query, cachedAt = now) })
                    Unit
                }
            }
    }
