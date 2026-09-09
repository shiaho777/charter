package dev.charter.core.data.mapper

import dev.charter.core.database.entity.RepoEntity
import dev.charter.core.model.Repo
import dev.charter.core.network.model.RepoDto

fun RepoDto.toEntity(
    query: String,
    cachedAt: Long,
): RepoEntity =
    RepoEntity(
        id = id,
        name = name,
        fullName = fullName,
        owner = owner.login,
        ownerAvatarUrl = owner.avatarUrl,
        description = description,
        stars = stars,
        forks = forks,
        language = language,
        url = url,
        query = query,
        cachedAt = cachedAt,
    )

fun RepoEntity.toModel(): Repo =
    Repo(
        id = id,
        name = name,
        fullName = fullName,
        owner = owner,
        ownerAvatarUrl = ownerAvatarUrl,
        description = description,
        stars = stars,
        forks = forks,
        language = language,
        url = url,
    )
