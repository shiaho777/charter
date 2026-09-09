package dev.charter.core.data.mapper

import dev.charter.core.database.entity.BangumiEntity
import dev.charter.core.database.entity.EpisodeEntity
import dev.charter.core.model.Bangumi
import dev.charter.core.model.BangumiRating
import dev.charter.core.model.Episode
import dev.charter.core.model.EpisodeType
import dev.charter.core.network.model.EpisodeDto
import dev.charter.core.network.model.ImagesDto
import dev.charter.core.network.model.SubjectDto
import dev.charter.core.network.model.SubjectLiteDto

private const val SEPARATOR = "|"
private const val MAX_TAGS = 8
private val RATING_BUCKETS = 1..10

/** Grid cards and detail posters both read fine at the 400px rendition. */
fun ImagesDto?.bestUrl(): String? = this?.let { it.common ?: it.large ?: it.medium ?: it.small ?: it.grid }

fun SubjectLiteDto.toEntity(
    source: String,
    sortIndex: Int,
    cachedAt: Long,
): BangumiEntity =
    BangumiEntity(
        id = id,
        source = source,
        name = name,
        nameCn = nameCn?.takeIf { it.isNotBlank() },
        summary = summary?.takeIf { it.isNotBlank() },
        airDate = airDate ?: date,
        airWeekday = airWeekday,
        rank = rank,
        imageUrl = images.bestUrl(),
        tags = null,
        ratingScore = rating?.score,
        ratingTotal = rating?.total,
        ratingRank = null,
        ratingCounts = null,
        sortIndex = sortIndex,
        cachedAt = cachedAt,
    )

fun SubjectDto.toDetailEntity(
    source: String,
    cachedAt: Long,
): BangumiEntity =
    BangumiEntity(
        id = id,
        source = source,
        name = name,
        nameCn = nameCn?.takeIf { it.isNotBlank() },
        summary = summary?.takeIf { it.isNotBlank() },
        airDate = date,
        airWeekday = null,
        rank = rating?.rank,
        imageUrl = images.bestUrl(),
        tags =
            tags
                .map { it.name }
                .take(MAX_TAGS)
                .joinToString(SEPARATOR)
                .ifBlank { null },
        ratingScore = rating?.score,
        ratingTotal = rating?.total,
        ratingRank = rating?.rank,
        ratingCounts =
            rating
                ?.count
                ?.let { counts -> RATING_BUCKETS.joinToString(SEPARATOR) { (counts[it.toString()] ?: 0).toString() } },
        sortIndex = 0,
        cachedAt = cachedAt,
    )

fun BangumiEntity.toModel(): Bangumi =
    Bangumi(
        id = id,
        name = name,
        nameCn = nameCn,
        summary = summary,
        airDate = airDate,
        airWeekday = airWeekday,
        rank = rank,
        imageUrl = imageUrl,
        tags = tags?.split(SEPARATOR)?.filter { it.isNotBlank() } ?: emptyList(),
        rating =
            if (ratingScore == null && ratingTotal == null) {
                null
            } else {
                BangumiRating(
                    score = ratingScore,
                    total = ratingTotal ?: 0,
                    rank = ratingRank,
                    counts = ratingCounts?.split(SEPARATOR)?.mapNotNull { it.toIntOrNull() } ?: emptyList(),
                )
            },
    )

fun EpisodeDto.toEntity(
    subjectId: Long,
    cachedAt: Long,
): EpisodeEntity =
    EpisodeEntity(
        id = id,
        subjectId = subjectId,
        sort = sort ?: 0f,
        type = type,
        name = name?.takeIf { it.isNotBlank() },
        nameCn = nameCn?.takeIf { it.isNotBlank() },
        cachedAt = cachedAt,
    )

fun EpisodeEntity.toModel(): Episode =
    Episode(
        id = id,
        sort = sort,
        type = EpisodeType.fromCode(type),
        name = name,
        nameCn = nameCn,
    )
