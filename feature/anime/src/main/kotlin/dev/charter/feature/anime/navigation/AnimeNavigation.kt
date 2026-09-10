package dev.charter.feature.anime.navigation

import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import dev.charter.feature.anime.about.AboutScreen
import dev.charter.feature.anime.collection.CollectionScreen
import dev.charter.feature.anime.detail.BangumiDetailScreen
import dev.charter.feature.anime.history.HistoryScreen
import dev.charter.feature.anime.my.MyScreen
import dev.charter.feature.anime.player.PlayerScreen
import dev.charter.feature.anime.popular.PopularScreen
import dev.charter.feature.anime.search.SearchScreen
import dev.charter.feature.anime.timeline.TimelineScreen
import kotlinx.serialization.Serializable

// Top-level tab destinations, in navigation-bar order.
@Serializable
data object PopularKey : NavKey

@Serializable
data object TimelineKey : NavKey

@Serializable
data object CollectionKey : NavKey

@Serializable
data object MyKey : NavKey

// Full-screen routes pushed on top of the tabs.
@Serializable
data object SearchKey : NavKey

@Serializable
data object HistoryKey : NavKey

@Serializable
data object AboutKey : NavKey

@Serializable
data class BangumiDetailKey(
    val bangumiId: Long,
) : NavKey

@Serializable
data class PlayerKey(
    val bangumiId: Long,
    val sourceName: String,
    val roadName: String,
    val episodeSort: Float,
) : NavKey

/** The shell renders its bar/rail from this list. */
val animeTabKeys: List<NavKey> = listOf(PopularKey, TimelineKey, CollectionKey, MyKey)

/** Tab labels, parallel to [animeTabKeys]. */
val AnimeTabLabels: List<String> = listOf("推荐", "时间表", "追番", "我的")

/**
 * The feature's navigation contract: routes are types (never strings), the
 * feature owns keys and entries, the app owns the back stack.
 */
fun animeEntries(
    onOpenBangumi: (Long) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenCollection: () -> Unit,
    onOpenAbout: () -> Unit,
    onPlay: (bangumiId: Long, sourceName: String, roadName: String, episodeSort: Float) -> Unit,
    onBack: () -> Unit,
) = entryProvider {
    entry<PopularKey> {
        PopularScreen(onOpenBangumi = onOpenBangumi, onOpenSearch = onOpenSearch)
    }
    entry<TimelineKey> {
        TimelineScreen(onOpenBangumi = onOpenBangumi)
    }
    entry<CollectionKey> {
        CollectionScreen(onOpenBangumi = onOpenBangumi)
    }
    entry<MyKey> {
        MyScreen(
            onOpenHistory = onOpenHistory,
            onOpenCollection = onOpenCollection,
            onOpenAbout = onOpenAbout,
        )
    }
    entry<SearchKey> {
        SearchScreen(onOpenBangumi = onOpenBangumi, onBack = onBack)
    }
    entry<BangumiDetailKey> { key ->
        BangumiDetailScreen(bangumiId = key.bangumiId, onPlay = onPlay, onBack = onBack)
    }
    entry<PlayerKey> { key ->
        PlayerScreen(
            bangumiId = key.bangumiId,
            sourceName = key.sourceName,
            roadName = key.roadName,
            episodeSort = key.episodeSort,
            onBack = onBack,
        )
    }
    entry<HistoryKey> {
        HistoryScreen(onResumePlay = onPlay, onBack = onBack)
    }
    entry<AboutKey> {
        AboutScreen(onBack = onBack)
    }
}
