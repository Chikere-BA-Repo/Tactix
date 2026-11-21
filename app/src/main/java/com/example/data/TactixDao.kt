package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TactixDao {

    @Query("SELECT * FROM match_records ORDER BY playedAtMillis DESC LIMIT 60")
    fun observeRecentMatches(): Flow<List<MatchRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(record: MatchRecordEntity): Long

    @Query("DELETE FROM match_records WHERE id = :matchId")
    suspend fun deleteMatch(matchId: Long)

    @Query("SELECT * FROM daily_progress ORDER BY dayKey DESC")
    fun observeAllDailyProgress(): Flow<List<DailyProgressEntity>>

    @Query("SELECT * FROM daily_progress WHERE dayKey = :dayKey LIMIT 1")
    suspend fun getDailyProgress(dayKey: Long): DailyProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDailyProgress(progress: DailyProgressEntity)

    @Query("SELECT * FROM player_profile WHERE id = 1 LIMIT 1")
    fun observePlayerProfile(): Flow<PlayerProfileEntity?>

    @Query("SELECT * FROM player_profile WHERE id = 1 LIMIT 1")
    suspend fun getPlayerProfile(): PlayerProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlayerProfile(profile: PlayerProfileEntity)
}
