package com.example.domain

object GameEngine {

    val WIN_LINES: List<List<Int>> = listOf(
        listOf(0, 1, 2),
        listOf(3, 4, 5),
        listOf(6, 7, 8),
        listOf(0, 3, 6),
        listOf(1, 4, 7),
        listOf(2, 5, 8),
        listOf(0, 4, 8),
        listOf(2, 4, 6),
    )

    fun createInitialState(startingPlayer: Player = Player.X): BoardSnapshot =
        BoardSnapshot(currentPlayer = startingPlayer)

    fun legalMoves(state: BoardSnapshot): List<Int> {
        if (state.isGameOver) return emptyList()
        val result = ArrayList<Int>(9)
        for (i in 0 until 9) {
            if (state.board[i] == null) {
                result.add(i)
            }
        }
        return result
    }

    fun applyMove(
        state: BoardSnapshot,
        cellIndex: Int,
        mode: GameMode,
        plyNumber: Int = (state.lastMove?.ply ?: 0) + 1,
    ): BoardSnapshot? {
        if (state.isGameOver || cellIndex !in 0..8 || state.board[cellIndex] != null) {
            return null
        }

        val player = state.currentPlayer
        val nextBoard = state.board.toMutableList()
        val nextXQueue = state.xQueue.toMutableList()
        val nextOQueue = state.oQueue.toMutableList()
        val activeQueue = if (player == Player.X) nextXQueue else nextOQueue

        var expiredIndex: Int? = null
        if (mode == GameMode.INFINITY && activeQueue.size >= 3) {
            expiredIndex = activeQueue.removeAt(0)
            nextBoard[expiredIndex] = null
        }

        nextBoard[cellIndex] = player
        activeQueue.add(cellIndex)

        val completedLine = findWinningLine(nextBoard, player)
        val winner = when {
            completedLine == null -> null
            mode == GameMode.MISERE -> player.opponent() // Reverse rules: 3-in-a-row LOSES!
            else -> player
        }
        val isDraw = winner == null && mode != GameMode.INFINITY && nextBoard.none { it == null }

        return BoardSnapshot(
            board = nextBoard,
            xQueue = nextXQueue,
            oQueue = nextOQueue,
            currentPlayer = if (winner == null && !isDraw) player.opponent() else player,
            lastMove = MoveEntry(
                ply = plyNumber,
                player = player,
                cellIndex = cellIndex,
                expiredCellIndex = expiredIndex,
            ),
            winningLine = completedLine,
            winner = winner,
            isDraw = isDraw,
        )
    }

    fun findWinningLine(board: List<Player?>, player: Player): List<Int>? {
        for (line in WIN_LINES) {
            if (board[line[0]] == player && board[line[1]] == player && board[line[2]] == player) {
                return line
            }
        }
        return null
    }

    fun countImmediateWinningMoves(state: BoardSnapshot, player: Player, mode: GameMode): List<Int> {
        if (mode == GameMode.MISERE) return emptyList()
        val probeState = state.copy(currentPlayer = player, winner = null, isDraw = false, winningLine = null)
        val winningCells = mutableListOf<Int>()
        for (i in 0 until 9) {
            if (probeState.board[i] == null) {
                val after = applyMove(probeState, i, mode)
                if (after?.winner == player) {
                    winningCells.add(i)
                }
            }
        }
        return winningCells
    }

    fun countImmediateSelfLossMoves(state: BoardSnapshot, player: Player, mode: GameMode): List<Int> {
        if (mode != GameMode.MISERE) return emptyList()
        val probeState = state.copy(currentPlayer = player, winner = null, isDraw = false, winningLine = null)
        val losingCells = mutableListOf<Int>()
        for (i in 0 until 9) {
            if (probeState.board[i] == null) {
                val after = applyMove(probeState, i, mode)
                if (after?.winner == player.opponent()) {
                    losingCells.add(i)
                }
            }
        }
        return losingCells
    }

    fun serializeMoveIndices(moves: List<MoveEntry>): String =
        moves.joinToString(",") { it.cellIndex.toString() }

    fun parseMoveIndices(csv: String): List<Int> {
        if (csv.isBlank()) return emptyList()
        return csv.split(",").mapNotNull { it.trim().toIntOrNull() }.filter { it in 0..8 }
    }

    fun reconstructReplaySnapshots(
        mode: GameMode,
        startingPlayer: Player,
        moveSequenceCsv: String,
    ): List<BoardSnapshot> {
        val snapshots = mutableListOf<BoardSnapshot>()
        var current = createInitialState(startingPlayer)
        snapshots.add(current)
        val indices = parseMoveIndices(moveSequenceCsv)
        indices.forEachIndexed { idx, cell ->
            val next = applyMove(current, cell, mode, plyNumber = idx + 1) ?: return@forEachIndexed
            snapshots.add(next)
            current = next
        }
        return snapshots
    }

    fun formatShareBoard(snapshot: BoardSnapshot): String {
        val sb = StringBuilder()
        for (r in 0 until 3) {
            for (c in 0 until 3) {
                val idx = r * 3 + c
                val ch = when (snapshot.board[idx]) {
                    Player.X -> "🟦"
                    Player.O -> "🟧"
                    null -> "⬜"
                }
                sb.append(ch)
            }
            if (r < 2) sb.append('\n')
        }
        return sb.toString()
    }
}
