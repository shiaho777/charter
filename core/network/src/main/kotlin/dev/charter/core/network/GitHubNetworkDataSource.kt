package dev.charter.core.network

import dev.charter.core.common.error.AppError
import dev.charter.core.common.result.Result
import dev.charter.core.network.api.GitHubApi
import dev.charter.core.network.model.RepoSearchResponse
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/**
 * Network boundary. This is where throwables become [AppError]s — below this
 * line nothing is allowed to escape as an exception (see ADR-0002).
 */
class GitHubNetworkDataSource
    @Inject
    constructor(
        private val api: GitHubApi,
    ) {
        suspend fun searchRepositories(query: String): Result<RepoSearchResponse> =
            try {
                Result.Success(api.searchRepositories(query))
            } catch (e: HttpException) {
                val code = e.code()
                if (code == HTTP_UNAUTHORIZED || code == HTTP_FORBIDDEN) {
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
        }
    }
