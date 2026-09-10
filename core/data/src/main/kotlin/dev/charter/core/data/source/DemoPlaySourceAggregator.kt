package dev.charter.core.data.source

import dev.charter.core.common.dispatchers.IoDispatcher
import dev.charter.core.model.Episode
import dev.charter.core.model.EpisodeType
import dev.charter.core.model.Road
import dev.charter.core.model.RoadEpisode
import dev.charter.core.model.SourceResult
import dev.charter.core.model.SourceStatus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import kotlin.random.Random

/**
 * Demo aggregator: staggered fake latency and a small random failure rate, so
 * the per-source status/retry/alias UI exercises all of its states honestly.
 * No third-party site is contacted — roads are built from cached Bangumi
 * episodes and playback is a placeholder (ADR-0008).
 */
class DemoPlaySourceAggregator
    @Inject
    constructor(
        @IoDispatcher private val io: CoroutineDispatcher,
    ) : PlaySourceAggregator {
        override val sourceNames: List<String>
            get() = DEMO_SOURCES

        override fun searchSource(
            sourceName: String,
            keyword: String,
            episodes: List<Episode>,
        ): Flow<SourceResult> =
            flow {
                emit(SourceResult(sourceName, SourceStatus.SEARCHING, keyword, emptyList(), null))
                val stagger = DEMO_SOURCES.indexOf(sourceName).coerceAtLeast(0) * STAGGER_MS
                delay(BASE_LATENCY_MS + stagger + Random.nextLong(JITTER_MS))
                if (Random.nextInt(PERCENT_MAX) < FAILURE_PERCENT) {
                    emit(SourceResult(sourceName, SourceStatus.FAILED, keyword, emptyList(), FAILURE_MESSAGE))
                } else {
                    emit(
                        SourceResult(
                            sourceName,
                            SourceStatus.SUCCESS,
                            keyword,
                            roadsFor(sourceName, episodes),
                            null,
                        ),
                    )
                }
            }.flowOn(io)

        override fun roadsFor(
            sourceName: String,
            episodes: List<Episode>,
        ): List<Road> {
            val playable = episodes.filter { it.type == EpisodeType.MAIN }.ifEmpty { episodes }
            val roadEpisodes =
                playable.map {
                    RoadEpisode(sort = it.sort, number = it.displayNumber, title = it.displayName)
                }
            return listOf(
                Road(DEFAULT_ROAD, roadEpisodes),
                Road(BACKUP_ROAD, roadEpisodes),
            )
        }

        companion object {
            /** Clearly-fictional names: the demo must not impersonate real sites. */
            val DEMO_SOURCES = listOf("樱花演示源", "云播演示源", "蓝光演示源")
            const val DEFAULT_ROAD = "默认线路"
            const val BACKUP_ROAD = "备用线路"
            private const val BASE_LATENCY_MS = 500L
            private const val STAGGER_MS = 350L
            private const val JITTER_MS = 600L
            private const val FAILURE_PERCENT = 20
            private const val PERCENT_MAX = 100
            private const val FAILURE_MESSAGE = "检索超时，请重试或修改关键词"
        }
    }
