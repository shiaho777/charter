package dev.charter.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Bangumi open API payloads. Wire format lives here and only here. */
@Serializable
data class SearchSubjectsRequest(
    @SerialName("keyword") val keyword: String,
    @SerialName("filter") val filter: SearchFilterDto? = null,
)

@Serializable
data class SearchFilterDto(
    @SerialName("type") val type: List<Int>? = null,
    /** Range terms like `">=2026-07-01"`, `"<2026-10-01"`. */
    @SerialName("air_date") val airDate: List<String>? = null,
)

@Serializable
data class SearchSubjectsResponse(
    @SerialName("data") val data: List<SubjectLiteDto> = emptyList(),
    @SerialName("total") val total: Int = 0,
    @SerialName("limit") val limit: Int = 0,
    @SerialName("offset") val offset: Int = 0,
)

/**
 * The lite subject shape from calendar and search results. Calendar items
 * carry `air_date`/`air_weekday`/`rank`; search items carry `date` and a
 * lite rating — both nullable, the mapper coalesces.
 */
@Serializable
data class SubjectLiteDto(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String,
    @SerialName("name_cn") val nameCn: String? = null,
    @SerialName("summary") val summary: String? = null,
    @SerialName("images") val images: ImagesDto? = null,
    @SerialName("air_date") val airDate: String? = null,
    @SerialName("date") val date: String? = null,
    @SerialName("air_weekday") val airWeekday: Int? = null,
    @SerialName("rank") val rank: Int? = null,
    @SerialName("rating") val rating: RatingLiteDto? = null,
)

@Serializable
data class RatingLiteDto(
    @SerialName("score") val score: Float? = null,
    @SerialName("total") val total: Int = 0,
)

@Serializable
data class ImagesDto(
    @SerialName("large") val large: String? = null,
    @SerialName("common") val common: String? = null,
    @SerialName("medium") val medium: String? = null,
    @SerialName("small") val small: String? = null,
    @SerialName("grid") val grid: String? = null,
)

@Serializable
data class CalendarDayDto(
    @SerialName("weekday") val weekday: WeekdayDto,
    @SerialName("items") val items: List<SubjectLiteDto> = emptyList(),
)

@Serializable
data class WeekdayDto(
    @SerialName("id") val id: Int,
    @SerialName("en") val en: String? = null,
    @SerialName("cn") val cn: String? = null,
    @SerialName("ja") val ja: String? = null,
)

/** The full subject shape from `/v0/subjects/{id}`. */
@Serializable
data class SubjectDto(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String,
    @SerialName("name_cn") val nameCn: String? = null,
    @SerialName("summary") val summary: String? = null,
    @SerialName("images") val images: ImagesDto? = null,
    @SerialName("date") val date: String? = null,
    @SerialName("rating") val rating: RatingDto? = null,
    @SerialName("tags") val tags: List<TagDto> = emptyList(),
    @SerialName("eps") val eps: Int? = null,
    @SerialName("total_episodes") val totalEpisodes: Int? = null,
)

@Serializable
data class RatingDto(
    @SerialName("score") val score: Float? = null,
    @SerialName("total") val total: Int = 0,
    @SerialName("rank") val rank: Int? = null,
    /** Vote distribution keyed "1".."10". */
    @SerialName("count") val count: Map<String, Int> = emptyMap(),
)

@Serializable
data class TagDto(
    @SerialName("name") val name: String,
    @SerialName("count") val count: Int? = null,
)

@Serializable
data class EpisodesResponse(
    @SerialName("data") val data: List<EpisodeDto> = emptyList(),
    @SerialName("total") val total: Int = 0,
)

@Serializable
data class EpisodeDto(
    @SerialName("id") val id: Long,
    @SerialName("sort") val sort: Float? = null,
    @SerialName("type") val type: Int = 0,
    @SerialName("name") val name: String? = null,
    @SerialName("name_cn") val nameCn: String? = null,
)
