package dev.charter.core.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.charter.core.database.CharterDatabase
import dev.charter.core.database.dao.RepoDao
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
}
