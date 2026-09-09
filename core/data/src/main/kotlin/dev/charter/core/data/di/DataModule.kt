package dev.charter.core.data.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.charter.core.common.dispatchers.DefaultDispatcher
import dev.charter.core.common.dispatchers.IoDispatcher
import dev.charter.core.common.dispatchers.MainDispatcher
import dev.charter.core.data.repository.BangumiRepository
import dev.charter.core.data.repository.CollectionRepository
import dev.charter.core.data.repository.DefaultBangumiRepository
import dev.charter.core.data.repository.DefaultCollectionRepository
import dev.charter.core.data.repository.DefaultHistoryRepository
import dev.charter.core.data.repository.DefaultRepoRepository
import dev.charter.core.data.repository.DefaultSearchHistoryRepository
import dev.charter.core.data.repository.HistoryRepository
import dev.charter.core.data.repository.RepoRepository
import dev.charter.core.data.repository.SearchHistoryRepository
import dev.charter.core.data.source.DemoPlaySourceAggregator
import dev.charter.core.data.source.PlaySourceAggregator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    @Singleton
    abstract fun bindRepoRepository(impl: DefaultRepoRepository): RepoRepository

    @Binds
    @Singleton
    abstract fun bindBangumiRepository(impl: DefaultBangumiRepository): BangumiRepository

    @Binds
    @Singleton
    abstract fun bindCollectionRepository(impl: DefaultCollectionRepository): CollectionRepository

    @Binds
    @Singleton
    abstract fun bindHistoryRepository(impl: DefaultHistoryRepository): HistoryRepository

    @Binds
    @Singleton
    abstract fun bindSearchHistoryRepository(impl: DefaultSearchHistoryRepository): SearchHistoryRepository

    @Binds
    @Singleton
    abstract fun bindPlaySourceAggregator(impl: DemoPlaySourceAggregator): PlaySourceAggregator

    companion object {
        @Provides
        @IoDispatcher
        fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

        @Provides
        @DefaultDispatcher
        fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

        @Provides
        @MainDispatcher
        fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
    }
}
