package dev.charter.core.data.source

import dev.charter.core.model.Episode
import dev.charter.core.model.Road
import dev.charter.core.model.SourceResult
import kotlinx.coroutines.flow.Flow

/**
 * Multi-source play lookup — the prototype's (Kazumi's) signature interaction,
 * minus the scraping: Charter ships a DEMO aggregator only (ADR-0008). The
 * contract keeps the prototype's semantics: each source searches independently,
 * reports its own status, retries alone, and may search under an edited keyword.
 */
interface PlaySourceAggregator {
    /** Built-in demo sources, in display order. */
    val sourceNames: List<String>

    /**
     * Simulates one source searching [keyword]: emits SEARCHING, then a
     * terminal SUCCESS (with roads) or FAILED. One flow per source — sources
     * never share fate.
     */
    fun searchSource(
        sourceName: String,
        keyword: String,
        episodes: List<Episode>,
    ): Flow<SourceResult>

    /** Deterministic road layout — lets the player rebuild the line switcher. */
    fun roadsFor(
        sourceName: String,
        episodes: List<Episode>,
    ): List<Road>
}
