package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "match_records")
data class MatchRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mode: String,
    val opponentType: String,
    val aiDifficulty: String,
    val startingPlayer: String,
    val outcome: String,
    val moveSequenceCsv: String,
    val winningLineCsv: String,
    val moveCount: Int,
    val eloDelta: Int,
    val accuracyPercent: Int = 100,
    val isBlitz: Boolean = false,
    val playedAtMillis: Long = System.currentTimeMillis(),
)

@Entity(tableName = "daily_progress")
data class DailyProgressEntity(
    @PrimaryKey val dayKey: Long,
    val solvedMask: Int = 0,
    val isCompleted: Boolean = false,
    val completedAtMillis: Long = 0L,
)

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey val id: Int = 1,
    val eloRating: Int = 1000,
    val hapticsEnabled: Boolean = true,
    val bestStreakRush: Int = 0,
    val totalWins: Int = 0,
    val totalLosses: Int = 0,
    val totalDraws: Int = 0,
    val currentWinStreak: Int = 0,
    val bestWinStreak: Int = 0,
    val unlockedMedalsMask: Int = 0,
    val selectedSkinId: String = "CLASSIC",
    val hasCompletedOnboarding: Boolean = false,
)
