package dev.charter.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.charter.core.database.dao.RepoDao
import dev.charter.core.database.entity.RepoEntity

@Database(entities = [RepoEntity::class], version = 1, exportSchema = true)
abstract class CharterDatabase : RoomDatabase() {
    abstract fun repoDao(): RepoDao

    companion object {
        const val NAME = "charter.db"
    }
}
