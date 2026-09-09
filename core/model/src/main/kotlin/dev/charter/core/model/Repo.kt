package dev.charter.core.model

/** A GitHub repository as the app understands it. DTO/entity mapping lives in core/data. */
data class Repo(
    val id: Long,
    val name: String,
    val fullName: String,
    val owner: String,
    val ownerAvatarUrl: String?,
    val description: String?,
    val stars: Int,
    val forks: Int,
    val language: String?,
    val url: String,
)
