package dev.charter.core.data.repository

import dev.charter.core.model.Season

/**
 * Cache partition keys for the bangumi table. One subject can be cached under
 * several sources at once; a refresh only ever replaces its own partition.
 */
object BangumiSources {
    const val DETAIL = "detail"
    const val CALENDAR = "calendar"

    fun seasonKey(season: Season): String = "season:${season.startDate}..${season.endDate}"

    fun searchKey(query: String): String = "search:$query"
}
