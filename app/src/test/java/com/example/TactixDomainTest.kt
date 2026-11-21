package com.example

import com.example.domain.AiDifficulty
import com.example.domain.BoardSnapshot
import com.example.domain.GameEngine
import com.example.domain.GameMode
import com.example.domain.Player
import com.example.domain.TacticalAiSolver
import com.example.domain.TacticalPuzzleGenerator
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TactixDomainTest {

    @Test
    fun infinityModeDropsOldestPieceOnFourthPlacement() {
        var state = GameEngine.createInitialState(Player.X)
        // X plays 0, O plays 1, X plays 3, O plays 4, X plays 8, O plays 5 (neither side has won)
        val moves = listOf(0, 1, 3, 4, 8, 5)
        for (cell in moves) {
            state = GameEngine.applyMove(state, cell, GameMode.INFINITY)!!
        }
        // Now X has 3 pieces [0, 3, 8]; 0 must be marked expiring!
        assertEquals(0, state.expiringCell(GameMode.INFINITY))

        // X places 4th piece at 2 -> 0 must vanish!
        val afterFourth = GameEngine.applyMove(state, 2, GameMode.INFINITY)!!
        assertNull(afterFourth.board[0])
        assertEquals(Player.X, afterFourth.board[2])
        assertEquals(0, afterFourth.lastMove?.expiredCellIndex)
        assertEquals(listOf(3, 8, 2), afterFourth.xQueue)
    }

    @Test
    fun replayReconstructionMatchesFinalBoard() {
        val snapshots = GameEngine.reconstructReplaySnapshots(
            mode = GameMode.CLASSIC,
            startingPlayer = Player.X,
            moveSequenceCsv = "0,3,1,4,2",
        )
        assertEquals(6, snapshots.size)
        assertEquals(Player.X, snapshots.last().winner)
        assertEquals(listOf(0, 1, 2), snapshots.last().winningLine)
    }

    @Test
    fun dailyPuzzleSetsAreAllMathematicallySolvable() {
        for (dayOffset in 0L until 30L) {
            val dayKey = 2026001L + dayOffset
            val puzzles = TacticalPuzzleGenerator.generateDailySet(dayKey)
            assertEquals(3, puzzles.size)

            puzzles.forEach { puzzle ->
                val initial = puzzle.toInitialSnapshot()
                val bestMoves = TacticalAiSolver.evaluateAllMoves(initial, puzzle.mode)
                assertTrue("Puzzle ${puzzle.id} has legal moves", bestMoves.isNotEmpty())
                val best = bestMoves.first()

                if (puzzle.movesToWin == 1) {
                    val after = GameEngine.applyMove(initial, best.cellIndex, puzzle.mode)!!
                    assertEquals(
                        "Win-in-1 puzzle ${puzzle.title} (${puzzle.id}) must win immediately",
                        Player.X,
                        after.winner,
                    )
                } else {
                    val afterFirst = GameEngine.applyMove(initial, best.cellIndex, puzzle.mode)!!
                    assertNull("Fork puzzle ${puzzle.id} should not win on move 1", afterFirst.winner)
                    val oBest = TacticalAiSolver.evaluateAllMoves(afterFirst, puzzle.mode).first()
                    assertTrue(
                        "Fork puzzle ${puzzle.id} must force win regardless of O defense (got score ${oBest.score})",
                        oBest.score <= -900,
                    )
                }

                val hint = TacticalAiSolver.computeHint(initial, puzzle.mode)
                assertNotNull("Hint must be available for ${puzzle.id}", hint)
            }
        }
    }

    @Test
    fun masterAiIsUnbeatableInClassicAsSecondPlayer() {
        fun exploreAllHumanLines(state: BoardSnapshot) {
            if (state.isGameOver) {
                assertNotEquals("Master AI (O) must never lose in Classic 3x3", Player.X, state.winner)
                return
            }
            if (state.currentPlayer == Player.X) {
                for (move in GameEngine.legalMoves(state)) {
                    exploreAllHumanLines(GameEngine.applyMove(state, move, GameMode.CLASSIC)!!)
                }
            } else {
                val aiMove = TacticalAiSolver.chooseAiMove(
                    state = state,
                    mode = GameMode.CLASSIC,
                    difficulty = AiDifficulty.MASTER,
                    playerElo = 1400,
                    random = Random(42),
                )!!
                exploreAllHumanLines(GameEngine.applyMove(state, aiMove, GameMode.CLASSIC)!!)
            }
        }
        exploreAllHumanLines(GameEngine.createInitialState(Player.X))
    }

    @Test
    fun masterAiIsUnbeatableInClassicAsFirstPlayer() {
        fun exploreAllHumanLines(state: BoardSnapshot) {
            if (state.isGameOver) {
                assertNotEquals("Master AI (X) must never lose in Classic 3x3", Player.O, state.winner)
                return
            }
            if (state.currentPlayer == Player.O) {
                for (move in GameEngine.legalMoves(state)) {
                    exploreAllHumanLines(GameEngine.applyMove(state, move, GameMode.CLASSIC)!!)
                }
            } else {
                val aiMove = TacticalAiSolver.chooseAiMove(
                    state = state,
                    mode = GameMode.CLASSIC,
                    difficulty = AiDifficulty.MASTER,
                    playerElo = 1400,
                    random = Random(7),
                )!!
                exploreAllHumanLines(GameEngine.applyMove(state, aiMove, GameMode.CLASSIC)!!)
            }
        }
        exploreAllHumanLines(GameEngine.createInitialState(Player.X))
    }

    @Test
    fun masterAiSpotsInfinityDecayWinAndAvoidsDecoy() {
        // O holds [0 (fades!), 4, 2]; X holds [3, 5, 7]. Playing 1 or 8 is a decoy because 0 fades; 6 wins (2-4-6)!
        val board = MutableList<Player?>(9) { null }.apply {
            this[0] = Player.O
            this[4] = Player.O
            this[2] = Player.O
            this[3] = Player.X
            this[5] = Player.X
            this[7] = Player.X
        }
        val state = BoardSnapshot(
            board = board,
            xQueue = listOf(3, 5, 7),
            oQueue = listOf(0, 4, 2),
            currentPlayer = Player.O,
        )
        val aiMove = TacticalAiSolver.chooseAiMove(
            state = state,
            mode = GameMode.INFINITY,
            difficulty = AiDifficulty.MASTER,
            playerElo = 1200,
            random = Random(1),
        )
        assertEquals("Master AI must choose real Infinity decay win (cell 6)", 6, aiMove)
    }

    @Test
    fun adaptiveAiAtBaseEloAlwaysBlocksImmediateThreat() {
        // X holds [0, 1] threatening 2! O has [4]. Adaptive AI at 1000 ELO must block 2 every single time!
        val board = MutableList<Player?>(9) { null }.apply {
            this[0] = Player.X
            this[1] = Player.X
            this[4] = Player.O
        }
        val state = BoardSnapshot(
            board = board,
            xQueue = listOf(0, 1),
            oQueue = listOf(4),
            currentPlayer = Player.O,
        )
        repeat(25) { trial ->
            val aiMove = TacticalAiSolver.chooseAiMove(
                state = state,
                mode = GameMode.CLASSIC,
                difficulty = AiDifficulty.ADAPTIVE,
                playerElo = 1000,
                random = Random(trial),
            )
            assertEquals("Adaptive AI must always block immediate mate-in-1 (trial $trial)", 2, aiMove)
        }
    }

    @Test
    fun misereModeInvertsWinCondition() {
        var state = GameEngine.createInitialState(Player.X)
        // X completes 0-1-2 -> in Misère, O wins!
        val moves = listOf(0, 3, 1, 4, 2)
        for (cell in moves) {
            state = GameEngine.applyMove(state, cell, GameMode.MISERE)!!
        }
        assertEquals("Completing 3-in-a-row in Misère loses, so O must win", Player.O, state.winner)
        assertEquals(listOf(0, 1, 2), state.winningLine)
    }

    @Test
    fun masterAiIsUnbeatableInMisereAsFirstPlayer() {
        // In 3x3 Misère Tic-Tac-Toe, first player can always force at least a draw with optimal center opening
        fun exploreAllHumanLines(state: BoardSnapshot) {
            if (state.isGameOver) {
                assertNotEquals("Master AI (X) must never lose in Misère", Player.O, state.winner)
                return
            }
            if (state.currentPlayer == Player.O) {
                for (move in GameEngine.legalMoves(state)) {
                    exploreAllHumanLines(GameEngine.applyMove(state, move, GameMode.MISERE)!!)
                }
            } else {
                val aiMove = TacticalAiSolver.chooseAiMove(
                    state = state,
                    mode = GameMode.MISERE,
                    difficulty = AiDifficulty.MASTER,
                    playerElo = 1400,
                    random = Random(13),
                )!!
                exploreAllHumanLines(GameEngine.applyMove(state, aiMove, GameMode.MISERE)!!)
            }
        }
        exploreAllHumanLines(GameEngine.createInitialState(Player.X))
    }

    @Test
    fun matchAnalyzerGradesBlundersAndAccuracy() {
        // X plays 0, O plays 3, X plays 1, O plays 7 (blunder! misses blocking 2), X plays 2 (wins!)
        val analysis = TacticalAiSolver.analyzeMatch(
            mode = GameMode.CLASSIC,
            startingPlayer = Player.X,
            moveSequenceCsv = "0,3,1,7,2",
            targetPlayer = Player.O,
        )
        assertEquals(5, analysis.moves.size)
        assertEquals(com.example.domain.MoveGrade.BLUNDER, analysis.moves[3].grade)
        assertEquals(2, analysis.moves[3].bestCell)
        assertTrue("Blundering match should have reduced accuracy", analysis.accuracyPercent < 75)
    }
}
