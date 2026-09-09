package dev.charter.core.network

import dev.charter.core.common.error.AppError
import dev.charter.core.common.result.Result
import dev.charter.core.network.api.BangumiApi
import dev.charter.core.network.model.CalendarDayDto
import dev.charter.core.network.model.EpisodesResponse
import dev.charter.core.network.model.SearchSubjectsRequest
import dev.charter.core.network.model.SearchSubjectsResponse
import dev.charter.core.network.model.SubjectDto
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/**
 * Bangumi network boundary — the same contract as [GitHubNetworkDataSource]:
 * below this line nothing escapes as an exception (ADR-0002).
 */
class BangumiNetworkDataSource
    @Inject
    constructor(
        private val api: BangumiApi,
    ) {
        suspend fun searchSubjects(
            request: SearchSubjectsRequest,
            limit: Int,
            offset: Int,
        ): Result<SearchSubjectsResponse> = guard { api.searchSubjects(limit, offset, request) }

        suspend fun calendar(): Result<List<CalendarDayDto>> = guard { api.calendar() }

        suspend fun subject(id: Long): Result<SubjectDto> = guard { api.subject(id) }

        suspend fun episodes(subjectId: Long): Result<EpisodesResponse> = guard { api.episodes(subjectId) }

        private suspend inline fun <T> guard(crossinline block: suspend () -> T): Result<T> =
            try {
                Result.Success(block())
            } catch (e: HttpException) {
                val code = e.code()
                if (code == HTTP_UNAUTHORIZED || code == HTTP_FORBIDDEN || code == HTTP_TOO_MANY_REQUESTS) {
                    Result.Failure(AppError.Rejected(code, "Request rejected ($code). Rate limit?", e))
                } else {
                    Result.Failure(AppError.Http(code, "Server error ($code)", e))
                }
            } catch (e: SerializationException) {
                Result.Failure(AppError.Serialization("Malformed response from server", e))
            } catch (e: IOException) {
                Result.Failure(AppError.Network("Network unavailable", e))
            }

        private companion object {
            const val HTTP_UNAUTHORIZED = 401
            const val HTTP_FORBIDDEN = 403
            const val HTTP_TOO_MANY_REQUESTS = 429
        }
    }
