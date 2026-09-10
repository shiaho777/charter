package dev.charter.core.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.charter.core.database.CharterDatabase
import dev.charter.core.database.dao.BangumiDao
import dev.charter.core.database.dao.CollectionDao
import dev.charter.core.database.dao.EpisodeDao
import dev.charter.core.database.dao.HistoryDao
import dev.charter.core.database.dao.RepoDao
import dev.charter.core.database.dao.SearchHistoryDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): CharterDatabase =
        Room
            .databaseBuilder(context, CharterDatabase::class.java, CharterDatabase.NAME)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideRepoDao(db: CharterDatabase): RepoDao = db.repoDao()

    @Provides
    fun provideBangumiDao(db: CharterDatabase): BangumiDao = db.bangumiDao()

    @Provides
    fun provideEpisodeDao(db: CharterDatabase): EpisodeDao = db.episodeDao()

    @Provides
    fun provideCollectionDao(db: CharterDatabase): CollectionDao = db.collectionDao()

    @Provides
    fun provideHistoryDao(db: CharterDatabase): HistoryDao = db.historyDao()

    @Provides
    fun provideSearchHistoryDao(db: CharterDatabase): SearchHistoryDao = db.searchHistoryDao()
}
