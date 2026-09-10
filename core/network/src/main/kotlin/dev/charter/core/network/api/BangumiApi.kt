package dev.charter.core.network.api

import dev.charter.core.network.model.CalendarDayDto
import dev.charter.core.network.model.EpisodesResponse
import dev.charter.core.network.model.SearchSubjectsRequest
import dev.charter.core.network.model.SearchSubjectsResponse
import dev.charter.core.network.model.SubjectDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Bangumi open API (api.bgm.tv). Verified against the live service:
 * search is a POST with a JSON body; the /v0/trending endpoint is gone, so
 * the popular page is a season-filtered search instead (see ADR-0008).
 */
interface BangumiApi {
    @POST("v0/search/subjects")
    suspend fun searchSubjects(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Body body: SearchSubjectsRequest,
    ): SearchSubjectsResponse

    @GET("calendar")
    suspend fun calendar(): List<CalendarDayDto>

    @GET("v0/subjects/{id}")
    suspend fun subject(
        @Path("id") id: Long,
    ): SubjectDto

    @GET("v0/episodes")
    suspend fun episodes(
        @Query("subject_id") subjectId: Long,
        @Query("type") type: Int = EPISODE_TYPE_MAIN,
        @Query("limit") limit: Int = EPISODE_PAGE_LIMIT,
        @Query("offset") offset: Int = 0,
    ): EpisodesResponse

    companion object {
        const val BASE_URL = "https://api.bgm.tv/"

        /** Bangumi requires a descriptive User-Agent identifying the app. */
        const val USER_AGENT = "charter-reference/0.1.0 (dev.charter.app; Compose reference app)"

        const val EPISODE_TYPE_MAIN = 0
        const val EPISODE_PAGE_LIMIT = 100
    }
}
