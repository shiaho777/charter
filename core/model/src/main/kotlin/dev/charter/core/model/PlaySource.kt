package dev.charter.core.model

/**
 * Play-source aggregation types. In the product prototype (Kazumi) these come
 * from XPath plugin scraping of third-party sites; Charter deliberately ships
 * a DEMO aggregator instead (no scraping — see docs/adr/0008). The shapes are
 * the prototype's: per-source status, roads (线路), episodes per road.
 */
enum class SourceStatus {
    PENDING,
    SEARCHING,
    SUCCESS,
    FAILED,
}

data class RoadEpisode(
    val sort: Float,
    val number: String,
    val title: String,
)

data class Road(
    val name: String,
    val episodes: List<RoadEpisode>,
)

data class SourceResult(
    val sourceName: String,
    val status: SourceStatus,
    /** The keyword this source actually searched — editable per source (alias). */
    val keyword: String,
    val roads: List<Road>,
    val errorMessage: String?,
)
