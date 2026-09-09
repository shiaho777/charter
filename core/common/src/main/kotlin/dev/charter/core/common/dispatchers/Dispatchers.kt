package dev.charter.core.common.dispatchers

import javax.inject.Qualifier

/**
 * Dispatchers are injected, never referenced as `Dispatchers.X` at call sites
 * in feature/data code — that is what makes ViewModels testable with
 * TestDispatcher (see core/testing).
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher
