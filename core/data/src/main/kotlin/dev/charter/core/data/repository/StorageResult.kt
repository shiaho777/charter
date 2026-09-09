package dev.charter.core.data.repository

import dev.charter.core.common.error.AppError
import dev.charter.core.common.result.Result

/**
 * Local-persistence guard: Room failures become [AppError.Storage] here so
 * repositories never throw across the layer boundary (ADR-0002).
 */
@Suppress("TooGenericExceptionCaught")
internal suspend fun <T> storageResult(block: suspend () -> T): Result<T> =
    try {
        Result.Success(block())
    } catch (e: Exception) {
        Result.Failure(AppError.Storage("Local storage failure", e))
    }
