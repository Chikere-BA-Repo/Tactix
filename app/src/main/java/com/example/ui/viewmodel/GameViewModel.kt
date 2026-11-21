package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MatchRecordEntity
import com.example.data.TactixRepository
import com.example.domain.AiDifficulty
import com.example.domain.BoardSnapshot
import com.example.domain.GameEngine
import com.example.domain.GameMode
import com.example.domain.MasteryMedal
import com.example.domain.MatchOutcome
import com.example.domain.MoveEntry
import com.example.domain.MoveHint
import com.example.domain.OpponentType
import com.example.domain.PieceSkin
import com.example.domain.Player
import com.example.domain.TacticalAiSolver
import com.example.ui.util.HapticsController
import com.example.ui.util.TactixHaptic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class GameUiState(
    val snapshot: BoardSnapshot = GameEngine.createInitialState(Player.X),
    val mode: GameMode = GameMode.INFINITY,
    val opponentType: OpponentType = OpponentType.AI,
    val aiDifficulty: AiDifficulty = AiDifficulty.ADAPTIVE,
    val humanSide: Player = Player.X,
    val startingPlayer: Player = Player.X,
    val moveHistory: List<MoveEntry> = emptyList(),
    val isAiThinking: Boolean = false,
    val activeHint: MoveHint? = null,
    val sessionXWins: Int = 0,
    val sessionOWins: Int = 0,
    val sessionDraws: Int = 0,
    val lastSavedMatch: MatchRecordEntity? = null,
    val lastEloDelta: Int? = null,
    val lastAccuracyPercent: Int? = null,
    val newlyUnlockedMedals: List<MasteryMedal> = emptyList(),
    val hapticsEnabled: Boolean = true,
    val playerElo: Int = 1000,
    val currentWinStreak: Int = 0,
    val selectedSkin: PieceSkin = PieceSkin.CLASSIC,
    val isBlitzEnabled: Boolean = false,
    val blitzRemainingMs: Long = BLITZ_TURN_MS,
    val isBlitzClockRunning: Boolean = false,
    val hasCompletedOnboarding: Boolean? = null,
) {
    val sessionHumanAdvantage: Int
        get() = if (humanSide == Player.X) sessionXWins - sessionOWins else sessionOWins - sessionXWins

    val adaptiveIntensityLabel: String
        get() = TacticalAiSolver.adaptiveIntensityLabel(playerElo, sessionHumanAdvantage)

    val blitzProgress: Float
        get() = (blitzRemainingMs.toFloat() / BLITZ_TURN_MS.toFloat()).coerceIn(0f, 1f)

    val hasActiveUnfinishedMatch: Boolean
        get() = moveHistory.isNotEmpty() && !snapshot.isGameOver

    companion object {
        const val BLITZ_TURN_MS: Long = 5_000L
    }
}

class GameViewModel(
    private val repository: TactixRepository,
    private val haptics: HapticsController,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var aiTurnJob: Job? = null
    private var blitzTimerJob: Job? = null
    private val recentStateKeys = ArrayDeque<Int>(16)

    init {
        viewModelScope.launch {
            repository.playerProfile.collect { profile ->
                _uiState.update {
                    it.copy(
                        hapticsEnabled = profile.hapticsEnabled,
                        playerElo = profile.eloRating,
                        currentWinStreak = profile.currentWinStreak,
                        selectedSkin = PieceSkin.fromId(profile.selectedSkinId),
                        hasCompletedOnboarding = profile.hasCompletedOnboarding,
                    )
                }
            }
        }
    }

    fun completeOnboarding() {
        haptics.perform(TactixHaptic.VICTORY, _uiState.value.hapticsEnabled)
        _uiState.update { it.copy(hasCompletedOnboarding = true) }
        viewModelScope.launch {
            repository.completeOnboarding()
        }
    }

    fun performTapHaptic() {
        haptics.perform(TactixHaptic.MOVE_TAP, _uiState.value.hapticsEnabled)
    }

    fun performCelebrationHaptic() {
        haptics.perform(TactixHaptic.VICTORY, _uiState.value.hapticsEnabled)
    }

    fun configureOpponentFromHub(opponentType: OpponentType) {
        if (_uiState.value.opponentType == opponentType) return
        haptics.perform(TactixHaptic.MOVE_TAP, _uiState.value.hapticsEnabled)
        _uiState.update {
            it.copy(
                opponentType = opponentType,
                humanSide = if (opponentType == OpponentType.PASS_AND_PLAY) Player.X else it.humanSide,
            )
        }
    }

    fun configureDifficultyFromHub(difficulty: AiDifficulty) {
        if (_uiState.value.aiDifficulty == difficulty) return
        haptics.perform(TactixHaptic.MOVE_TAP, _uiState.value.hapticsEnabled)
        _uiState.update { it.copy(aiDifficulty = difficulty) }
    }

    fun configureBlitzFromHub() {
        val next = !_uiState.value.isBlitzEnabled
        haptics.perform(TactixHaptic.HINT_REVEAL, _uiState.value.hapticsEnabled)
        _uiState.update {
            it.copy(
                isBlitzEnabled = next,
                blitzRemainingMs = GameUiState.BLITZ_TURN_MS,
                isBlitzClockRunning = false,
            )
        }
    }

    fun configureHumanSideFromHub() {
        if (_uiState.value.opponentType != OpponentType.AI) return
        haptics.perform(TactixHaptic.MOVE_TAP, _uiState.value.hapticsEnabled)
        _uiState.update { it.copy(humanSide = it.humanSide.opponent()) }
    }

    fun launchModeFromHub(mode: GameMode) {
        aiTurnJob?.cancel()
        stopBlitzClock()
        haptics.perform(TactixHaptic.MOVE_TAP, _uiState.value.hapticsEnabled)
        val modeChanged = _uiState.value.mode != mode
        _uiState.update {
            it.copy(
                mode = mode,
                sessionXWins = if (modeChanged) 0 else it.sessionXWins,
                sessionOWins = if (modeChanged) 0 else it.sessionOWins,
                sessionDraws = if (modeChanged) 0 else it.sessionDraws,
            )
        }
        startNewRound()
    }

    fun pauseArena() {
        aiTurnJob?.cancel()
        aiTurnJob = null
        stopBlitzClock()
        _uiState.update { it.copy(isAiThinking = false) }
    }

    fun resumeArena() {
        val state = _uiState.value
        if (state.snapshot.isGameOver) return
        if (state.opponentType == OpponentType.AI && state.snapshot.currentPlayer != state.humanSide) {
            scheduleAiMove()
        } else if (state.isBlitzEnabled && state.moveHistory.isNotEmpty()) {
            startBlitzClock()
        }
    }

    fun onCellTap(cellIndex: Int) {
        val state = _uiState.value
        if (state.isAiThinking || state.snapshot.isGameOver) return
        if (state.opponentType == OpponentType.AI && state.snapshot.currentPlayer != state.humanSide) return

        val next = GameEngine.applyMove(
            state = state.snapshot,
            cellIndex = cellIndex,
            mode = state.mode,
            plyNumber = state.moveHistory.size + 1,
        ) ?: return

        stopBlitzClock()
        recordStateKey(next, state.mode)
        val entry = next.lastMove!!
        val updatedMoves = state.moveHistory + entry

        if (entry.expiredCellIndex != null) {
            haptics.perform(TactixHaptic.DECAY_SWAP, state.hapticsEnabled)
        } else {
            haptics.perform(TactixHaptic.MOVE_TAP, state.hapticsEnabled)
        }

        _uiState.update {
            it.copy(
                snapshot = next,
                moveHistory = updatedMoves,
                activeHint = null,
                blitzRemainingMs = GameUiState.BLITZ_TURN_MS,
            )
        }

        if (next.isGameOver) {
            handleMatchCompleted(next, updatedMoves)
        } else if (state.opponentType == OpponentType.AI && next.currentPlayer != state.humanSide) {
            scheduleAiMove()
        } else if (state.isBlitzEnabled) {
            startBlitzClock()
        }
    }

    fun onSelectMode(mode: GameMode) {
        if (_uiState.value.mode == mode) return
        aiTurnJob?.cancel()
        stopBlitzClock()
        _uiState.update {
            it.copy(
                mode = mode,
                sessionXWins = 0,
                sessionOWins = 0,
                sessionDraws = 0,
            )
        }
        startNewRound()
    }

    fun onSelectOpponent(opponentType: OpponentType) {
        if (_uiState.value.opponentType == opponentType) return
        aiTurnJob?.cancel()
        stopBlitzClock()
        _uiState.update {
            it.copy(
                opponentType = opponentType,
                humanSide = Player.X,
                sessionXWins = 0,
                sessionOWins = 0,
                sessionDraws = 0,
            )
        }
        startNewRound()
    }

    fun onSelectDifficulty(difficulty: AiDifficulty) {
        if (_uiState.value.aiDifficulty == difficulty) return
        _uiState.update { it.copy(aiDifficulty = difficulty) }
        if (_uiState.value.moveHistory.isNotEmpty() || _uiState.value.snapshot.isGameOver) {
            startNewRound()
        }
    }

    fun onToggleBlitz() {
        val enabling = !_uiState.value.isBlitzEnabled
        stopBlitzClock()
        _uiState.update {
            it.copy(
                isBlitzEnabled = enabling,
                blitzRemainingMs = GameUiState.BLITZ_TURN_MS,
                isBlitzClockRunning = false,
            )
        }
        haptics.perform(TactixHaptic.HINT_REVEAL, _uiState.value.hapticsEnabled)
        if (enabling && _uiState.value.moveHistory.isNotEmpty() &&
            !_uiState.value.snapshot.isGameOver && !_uiState.value.isAiThinking
        ) {
            startBlitzClock()
        }
    }

    fun onSwapHumanSide() {
        val current = _uiState.value
        if (current.opponentType != OpponentType.AI) return
        val nextSide = current.humanSide.opponent()
        _uiState.update { it.copy(humanSide = nextSide) }
        startNewRound()
    }

    fun onRequestHint() {
        val state = _uiState.value
        if (state.isAiThinking || state.snapshot.isGameOver) return
        viewModelScope.launch {
            val hint = withContext(Dispatchers.Default) {
                TacticalAiSolver.computeHint(state.snapshot, state.mode)
            } ?: return@launch
            haptics.perform(TactixHaptic.HINT_REVEAL, _uiState.value.hapticsEnabled)
            _uiState.update { it.copy(activeHint = hint) }
        }
    }

    fun onDismissHint() {
        _uiState.update { it.copy(activeHint = null) }
    }

    fun onToggleHaptics() {
        viewModelScope.launch {
            val enabled = repository.toggleHaptics()
            if (enabled) {
                haptics.perform(TactixHaptic.MOVE_TAP, true)
            }
        }
    }

    fun buildShareSummary(): String {
        val state = _uiState.value
        val modeName = when (state.mode) {
            GameMode.INFINITY -> "Infinity"
            GameMode.CLASSIC -> "Classic"
            GameMode.MISERE -> "Misère"
        }
        val blitzTag = if (state.isBlitzEnabled) " ⚡5s Blitz" else ""
        val outcomeLine = when {
            state.snapshot.isDraw -> "Tie Game"
            state.opponentType == OpponentType.AI && state.snapshot.winner == state.humanSide -> "I Won!"
            state.opponentType == OpponentType.AI -> "Tough Loss"
            state.snapshot.winner == Player.X -> "Player X Wins"
            else -> "Player O Wins"
        }
        val accuracyPart = state.lastAccuracyPercent?.let { " • $it% Accuracy" } ?: ""
        val grid = GameEngine.formatShareBoard(state.snapshot)
        return "Tactix — $modeName$blitzTag\n$outcomeLine (${state.moveHistory.size} moves$accuracyPart)\n\n$grid\n\nA Chikere Asogwa Solutions Game"
    }

    fun startNewRound() {
        aiTurnJob?.cancel()
        stopBlitzClock()
        recentStateKeys.clear()
        val state = _uiState.value
        val freshBoard = GameEngine.createInitialState(Player.X)
        recordStateKey(freshBoard, state.mode)
        _uiState.update {
            it.copy(
                snapshot = freshBoard,
                startingPlayer = Player.X,
                moveHistory = emptyList(),
                isAiThinking = false,
                activeHint = null,
                lastSavedMatch = null,
                lastEloDelta = null,
                lastAccuracyPercent = null,
                newlyUnlockedMedals = emptyList(),
                blitzRemainingMs = GameUiState.BLITZ_TURN_MS,
                isBlitzClockRunning = false,
            )
        }
        if (state.opponentType == OpponentType.AI && state.humanSide == Player.O) {
            scheduleAiMove()
        }
    }

    private fun startBlitzClock() {
        blitzTimerJob?.cancel()
        _uiState.update {
            it.copy(
                blitzRemainingMs = GameUiState.BLITZ_TURN_MS,
                isBlitzClockRunning = true,
            )
        }
        blitzTimerJob = viewModelScope.launch {
            var remaining = GameUiState.BLITZ_TURN_MS
            var warnedUrgent = false
            val stepMs = 50L
            while (remaining > 0L) {
                delay(stepMs)
                remaining = (remaining - stepMs).coerceAtLeast(0L)
                if (!warnedUrgent && remaining <= 1_500L && remaining > 0L) {
                    warnedUrgent = true
                    haptics.perform(TactixHaptic.HINT_REVEAL, _uiState.value.hapticsEnabled)
                }
                _uiState.update { it.copy(blitzRemainingMs = remaining) }
            }
            handleBlitzTimeout()
        }
    }

    private fun stopBlitzClock() {
        blitzTimerJob?.cancel()
        blitzTimerJob = null
        _uiState.update { it.copy(isBlitzClockRunning = false) }
    }

    private fun handleBlitzTimeout() {
        val state = _uiState.value
        if (state.snapshot.isGameOver || state.isAiThinking) return
        val winnerOnTime = state.snapshot.currentPlayer.opponent()
        val timedOutSnapshot = state.snapshot.copy(
            winner = winnerOnTime,
            isDraw = false,
            endedByTimeout = true,
        )
        haptics.perform(TactixHaptic.ERROR_SHAKE, state.hapticsEnabled)
        _uiState.update {
            it.copy(
                snapshot = timedOutSnapshot,
                isBlitzClockRunning = false,
                blitzRemainingMs = 0L,
                activeHint = null,
            )
        }
        handleMatchCompleted(timedOutSnapshot, state.moveHistory)
    }

    private fun recordStateKey(snapshot: BoardSnapshot, mode: GameMode) {
        val key = TacticalAiSolver.encodeStateKey(snapshot, mode)
        if (recentStateKeys.size >= 14) {
            recentStateKeys.removeFirst()
        }
        recentStateKeys.addLast(key)
    }

    private fun scheduleAiMove() {
        aiTurnJob?.cancel()
        aiTurnJob = viewModelScope.launch {
            _uiState.update { it.copy(isAiThinking = true) }
            val startMs = System.currentTimeMillis()
            val current = _uiState.value
            if (current.snapshot.isGameOver) {
                _uiState.update { it.copy(isAiThinking = false) }
                return@launch
            }
            val historySnapshot = recentStateKeys.toSet()
            val aiCell = withContext(Dispatchers.Default) {
                TacticalAiSolver.chooseAiMove(
                    state = current.snapshot,
                    mode = current.mode,
                    difficulty = current.aiDifficulty,
                    playerElo = current.playerElo,
                    sessionAdvantage = current.sessionHumanAdvantage,
                    recentStateKeys = historySnapshot,
                )
            }
            val elapsed = System.currentTimeMillis() - startMs
            val targetCadence = if (current.isBlitzEnabled) 240L else 360L
            val remainingCadence = (targetCadence - elapsed).coerceAtLeast(0L)
            if (remainingCadence > 0L) delay(remainingCadence)

            val latest = _uiState.value
            if (aiCell == null || latest.snapshot.isGameOver) {
                _uiState.update { it.copy(isAiThinking = false) }
                return@launch
            }
            val next = GameEngine.applyMove(
                state = latest.snapshot,
                cellIndex = aiCell,
                mode = latest.mode,
                plyNumber = latest.moveHistory.size + 1,
            )
            if (next == null) {
                _uiState.update { it.copy(isAiThinking = false) }
                return@launch
            }
            recordStateKey(next, latest.mode)
            val entry = next.lastMove!!
            val updatedMoves = latest.moveHistory + entry
            if (entry.expiredCellIndex != null) {
                haptics.perform(TactixHaptic.DECAY_SWAP, latest.hapticsEnabled)
            } else {
                haptics.perform(TactixHaptic.MOVE_TAP, latest.hapticsEnabled)
            }
            _uiState.update {
                it.copy(
                    snapshot = next,
                    moveHistory = updatedMoves,
                    isAiThinking = false,
                )
            }
            if (next.isGameOver) {
                handleMatchCompleted(next, updatedMoves)
            } else if (latest.isBlitzEnabled) {
                startBlitzClock()
            }
        }
    }

    private fun handleMatchCompleted(finalSnapshot: BoardSnapshot, moves: List<MoveEntry>) {
        stopBlitzClock()
        val state = _uiState.value
        val outcome = when (finalSnapshot.winner) {
            Player.X -> MatchOutcome.X_WIN
            Player.O -> MatchOutcome.O_WIN
            null -> MatchOutcome.DRAW
        }

        if (!finalSnapshot.endedByTimeout) {
            if (outcome == MatchOutcome.DRAW) {
                haptics.perform(TactixHaptic.DRAW, state.hapticsEnabled)
            } else {
                haptics.perform(TactixHaptic.VICTORY, state.hapticsEnabled)
            }
        }

        _uiState.update {
            it.copy(
                sessionXWins = it.sessionXWins + if (outcome == MatchOutcome.X_WIN) 1 else 0,
                sessionOWins = it.sessionOWins + if (outcome == MatchOutcome.O_WIN) 1 else 0,
                sessionDraws = it.sessionDraws + if (outcome == MatchOutcome.DRAW) 1 else 0,
            )
        }

        viewModelScope.launch {
            val moveCsv = GameEngine.serializeMoveIndices(moves)
            val analysis = withContext(Dispatchers.Default) {
                TacticalAiSolver.analyzeMatch(
                    mode = state.mode,
                    startingPlayer = state.startingPlayer,
                    moveSequenceCsv = moveCsv,
                    targetPlayer = if (state.opponentType == OpponentType.AI) state.humanSide else null,
                )
            }

            val result = repository.recordCompletedMatch(
                mode = state.mode,
                opponentType = state.opponentType,
                aiDifficulty = state.aiDifficulty,
                startingPlayer = state.startingPlayer,
                outcome = outcome,
                humanSide = state.humanSide,
                moves = moves,
                winningLine = finalSnapshot.winningLine,
                isBlitz = state.isBlitzEnabled,
                accuracyPercent = analysis.accuracyPercent,
            )
            _uiState.update {
                it.copy(
                    lastSavedMatch = result.match,
                    lastEloDelta = if (state.opponentType == OpponentType.AI) result.eloDelta else null,
                    lastAccuracyPercent = result.accuracyPercent,
                    newlyUnlockedMedals = result.newlyUnlockedMedals,
                )
            }
        }
    }
}
