package dev.charter.core.testing.data

import dev.charter.core.model.Repo

object TestRepos {
    fun repo(
        id: Long = 1,
        name: String = "charter",
        stars: Int = 1000,
        language: String? = "Kotlin",
    ): Repo =
        Repo(
            id = id,
            name = name,
            fullName = "charter-dev/$name",
            owner = "charter-dev",
            ownerAvatarUrl = null,
            description = "Test repository $name",
            stars = stars,
            forks = stars / 10,
            language = language,
            url = "https://github.com/charter-dev/$name",
        )

    val sample: List<Repo> =
        listOf(
            repo(id = 1, name = "charter", stars = 5000),
            repo(id = 2, name = "design-review", stars = 2400),
            repo(id = 3, name = "agent-contract", stars = 1200),
        )
}
