package dev.charter.core.common.result

import dev.charter.core.common.error.AppError

/**
 * The only legal return type for fallible operations in Charter.
 * Exceptions are converted to [AppError] at the layer boundary (network,
 * database) and never thrown across layers — see ADR-0002.
 *
 * Transform helpers live as top-level extensions below: interfaces may not
 * declare inline members.
 */
sealed interface Result<out T> {
    data class Success<T>(
        val data: T,
    ) : Result<T>

    data class Failure(
        val error: AppError,
    ) : Result<Nothing>

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure

    fun getOrNull(): T? = (this as? Success)?.data
}

inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> =
    when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Failure -> this
    }

inline fun <T, R> Result<T>.flatMap(transform: (T) -> Result<R>): Result<R> =
    when (this) {
        is Result.Success -> transform(data)
        is Result.Failure -> this
    }

inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

inline fun <T> Result<T>.onFailure(action: (AppError) -> Unit): Result<T> {
    if (this is Result.Failure) action(error)
    return this
}

/**
 * Catches any exception — by design: this is the universal escape hatch that
 * converts throwable failures into typed [Result.Failure]s at boundaries.
 */
@Suppress("TooGenericExceptionCaught")
inline fun <T> resultOf(block: () -> T): Result<T> =
    try {
        Result.Success(block())
    } catch (e: Exception) {
        Result.Failure(AppError.fromThrowable(e))
    }
