package dev.charter.core.data.repository

import dev.charter.core.common.dispatchers.IoDispatcher
import dev.charter.core.common.result.Result
import dev.charter.core.common.result.map
import dev.charter.core.common.result.onSuccess
import dev.charter.core.data.mapper.toDetailEntity
import dev.charter.core.data.mapper.toEntity
import dev.charter.core.data.mapper.toModel
import dev.charter.core.database.dao.BangumiDao
import dev.charter.core.database.dao.EpisodeDao
import dev.charter.core.model.Bangumi
import dev.charter.core.model.CalendarDay
import dev.charter.core.model.Episode
import dev.charter.core.model.Season
import dev.charter.core.network.BangumiNetworkDataSource
import dev.charter.core.network.model.SearchFilterDto
import dev.charter.core.network.model.SearchSubjectsRequest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** The Room cache is the single source of truth; Bangumi is the write-through channel. */
class DefaultBangumiRepository
    @Inject
    constructor(
        private val network: BangumiNetworkDataSource,
        private val bangumiDao: BangumiDao,
        private val episodeDao: EpisodeDao,
        @IoDispatcher private val io: CoroutineDispatcher,
    ) : BangumiRepository {
        override fun observeSeason(season: Season): Flow<List<Bangumi>> =
            bangumiDao.observeBySource(BangumiSources.seasonKey(season)).mapToModels()

        override suspend fun loadSeasonPage(
            season: Season,
            page: Int,
        ): Result<Int> =
            withContext(io) {
                val offset = page * BangumiRepository.PAGE_SIZE
                network
                    .searchSubjects(
                        SearchSubjectsRequest(
                            keyword = "",
                            filter =
                                SearchFilterDto(
                                    type = listOf(SUBJECT_TYPE_ANIME),
                                    airDate = listOf(">=${season.startDate}", "<${season.endDate}"),
                                ),
                        ),
                        limit = BangumiRepository.PAGE_SIZE,
                        offset = offset,
                    ).map { response ->
                        val now = System.currentTimeMillis()
                        val source = BangumiSources.seasonKey(season)
                        bangumiDao.upsertAll(
                            response.data.mapIndexed { index, dto -> dto.toEntity(source, offset + index, now) },
                        )
                        response.data.size
                    }
            }

        override fun observeCalendar(): Flow<List<CalendarDay>> =
            bangumiDao
                .observeBySource(BangumiSources.CALENDAR)
                .map { entities ->
                    val models = entities.map { it.toModel() }
                    (MONDAY..SUNDAY).map { weekday ->
                        CalendarDay(weekday, models.filter { it.airWeekday == weekday })
                    }
                }

        override suspend fun refreshCalendar(): Result<Unit> =
            withContext(io) {
                network.calendar().map { days ->
                    val now = System.currentTimeMillis()
                    val rows =
                        days.flatMap { day ->
                            day.items.mapIndexed { index, item ->
                                item
                                    .toEntity(BangumiSources.CALENDAR, day.weekday.id * DAY_STRIDE + index, now)
                                    .copy(airWeekday = item.airWeekday ?: day.weekday.id)
                            }
                        }
                    // Replace-then-insert: collectors may briefly see an empty
                    // week mid-refresh; the VM keeps showing stale data behind
                    // its refresh indicator, so the flicker never reaches users.
                    bangumiDao.deleteBySource(BangumiSources.CALENDAR)
                    bangumiDao.upsertAll(rows)
                    Unit
                }
            }

        override fun observeSearch(query: String): Flow<List<Bangumi>> =
            bangumiDao.observeBySource(BangumiSources.searchKey(query)).mapToModels()

        override suspend fun refreshSearch(query: String): Result<Int> =
            withContext(io) {
                network
                    .searchSubjects(
                        SearchSubjectsRequest(
                            keyword = query,
                            filter = SearchFilterDto(type = listOf(SUBJECT_TYPE_ANIME)),
                        ),
                        limit = BangumiRepository.SEARCH_LIMIT,
                        offset = 0,
                    ).map { response ->
                        val now = System.currentTimeMillis()
                        val source = BangumiSources.searchKey(query)
                        bangumiDao.deleteBySource(source)
                        bangumiDao.upsertAll(
                            response.data.mapIndexed { index, dto -> dto.toEntity(source, index, now) },
                        )
                        response.total
                    }
            }

        override fun observeDetail(id: Long): Flow<Bangumi?> = bangumiDao.observeDetail(id).map { it?.toModel() }

        override fun observeEpisodes(id: Long): Flow<List<Episode>> =
            episodeDao.observeBySubject(id).map { rows -> rows.map { it.toModel() } }

        override suspend fun refreshDetail(id: Long): Result<Unit> =
            withContext(io) {
                val now = System.currentTimeMillis()
                network
                    .episodes(id)
                    .onSuccess { response ->
                        episodeDao.replaceAll(id, response.data.map { it.toEntity(id, now) })
                    }
                network.subject(id).map { dto ->
                    bangumiDao.upsertAll(listOf(dto.toDetailEntity(BangumiSources.DETAIL, now)))
                    Unit
                }
            }

        private fun Flow<List<dev.charter.core.database.entity.BangumiEntity>>.mapToModels(): Flow<List<Bangumi>> =
            map { rows -> rows.map { it.toModel() } }

        private companion object {
            const val SUBJECT_TYPE_ANIME = 2
            const val MONDAY = 1
            const val SUNDAY = 7
            const val DAY_STRIDE = 1000
        }
    }
