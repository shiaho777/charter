package dev.charter.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dev.charter.feature.repos.navigation.RepoDetailKey
import dev.charter.feature.repos.navigation.RepoListKey
import dev.charter.feature.repos.navigation.reposEntries

@Composable
fun AppRoot(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(RepoListKey)

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators =
            listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
        entryProvider =
            reposEntries(
                onOpenRepo = { id -> backStack.add(RepoDetailKey(id)) },
            ),
    )
}
