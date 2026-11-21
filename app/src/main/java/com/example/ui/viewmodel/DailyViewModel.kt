package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.TactixRepository
import com.example.domain.AiDifficulty
import com.example.domain.BoardSnapshot
import com.example.domain.GameEngine
import com.example.domain.MoveHint
import com.example.domain.Player
import com.example.domain.TacticalAiSolver
import com.example.domain.TacticalPuzzle
import com.example.domain.TacticalPuzzleGenerator
import com.example.ui.util.HapticsController
import com.example.ui.util.TactixHaptic
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PuzzlePlayStatus {
    PLAYING,
    AI_DEFENDING,
    SOLVED,
    WRONG_MOVE,
}

data class DailyUiState(
    val dayKey: Long = TacticalPuzzleGenerator.todayDayKey(),
    val dailyPuzzles: List<TacticalPuzzle> = emptyList(),
    val selectedDailyIndex: Int = 0,
    val solvedMask: Int = 0,
    val dailyStreak: Int = 0,
    val sevenDayCompletion: List<Boolean> = List(7) { false },
    val isRushMode: Boolean = false,
    val rushStreak: Int = 0,
    val bestRushStreak: Int = 0,
    val activePuzzle: TacticalPuzzle? = null,
    val snapshot: BoardSnapshot = BoardSnapshot(),
    val playerMovesMade: Int = 0,
    val status: PuzzlePlayStatus = PuzzlePlayStatus.PLAYING,
    val feedbackMessage: String? = null,
    val activeHint: MoveHint? = null,
    val shakeCounter: Int = 0,
    val hapticsEnabled: Boolean = true,
    val selectedSkin: com.example.domain.PieceSkin = com.example.domain.PieceSkin.CLASSIC,
) {
    val isDailyComplete: Boolean
        get() = (solvedMask and 0b111) == 0b111

    fun isPuzzleSolved(index: Int): Boolean = (solvedMask and (1 shl index)) != 0
}

class DailyViewModel(
    private val repository: TactixRepository,
    private val haptics: HapticsController,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailyUiState())
    val uiState: StateFlow<DailyUiState> = _uiState.asStateFlow()

    private var rushSeed: Long = System.currentTimeMillis()
    private var transitionJob: Job? = null

    init {
        val today = TacticalPuzzleGenerator.todayDayKey()
        val puzzles = TacticalPuzzleGenerator.generateDailySet(today)
        _uiState.update {
            it.copy(
                dayKey = today,
                dailyPuzzles = puzzles,
                selectedDailyIndex = 0,
                activePuzzle = puzzles.first(),
                snapshot = puzzles.first().toInitialSnapshot(),
            )
        }

        viewModelScope.launch {
            repository.playerProfile.collect { profile ->
                _uiState.update {
                    it.copy(
                        bestRushStreak = profile.bestStreakRush,
                        hapticsEnabled = profile.hapticsEnabled,
                        selectedSkin = com.example.domain.PieceSkin.fromId(profile.selectedSkinId),
                    )
                }
            }
        }

        viewModelScope.launch {
            repository.dailyProgressHistory.collect { records ->
                val todayKey = _uiState.value.dayKey
                val todayRecord = records.firstOrNull { it.dayKey == todayKey }
                val completedKeys = records.filter { it.isCompleted }.map { it.dayKey }.toSet()
                val sevenDays = TacticalPuzzleGenerator.recentSevenDayKeys(todayKey).map { it in completedKeys }
                val streak = repository.calculateCurrentDailyStreak(records)

                _uiState.update { state ->
                    state.copy(
                        solvedMask = todayRecord?.solvedMask ?: 0,
                        dailyStreak = streak,
                        sevenDayCompletion = sevenDays,
                    )
                }
            }
        }
    }

    fun onSelectDailyPuzzle(index: Int) {
        val state = _uiState.value
        if (index !in state.dailyPuzzles.indices) return
        transitionJob?.cancel()
        val puzzle = state.dailyPuzzles[index]
        _uiState.update {
            it.copy(
                isRushMode = false,
                selectedDailyIndex = index,
                activePuzzle = puzzle,
                snapshot = puzzle.toInitialSnapshot(),
                playerMovesMade = 0,
                status = PuzzlePlayStatus.PLAYING,
                feedbackMessage = null,
                activeHint = null,
            )
        }
    }

    fun onStartStreakRush() {
        transitionJob?.cancel()
        rushSeed = System.currentTimeMillis()
        val firstRush = TacticalPuzzleGenerator.generateRushPuzzle(0, rushSeed)
        _uiState.update {
            it.copy(
                isRushMode = true,
                rushStreak = 0,
                activePuzzle = firstRush,
                snapshot = firstRush.toInitialSnapshot(),
                playerMovesMade = 0,
                status = PuzzlePlayStatus.PLAYING,
                feedbackMessage = null,
                activeHint = null,
            )
        }
    }

    fun onReturnToDailySet() {
        val state = _uiState.value
        val nextUnsolved = (0 until state.dailyPuzzles.size).firstOrNull { !state.isPuzzleSolved(it) } ?: 0
        onSelectDailyPuzzle(nextUnsolved)
    }

    fun onRetryCurrentPuzzle() {
        val puzzle = _uiState.value.activePuzzle ?: return
        transitionJob?.cancel()
        _uiState.update {
            it.copy(
                snapshot = puzzle.toInitialSnapshot(),
                playerMovesMade = 0,
                status = PuzzlePlayStatus.PLAYING,
                feedbackMessage = null,
                activeHint = null,
            )
        }
    }

    fun onRequestPuzzleHint() {
        val state = _uiState.value
        val puzzle = state.activePuzzle ?: return
        if (state.status != PuzzlePlayStatus.PLAYING) return
        val hint = TacticalAiSolver.computeHint(state.snapshot, puzzle.mode) ?: return
        haptics.perform(TactixHaptic.HINT_REVEAL, state.hapticsEnabled)
        _uiState.update { it.copy(activeHint = hint) }
    }

    fun onCellTap(cellIndex: Int) {
        val state = _uiState.value
        val puzzle = state.activePuzzle ?: return
        if (state.status != PuzzlePlayStatus.PLAYING) return
        if (state.snapshot.currentPlayer != puzzle.playerToMove) return

        val next = GameEngine.applyMove(state.snapshot, cellIndex, puzzle.mode) ?: return
        val newMovesMade = state.playerMovesMade + 1

        // Case 1: Immediate victory!
        if (next.winner == puzzle.playerToMove) {
            haptics.perform(TactixHaptic.VICTORY, state.hapticsEnabled)
            _uiState.update {
                it.copy(
                    snapshot = next,
                    playerMovesMade = newMovesMade,
                    status = PuzzlePlayStatus.SOLVED,
                    feedbackMessage = "Sharp tactic! Puzzle solved.",
                    activeHint = null,
                )
            }
            handlePuzzleSolved()
            return
        }

        // Case 2: Multi-move tactical puzzle (e.g., Fork Architect in 2 moves)
        if (newMovesMade < puzzle.movesToWin && !next.isGameOver) {
            // Verify that X's move leaves O with NO winning reply AND guarantees X wins next turn!
            val oBest = TacticalAiSolver.evaluateAllMoves(next, puzzle.mode).firstOrNull()
            // From O's perspective, a score <= -900 means X has a forced win regardless of O's defense!
            if (oBest != null && oBest.score <= -900) {
                haptics.perform(TactixHaptic.MOVE_TAP, state.hapticsEnabled)
                _uiState.update {
                    it.copy(
                        snapshot = next,
                        playerMovesMade = newMovesMade,
                        status = PuzzlePlayStatus.AI_DEFENDING,
                        feedbackMessage = "Fork locked! Opponent tries to block…",
                        activeHint = null,
                    )
                }
                scheduleOpponentDefense(next, puzzle)
                return
            }
        }

        // Otherwise: Not the winning tactical move!
        haptics.perform(TactixHaptic.ERROR_SHAKE, state.hapticsEnabled)
        val rushEnded = state.isRushMode
        val wrongReason = if (puzzle.mode == com.example.domain.GameMode.INFINITY &&
            state.snapshot.expiringCell(puzzle.mode) != null
        ) {
            "Your oldest X faded before completing that line! Try again."
        } else if (puzzle.movesToWin > 1 && newMovesMade == 1) {
            "That move lets O escape—look for a square that creates two threats at once."
        } else {
            "Missed the winning line—tap Retry or use a Hint."
        }

        _uiState.update {
            it.copy(
                snapshot = next,
                status = PuzzlePlayStatus.WRONG_MOVE,
                feedbackMessage = wrongReason,
                activeHint = null,
                shakeCounter = it.shakeCounter + 1,
                rushStreak = if (rushEnded) 0 else it.rushStreak,
            )
        }
    }

    private fun scheduleOpponentDefense(afterPlayerMove: BoardSnapshot, puzzle: TacticalPuzzle) {
        transitionJob?.cancel()
        transitionJob = viewModelScope.launch {
            delay(460L)
            val aiCell = TacticalAiSolver.chooseAiMove(
                state = afterPlayerMove,
                mode = puzzle.mode,
                difficulty = AiDifficulty.MASTER,
                playerElo = 1300,
            ) ?: return@launch
            val afterDefense = GameEngine.applyMove(afterPlayerMove, aiCell, puzzle.mode) ?: return@launch
            haptics.perform(TactixHaptic.MOVE_TAP, _uiState.value.hapticsEnabled)
            _uiState.update {
                it.copy(
                    snapshot = afterDefense,
                    status = PuzzlePlayStatus.PLAYING,
                    feedbackMessage = "Finish the fork to win!",
                )
            }
        }
    }

    private fun handlePuzzleSolved() {
        val state = _uiState.value
        if (state.isRushMode) {
            val newRush = state.rushStreak + 1
            _uiState.update { it.copy(rushStreak = newRush) }
            viewModelScope.launch {
                repository.recordStreakRushScore(newRush)
            }
        } else {
            viewModelScope.launch {
                repository.markDailyPuzzleSolved(state.dayKey, state.selectedDailyIndex)
            }
        }
    }

    fun onAdvanceNextPuzzle() {
        val state = _uiState.value
        if (state.isRushMode) {
            val nextRush = TacticalPuzzleGenerator.generateRushPuzzle(state.rushStreak, rushSeed)
            _uiState.update {
                it.copy(
                    activePuzzle = nextRush,
                    snapshot = nextRush.toInitialSnapshot(),
                    playerMovesMade = 0,
                    status = PuzzlePlayStatus.PLAYING,
                    feedbackMessage = null,
                    activeHint = null,
                )
            }
        } else {
            val nextIdx = (state.selectedDailyIndex + 1) % state.dailyPuzzles.size
            onSelectDailyPuzzle(nextIdx)
        }
    }
}
