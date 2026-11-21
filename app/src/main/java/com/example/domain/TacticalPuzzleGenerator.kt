package com.example.domain

import java.util.Calendar
import kotlin.random.Random

object TacticalPuzzleGenerator {

    fun todayDayKey(): Long {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR).toLong()
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR).toLong()
        return year * 1000L + dayOfYear
    }

    fun recentSevenDayKeys(todayKey: Long = todayDayKey()): List<Long> {
        val cal = Calendar.getInstance()
        val keys = mutableListOf<Long>()
        cal.add(Calendar.DAY_OF_YEAR, -6)
        repeat(7) {
            val y = cal.get(Calendar.YEAR).toLong()
            val d = cal.get(Calendar.DAY_OF_YEAR).toLong()
            keys.add(y * 1000L + d)
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return keys
    }

    fun generateDailySet(dayKey: Long): List<TacticalPuzzle> {
        val rng = Random(dayKey * 31_337L + 17L)
        return listOf(
            buildClassicWinInOne(id = "daily_${dayKey}_0", rng = rng),
            buildClassicForkInTwo(id = "daily_${dayKey}_1", rng = rng),
            buildInfinityTrap(id = "daily_${dayKey}_2", rng = rng),
        )
    }

    fun generateRushPuzzle(streakIndex: Int, sessionSeed: Long): TacticalPuzzle {
        val rng = Random(sessionSeed + streakIndex * 7_919L)
        val id = "rush_${sessionSeed}_$streakIndex"
        return when (streakIndex % 3) {
            0 -> buildClassicWinInOne(id, rng)
            1 -> buildInfinityTrap(id, rng)
            else -> buildClassicForkInTwo(id, rng)
        }
    }

    private fun buildClassicWinInOne(id: String, rng: Random): TacticalPuzzle {
        // Generate symmetry-transformed verified win-in-1 boards with a rival threat to keep tension high
        val templates = listOf(
            Pair(listOf(0, 4), listOf(1, 2)), // Win at 8 (diag), rival also threatens? Wait 1,2 -> rival threatens 0 (blocked)
            Pair(listOf(0, 6), listOf(1, 4)), // Win at 3; rival threatens 7!
            Pair(listOf(2, 4), listOf(0, 1)), // Win at 6; rival threatens 2 (occupied) or swap
            Pair(listOf(0, 2, 6), listOf(1, 4, 5)), // Win at 3
            Pair(listOf(0, 4, 5), listOf(1, 2, 3)), // Win at 8
            Pair(listOf(2, 5, 6), listOf(0, 4, 7)), // Win at 8
        )
        val base = templates[rng.nextInt(templates.size)]
        val transform = randomSymmetry(rng)
        val xQueue = base.first.map { transform[it] }
        val oQueue = base.second.map { transform[it] }
        return TacticalPuzzle(
            id = id,
            title = "Quick Finish",
            objective = "Spot the winning square for X.",
            mode = GameMode.CLASSIC,
            initialXQueue = xQueue,
            initialOQueue = oQueue,
            playerToMove = Player.X,
            movesToWin = 1,
            difficultyLabel = "Warm-Up",
        )
    }

    private fun buildClassicForkInTwo(id: String, rng: Random): TacticalPuzzle {
        val templates = listOf(
            // X=[0, 4], O=[1, 8] -> X plays 6 or 3 to fork two empty winning lines
            Pair(listOf(0, 4), listOf(1, 8)),
            // X=[0, 6], O=[3, 8] -> X plays 2 to fork 1 (0-1-2) and 4 (2-4-6)
            Pair(listOf(0, 6), listOf(3, 8)),
            // X=[0, 7], O=[1, 3] -> X plays 8 to fork 4 (0-4-8) and 6 (6-7-8)
            Pair(listOf(0, 7), listOf(1, 3)),
            // X=[0, 5], O=[1, 7] -> X plays 4 to simultaneously block 1-4-7 and fork 3 & 8
            Pair(listOf(0, 5), listOf(1, 7)),
            // X=[2, 7], O=[1, 5] -> X plays 6 to fork 4 (2-4-6) and 8 (6-7-8)
            Pair(listOf(2, 7), listOf(1, 5)),
            // X=[0, 2], O=[1, 5] -> X plays 6 to fork 3 (0-3-6) and 4 (2-4-6)
            Pair(listOf(0, 2), listOf(1, 5)),
        )
        val base = templates[rng.nextInt(templates.size)]
        val transform = randomSymmetry(rng)
        val xQueue = base.first.map { transform[it] }
        val oQueue = base.second.map { transform[it] }
        return TacticalPuzzle(
            id = id,
            title = "Double Threat",
            objective = "Set up two winning lines at once to win in 2 moves.",
            mode = GameMode.CLASSIC,
            initialXQueue = xQueue,
            initialOQueue = oQueue,
            playerToMove = Player.X,
            movesToWin = 2,
            difficultyLabel = "Tricky",
        )
    }

    private fun buildInfinityTrap(id: String, rng: Random): TacticalPuzzle {
        // X already holds 3 marks; xQueue[0] fades on placement. Each board includes empty decoy wins!
        val templates = listOf(
            Pair(listOf(0, 4, 2), listOf(3, 5, 7)), // 0 fades -> decoys 1, 8 fail; 6 wins (2-4-6)
            Pair(listOf(2, 4, 8), listOf(1, 3, 7)), // 2 fades -> decoys 5, 6 fail; 0 wins (0-4-8)
            Pair(listOf(6, 1, 4), listOf(0, 3, 8)), // 6 fades -> decoy 2 fails; 7 wins (1-4-7)
            Pair(listOf(8, 0, 3), listOf(1, 2, 5)), // 8 fades -> decoy 4 fails; 6 wins (0-3-6)
            Pair(listOf(0, 6, 8), listOf(1, 2, 5)), // 0 fades -> decoys 3, 4 fail; 7 wins (6-7-8)
            Pair(listOf(0, 2, 5), listOf(3, 6, 7)), // 0 fades -> decoy 1 fails; 8 wins (2-5-8)
        )
        val base = templates[rng.nextInt(templates.size)]
        val transform = randomSymmetry(rng)
        val xQueue = base.first.map { transform[it] }
        val oQueue = base.second.map { transform[it] }
        return TacticalPuzzle(
            id = id,
            title = "Vanishing Act",
            objective = "Watch your pulsing X — win with the marks that stay.",
            mode = GameMode.INFINITY,
            initialXQueue = xQueue,
            initialOQueue = oQueue,
            playerToMove = Player.X,
            movesToWin = 1,
            difficultyLabel = "Infinity",
        )
    }

    private fun randomSymmetry(rng: Random): List<Int> {
        val symmetries = listOf(
            listOf(0, 1, 2, 3, 4, 5, 6, 7, 8), // Identity
            listOf(6, 3, 0, 7, 4, 1, 8, 5, 2), // Rotate 90
            listOf(8, 7, 6, 5, 4, 3, 2, 1, 0), // Rotate 180
            listOf(2, 5, 8, 1, 4, 7, 0, 3, 6), // Rotate 270
            listOf(2, 1, 0, 5, 4, 3, 8, 7, 6), // Flip H
            listOf(6, 7, 8, 3, 4, 5, 0, 1, 2), // Flip V
            listOf(0, 3, 6, 1, 4, 7, 2, 5, 8), // Transpose
            listOf(8, 5, 2, 7, 4, 1, 6, 3, 0), // Anti-transpose
        )
        return symmetries[rng.nextInt(symmetries.size)]
    }
}
