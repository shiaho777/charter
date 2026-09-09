package dev.charter.feature.repos.navigation

import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import dev.charter.feature.repos.detail.RepoDetailScreen
import dev.charter.feature.repos.list.RepoListScreen
import kotlinx.serialization.Serializable

@Serializable
data object RepoListKey : NavKey

@Serializable
data class RepoDetailKey(
    val repoId: Long,
) : NavKey

/**
 * The feature's navigation contract. App wires the back stack; the feature
 * owns its keys and entry providers. Routes are types, never strings.
 */
fun reposEntries(onOpenRepo: (Long) -> Unit) =
    entryProvider {
        entry<RepoListKey> {
            RepoListScreen(onOpenRepo = onOpenRepo)
        }
        entry<RepoDetailKey> { key ->
            RepoDetailScreen(repoId = key.repoId)
        }
    }
