package com.example.domain

import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

object TacticalAiSolver {

    const val WIN_SCORE = 1000
    private const val MATE_THRESHOLD = 900
    private const val MAX_EXACT_DEPTH = 9
    private const val MASTER_INFINITY_DEPTH = 11
    private const val TACTICAL_INFINITY_DEPTH = 8
    private const val CASUAL_INFINITY_DEPTH = 4
    private const val SEARCH_TIME_BUDGET_MS = 180L

    private const val FLAG_EXACT = 0
    private const val FLAG_LOWER = 1
    private const val FLAG_UPPER = 2

    private data class TTEntry(
        val depth: Int,
        val score: Int,
        val flag: Int,
        val bestMove: Int,
    )

    data class ScoredMove(
        val cellIndex: Int,
        val score: Int,
        val trapScore: Int = 0,
    )

    fun evaluateAllMoves(
        state: BoardSnapshot,
        mode: GameMode,
        maxDepth: Int = if (mode == GameMode.INFINITY) MASTER_INFINITY_DEPTH else MAX_EXACT_DEPTH,
        recentStateKeys: Set<Int> = emptySet(),
    ): List<ScoredMove> {
        val moves = GameEngine.legalMoves(state)
        if (moves.isEmpty()) return emptyList()
        val maximizingPlayer = state.currentPlayer
        val tt = HashMap<Int, TTEntry>(2048)
        val deadline = System.currentTimeMillis() + SEARCH_TIME_BUDGET_MS

        var latestScores = moves.associateWith { 0 }.toMutableMap()

        if (mode != GameMode.INFINITY) {
            // Exact full-tree Alpha-Beta for Classic & Misère
            val ordered = orderMoves(state, moves, mode, ttBestMove = -1)
            for (cell in ordered) {
                val next = GameEngine.applyMove(state, cell, mode) ?: continue
                val score = if (next.winner == maximizingPlayer) {
                    WIN_SCORE
                } else if (next.winner != null) {
                    -WIN_SCORE
                } else {
                    minimax(
                        state = next,
                        mode = mode,
                        depth = 1,
                        maxDepth = maxDepth,
                        alpha = -WIN_SCORE * 2,
                        beta = WIN_SCORE * 2,
                        maximizingPlayer = maximizingPlayer,
                        tt = tt,
                        deadlineMs = Long.MAX_VALUE,
                    )
                }
                latestScores[cell] = score
            }
        } else {
            // Iterative deepening for Infinity Mode with Transposition Table & time budget
            val depths = (2..maxDepth step 2).toMutableList()
            if (depths.lastOrNull() != maxDepth) depths.add(maxDepth)

            for (targetDepth in depths) {
                if (targetDepth > 4 && System.currentTimeMillis() >= deadline) break
                val currentPass = HashMap<Int, Int>(moves.size)
                val ordered = moves.sortedByDescending { latestScores[it] ?: 0 }
                var completedPass = true

                for (cell in ordered) {
                    if (targetDepth > 4 && System.currentTimeMillis() >= deadline) {
                        completedPass = false
                        break
                    }
                    val next = GameEngine.applyMove(state, cell, mode) ?: continue
                    val score = if (next.winner == maximizingPlayer) {
                        WIN_SCORE
                    } else {
                        minimax(
                            state = next,
                            mode = mode,
                            depth = 1,
                            maxDepth = targetDepth,
                            alpha = -WIN_SCORE * 2,
                            beta = WIN_SCORE * 2,
                            maximizingPlayer = maximizingPlayer,
                            tt = tt,
                            deadlineMs = if (targetDepth <= 4) Long.MAX_VALUE else deadline,
                        )
                    }
                    currentPass[cell] = score
                }
                if (completedPass) {
                    latestScores = currentPass
                    if (latestScores.values.any { it >= MATE_THRESHOLD }) break
                }
            }
        }

        val bestScore = latestScores.values.maxOrNull() ?: 0

        return moves.map { cell ->
            val score = latestScores[cell] ?: 0
            val trap = if (score >= bestScore - 35) {
                computeTrapScore(
                    parentState = state,
                    cellIndex = cell,
                    mode = mode,
                    aiPlayer = maximizingPlayer,
                    recentStateKeys = recentStateKeys,
                )
            } else {
                0
            }
            ScoredMove(cellIndex = cell, score = score, trapScore = trap)
        }.sortedWith(
            compareByDescending<ScoredMove> { it.score }
                .thenByDescending { it.trapScore },
        )
    }

    fun chooseAiMove(
        state: BoardSnapshot,
        mode: GameMode,
        difficulty: AiDifficulty,
        playerElo: Int,
        sessionAdvantage: Int = 0,
        recentStateKeys: Set<Int> = emptySet(),
        random: Random = Random.Default,
    ): Int? {
        val effectiveElo = (playerElo + sessionAdvantage * 45).coerceIn(700, 1800)
        val searchDepth = when {
            mode != GameMode.INFINITY -> MAX_EXACT_DEPTH
            difficulty == AiDifficulty.MASTER -> MASTER_INFINITY_DEPTH
            difficulty == AiDifficulty.TACTICAL -> TACTICAL_INFINITY_DEPTH
            difficulty == AiDifficulty.CASUAL -> CASUAL_INFINITY_DEPTH
            else -> when {
                effectiveElo >= 1120 -> MASTER_INFINITY_DEPTH
                effectiveElo >= 980 -> 9
                else -> 7
            }
        }

        val scored = evaluateAllMoves(
            state = state,
            mode = mode,
            maxDepth = searchDepth,
            recentStateKeys = recentStateKeys,
        )
        if (scored.isEmpty()) return null

        val top = scored.first()

        if (top.score >= WIN_SCORE - 1) {
            if (difficulty != AiDifficulty.CASUAL || random.nextFloat() < 0.88f) {
                return top.cellIndex
            }
        }

        val safeMoves = scored.filter { it.score > -MATE_THRESHOLD }
        val pool = if (safeMoves.isNotEmpty()) safeMoves else scored

        return when (difficulty) {
            AiDifficulty.MASTER -> {
                val bestTier = scored.filter {
                    it.score == top.score && it.trapScore >= top.trapScore - 4
                }
                bestTier.random(random).cellIndex
            }

            AiDifficulty.TACTICAL -> {
                if (random.nextFloat() < 0.90f || pool.size == 1) {
                    top.cellIndex
                } else {
                    pool[1].cellIndex
                }
            }

            AiDifficulty.ADAPTIVE -> {
                val precision = (0.76f + (effectiveElo - 960) / 700f).coerceIn(0.64f, 0.99f)
                val neverBlunderSafe = effectiveElo >= 920
                if (random.nextFloat() <= precision || scored.size == 1) {
                    val bestTier = scored.filter { it.score == top.score && it.trapScore >= top.trapScore - 6 }
                    bestTier.random(random).cellIndex
                } else if (neverBlunderSafe) {
                    if (pool.size <= 1) {
                        pool.first().cellIndex
                    } else {
                        pool.drop(1).take(2).random(random).cellIndex
                    }
                } else {
                    scored.drop(1).take(2).random(random).cellIndex
                }
            }

            AiDifficulty.CASUAL -> {
                val isForcedSafe = safeMoves.isNotEmpty() && safeMoves.size < scored.size
                if (isForcedSafe && random.nextFloat() < 0.75f) {
                    safeMoves.first().cellIndex
                } else if (random.nextFloat() < 0.58f) {
                    top.cellIndex
                } else {
                    scored.drop(1).take(3).ifEmpty { listOf(top) }.random(random).cellIndex
                }
            }
        }
    }

    fun adaptiveIntensityLabel(playerElo: Int, sessionAdvantage: Int): String {
        val effective = playerElo + sessionAdvantage * 45
        return when {
            effective >= 1180 -> "Pro Pace"
            effective >= 1060 -> "Fierce"
            effective >= 960 -> "Sharp"
            else -> "Steady"
        }
    }

    fun computeHint(state: BoardSnapshot, mode: GameMode): MoveHint? {
        if (state.isGameOver) return null
        val scored = evaluateAllMoves(state, mode)
        if (scored.isEmpty()) return null

        val best = scored.first()
        val player = state.currentPlayer
        val opponent = player.opponent()
        val afterBest = GameEngine.applyMove(state, best.cellIndex, mode) ?: return null

        if (mode == GameMode.MISERE) {
            if (best.score >= MATE_THRESHOLD) {
                return MoveHint(
                    cellIndex = best.cellIndex,
                    reason = "Clever trap! Leaves ${opponent.symbol} with only losing moves.",
                    isWinningMove = true,
                )
            }
            val safeCount = scored.count { it.score > -MATE_THRESHOLD }
            if (safeCount == 1) {
                return MoveHint(
                    cellIndex = best.cellIndex,
                    reason = "Only safe square left — every other spot makes 3-in-a-row!",
                )
            }
            val oppSelfLossAfter = GameEngine.countImmediateSelfLossMoves(afterBest, opponent, mode)
            if (oppSelfLossAfter.size >= 2) {
                return MoveHint(
                    cellIndex = best.cellIndex,
                    reason = "Squeezes the board and cuts off ${opponent.symbol}'s safe spots.",
                )
            }
            return MoveHint(
                cellIndex = best.cellIndex,
                reason = "Keeps you safe while leaving ${opponent.symbol} fewer good moves.",
            )
        }

        if (afterBest.winner == player) {
            val expired = afterBest.lastMove?.expiredCellIndex
            val reason = if (expired != null) {
                "Fades your oldest mark and completes 3-in-a-row!"
            } else {
                "Play here to win right now!"
            }
            return MoveHint(best.cellIndex, reason, isWinningMove = true)
        }

        val opponentWinsNow = GameEngine.countImmediateWinningMoves(state, opponent, mode)
        val myThreatsAfter = GameEngine.countImmediateWinningMoves(afterBest, player, mode)

        if (opponentWinsNow.isNotEmpty() && best.cellIndex in opponentWinsNow) {
            val reason = if (myThreatsAfter.size >= 2) {
                "Blocks ${opponent.symbol} and sets up a double threat!"
            } else {
                "Blocks ${opponent.symbol} from winning next turn."
            }
            return MoveHint(cellIndex = best.cellIndex, reason = reason)
        }

        if (myThreatsAfter.size >= 2) {
            return MoveHint(
                cellIndex = best.cellIndex,
                reason = "Sets up two winning lines — ${opponent.symbol} can only block one.",
            )
        }

        if (best.score >= MATE_THRESHOLD) {
            val reason = if (mode == GameMode.INFINITY) {
                "Locks in a winning chain — ${opponent.symbol} runs out of blocks."
            } else {
                "Sets up a guaranteed win in the next two turns."
            }
            return MoveHint(best.cellIndex, reason)
        }

        val opponentWinsAfter = GameEngine.countImmediateWinningMoves(afterBest, opponent, mode)
        if (opponentWinsNow.isNotEmpty() && opponentWinsAfter.size < opponentWinsNow.size) {
            return MoveHint(
                cellIndex = best.cellIndex,
                reason = "Stops ${opponent.symbol}'s immediate threat.",
            )
        }

        if (myThreatsAfter.size == 1) {
            return MoveHint(
                cellIndex = best.cellIndex,
                reason = "Puts pressure on ${opponent.symbol} and forces a block.",
            )
        }

        val positionalReason = when (best.cellIndex) {
            4 -> "Grab the center — it opens up the most winning lines."
            0, 2, 6, 8 -> "Take a corner to set up strong diagonal plays."
            else -> "Solid move that keeps ${opponent.symbol} from building a trap."
        }
        return MoveHint(best.cellIndex, positionalReason)
    }

    /**
     * Chess-style post-match tactical accuracy & move-by-move grading analyzer.
     */
    fun analyzeMatch(
        mode: GameMode,
        startingPlayer: Player,
        moveSequenceCsv: String,
        targetPlayer: Player? = null,
    ): MatchAnalysis {
        val indices = GameEngine.parseMoveIndices(moveSequenceCsv)
        if (indices.isEmpty()) return MatchAnalysis(100, emptyList())

        val evalDepth = if (mode == GameMode.INFINITY) 6 else MAX_EXACT_DEPTH
        var state = GameEngine.createInitialState(startingPlayer)
        val analyzedMoves = mutableListOf<MoveAnalysis>()
        var totalScoreWeight = 0.0
        var countedMoves = 0

        indices.forEachIndexed { idx, playedCell ->
            if (state.isGameOver) return@forEachIndexed
            val mover = state.currentPlayer
            val scored = evaluateAllMoves(state, mode, maxDepth = evalDepth)
            val best = scored.firstOrNull()
            val played = scored.firstOrNull { it.cellIndex == playedCell }

            if (best != null && played != null) {
                val delta = best.score - played.score
                val isOnlySafeOrFork = scored.size > 1 && best.score >= MATE_THRESHOLD && scored[1].score < MATE_THRESHOLD
                val hungImmediateMate = best.score > -(WIN_SCORE - 2) && played.score <= -(WIN_SCORE - 2)
                val missedImmediateWin = best.score == WIN_SCORE && played.score < MATE_THRESHOLD
                val threwSafePosition = best.score > -MATE_THRESHOLD && played.score <= -MATE_THRESHOLD
                val grade = when {
                    hungImmediateMate || missedImmediateWin || threwSafePosition -> MoveGrade.BLUNDER
                    delta <= 0 && isOnlySafeOrFork && idx >= 2 -> MoveGrade.BRILLIANT
                    delta <= 5 -> MoveGrade.BEST
                    delta <= 35 -> MoveGrade.GOOD
                    delta <= 140 -> MoveGrade.INACCURACY
                    else -> MoveGrade.BLUNDER
                }
                analyzedMoves.add(
                    MoveAnalysis(
                        ply = idx + 1,
                        player = mover,
                        playedCell = playedCell,
                        bestCell = best.cellIndex,
                        grade = grade,
                    ),
                )

                if (targetPlayer == null || mover == targetPlayer) {
                    val moveAccuracy = when (grade) {
                        MoveGrade.BRILLIANT, MoveGrade.BEST -> 100.0
                        MoveGrade.GOOD -> 88.0
                        MoveGrade.INACCURACY -> 62.0
                        MoveGrade.BLUNDER -> 20.0
                    }
                    totalScoreWeight += moveAccuracy
                    countedMoves++
                }
            }

            state = GameEngine.applyMove(state, playedCell, mode, plyNumber = idx + 1) ?: return@forEachIndexed
        }

        val finalAccuracy = if (countedMoves > 0) {
            (totalScoreWeight / countedMoves).roundToInt().coerceIn(0, 100)
        } else {
            100
        }
        return MatchAnalysis(accuracyPercent = finalAccuracy, moves = analyzedMoves)
    }

    fun calculateEloDelta(
        playerElo: Int,
        outcome: MatchOutcome,
        humanSide: Player,
        difficulty: AiDifficulty,
    ): Int {
        val aiElo = when (difficulty) {
            AiDifficulty.CASUAL -> 880
            AiDifficulty.TACTICAL -> 1140
            AiDifficulty.MASTER -> 1380
            AiDifficulty.ADAPTIVE -> (playerElo + 30)
        }
        val actualScore = when {
            outcome == MatchOutcome.DRAW -> 0.5
            (outcome == MatchOutcome.X_WIN && humanSide == Player.X) ||
                (outcome == MatchOutcome.O_WIN && humanSide == Player.O) -> 1.0
            else -> 0.0
        }
        val expectedScore = 1.0 / (1.0 + 10.0.pow((aiElo - playerElo) / 400.0))
        val kFactor = 28.0
        return (kFactor * (actualScore - expectedScore)).roundToInt()
    }

    fun encodeStateKey(state: BoardSnapshot, mode: GameMode): Int {
        var key = if (state.currentPlayer == Player.X) 0 else 1
        if (mode == GameMode.INFINITY) {
            var shift = 1
            for (i in 0 until 3) {
                val cell = state.xQueue.getOrElse(i) { 15 }
                key = key or (cell shl shift)
                shift += 4
            }
            for (i in 0 until 3) {
                val cell = state.oQueue.getOrElse(i) { 15 }
                key = key or (cell shl shift)
                shift += 4
            }
        } else {
            var shift = 1
            for (i in 0 until 9) {
                val bits = when (state.board[i]) {
                    null -> 0
                    Player.X -> 1
                    Player.O -> 2
                }
                key = key or (bits shl shift)
                shift += 2
            }
        }
        return key
    }

    private fun computeTrapScore(
        parentState: BoardSnapshot,
        cellIndex: Int,
        mode: GameMode,
        aiPlayer: Player,
        recentStateKeys: Set<Int>,
    ): Int {
        val after = GameEngine.applyMove(parentState, cellIndex, mode) ?: return 0
        if (after.winner == aiPlayer) return 500
        if (after.winner == aiPlayer.opponent()) return -500

        if (mode == GameMode.MISERE) {
            // In Misère, maximize how many opponent replies force the opponent to lose immediately!
            val oppMoves = GameEngine.legalMoves(after)
            var oppFatalReplies = 0
            var oppSafeReplies = 0
            for (oppCell in oppMoves) {
                val afterOpp = GameEngine.applyMove(after, oppCell, mode) ?: continue
                if (afterOpp.winner == aiPlayer) {
                    oppFatalReplies++
                } else {
                    oppSafeReplies++
                }
            }
            var trap = oppFatalReplies * 24
            if (oppMoves.size > 1 && oppSafeReplies == 1) trap += 35
            // Prefer keeping center open or balanced depending on symmetry
            if (cellIndex == 4 && parentState.xQueue.isEmpty() && parentState.oQueue.isEmpty()) trap += 12
            return trap
        }

        var trap = 0
        val nextKey = encodeStateKey(after, mode)
        if (mode == GameMode.INFINITY && nextKey in recentStateKeys) {
            trap -= 30
        }

        val myImmediateWins = GameEngine.countImmediateWinningMoves(after, aiPlayer, mode)
        trap += when (myImmediateWins.size) {
            0 -> 0
            1 -> 26
            else -> 90
        }

        val oppMoves = GameEngine.legalMoves(after)
        var lethalTraps = 0
        var safeReplies = 0
        for (oppCell in oppMoves) {
            val afterOpp = GameEngine.applyMove(after, oppCell, mode) ?: continue
            if (afterOpp.winner == aiPlayer.opponent()) {
                return -400
            }
            val aiWinsNow = GameEngine.countImmediateWinningMoves(afterOpp, aiPlayer, mode)
            if (aiWinsNow.isNotEmpty()) {
                lethalTraps++
            } else {
                val hasFollowUpFork = GameEngine.legalMoves(afterOpp).any { follow ->
                    val afterFollow = GameEngine.applyMove(afterOpp, follow, mode) ?: return@any false
                    afterFollow.winner == aiPlayer ||
                        (
                            GameEngine.countImmediateWinningMoves(afterFollow, aiPlayer.opponent(), mode).isEmpty() &&
                                GameEngine.countImmediateWinningMoves(afterFollow, aiPlayer, mode).size >= 2
                            )
                }
                if (hasFollowUpFork) {
                    lethalTraps++
                } else {
                    safeReplies++
                }
            }
        }

        trap += lethalTraps * 16
        if (oppMoves.size > 1 && safeReplies == 1) {
            trap += 32
        }

        trap += when (cellIndex) {
            4 -> 8
            0, 2, 6, 8 -> 5
            else -> 1
        }

        if (mode == GameMode.INFINITY) {
            trap += heuristicScore(after, aiPlayer, mode) / 3
        }

        return trap
    }

    private fun orderMoves(
        state: BoardSnapshot,
        moves: List<Int>,
        mode: GameMode,
        ttBestMove: Int,
    ): List<Int> {
        if (moves.size <= 1) return moves
        val player = state.currentPlayer
        val opponent = player.opponent()

        if (mode == GameMode.MISERE) {
            val fatal = GameEngine.countImmediateSelfLossMoves(state, player, mode).toSet()
            return moves.sortedByDescending { cell ->
                var priority = 0
                if (cell == ttBestMove) priority += 5000
                if (cell in fatal) priority -= 3000
                priority += when (cell) {
                    4 -> 20
                    0, 2, 6, 8 -> 12
                    else -> 8
                }
                priority
            }
        }

        val myWins = GameEngine.countImmediateWinningMoves(state, player, mode).toSet()
        val oppWins = GameEngine.countImmediateWinningMoves(state, opponent, mode).toSet()

        return moves.sortedByDescending { cell ->
            var priority = 0
            if (cell == ttBestMove) priority += 5000
            if (cell in myWins) priority += 2000
            if (cell in oppWins) priority += 1000
            priority += when (cell) {
                4 -> 40
                0, 2, 6, 8 -> 20
                else -> 5
            }
            priority
        }
    }

    private fun minimax(
        state: BoardSnapshot,
        mode: GameMode,
        depth: Int,
        maxDepth: Int,
        alpha: Int,
        beta: Int,
        maximizingPlayer: Player,
        tt: HashMap<Int, TTEntry>,
        deadlineMs: Long,
    ): Int {
        if (state.winner == maximizingPlayer) return WIN_SCORE - depth
        if (state.winner == maximizingPlayer.opponent()) return -WIN_SCORE + depth
        if (state.isDraw) return 0

        val remaining = maxDepth - depth
        if (remaining <= 0 || (depth >= 4 && System.currentTimeMillis() >= deadlineMs)) {
            return heuristicScore(state, maximizingPlayer, mode)
        }

        val stateKey = encodeStateKey(state, mode)
        val ttEntry = tt[stateKey]
        var a = alpha
        var b = beta

        if (ttEntry != null && ttEntry.depth >= remaining) {
            when (ttEntry.flag) {
                FLAG_EXACT -> return ttEntry.score
                FLAG_LOWER -> if (ttEntry.score > a) a = ttEntry.score
                FLAG_UPPER -> if (ttEntry.score < b) b = ttEntry.score
            }
            if (a >= b) return ttEntry.score
        }

        val moves = orderMoves(
            state = state,
            moves = GameEngine.legalMoves(state),
            mode = mode,
            ttBestMove = ttEntry?.bestMove ?: -1,
        )
        if (moves.isEmpty()) return 0

        val isMaximizingTurn = state.currentPlayer == maximizingPlayer
        val origAlpha = a
        val origBeta = b
        var bestMove = moves.first()
        var bestValue = if (isMaximizingTurn) -WIN_SCORE * 2 else WIN_SCORE * 2

        if (isMaximizingTurn) {
            for (cell in moves) {
                val next = GameEngine.applyMove(state, cell, mode) ?: continue
                val eval = minimax(
                    state = next,
                    mode = mode,
                    depth = depth + 1,
                    maxDepth = maxDepth,
                    alpha = a,
                    beta = b,
                    maximizingPlayer = maximizingPlayer,
                    tt = tt,
                    deadlineMs = deadlineMs,
                )
                if (eval > bestValue) {
                    bestValue = eval
                    bestMove = cell
                }
                if (bestValue > a) a = bestValue
                if (a >= b) break
            }
        } else {
            for (cell in moves) {
                val next = GameEngine.applyMove(state, cell, mode) ?: continue
                val eval = minimax(
                    state = next,
                    mode = mode,
                    depth = depth + 1,
                    maxDepth = maxDepth,
                    alpha = a,
                    beta = b,
                    maximizingPlayer = maximizingPlayer,
                    tt = tt,
                    deadlineMs = deadlineMs,
                )
                if (eval < bestValue) {
                    bestValue = eval
                    bestMove = cell
                }
                if (bestValue < b) b = bestValue
                if (a >= b) break
            }
        }

        val flag = when {
            bestValue <= origAlpha -> FLAG_UPPER
            bestValue >= origBeta -> FLAG_LOWER
            else -> FLAG_EXACT
        }
        if (tt.size < 45_000) {
            tt[stateKey] = TTEntry(
                depth = remaining,
                score = bestValue,
                flag = flag,
                bestMove = bestMove,
            )
        }
        return bestValue
    }

    private fun heuristicScore(state: BoardSnapshot, maximizingPlayer: Player, mode: GameMode): Int {
        if (mode == GameMode.MISERE) return 0

        val opp = maximizingPlayer.opponent()
        val myQueue = if (maximizingPlayer == Player.X) state.xQueue else state.oQueue
        val oppQueue = if (opp == Player.X) state.xQueue else state.oQueue

        val myExpiring = if (mode == GameMode.INFINITY && myQueue.size >= 3) myQueue.first() else null
        val oppExpiring = if (mode == GameMode.INFINITY && oppQueue.size >= 3) oppQueue.first() else null

        var score = 0
        val center = state.board[4]
        if (center == maximizingPlayer) {
            score += if (myExpiring == 4) 6 else 22
        } else if (center == opp) {
            score -= if (oppExpiring == 4) 6 else 22
        }

        var myTwoInARow = 0
        var oppTwoInARow = 0

        for (line in GameEngine.WIN_LINES) {
            var mineFresh = 0
            var mineExpiring = 0
            var theirsFresh = 0
            var theirsExpiring = 0

            for (idx in line) {
                val cell = state.board[idx]
                if (cell == maximizingPlayer) {
                    if (idx == myExpiring) mineExpiring++ else mineFresh++
                } else if (cell == opp) {
                    if (idx == oppExpiring) theirsExpiring++ else theirsFresh++
                }
            }

            val totalMine = mineFresh + mineExpiring
            val totalTheirs = theirsFresh + theirsExpiring

            if (totalMine > 0 && totalTheirs == 0) {
                if (mineFresh == 2) {
                    myTwoInARow++
                    score += 48
                } else if (totalMine == 2 && state.currentPlayer != maximizingPlayer) {
                    score += 14
                } else if (mineFresh == 1) {
                    score += 8
                }
            } else if (totalTheirs > 0 && totalMine == 0) {
                if (theirsFresh == 2) {
                    oppTwoInARow++
                    score -= 48
                } else if (totalTheirs == 2 && state.currentPlayer != opp) {
                    score -= 14
                } else if (theirsFresh == 1) {
                    score -= 8
                }
            }
        }

        if (myTwoInARow >= 2) score += 110
        if (oppTwoInARow >= 2) score -= 110

        return score
    }
}
