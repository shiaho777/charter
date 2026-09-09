package dev.charter.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** GitHub Search API payloads. Wire format lives here and only here. */
@Serializable
data class RepoSearchResponse(
    @SerialName("total_count") val totalCount: Int,
    @SerialName("items") val items: List<RepoDto> = emptyList(),
)

@Serializable
data class RepoDto(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String,
    @SerialName("full_name") val fullName: String,
    @SerialName("owner") val owner: OwnerDto,
    @SerialName("description") val description: String? = null,
    @SerialName("stargazers_count") val stars: Int = 0,
    @SerialName("forks_count") val forks: Int = 0,
    @SerialName("language") val language: String? = null,
    @SerialName("html_url") val url: String,
)

@Serializable
data class OwnerDto(
    @SerialName("login") val login: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
)
