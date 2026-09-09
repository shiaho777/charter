package dev.charter.core.testing.data

import dev.charter.core.model.Bangumi
import dev.charter.core.model.BangumiRating
import dev.charter.core.model.CalendarDay
import dev.charter.core.model.CollectStatus
import dev.charter.core.model.CollectionEntry
import dev.charter.core.model.Episode
import dev.charter.core.model.EpisodeType
import dev.charter.core.model.HistoryEntry
import dev.charter.core.model.SearchHistoryEntry

object TestBangumi {
    private const val RATING_TOTAL = 1000
    private const val BASE_TIMESTAMP = 1_700_000_000_000L
    private const val EPISODE_COUNT = 12
    private const val WEEKDAY_COUNT = 7
    private const val DAY_ID_STRIDE = 10L

    fun bangumi(
        id: Long = 1,
        name: String = "Test Anime",
        nameCn: String? = "测试番剧",
        airWeekday: Int? = 1,
        rank: Int? = 42,
        score: Float? = 8.2f,
    ): Bangumi =
        Bangumi(
            id = id,
            name = name,
            nameCn = nameCn,
            summary = "A test subject used across Charter's anime feature tests.",
            airDate = "2026-07-06",
            airWeekday = airWeekday,
            rank = rank,
            imageUrl = null,
            tags = listOf("测试", "TV"),
            rating = score?.let { BangumiRating(it, RATING_TOTAL, rank, emptyList()) },
        )

    val sampleSeason: List<Bangumi> =
        listOf(
            bangumi(id = 1, name = "Season One"),
            bangumi(id = 2, name = "Season Two", airWeekday = 3),
            bangumi(id = 3, name = "Season Three", airWeekday = 5, score = null),
        )

    fun episode(
        id: Long = 1,
        sort: Float = 1f,
        type: EpisodeType = EpisodeType.MAIN,
        nameCn: String? = null,
    ): Episode = Episode(id, sort, type, "Episode $sort", nameCn)

    val sampleEpisodes: List<Episode> = (1..EPISODE_COUNT).map { episode(id = it.toLong(), sort = it.toFloat()) }

    fun calendar(itemsPerDay: Int = 1): List<CalendarDay> =
        (1..WEEKDAY_COUNT).map { weekday ->
            CalendarDay(
                weekday,
                (1..itemsPerDay).map { bangumi(id = weekday * DAY_ID_STRIDE + it, airWeekday = weekday) },
            )
        }

    fun collectionEntry(
        bangumiId: Long = 1,
        status: CollectStatus = CollectStatus.WATCHING,
    ): CollectionEntry =
        CollectionEntry(
            bangumiId = bangumiId,
            name = "Collected $bangumiId",
            nameCn = "收藏 $bangumiId",
            imageUrl = null,
            ratingScore = 8f,
            status = status,
            updatedAt = BASE_TIMESTAMP + bangumiId,
        )

    fun historyEntry(
        bangumiId: Long = 1,
        lastEpisodeSort: Float = 3f,
        progressRatio: Float = 0.5f,
    ): HistoryEntry =
        HistoryEntry(
            bangumiId = bangumiId,
            name = "Watched $bangumiId",
            nameCn = "在看 $bangumiId",
            imageUrl = null,
            sourceName = "樱花演示源",
            roadName = "默认线路",
            lastEpisodeSort = lastEpisodeSort,
            lastEpisodeName = "第${lastEpisodeSort.toInt()}话",
            progressRatio = progressRatio,
            updatedAt = BASE_TIMESTAMP + bangumiId,
        )

    fun searchHistory(vararg keywords: String): List<SearchHistoryEntry> =
        keywords.mapIndexed { index, keyword ->
            SearchHistoryEntry(keyword, BASE_TIMESTAMP + index)
        }
}
