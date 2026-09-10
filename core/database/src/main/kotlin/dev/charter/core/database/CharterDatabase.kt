package dev.charter.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.charter.core.database.dao.BangumiDao
import dev.charter.core.database.dao.CollectionDao
import dev.charter.core.database.dao.EpisodeDao
import dev.charter.core.database.dao.HistoryDao
import dev.charter.core.database.dao.RepoDao
import dev.charter.core.database.dao.SearchHistoryDao
import dev.charter.core.database.entity.BangumiEntity
import dev.charter.core.database.entity.CollectionEntity
import dev.charter.core.database.entity.EpisodeEntity
import dev.charter.core.database.entity.HistoryEntity
import dev.charter.core.database.entity.RepoEntity
import dev.charter.core.database.entity.SearchHistoryEntity

// v2: anime browsing tables (bangumi cache, episodes, collection, history,
// search history). Destructive migration is configured in DatabaseModule —
// every table here is a cache or user-local library, never primary storage.
@Database(
    entities = [
        RepoEntity::class,
        BangumiEntity::class,
        EpisodeEntity::class,
        CollectionEntity::class,
        HistoryEntity::class,
        SearchHistoryEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class CharterDatabase : RoomDatabase() {
    abstract fun repoDao(): RepoDao

    abstract fun bangumiDao(): BangumiDao

    abstract fun episodeDao(): EpisodeDao

    abstract fun collectionDao(): CollectionDao

    abstract fun historyDao(): HistoryDao

    abstract fun searchHistoryDao(): SearchHistoryDao

    companion object {
        const val NAME = "charter.db"
    }
}
