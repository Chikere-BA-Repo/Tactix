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
                )
                    // Player ratings, medals and match history must survive app updates.
                    // When the schema version goes up, add a Migration here instead of wiping data.
                    // Only a downgrade (installing an older build over a newer one) resets the store.
                    .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
                    .build().also {
                    INSTANCE = it
                }
            }
        }
    }
}
