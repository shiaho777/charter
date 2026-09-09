package dev.charter.core.network.api

import dev.charter.core.network.model.RepoSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GitHubApi {
    @GET("search/repositories")
    suspend fun searchRepositories(
        @Query("q") query: String,
        @Query("sort") sort: String = "stars",
        @Query("per_page") perPage: Int = 30,
    ): RepoSearchResponse

    companion object {
        const val BASE_URL = "https://api.github.com/"
    }
}
