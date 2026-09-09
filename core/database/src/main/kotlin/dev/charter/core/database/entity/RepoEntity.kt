package dev.charter.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "repos")
data class RepoEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val fullName: String,
    val owner: String,
    val ownerAvatarUrl: String?,
    val description: String?,
    val stars: Int,
    val forks: Int,
    val language: String?,
    val url: String,
    val query: String,
    val cachedAt: Long,
)
