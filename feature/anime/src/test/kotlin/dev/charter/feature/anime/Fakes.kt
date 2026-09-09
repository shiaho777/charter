package dev.charter.feature.anime

import dev.charter.core.common.result.Result
import dev.charter.core.data.repository.BangumiRepository
import dev.charter.core.data.repository.CollectionRepository
import dev.charter.core.data.repository.HistoryRepository
import dev.charter.core.data.repository.SearchHistoryRepository
import dev.charter.core.data.source.PlaySourceAggregator
import dev.charter.core.model.Bangumi
import dev.charter.core.model.CalendarDay
import dev.charter.core.model.CollectStatus
import dev.charter.core.model.CollectionEntry
import dev.charter.core.model.Episode
import dev.charter.core.model.HistoryEntry
import dev.charter.core.model.Road
import dev.charter.core.model.RoadEpisode
import dev.charter.core.model.SearchHistoryEntry
import dev.charter.core.model.Season
import dev.charter.core.model.SourceResult
import dev.charter.core.model.SourceStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class FakeBangumiRepository : BangumiRepository {
    val seasonItems = MutableStateFlow<List<Bangumi>>(emptyList())
    val calendarDays = MutableStateFlow<List<CalendarDay>>(emptyList())
    val searchItems = MutableStateFlow<List<Bangumi>>(emptyList())
    val detail = MutableStateFlow<Bangumi?>(null)
    val episodes = MutableStateFlow<List<Episode>>(emptyList())

    var seasonResult: Result<Int> = Result.Success(0)
    var calendarResult: Result<Unit> = Result.Success(Unit)
    var searchResult: Result<Int> = Result.Success(0)
    var detailResult: Result<Unit> = Result.Success(Unit)

    val seasonPageCalls = mutableListOf<Pair<Season, Int>>()
    var refreshCalendarCalls = 0
    var refreshDetailCalls = 0

    override fun observeSeason(season: Season): Flow<List<Bangumi>> = seasonItems

    override suspend fun loadSeasonPage(
        season: Season,
        page: Int,
    ): Result<Int> {
        seasonPageCalls += season to page
        return seasonResult
    }

    override fun observeCalendar(): Flow<List<CalendarDay>> = calendarDays

    override suspend fun refreshCalendar(): Result<Unit> {
        refreshCalendarCalls += 1
        return calendarResult
    }

    override fun observeSearch(query: String): Flow<List<Bangumi>> = searchItems

    override suspend fun refreshSearch(query: String): Result<Int> = searchResult

    override fun observeDetail(id: Long): Flow<Bangumi?> = detail

    override fun observeEpisodes(id: Long): Flow<List<Episode>> = episodes

    override suspend fun refreshDetail(id: Long): Result<Unit> {
        refreshDetailCalls += 1
        return detailResult
    }
}

class FakeCollectionRepository : CollectionRepository {
    val entries = MutableStateFlow<List<CollectionEntry>>(emptyList())
    val setStatusCalls = mutableListOf<Pair<Long, CollectStatus>>()
    val removeCalls = mutableListOf<Long>()
    var writeResult: Result<Unit> = Result.Success(Unit)

    override fun observeAll(): Flow<List<CollectionEntry>> = entries

    override fun observeOne(bangumiId: Long): Flow<CollectionEntry?> =
        entries.map { list -> list.firstOrNull { it.bangumiId == bangumiId } }

    override suspend fun setStatus(
        bangumi: Bangumi,
        status: CollectStatus,
    ): Result<Unit> {
        setStatusCalls += bangumi.id to status
        if (writeResult.isSuccess) {
            entries.value =
                (
                    entries.value.filterNot { it.bangumiId == bangumi.id } +
                        CollectionEntry(bangumi.id, bangumi.name, bangumi.nameCn, bangumi.imageUrl, null, status, 1L)
                )
        }
        return writeResult
    }

    override suspend fun remove(bangumiId: Long): Result<Unit> {
        removeCalls += bangumiId
        entries.value = entries.value.filterNot { it.bangumiId == bangumiId }
        return writeResult
    }
}

class FakeHistoryRepository : HistoryRepository {
    val entries = MutableStateFlow<List<HistoryEntry>>(emptyList())
    val recorded = mutableListOf<HistoryEntry>()
    var clearAllCalls = 0

    override fun observeAll(): Flow<List<HistoryEntry>> = entries

    override fun observeOne(bangumiId: Long): Flow<HistoryEntry?> =
        entries.map { list -> list.firstOrNull { it.bangumiId == bangumiId } }

    override suspend fun recordWatch(entry: HistoryEntry): Result<Unit> {
        recorded += entry
        entries.value = (entries.value.filterNot { it.bangumiId == entry.bangumiId } + entry)
        return Result.Success(Unit)
    }

    override suspend fun remove(bangumiId: Long): Result<Unit> {
        entries.value = entries.value.filterNot { it.bangumiId == bangumiId }
        return Result.Success(Unit)
    }

    override suspend fun clearAll(): Result<Unit> {
        clearAllCalls += 1
        entries.value = emptyList()
        return Result.Success(Unit)
    }
}

class FakeSearchHistoryRepository : SearchHistoryRepository {
    val entries = MutableStateFlow<List<SearchHistoryEntry>>(emptyList())

    override fun observeRecent(): Flow<List<SearchHistoryEntry>> = entries

    override suspend fun add(keyword: String): Result<Unit> {
        entries.value = listOf(SearchHistoryEntry(keyword, 1L)) + entries.value.filterNot { it.keyword == keyword }
        return Result.Success(Unit)
    }

    override suspend fun remove(keyword: String): Result<Unit> {
        entries.value = entries.value.filterNot { it.keyword == keyword }
        return Result.Success(Unit)
    }

    override suspend fun clear(): Result<Unit> {
        entries.value = emptyList()
        return Result.Success(Unit)
    }
}

class FakePlaySourceAggregator : PlaySourceAggregator {
    override val sourceNames: List<String> = listOf("源A", "源B")

    /** Per-source canned terminal results; defaults to SUCCESS with roads. */
    val results: MutableMap<String, SourceResult> = mutableMapOf()

    /** Fail the next N searches of a source — for retry tests. */
    val failNext: MutableMap<String, Int> = mutableMapOf()

    override fun searchSource(
        sourceName: String,
        keyword: String,
        episodes: List<Episode>,
    ): Flow<SourceResult> =
        flow {
            val remaining = failNext[sourceName] ?: 0
            if (remaining > 0) {
                failNext[sourceName] = remaining - 1
                emit(SourceResult(sourceName, SourceStatus.FAILED, keyword, emptyList(), "检索超时"))
            } else {
                val canned = results[sourceName]
                emit(
                    canned?.copy(keyword = keyword)
                        ?: SourceResult(
                            sourceName,
                            SourceStatus.SUCCESS,
                            keyword,
                            roadsFor(sourceName, episodes),
                            null,
                        ),
                )
            }
        }

    override fun roadsFor(
        sourceName: String,
        episodes: List<Episode>,
    ): List<Road> =
        listOf(
            Road(
                name = "默认线路",
                episodes = episodes.map { RoadEpisode(it.sort, it.displayNumber, it.displayName) },
            ),
        )
}
