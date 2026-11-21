package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        MatchRecordEntity::class,
        DailyProgressEntity::class,
        PlayerProfileEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class TactixDatabase : RoomDatabase() {
    abstract fun tactixDao(): TactixDao

    companion object {
        @Volatile
        private var INSTANCE: TactixDatabase? = null

        fun getInstance(context: Context): TactixDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TactixDatabase::class.java,
                    "tactix_local.db",
                ).fallbackToDestructiveMigration(dropAllTables = true).build().also {
                    INSTANCE = it
                }
            }
        }
    }
}
