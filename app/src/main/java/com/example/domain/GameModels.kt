package com.example.domain

enum class Player(val symbol: String) {
    X("X"),
    O("O");

    fun opponent(): Player = if (this == X) O else X
}

enum class GameMode {
    INFINITY,
    CLASSIC,
    MISERE,
}

enum class OpponentType {
    AI,
    PASS_AND_PLAY,
}

enum class AiDifficulty {
    ADAPTIVE,
    CASUAL,
    TACTICAL,
    MASTER,
}

enum class MatchOutcome {
    X_WIN,
    O_WIN,
    DRAW,
}

enum class PieceSkin(
    val id: String,
    val displayName: String,
    val unlockMedalsRequired: Int,
) {
    CLASSIC("CLASSIC", "Studio", 0),
    PRISM("PRISM", "Prism", 1),
    HALO("HALO", "Dual Halo", 3),
    APEX("APEX", "Apex", 5);

    companion object {
        fun fromId(id: String): PieceSkin = entries.firstOrNull { it.id == id } ?: CLASSIC
    }
}

enum class MasteryMedal(
    val bitIndex: Int,
    val title: String,
    val description: String,
) {
    FIRST_VICTORY(0, "First Win", "Win your first solo match"),
    DECAY_MASTER(1, "Long Game", "Win an Infinity match in 9+ moves"),
    MISERE_GENIUS(2, "Mind Bender", "Win a reverse Misère match"),
    BLITZ_REFLEX(3, "Speed Demon", "Win a match with the 5s Blitz clock"),
    SURGICAL(4, "Flawless", "Finish a match with 95%+ accuracy"),
    GIANT_SLAYER(5, "Giant Slayer", "Beat Master — or hold a Classic draw"),
    STREAK_KEEPER(6, "Daily Hat Trick", "Solve all 3 daily puzzles in one day"),
    RUSH_LEGEND(7, "Hot Streak", "Reach 5 in a row in Puzzle Rush");

    val bitMask: Int
        get() = 1 shl bitIndex
}

enum class MoveGrade(val label: String) {
    BRILLIANT("Brilliant"),
    BEST("Best"),
    GOOD("Good"),
    INACCURACY("Slip"),
    BLUNDER("Mistake"),
}

data class MoveAnalysis(
    val ply: Int,
    val player: Player,
    val playedCell: Int,
    val bestCell: Int,
    val grade: MoveGrade,
)

data class MatchAnalysis(
    val accuracyPercent: Int,
    val moves: List<MoveAnalysis>,
)

data class MoveEntry(
    val ply: Int,
    val player: Player,
    val cellIndex: Int,
    val expiredCellIndex: Int? = null,
)

data class MoveHint(
    val cellIndex: Int,
    val reason: String,
    val isWinningMove: Boolean = false,
)

data class BoardSnapshot(
    val board: List<Player?> = List(9) { null },
    val xQueue: List<Int> = emptyList(),
    val oQueue: List<Int> = emptyList(),
    val currentPlayer: Player = Player.X,
    val lastMove: MoveEntry? = null,
    val winningLine: List<Int>? = null,
    val winner: Player? = null,
    val isDraw: Boolean = false,
    val endedByTimeout: Boolean = false,
) {
    val isGameOver: Boolean
        get() = winner != null || isDraw

    fun expiringCell(mode: GameMode): Int? {
        if (mode != GameMode.INFINITY || isGameOver) return null
        val queue = if (currentPlayer == Player.X) xQueue else oQueue
        return if (queue.size >= 3) queue.firstOrNull() else null
    }
}

data class TacticalPuzzle(
    val id: String,
    val title: String,
    val objective: String,
    val mode: GameMode,
    val initialXQueue: List<Int>,
    val initialOQueue: List<Int>,
    val playerToMove: Player,
    val movesToWin: Int,
    val difficultyLabel: String,
) {
    fun toInitialSnapshot(): BoardSnapshot {
        val cells = MutableList<Player?>(9) { null }
        initialXQueue.forEach { cells[it] = Player.X }
        initialOQueue.forEach { cells[it] = Player.O }
        return BoardSnapshot(
            board = cells,
            xQueue = initialXQueue,
            oQueue = initialOQueue,
            currentPlayer = playerToMove,
        )
    }
}
