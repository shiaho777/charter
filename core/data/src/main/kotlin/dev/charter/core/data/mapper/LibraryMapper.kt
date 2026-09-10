package dev.charter.core.data.mapper

import dev.charter.core.database.entity.CollectionEntity
import dev.charter.core.database.entity.HistoryEntity
import dev.charter.core.database.entity.SearchHistoryEntity
import dev.charter.core.model.Bangumi
import dev.charter.core.model.CollectStatus
import dev.charter.core.model.CollectionEntry
import dev.charter.core.model.HistoryEntry
import dev.charter.core.model.SearchHistoryEntry

fun Bangumi.toCollectionEntity(
    status: CollectStatus,
    updatedAt: Long,
): CollectionEntity =
    CollectionEntity(
        bangumiId = id,
        name = name,
        nameCn = nameCn,
        imageUrl = imageUrl,
        ratingScore = rating?.score,
        status = status.code,
        updatedAt = updatedAt,
    )

/** Null when the stored status code is unknown — callers mapNotNull. */
fun CollectionEntity.toModel(): CollectionEntry? =
    CollectStatus.fromCode(status)?.let { resolved ->
        CollectionEntry(
            bangumiId = bangumiId,
            name = name,
            nameCn = nameCn,
            imageUrl = imageUrl,
            ratingScore = ratingScore,
            status = resolved,
            updatedAt = updatedAt,
        )
    }

fun HistoryEntry.toEntity(): HistoryEntity =
    HistoryEntity(
        bangumiId = bangumiId,
        name = name,
        nameCn = nameCn,
        imageUrl = imageUrl,
        sourceName = sourceName,
        roadName = roadName,
        lastEpisodeSort = lastEpisodeSort,
        lastEpisodeName = lastEpisodeName,
        progressRatio = progressRatio,
        updatedAt = updatedAt,
    )

fun HistoryEntity.toModel(): HistoryEntry =
    HistoryEntry(
        bangumiId = bangumiId,
        name = name,
        nameCn = nameCn,
        imageUrl = imageUrl,
        sourceName = sourceName,
        roadName = roadName,
        lastEpisodeSort = lastEpisodeSort,
        lastEpisodeName = lastEpisodeName,
        progressRatio = progressRatio,
        updatedAt = updatedAt,
    )

fun SearchHistoryEntity.toModel(): SearchHistoryEntry = SearchHistoryEntry(keyword, searchedAt)
