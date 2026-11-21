package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.MatchRecordEntity
import com.example.domain.AiDifficulty
import com.example.domain.GameMode
import com.example.domain.OpponentType
import com.example.domain.Player
import com.example.ui.components.AiThinkingPulse
import com.example.ui.components.CeramicChoiceChips
import com.example.ui.components.CeramicIconButton
import com.example.ui.components.CeramicPiece
import com.example.ui.components.CeramicPrimaryButton
import com.example.ui.components.CeramicSecondaryButton
import com.example.ui.components.CeramicSectionLabel
import com.example.ui.components.CeramicSegmented
import com.example.ui.components.CeramicSwitchRow
import com.example.ui.components.TactixBoard
import com.example.ui.components.modeLook
import com.example.ui.theme.Ceramic
import com.example.ui.viewmodel.GameUiState
import kotlinx.coroutines.delay

@Composable
fun PlayScreen(
    uiState: GameUiState,
    onBackToHome: () -> Unit,
    onCellTap: (Int) -> Unit,
    onSelectMode: (GameMode) -> Unit,
    onSelectOpponent: (OpponentType) -> Unit,
    onSelectDifficulty: (AiDifficulty) -> Unit,
    onToggleBlitz: () -> Unit,
    onSwapHumanSide: () -> Unit,
    onRequestHint: () -> Unit,
    onDismissHint: () -> Unit,
    onNewRound: () -> Unit,
    onToggleHaptics: () -> Unit,
    onOpenReplay: (MatchRecordEntity) -> Unit,
    onShareResult: () -> Unit,
    contentBottomPadding: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier,
) {
    var showOptions by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }

    // Let the winning line draw on the board before the result screen takes over.
    LaunchedEffect(uiState.snapshot.isGameOver, uiState.moveHistory.size) {
        if (uiState.snapshot.isGameOver) {
            delay(750)
            showResult = true
        } else {
            showResult = false
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 10.dp)
                .padding(contentBottomPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 520.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                PlayHeader(
                    uiState = uiState,
                    onBackToHome = onBackToHome,
                    onOpenOptions = { showOptions = true },
                )

                PlayerCards(uiState = uiState)

                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    val viewport = maxHeight
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .heightIn(min = viewport),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                    ) {
                        TurnStatus(uiState = uiState)
                        BlitzClock(uiState = uiState)
                        TactixBoard(
                            snapshot = uiState.snapshot,
                            mode = uiState.mode,
                            skin = uiState.selectedSkin,
                            onCellClick = onCellTap,
                            enabled = !uiState.isAiThinking && !uiState.snapshot.isGameOver,
                            hintCellIndex = uiState.activeHint?.cellIndex,
                            testTagPrefix = "play_board",
                            modifier = Modifier.padding(vertical = 4.dp),
                        )
                        HintCard(uiState = uiState, onDismissHint = onDismissHint)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    CeramicSecondaryButton(
                        label = stringResource(R.string.action_hint),
                        icon = Icons.Rounded.Lightbulb,
                        iconTint = Ceramic.Cobalt,
                        onClick = onRequestHint,
                        enabled = !uiState.isAiThinking && !uiState.snapshot.isGameOver,
                        testTag = "hint_button",
                        modifier = Modifier.weight(1f),
                    )
                    CeramicSecondaryButton(
                        label = stringResource(R.string.action_reset),
                        icon = Icons.Rounded.Refresh,
                        iconTint = Ceramic.VermilionDeep,
                        onClick = onNewRound,
                        testTag = "new_round_button",
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = showResult && uiState.snapshot.isGameOver,
            enter = fadeIn(tween(280)) + scaleIn(initialScale = 0.96f, animationSpec = tween(320)),
            exit = fadeOut(tween(180)),
        ) {
            ResultScreen(
                uiState = uiState,
                onBackToHome = onBackToHome,
                onNewRound = onNewRound,
                onOpenReplay = onOpenReplay,
                onShareResult = onShareResult,
            )
        }
    }

    if (showOptions) {
        GameOptionsSheet(
            uiState = uiState,
            onSelectMode = onSelectMode,
            onSelectOpponent = onSelectOpponent,
            onSelectDifficulty = onSelectDifficulty,
            onToggleBlitz = onToggleBlitz,
            onSwapHumanSide = onSwapHumanSide,
            onToggleHaptics = onToggleHaptics,
            onDismiss = { showOptions = false },
        )
    }
}

private fun difficultyLabel(difficulty: AiDifficulty): String = when (difficulty) {
    AiDifficulty.ADAPTIVE -> "Adaptive"
    AiDifficulty.CASUAL -> "Easy"
    AiDifficulty.TACTICAL -> "Hard"
    AiDifficulty.MASTER -> "Master"
}

@Composable
private fun PlayHeader(
    uiState: GameUiState,
    onBackToHome: () -> Unit,
    onOpenOptions: () -> Unit,
) {
    val look = modeLook(uiState.mode)
    val subtitle = if (uiState.opponentType == OpponentType.AI) {
        val level = if (uiState.aiDifficulty == AiDifficulty.ADAPTIVE) {
            "Adaptive · ${uiState.adaptiveIntensityLabel}"
        } else {
            difficultyLabel(uiState.aiDifficulty)
        }
        "vs Computer · $level"
    } else {
        "2 players"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        CeramicIconButton(
            icon = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = stringResource(R.string.cd_back_to_modes),
            onClick = onBackToHome,
            modifier = Modifier.testTag("arena_back_to_modes_button"),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = look.title,
                style = MaterialTheme.typography.titleLarge,
                color = Ceramic.Obsidian,
                maxLines = 1,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = Ceramic.Muted,
                maxLines = 1,
            )
        }
        CeramicIconButton(
            icon = Icons.Rounded.MoreHoriz,
            contentDescription = stringResource(R.string.cd_match_settings),
            onClick = onOpenOptions,
            modifier = Modifier.testTag("arena_quick_setup_button"),
        )
    }
}

@Composable
private fun PlayerCards(uiState: GameUiState) {
    val isAi = uiState.opponentType == OpponentType.AI
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        listOf(Player.X, Player.O).forEach { player ->
            val isHuman = !isAi || player == uiState.humanSide
            val name = when {
                !isAi -> "Player ${player.symbol}"
                isHuman -> "You"
                else -> "Computer"
            }
            val active = !uiState.snapshot.isGameOver && uiState.snapshot.currentPlayer == player
            val status = when {
                !active -> "Waiting"
                isAi && !isHuman && uiState.isAiThinking -> "Thinking…"
                isAi && isHuman -> "Your turn"
                else -> "To move"
            }
            val score = if (player == Player.X) uiState.sessionXWins else uiState.sessionOWins
            PlayerCard(
                player = player,
                name = name,
                status = status,
                score = score,
                active = active,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PlayerCard(
    player: Player,
    name: String,
    status: String,
    score: Int,
    active: Boolean,
    modifier: Modifier = Modifier,
) {
    val pieceColor = if (player == Player.X) Ceramic.Cobalt else Ceramic.Vermilion
    val activeContent = if (player == Player.X) Color.White else Ceramic.Obsidian
    val background by animateColorAsState(
        targetValue = if (active) pieceColor else Ceramic.Card,
        animationSpec = tween(260),
        label = "player_card_bg_${player.symbol}",
    )
    val content = if (active) activeContent else Ceramic.Obsidian
    val shape = RoundedCornerShape(22.dp)
    Row(
        modifier = modifier
            .shadow(
                elevation = if (active) 10.dp else 2.dp,
                shape = shape,
                clip = false,
                ambientColor = if (active) pieceColor else Color.Black,
                spotColor = if (active) pieceColor else Color.Black,
            )
            .clip(shape)
            .background(background)
            .padding(12.dp)
            .testTag("player_card_${player.symbol}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (active) Color.White else Ceramic.Socket),
            contentAlignment = Alignment.Center,
        ) {
            CeramicPiece(player = player, color = pieceColor, modifier = Modifier.size(30.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = content,
                maxLines = 1,
            )
            Text(
                text = status,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = if (active) content else Ceramic.Muted,
                maxLines = 1,
            )
        }
        Text(
            text = score.toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = content,
        )
    }
}

@Composable
private fun TurnStatus(uiState: GameUiState) {
    val snapshot = uiState.snapshot
    val isAi = uiState.opponentType == OpponentType.AI
    val title = when {
        snapshot.endedByTimeout -> stringResource(R.string.status_timeout)
        snapshot.isGameOver -> outcomeHeadline(uiState)
        uiState.isAiThinking -> "Thinking…"
        isAi -> stringResource(R.string.status_your_turn)
        else -> "${snapshot.currentPlayer.symbol} to move"
    }
    val expiring = snapshot.expiringCell(uiState.mode)
    val tip = when {
        snapshot.isGameOver -> "${uiState.moveHistory.size} moves played"
        uiState.mode == GameMode.INFINITY && expiring != null ->
            if (isAi && snapshot.currentPlayer == uiState.humanSide) {
                "Your faded ${snapshot.currentPlayer.symbol} vanishes when you play."
            } else {
                "The faded ${snapshot.currentPlayer.symbol} vanishes on this move."
            }
        uiState.mode == GameMode.INFINITY -> "Only 3 marks stay on the board."
        uiState.mode == GameMode.CLASSIC -> "Get three in a row to win."
        else -> "Three in a row loses. Trap your rival."
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (uiState.isAiThinking) {
                AiThinkingPulse(color = Ceramic.Vermilion)
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = Ceramic.Obsidian,
                textAlign = TextAlign.Center,
            )
        }
        Text(
            text = tip,
            style = MaterialTheme.typography.bodyMedium,
            color = Ceramic.Muted,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun BlitzClock(uiState: GameUiState) {
    if (!uiState.isBlitzEnabled) return
    val urgent = uiState.blitzRemainingMs <= 1_800L && uiState.isBlitzClockRunning
    val fill by animateFloatAsState(
        targetValue = uiState.blitzProgress,
        animationSpec = tween(120),
        label = "blitz_fill",
    )
    val fillColor = if (urgent) Ceramic.Vermilion else Ceramic.Cobalt
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("blitz_clock"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(CircleShape)
                .background(Ceramic.Socket),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fill.coerceIn(0f, 1f))
                    .clip(CircleShape)
                    .background(fillColor),
            )
        }
        val seconds = (uiState.blitzRemainingMs / 100L) / 10f
        Text(
            text = String.format(java.util.Locale.UK, "%.1fs", seconds),
            style = MaterialTheme.typography.labelLarge,
            color = if (urgent) Ceramic.VermilionDeep else Ceramic.Obsidian,
        )
    }
}

@Composable
private fun HintCard(uiState: GameUiState, onDismissHint: () -> Unit) {
    val hint = uiState.activeHint
    AnimatedVisibility(
        visible = hint != null && !uiState.snapshot.isGameOver,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(22.dp), clip = false)
                .clip(RoundedCornerShape(22.dp))
                .background(Ceramic.Card)
                .padding(start = 14.dp, top = 10.dp, bottom = 10.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Ceramic.CobaltTint),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lightbulb,
                    contentDescription = null,
                    tint = Ceramic.Cobalt,
                    modifier = Modifier.size(20.dp),
                )
            }
            Text(
                text = hint?.reason.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = Ceramic.Obsidian,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onDismissHint) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Dismiss hint",
                    tint = Ceramic.Muted,
                )
            }
        }
    }
}

private fun outcomeHeadline(uiState: GameUiState): String {
    val snapshot = uiState.snapshot
    val isAi = uiState.opponentType == OpponentType.AI
    val misere = uiState.mode == GameMode.MISERE
    return when {
        snapshot.isDraw -> "Tie game"
        isAi && snapshot.winner == uiState.humanSide -> if (misere) "Trap sprung!" else "You win!"
        isAi && snapshot.endedByTimeout -> "Time’s up"
        isAi -> if (misere) "Caught in three" else "Outplayed"
        else -> "${snapshot.winner?.symbol ?: "X"} wins!"
    }
}

/**
 * Full-screen result. A human win turns the screen cobalt, a loss or tie turns it obsidian,
 * and in two-player games the winner's colour takes over.
 */
@Composable
private fun ResultScreen(
    uiState: GameUiState,
    onBackToHome: () -> Unit,
    onNewRound: () -> Unit,
    onOpenReplay: (MatchRecordEntity) -> Unit,
    onShareResult: () -> Unit,
) {
    val snapshot = uiState.snapshot
    val isAi = uiState.opponentType == OpponentType.AI
    val humanWon = isAi && snapshot.winner == uiState.humanSide
    val background = when {
        snapshot.isDraw -> Ceramic.Obsidian
        isAi -> if (humanWon) Ceramic.Cobalt else Ceramic.Obsidian
        snapshot.winner == Player.O -> Ceramic.Vermilion
        else -> Ceramic.Cobalt
    }
    val content = if (background == Ceramic.Vermilion) Ceramic.Obsidian else Color.White
    val panel = when (background) {
        Ceramic.Vermilion -> Ceramic.Obsidian.copy(alpha = 0.12f)
        Ceramic.Obsidian -> Color.White.copy(alpha = 0.10f)
        else -> Ceramic.Obsidian.copy(alpha = 0.20f)
    }
    val look = modeLook(uiState.mode)
    val scoreLine = if (isAi) {
        val you = if (uiState.humanSide == Player.X) uiState.sessionXWins else uiState.sessionOWins
        val cpu = if (uiState.humanSide == Player.X) uiState.sessionOWins else uiState.sessionXWins
        "You $you · Computer $cpu"
    } else {
        "X ${uiState.sessionXWins} · O ${uiState.sessionOWins}"
    }

    val stats = buildList {
        uiState.lastAccuracyPercent?.let { add("$it%" to "Accuracy") }
        uiState.lastEloDelta?.let { add((if (it >= 0) "+$it" else "$it") to "Rating") }
        if (humanWon && uiState.currentWinStreak > 0) {
            add(uiState.currentWinStreak.toString() to "Win streak")
        } else {
            add(uiState.moveHistory.size.toString() to "Moves")
        }
    }.take(3)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            )
            .testTag("result_screen"),
    ) {
        if (background != Ceramic.Obsidian || humanWon) {
            CeramicPiece(
                player = Player.X,
                color = Ceramic.Ivory,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 18.dp, y = 110.dp)
                    .size(66.dp)
                    .rotate(22f),
            )
        }
        CeramicPiece(
            player = Player.O,
            color = if (background == Ceramic.Vermilion) Ceramic.Cobalt else Ceramic.Vermilion,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (-22).dp, y = (-40).dp)
                .size(72.dp)
                .rotate(-20f),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CeramicIconButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back_to_modes),
                    onClick = onBackToHome,
                    container = panel,
                    tint = content,
                )
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = look.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = content,
                    )
                    Text(
                        text = scoreLine,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        fontWeight = FontWeight.Medium,
                        color = content,
                    )
                }
                Spacer(modifier = Modifier.size(44.dp))
            }

            TactixBoard(
                snapshot = snapshot,
                mode = uiState.mode,
                skin = uiState.selectedSkin,
                onCellClick = {},
                enabled = false,
                testTagPrefix = "result_board",
                modifier = Modifier.widthIn(max = 280.dp),
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = if (snapshot.endedByTimeout && !humanWon) {
                        stringResource(R.string.status_timeout)
                    } else {
                        outcomeHeadline(uiState)
                    },
                    style = MaterialTheme.typography.displaySmall.copy(fontSize = 40.sp, lineHeight = 44.sp),
                    color = content,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "${uiState.moveHistory.size} moves · ${look.title}" +
                        if (uiState.isBlitzEnabled) " · Blitz" else "",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = content,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                stats.forEach { (value, label) ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(panel)
                            .padding(vertical = 14.dp, horizontal = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text(
                            text = value,
                            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp),
                            color = content,
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                            color = content,
                        )
                    }
                }
            }

            if (uiState.newlyUnlockedMedals.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 520.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(panel)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.EmojiEvents,
                        contentDescription = null,
                        tint = content,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = "Medal unlocked: " + uiState.newlyUnlockedMedals.joinToString(", ") { it.title },
                        style = MaterialTheme.typography.labelLarge,
                        color = content,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp)
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CeramicPrimaryButton(
                    label = stringResource(R.string.action_play_again),
                    onClick = onNewRound,
                    icon = Icons.Rounded.Refresh,
                    testTag = "play_again_button",
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    val saved = uiState.lastSavedMatch
                    if (saved != null) {
                        CeramicSecondaryButton(
                            label = stringResource(R.string.action_replay),
                            icon = Icons.Rounded.PlayArrow,
                            iconTint = content,
                            onClick = { onOpenReplay(saved) },
                            container = panel,
                            content = content,
                            testTag = "replay_last_match_button",
                            modifier = Modifier.weight(1f),
                        )
                    }
                    CeramicSecondaryButton(
                        label = "Share",
                        icon = Icons.Rounded.Share,
                        iconTint = content,
                        onClick = onShareResult,
                        container = panel,
                        content = content,
                        testTag = "share_result_button",
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GameOptionsSheet(
    uiState: GameUiState,
    onSelectMode: (GameMode) -> Unit,
    onSelectOpponent: (OpponentType) -> Unit,
    onSelectDifficulty: (AiDifficulty) -> Unit,
    onToggleBlitz: () -> Unit,
    onSwapHumanSide: () -> Unit,
    onToggleHaptics: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp),
        containerColor = Ceramic.Card,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                .padding(bottom = 28.dp)
                .navigationBarsPadding()
                .testTag("game_options_sheet"),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(
                text = "Game options",
                style = MaterialTheme.typography.headlineSmall,
                color = Ceramic.Obsidian,
            )
            Text(
                text = "Changing the mode, opponent or difficulty starts a new round.",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = Ceramic.Muted,
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CeramicSectionLabel("Mode")
                CeramicSegmented(
                    options = listOf(
                        GameMode.INFINITY to stringResource(R.string.mode_infinity),
                        GameMode.CLASSIC to stringResource(R.string.mode_classic),
                        GameMode.MISERE to stringResource(R.string.mode_misere),
                    ),
                    selected = uiState.mode,
                    onSelect = onSelectMode,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CeramicSectionLabel("Opponent")
                CeramicSegmented(
                    options = listOf(
                        OpponentType.AI to "Computer",
                        OpponentType.PASS_AND_PLAY to "A friend",
                    ),
                    selected = uiState.opponentType,
                    onSelect = onSelectOpponent,
                )
            }

            if (uiState.opponentType == OpponentType.AI) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    CeramicSectionLabel("Difficulty")
                    CeramicChoiceChips(
                        options = listOf(
                            AiDifficulty.ADAPTIVE to stringResource(R.string.ai_adaptive),
                            AiDifficulty.CASUAL to stringResource(R.string.ai_casual),
                            AiDifficulty.TACTICAL to stringResource(R.string.ai_tactical),
                            AiDifficulty.MASTER to stringResource(R.string.ai_master),
                        ),
                        selected = uiState.aiDifficulty,
                        onSelect = onSelectDifficulty,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Ceramic.Paper)
                    .padding(horizontal = 16.dp, vertical = 2.dp),
            ) {
                CeramicSwitchRow(
                    title = "Blitz clock",
                    subtitle = "5 seconds per move",
                    checked = uiState.isBlitzEnabled,
                    onToggle = onToggleBlitz,
                    testTag = "blitz_toggle_button",
                )
                if (uiState.opponentType == OpponentType.AI) {
                    CeramicSwitchRow(
                        title = "You go first",
                        subtitle = if (uiState.humanSide == Player.X) "You play X" else "You play O",
                        checked = uiState.humanSide == Player.X,
                        onToggle = onSwapHumanSide,
                        testTag = "swap_sides_button",
                    )
                }
                CeramicSwitchRow(
                    title = "Vibration",
                    subtitle = "Feel each move and win",
                    checked = uiState.hapticsEnabled,
                    onToggle = onToggleHaptics,
                    testTag = "haptics_toggle_button",
                )
            }
        }
    }
}
