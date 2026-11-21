package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MatchRecordEntity
import com.example.data.PlayerProfileEntity
import com.example.data.TactixRepository
import com.example.domain.BoardSnapshot
import com.example.domain.GameEngine
import com.example.domain.GameMode
import com.example.domain.MatchAnalysis
import com.example.domain.MoveAnalysis
import com.example.domain.PieceSkin
import com.example.domain.Player
import com.example.domain.TacticalAiSolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ReplayStudioState(
    val match: MatchRecordEntity,
    val mode: GameMode,
    val snapshots: List<BoardSnapshot>,
    val currentStep: Int = 0,
    val isPlaying: Boolean = false,
    val analysis: MatchAnalysis? = null,
) {
    val activeSnapshot: BoardSnapshot
        get() = snapshots.getOrElse(currentStep) { snapshots.first() }

    val currentMoveAnalysis: MoveAnalysis?
        get() = if (currentStep <= 0) null else analysis?.moves?.firstOrNull { it.ply == currentStep }
}

data class ArchiveUiState(
    val allMatches: List<MatchRecordEntity> = emptyList(),
    val filteredMatches: List<MatchRecordEntity> = emptyList(),
    val selectedFilter: GameMode? = null,
    val profile: PlayerProfileEntity = PlayerProfileEntity(),
    val dailyStreak: Int = 0,
    val replayStudio: ReplayStudioState? = null,
) {
    val selectedSkin: PieceSkin
        get() = PieceSkin.fromId(profile.selectedSkinId)

    val unlockedMedalCount: Int
        get() = Integer.bitCount(profile.unlockedMedalsMask)
}

class ArchiveViewModel(
    private val repository: TactixRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArchiveUiState())
    val uiState: StateFlow<ArchiveUiState> = _uiState.asStateFlow()

    private var playbackJob: Job? = null
    private var analysisJob: Job? = null

    init {
        viewModelScope.launch {
            combine(
                repository.recentMatches,
                repository.playerProfile,
                repository.dailyProgressHistory,
            ) { matches, profile, dailyHistory ->
                Triple(matches, profile, repository.calculateCurrentDailyStreak(dailyHistory))
            }.collect { (matches, profile, streak) ->
                _uiState.update { state ->
                    state.copy(
                        allMatches = matches,
                        filteredMatches = applyFilter(matches, state.selectedFilter),
                        profile = profile,
                        dailyStreak = streak,
                    )
                }
            }
        }
    }

    fun onSelectFilter(filter: GameMode?) {
        _uiState.update { state ->
            state.copy(
                selectedFilter = filter,
                filteredMatches = applyFilter(state.allMatches, filter),
            )
        }
    }

    fun onSelectPieceSkin(skin: PieceSkin) {
        viewModelScope.launch {
            repository.selectPieceSkin(skin)
        }
    }

    fun onDeleteMatch(matchId: Long) {
        viewModelScope.launch {
            repository.deleteMatch(matchId)
        }
    }

    fun openReplay(match: MatchRecordEntity, autoPlay: Boolean = true) {
        playbackJob?.cancel()
        analysisJob?.cancel()
        val mode = runCatching { GameMode.valueOf(match.mode) }.getOrDefault(GameMode.CLASSIC)
        val starter = runCatching { Player.valueOf(match.startingPlayer) }.getOrDefault(Player.X)
        val snapshots = GameEngine.reconstructReplaySnapshots(
            mode = mode,
            startingPlayer = starter,
            moveSequenceCsv = match.moveSequenceCsv,
        )
        _uiState.update {
            it.copy(
                replayStudio = ReplayStudioState(
                    match = match,
                    mode = mode,
                    snapshots = snapshots,
                    currentStep = 0,
                    isPlaying = autoPlay && snapshots.size > 1,
                ),
            )
        }

        // Compute Chess-style move-by-move tactical grading in background for Replay Studio
        analysisJob = viewModelScope.launch {
            val computed = withContext(Dispatchers.Default) {
                TacticalAiSolver.analyzeMatch(
                    mode = mode,
                    startingPlayer = starter,
                    moveSequenceCsv = match.moveSequenceCsv,
                )
            }
            _uiState.update { state ->
                val studio = state.replayStudio ?: return@update state
                if (studio.match.id != match.id) return@update state
                state.copy(replayStudio = studio.copy(analysis = computed))
            }
        }

        if (autoPlay && snapshots.size > 1) {
            startPlaybackLoop()
        }
    }

    fun closeReplay() {
        playbackJob?.cancel()
        analysisJob?.cancel()
        _uiState.update { it.copy(replayStudio = null) }
    }

    fun toggleReplayPlayback() {
        val studio = _uiState.value.replayStudio ?: return
        if (studio.isPlaying) {
            playbackJob?.cancel()
            _uiState.update { it.copy(replayStudio = studio.copy(isPlaying = false)) }
        } else {
            val resetStep = if (studio.currentStep >= studio.snapshots.lastIndex) 0 else studio.currentStep
            _uiState.update {
                it.copy(replayStudio = studio.copy(currentStep = resetStep, isPlaying = true))
            }
            startPlaybackLoop()
        }
    }

    fun stepReplay(delta: Int) {
        val studio = _uiState.value.replayStudio ?: return
        playbackJob?.cancel()
        val target = (studio.currentStep + delta).coerceIn(0, studio.snapshots.lastIndex)
        _uiState.update {
            it.copy(replayStudio = studio.copy(currentStep = target, isPlaying = false))
        }
    }

    fun seekReplay(step: Int) {
        val studio = _uiState.value.replayStudio ?: return
        playbackJob?.cancel()
        val target = step.coerceIn(0, studio.snapshots.lastIndex)
        _uiState.update {
            it.copy(replayStudio = studio.copy(currentStep = target, isPlaying = false))
        }
    }

    private fun startPlaybackLoop() {
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            while (true) {
                delay(780L)
                val studio = _uiState.value.replayStudio ?: break
                if (!studio.isPlaying) break
                val nextStep = studio.currentStep + 1
                if (nextStep >= studio.snapshots.lastIndex) {
                    _uiState.update {
                        it.copy(
                            replayStudio = studio.copy(
                                currentStep = studio.snapshots.lastIndex,
                                isPlaying = false,
                            ),
                        )
                    }
                    break
                } else {
                    _uiState.update {
                        it.copy(replayStudio = studio.copy(currentStep = nextStep))
                    }
                }
            }
        }
    }

    private fun applyFilter(matches: List<MatchRecordEntity>, filter: GameMode?): List<MatchRecordEntity> {
        if (filter == null) return matches
        return matches.filter { it.mode == filter.name }
    }
}
