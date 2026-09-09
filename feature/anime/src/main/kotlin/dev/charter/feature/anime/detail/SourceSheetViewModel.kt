package dev.charter.feature.anime.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.charter.core.data.source.PlaySourceAggregator
import dev.charter.core.model.Episode
import dev.charter.core.model.SourceResult
import dev.charter.core.model.SourceStatus
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Drives the multi-source lookup sheet: one independent search per source,
 * per-source retry and keyword override. A failing source never blocks or
 * taints the others — the sheet's core promise (feature AGENTS.md).
 */
@HiltViewModel
class SourceSheetViewModel
    @Inject
    constructor(
        private val aggregator: PlaySourceAggregator,
    ) : ViewModel() {
        private val _results = MutableStateFlow<ImmutableList<SourceResult>>(persistentListOf())
        val results: StateFlow<ImmutableList<SourceResult>> = _results.asStateFlow()

        private var episodes: List<Episode> = emptyList()
        private var started = false

        fun start(
            keyword: String,
            episodes: List<Episode>,
        ) {
            if (started) return
            started = true
            this.episodes = episodes
            aggregator.sourceNames.forEach { name ->
                _results.update { current ->
                    (current + SourceResult(name, SourceStatus.PENDING, keyword, emptyList(), null)).toImmutableList()
                }
                launchSearch(name, keyword)
            }
        }

        fun retry(sourceName: String) {
            val current = _results.value.firstOrNull { it.sourceName == sourceName } ?: return
            launchSearch(sourceName, current.keyword)
        }

        fun changeKeyword(
            sourceName: String,
            keyword: String,
        ) {
            val trimmed = keyword.trim()
            if (trimmed.isBlank()) return
            launchSearch(sourceName, trimmed)
        }

        private fun launchSearch(
            sourceName: String,
            keyword: String,
        ) {
            updateSource(sourceName) { it.copy(status = SourceStatus.SEARCHING, keyword = keyword) }
            viewModelScope.launch {
                aggregator.searchSource(sourceName, keyword, episodes).collect { result ->
                    updateSource(sourceName) { result }
                }
            }
        }

        private fun updateSource(
            sourceName: String,
            transform: (SourceResult) -> SourceResult,
        ) {
            _results.update { list ->
                list.map { if (it.sourceName == sourceName) transform(it) else it }.toImmutableList()
            }
        }
    }
