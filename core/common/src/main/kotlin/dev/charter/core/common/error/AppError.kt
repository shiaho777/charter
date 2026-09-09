package dev.charter.core.common.error

import java.io.IOException

/**
 * Exhaustive error taxonomy. UI maps these to user-facing copy; it never
 * inspects raw exceptions.
 */
sealed interface AppError {
    val message: String
    val cause: Throwable?

    /** No connectivity, DNS failure, timeout. Usually recoverable by retry. */
    data class Network(
        override val message: String,
        override val cause: Throwable? = null,
    ) : AppError

    /** Server answered with a non-2xx status. */
    data class Http(
        val code: Int,
        override val message: String,
        override val cause: Throwable? = null,
    ) : AppError

    /** Response body did not match the expected schema. */
    data class Serialization(
        override val message: String,
        override val cause: Throwable? = null,
    ) : AppError

    /** Local persistence failure. */
    data class Storage(
        override val message: String,
        override val cause: Throwable? = null,
    ) : AppError

    /** The server refused the request as-is (rate limit, auth). */
    data class Rejected(
        val code: Int,
        override val message: String,
        override val cause: Throwable? = null,
    ) : AppError

    /** Everything else. The taxonomy grows here, deliberately. */
    data class Unknown(
        override val message: String,
        override val cause: Throwable? = null,
    ) : AppError

    companion object {
        /**
         * Generic fallback. Serialization mapping happens at the network boundary
         * (core/network catches SerializationException itself), so common stays
         * dependency-free.
         */
        fun fromThrowable(t: Throwable): AppError =
            when (t) {
                is IOException -> Network(t.message ?: "Network unavailable", t)
                else -> Unknown(t.message ?: "Something went wrong", t)
            }
    }
}
