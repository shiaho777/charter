package dev.charter.app.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dev.charter.core.designsystem.tokens.WindowSizeClass
import dev.charter.core.designsystem.tokens.currentWindowSizeClass
import dev.charter.feature.anime.navigation.AboutKey
import dev.charter.feature.anime.navigation.AnimeTabLabels
import dev.charter.feature.anime.navigation.BangumiDetailKey
import dev.charter.feature.anime.navigation.HistoryKey
import dev.charter.feature.anime.navigation.PlayerKey
import dev.charter.feature.anime.navigation.PopularKey
import dev.charter.feature.anime.navigation.SearchKey
import dev.charter.feature.anime.navigation.animeEntries
import dev.charter.feature.anime.navigation.animeTabKeys

private val TAB_ICONS: List<(selected: Boolean) -> ImageVector> =
    listOf(
        { Icons.Filled.Home },
        { Icons.Filled.DateRange },
        { selected -> if (selected) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder },
        { Icons.Filled.Person },
    )

private const val COLLECTION_TAB_INDEX = 2

/**
 * The adaptive app shell: bottom bar in compact windows, navigation rail in
 * wider ones (the prototype's dual-form shell). Tabs share one back stack —
 * switching tabs resets to the tab root; detail/search/player push on top.
 */
@Composable
fun AppRoot(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(PopularKey)
    var currentTab by rememberSaveable { mutableIntStateOf(0) }
    val windowSizeClass = currentWindowSizeClass()

    val currentKey = backStack.lastOrNull()
    val onTabRoute = currentKey != null && animeTabKeys.any { it == currentKey }

    fun selectTab(index: Int) {
        currentTab = index
        backStack.clear()
        backStack.add(animeTabKeys[index])
    }

    val entries =
        animeEntries(
            onOpenBangumi = { id -> backStack.add(BangumiDetailKey(id)) },
            onOpenSearch = { backStack.add(SearchKey) },
            onOpenHistory = { backStack.add(HistoryKey) },
            onOpenCollection = { selectTab(COLLECTION_TAB_INDEX) },
            onOpenAbout = { backStack.add(AboutKey) },
            onPlay = { bangumiId, source, road, sort ->
                backStack.add(PlayerKey(bangumiId, source, road, sort))
            },
            onBack = { backStack.removeLastOrNull() },
        )

    val entryDecorators =
        listOf(
            rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
            rememberViewModelStoreNavEntryDecorator<NavKey>(),
        )

    if (windowSizeClass == WindowSizeClass.Compact) {
        Scaffold(
            modifier = modifier,
            bottomBar = {
                if (onTabRoute) {
                    AppBottomBar(currentTab = currentTab, onSelect = { selectTab(it) })
                }
            },
        ) { innerPadding ->
            NavDisplay(
                backStack = backStack,
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                onBack = { backStack.removeLastOrNull() },
                entryDecorators = entryDecorators,
                entryProvider = entries,
            )
        }
    } else {
        Row(modifier.fillMaxSize()) {
            if (onTabRoute) {
                AppNavRail(
                    currentTab = currentTab,
                    onSelect = { selectTab(it) },
                    onOpenSearch = { backStack.add(SearchKey) },
                )
            }
            NavDisplay(
                backStack = backStack,
                modifier = Modifier.weight(1f).fillMaxSize(),
                onBack = { backStack.removeLastOrNull() },
                entryDecorators = entryDecorators,
                entryProvider = entries,
            )
        }
    }
}

@Composable
private fun AppBottomBar(
    currentTab: Int,
    onSelect: (Int) -> Unit,
) {
    ShortNavigationBar {
        animeTabKeys.forEachIndexed { index, _ ->
            ShortNavigationBarItem(
                selected = index == currentTab,
                onClick = { onSelect(index) },
                icon = {
                    Icon(
                        imageVector = TAB_ICONS[index](index == currentTab),
                        contentDescription = AnimeTabLabels[index],
                    )
                },
                label = { Text(AnimeTabLabels[index]) },
            )
        }
    }
}

@Composable
private fun AppNavRail(
    currentTab: Int,
    onSelect: (Int) -> Unit,
    onOpenSearch: () -> Unit,
) {
    NavigationRail {
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onOpenSearch) {
            Icon(Icons.Filled.Search, contentDescription = "搜索")
        }
        animeTabKeys.forEachIndexed { index, _ ->
            NavigationRailItem(
                selected = index == currentTab,
                onClick = { onSelect(index) },
                icon = {
                    Icon(
                        imageVector = TAB_ICONS[index](index == currentTab),
                        contentDescription = AnimeTabLabels[index],
                    )
                },
                label = { Text(AnimeTabLabels[index]) },
            )
        }
        Spacer(Modifier.weight(1f))
    }
}
