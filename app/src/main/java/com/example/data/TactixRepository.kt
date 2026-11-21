package com.example.data

import com.example.domain.AiDifficulty
import com.example.domain.GameEngine
import com.example.domain.GameMode
import com.example.domain.MasteryMedal
import com.example.domain.MatchOutcome
import com.example.domain.MoveEntry
import com.example.domain.OpponentType
import com.example.domain.PieceSkin
import com.example.domain.Player
import com.example.domain.TacticalAiSolver
import com.example.domain.TacticalPuzzleGenerator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class RecordedMatchResult(
    val match: MatchRecordEntity,
    val eloDelta: Int,
    val accuracyPercent: Int,
    val newlyUnlockedMedals: List<MasteryMedal>,
)

class TactixRepository(private val dao: TactixDao) {

    val recentMatches: Flow<List<MatchRecordEntity>> = dao.observeRecentMatches()

    val playerProfile: Flow<PlayerProfileEntity> = dao.observePlayerProfile()
        .map { it ?: PlayerProfileEntity() }

    val dailyProgressHistory: Flow<List<DailyProgressEntity>> = dao.observeAllDailyProgress()

    suspend fun getOrCreateProfile(): PlayerProfileEntity {
        val existing = dao.getPlayerProfile()
        if (existing != null) return existing
        val created = PlayerProfileEntity()
        dao.upsertPlayerProfile(created)
        return created
    }

    suspend fun toggleHaptics(): Boolean {
        val profile = getOrCreateProfile()
        val updated = profile.copy(hapticsEnabled = !profile.hapticsEnabled)
        dao.upsertPlayerProfile(updated)
        return updated.hapticsEnabled
    }

    suspend fun selectPieceSkin(skin: PieceSkin) {
        val profile = getOrCreateProfile()
        val unlockedCount = Integer.bitCount(profile.unlockedMedalsMask)
        if (unlockedCount >= skin.unlockMedalsRequired) {
            dao.upsertPlayerProfile(profile.copy(selectedSkinId = skin.id))
        }
    }

    suspend fun completeOnboarding() {
        val profile = getOrCreateProfile()
        if (!profile.hasCompletedOnboarding) {
            dao.upsertPlayerProfile(profile.copy(hasCompletedOnboarding = true))
        }
    }

    suspend fun recordCompletedMatch(
        mode: GameMode,
        opponentType: OpponentType,
        aiDifficulty: AiDifficulty,
        startingPlayer: Player,
        outcome: MatchOutcome,
        humanSide: Player,
        moves: List<MoveEntry>,
        winningLine: List<Int>?,
        isBlitz: Boolean,
        accuracyPercent: Int,
    ): RecordedMatchResult {
        val profile = getOrCreateProfile()
        val eloDelta = if (opponentType == OpponentType.AI) {
            TacticalAiSolver.calculateEloDelta(
                playerElo = profile.eloRating,
                outcome = outcome,
                humanSide = humanSide,
                difficulty = aiDifficulty,
            )
        } else {
            0
        }

        val entity = MatchRecordEntity(
            mode = mode.name,
            opponentType = opponentType.name,
            aiDifficulty = aiDifficulty.name,
            startingPlayer = startingPlayer.name,
            outcome = outcome.name,
            moveSequenceCsv = GameEngine.serializeMoveIndices(moves),
            winningLineCsv = winningLine?.joinToString(",") ?: "",
            moveCount = moves.size,
            eloDelta = eloDelta,
            accuracyPercent = accuracyPercent,
            isBlitz = isBlitz,
        )
        val insertedId = dao.insertMatch(entity)

        val newlyUnlocked = mutableListOf<MasteryMedal>()
        if (opponentType == OpponentType.AI) {
            val isHumanWin = (outcome == MatchOutcome.X_WIN && humanSide == Player.X) ||
                (outcome == MatchOutcome.O_WIN && humanSide == Player.O)
            val isDraw = outcome == MatchOutcome.DRAW
            val nextWinStreak = if (isHumanWin) profile.currentWinStreak + 1 else 0

            var medalMask = profile.unlockedMedalsMask
            fun grant(medal: MasteryMedal) {
                if ((medalMask and medal.bitMask) == 0) {
                    medalMask = medalMask or medal.bitMask
                    newlyUnlocked.add(medal)
                }
            }

            if (isHumanWin) {
                grant(MasteryMedal.FIRST_VICTORY)
                if (mode == GameMode.INFINITY && moves.size >= 9) {
                    grant(MasteryMedal.DECAY_MASTER)
                }
                if (mode == GameMode.MISERE) {
                    grant(MasteryMedal.MISERE_GENIUS)
                }
                if (isBlitz) {
                    grant(MasteryMedal.BLITZ_REFLEX)
                }
            }
            if (accuracyPercent >= 95 && moves.size >= 5 &&
                (aiDifficulty == AiDifficulty.TACTICAL || aiDifficulty == AiDifficulty.MASTER || aiDifficulty == AiDifficulty.ADAPTIVE)
            ) {
                grant(MasteryMedal.SURGICAL)
            }
            if (aiDifficulty == AiDifficulty.MASTER && (isHumanWin || (isDraw && mode == GameMode.CLASSIC))) {
                grant(MasteryMedal.GIANT_SLAYER)
            }

            val updatedProfile = profile.copy(
                eloRating = (profile.eloRating + eloDelta).coerceIn(600, 2000),
                totalWins = profile.totalWins + if (isHumanWin) 1 else 0,
                totalLosses = profile.totalLosses + if (!isHumanWin && !isDraw) 1 else 0,
                totalDraws = profile.totalDraws + if (isDraw) 1 else 0,
                currentWinStreak = nextWinStreak,
                bestWinStreak = maxOf(profile.bestWinStreak, nextWinStreak),
                unlockedMedalsMask = medalMask,
            )
            dao.upsertPlayerProfile(updatedProfile)
        }

        return RecordedMatchResult(
            match = entity.copy(id = insertedId),
            eloDelta = eloDelta,
            accuracyPercent = accuracyPercent,
            newlyUnlockedMedals = newlyUnlocked,
        )
    }

    suspend fun deleteMatch(matchId: Long) {
        dao.deleteMatch(matchId)
    }

    suspend fun markDailyPuzzleSolved(dayKey: Long, puzzleIndex: Int): Boolean {
        val current = dao.getDailyProgress(dayKey) ?: DailyProgressEntity(dayKey = dayKey)
        val updatedMask = current.solvedMask or (1 shl puzzleIndex)
        val allThreeSolved = (updatedMask and 0b111) == 0b111
        dao.upsertDailyProgress(
            current.copy(
                solvedMask = updatedMask,
                isCompleted = allThreeSolved,
                completedAtMillis = if (allThreeSolved && current.completedAtMillis == 0L) {
                    System.currentTimeMillis()
                } else {
                    current.completedAtMillis
                },
            ),
        )
        if (allThreeSolved) {
            unlockMedal(MasteryMedal.STREAK_KEEPER)
        }
        return allThreeSolved
    }

    suspend fun recordStreakRushScore(score: Int) {
        val profile = getOrCreateProfile()
        var updated = profile
        if (score > profile.bestStreakRush) {
            updated = updated.copy(bestStreakRush = score)
        }
        if (score >= 5 && (updated.unlockedMedalsMask and MasteryMedal.RUSH_LEGEND.bitMask) == 0) {
            updated = updated.copy(
                unlockedMedalsMask = updated.unlockedMedalsMask or MasteryMedal.RUSH_LEGEND.bitMask,
            )
        }
        if (updated != profile) {
            dao.upsertPlayerProfile(updated)
        }
    }

    private suspend fun unlockMedal(medal: MasteryMedal) {
        val profile = getOrCreateProfile()
        if ((profile.unlockedMedalsMask and medal.bitMask) == 0) {
            dao.upsertPlayerProfile(
                profile.copy(unlockedMedalsMask = profile.unlockedMedalsMask or medal.bitMask),
            )
        }
    }

    fun calculateCurrentDailyStreak(records: List<DailyProgressEntity>): Int {
        val completedKeys = records.filter { it.isCompleted }.map { it.dayKey }.toSet()
        if (completedKeys.isEmpty()) return 0

        val cal = java.util.Calendar.getInstance()
        fun calKey(): Long = cal.get(java.util.Calendar.YEAR).toLong() * 1000L +
            cal.get(java.util.Calendar.DAY_OF_YEAR).toLong()

        val todayKey = TacticalPuzzleGenerator.todayDayKey()
        if (todayKey !in completedKeys) {
            cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
            if (calKey() !in completedKeys) return 0
        }

        var streak = 0
        while (calKey() in completedKeys) {
            streak++
            cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        }
        return streak
    }
}
