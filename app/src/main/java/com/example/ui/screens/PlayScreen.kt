package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AllInclusive
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Flip
import androidx.compose.material.icons.rounded.Grid3x3
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.MobileOff
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.MatchRecordEntity
import com.example.domain.AiDifficulty
import com.example.domain.GameMode
import com.example.domain.OpponentType
import com.example.domain.Player
import com.example.ui.components.AiThinkingPulse
import com.example.ui.components.PillOption
import com.example.ui.components.RollingScoreCounter
import com.example.ui.components.TactixBoard
import com.example.ui.components.TactixMiniEmblem
import com.example.ui.components.TactixPillSwitcher
import com.example.ui.viewmodel.GameUiState
import java.util.Locale

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
    var showQuickSetup by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        val isWide = maxWidth >= 700.dp

        if (isWide) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .widthIn(max = 420.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TurnStatusPill(uiState = uiState)
                    BlitzClockBar(uiState = uiState)
                    TactixBoard(
                        snapshot = uiState.snapshot,
                        mode = uiState.mode,
                        skin = uiState.selectedSkin,
                        onCellClick = onCellTap,
                        enabled = !uiState.isAiThinking && !uiState.snapshot.isGameOver,
                        hintCellIndex = uiState.activeHint?.cellIndex,
                        testTagPrefix = "play_board",
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .widthIn(max = 430.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    PlayHeader(
                        uiState = uiState,
                        showQuickSetup = showQuickSetup,
                        onBackToHome = onBackToHome,
                        onToggleQuickSetup = { showQuickSetup = !showQuickSetup },
                        onToggleBlitz = onToggleBlitz,
                        onSwapHumanSide = onSwapHumanSide,
                        onToggleHaptics = onToggleHaptics,
                    )
                    AnimatedVisibility(
                        visible = showQuickSetup,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                    ) {
                        ModeAndOpponentControls(
                            uiState = uiState,
                            onSelectMode = onSelectMode,
                            onSelectOpponent = onSelectOpponent,
                            onSelectDifficulty = onSelectDifficulty,
                        )
                    }
                    DuelScoreBar(uiState = uiState)
                    HintOrResultSection(
                        uiState = uiState,
                        onDismissHint = onDismissHint,
                        onNewRound = onNewRound,
                        onOpenReplay = onOpenReplay,
                        onShareResult = onShareResult,
                    )
                    PlayActionDock(
                        uiState = uiState,
                        onRequestHint = onRequestHint,
                        onNewRound = onNewRound,
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 560.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 12.dp)
                    .padding(contentBottomPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(13.dp),
            ) {
                PlayHeader(
                    uiState = uiState,
                    showQuickSetup = showQuickSetup,
                    onBackToHome = onBackToHome,
                    onToggleQuickSetup = { showQuickSetup = !showQuickSetup },
                    onToggleBlitz = onToggleBlitz,
                    onSwapHumanSide = onSwapHumanSide,
                    onToggleHaptics = onToggleHaptics,
                )

                AnimatedVisibility(
                    visible = showQuickSetup,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    ModeAndOpponentControls(
                        uiState = uiState,
                        onSelectMode = onSelectMode,
                        onSelectOpponent = onSelectOpponent,
                        onSelectDifficulty = onSelectDifficulty,
                    )
                }

                DuelScoreBar(uiState = uiState)

                TurnStatusPill(uiState = uiState)

                BlitzClockBar(uiState = uiState)

                TactixBoard(
                    snapshot = uiState.snapshot,
                    mode = uiState.mode,
                    skin = uiState.selectedSkin,
                    onCellClick = onCellTap,
                    enabled = !uiState.isAiThinking && !uiState.snapshot.isGameOver,
                    hintCellIndex = uiState.activeHint?.cellIndex,
                    testTagPrefix = "play_board",
                )

                HintOrResultSection(
                    uiState = uiState,
                    onDismissHint = onDismissHint,
                    onNewRound = onNewRound,
                    onOpenReplay = onOpenReplay,
                    onShareResult = onShareResult,
                )

                PlayActionDock(
                    uiState = uiState,
                    onRequestHint = onRequestHint,
                    onNewRound = onNewRound,
                )

                Text(
                    text = when (uiState.mode) {
                        GameMode.INFINITY -> stringResource(R.string.infinity_rule_tip)
                        GameMode.CLASSIC -> stringResource(R.string.classic_rule_tip)
                        GameMode.MISERE -> stringResource(R.string.misere_rule_tip)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun PlayHeader(
    uiState: GameUiState,
    showQuickSetup: Boolean,
    onBackToHome: () -> Unit,
    onToggleQuickSetup: () -> Unit,
    onToggleBlitz: () -> Unit,
    onSwapHumanSide: () -> Unit,
    onToggleHaptics: () -> Unit,
) {
    val modeTitle = when (uiState.mode) {
        GameMode.INFINITY -> stringResource(R.string.mode_infinity)
        GameMode.CLASSIC -> stringResource(R.string.mode_classic)
        GameMode.MISERE -> stringResource(R.string.mode_misere)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f),
        ) {
            FilledTonalIconButton(
                onClick = onBackToHome,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
                modifier = Modifier
                    .size(42.dp)
                    .minimumInteractiveComponentSize()
                    .testTag("arena_back_to_modes_button"),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back_to_modes),
                    modifier = Modifier.size(20.dp),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = modeTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                )
                if (uiState.opponentType == OpponentType.AI) {
                    val tierLabel = when (uiState.aiDifficulty) {
                        AiDifficulty.ADAPTIVE -> "Adaptive • ${uiState.adaptiveIntensityLabel}"
                        AiDifficulty.CASUAL -> "Easy"
                        AiDifficulty.TACTICAL -> "Hard"
                        AiDifficulty.MASTER -> "Master"
                    }
                    Text(
                        text = "$tierLabel  •  ${uiState.playerElo} Rating",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                    )
                } else {
                    Text(
                        text = "2 Players",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // Collapsible quick match setup drawer toggle
            FilledTonalIconButton(
                onClick = onToggleQuickSetup,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = if (showQuickSetup) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                    contentColor = if (showQuickSetup) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                ),
                modifier = Modifier
                    .size(40.dp)
                    .testTag("arena_quick_setup_button"),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Tune,
                    contentDescription = stringResource(R.string.cd_match_settings),
                    modifier = Modifier.size(19.dp),
                )
            }

            // 5-second Blitz Clock toggle pill
            FilledTonalIconButton(
                onClick = onToggleBlitz,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = if (uiState.isBlitzEnabled) {
                        MaterialTheme.colorScheme.tertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                    contentColor = if (uiState.isBlitzEnabled) {
                        MaterialTheme.colorScheme.onTertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                ),
                modifier = Modifier
                    .size(40.dp)
                    .testTag("blitz_toggle_button"),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Timer,
                    contentDescription = stringResource(R.string.cd_toggle_blitz),
                    modifier = Modifier.size(19.dp),
                )
            }

            if (uiState.opponentType == OpponentType.AI) {
                FilledTonalIconButton(
                    onClick = onSwapHumanSide,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ),
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("swap_sides_button"),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SwapHoriz,
                        contentDescription = stringResource(R.string.cd_swap_sides),
                        modifier = Modifier.size(19.dp),
                    )
                }
            }

            FilledTonalIconButton(
                onClick = onToggleHaptics,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = if (uiState.hapticsEnabled) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                    contentColor = if (uiState.hapticsEnabled) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                ),
                modifier = Modifier
                    .size(40.dp)
                    .testTag("haptics_toggle_button"),
            ) {
                Icon(
                    imageVector = if (uiState.hapticsEnabled) Icons.Rounded.Vibration else Icons.Rounded.MobileOff,
                    contentDescription = stringResource(R.string.cd_toggle_haptics),
                    modifier = Modifier.size(19.dp),
                )
            }
        }
    }
}

@Composable
private fun BlitzClockBar(uiState: GameUiState) {
    AnimatedVisibility(
        visible = uiState.isBlitzEnabled,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        val urgent = uiState.blitzRemainingMs <= 1_800L && uiState.isBlitzClockRunning
        val barColor by animateColorAsState(
            targetValue = when {
                urgent -> MaterialTheme.colorScheme.error
                uiState.isBlitzClockRunning -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.primary
            },
            animationSpec = tween(220),
            label = "blitz_bar_color",
        )
        val animatedProgress by animateFloatAsState(
            targetValue = uiState.blitzProgress,
            animationSpec = tween(60),
            label = "blitz_bar_progress",
        )
        val secondsText = String.format(Locale.US, "%.1fs", uiState.blitzRemainingMs / 1000f)

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.9f),
            border = BorderStroke(1.dp, barColor.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 396.dp),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Timer,
                    contentDescription = null,
                    tint = barColor,
                    modifier = Modifier.size(16.dp),
                )
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(CircleShape),
                    color = barColor,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    strokeCap = StrokeCap.Round,
                )
                Text(
                    text = if (uiState.isBlitzClockRunning) secondsText else "5.0s",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = barColor,
                )
            }
        }
    }
}

@Composable
private fun ModeAndOpponentControls(
    uiState: GameUiState,
    onSelectMode: (GameMode) -> Unit,
    onSelectOpponent: (OpponentType) -> Unit,
    onSelectDifficulty: (AiDifficulty) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        // 3-Mode Selector: Infinity | Classic | Misère
        TactixPillSwitcher(
            options = listOf(
                PillOption(
                    value = GameMode.INFINITY,
                    label = stringResource(R.string.mode_infinity),
                    icon = Icons.Rounded.AllInclusive,
                    testTag = "mode_infinity_button",
                ),
                PillOption(
                    value = GameMode.CLASSIC,
                    label = stringResource(R.string.mode_classic),
                    icon = Icons.Rounded.Grid3x3,
                    testTag = "mode_classic_button",
                ),
                PillOption(
                    value = GameMode.MISERE,
                    label = stringResource(R.string.mode_misere),
                    icon = Icons.Rounded.Flip,
                    testTag = "mode_misere_button",
                ),
            ),
            selected = uiState.mode,
            onSelect = onSelectMode,
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TactixPillSwitcher(
                options = listOf(
                    PillOption(
                        value = OpponentType.AI,
                        label = stringResource(R.string.opponent_ai),
                        icon = Icons.Rounded.SmartToy,
                        testTag = "opponent_ai_button",
                    ),
                    PillOption(
                        value = OpponentType.PASS_AND_PLAY,
                        label = stringResource(R.string.opponent_pass_play),
                        icon = Icons.Rounded.Groups,
                        testTag = "opponent_pvp_button",
                    ),
                ),
                selected = uiState.opponentType,
                onSelect = onSelectOpponent,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        AnimatedVisibility(
            visible = uiState.opponentType == OpponentType.AI,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            val tiers = listOf(
                AiDifficulty.ADAPTIVE to stringResource(R.string.ai_adaptive),
                AiDifficulty.CASUAL to stringResource(R.string.ai_casual),
                AiDifficulty.TACTICAL to stringResource(R.string.ai_tactical),
                AiDifficulty.MASTER to stringResource(R.string.ai_master),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                tiers.forEach { (difficulty, label) ->
                    val selected = uiState.aiDifficulty == difficulty
                    val pillBg by animateColorAsState(
                        targetValue = if (selected) {
                            if (difficulty == AiDifficulty.MASTER) {
                                MaterialTheme.colorScheme.tertiaryContainer
                            } else {
                                MaterialTheme.colorScheme.secondaryContainer
                            }
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.8f)
                        },
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "tier_bg_${difficulty.name}",
                    )
                    val pillFg by animateColorAsState(
                        targetValue = if (selected) {
                            if (difficulty == AiDifficulty.MASTER) {
                                MaterialTheme.colorScheme.onTertiaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            }
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        animationSpec = tween(180),
                        label = "tier_fg_${difficulty.name}",
                    )
                    val borderColor = if (selected) {
                        if (difficulty == AiDifficulty.MASTER) {
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.55f)
                        } else {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                        }
                    } else {
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .minimumInteractiveComponentSize()
                            .clip(CircleShape)
                            .background(pillBg)
                            .border(1.dp, borderColor, CircleShape)
                            .testTag("ai_tier_${difficulty.name.lowercase()}")
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onSelectDifficulty(difficulty) },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            if (difficulty == AiDifficulty.ADAPTIVE && selected) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = pillFg,
                                    modifier = Modifier.size(13.dp),
                                )
                            }
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = pillFg,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DuelScoreBar(uiState: GameUiState) {
    val isXTurn = !uiState.snapshot.isGameOver && uiState.snapshot.currentPlayer == Player.X
    val isOTurn = !uiState.snapshot.isGameOver && uiState.snapshot.currentPlayer == Player.O

    val xLabel = when {
        uiState.opponentType == OpponentType.PASS_AND_PLAY -> "PLAYER X"
        uiState.humanSide == Player.X -> "YOU • X"
        else -> "RIVAL • X"
    }
    val oLabel = when {
        uiState.opponentType == OpponentType.PASS_AND_PLAY -> "PLAYER O"
        uiState.humanSide == Player.O -> "YOU • O"
        else -> "RIVAL • O"
    }

    val xBg by animateColorAsState(
        targetValue = if (isXTurn) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.85f)
        },
        animationSpec = tween(260),
        label = "x_score_bg",
    )
    val oBg by animateColorAsState(
        targetValue = if (isOTurn) {
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.92f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.85f)
        },
        animationSpec = tween(260),
        label = "o_score_bg",
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(22.dp),
            color = xBg,
            border = BorderStroke(
                width = if (isXTurn) 1.5.dp else 1.dp,
                color = if (isXTurn) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                },
            ),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = xLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isXTurn) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                    Box(
                        modifier = Modifier
                            .width(18.dp)
                            .height(3.dp)
                            .clip(CircleShape)
                            .background(
                                if (isXTurn) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                },
                            ),
                    )
                }
                RollingScoreCounter(
                    count = uiState.sessionXWins,
                    color = if (isXTurn) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
            }
        }

        if (uiState.mode != GameMode.INFINITY) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "DRAW",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = uiState.sessionDraws.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(22.dp),
            color = oBg,
            border = BorderStroke(
                width = if (isOTurn) 1.5.dp else 1.dp,
                color = if (isOTurn) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                },
            ),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RollingScoreCounter(
                    count = uiState.sessionOWins,
                    color = if (isOTurn) {
                        MaterialTheme.colorScheme.onTertiaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = oLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOTurn) {
                            MaterialTheme.colorScheme.onTertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                    Box(
                        modifier = Modifier
                            .width(18.dp)
                            .height(3.dp)
                            .clip(CircleShape)
                            .background(
                                if (isOTurn) {
                                    MaterialTheme.colorScheme.tertiary
                                } else {
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f)
                                },
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun TurnStatusPill(uiState: GameUiState) {
    val snapshot = uiState.snapshot
    val text = when {
        snapshot.endedByTimeout -> stringResource(R.string.status_timeout)
        snapshot.winner != null -> {
            if (uiState.opponentType == OpponentType.AI) {
                if (snapshot.winner == uiState.humanSide) {
                    stringResource(R.string.status_you_win)
                } else {
                    stringResource(R.string.status_ai_wins)
                }
            } else {
                if (snapshot.winner == Player.X) {
                    stringResource(R.string.status_x_wins)
                } else {
                    stringResource(R.string.status_o_wins)
                }
            }
        }
        snapshot.isDraw -> stringResource(R.string.status_draw)
        uiState.isAiThinking -> stringResource(R.string.status_ai_thinking)
        uiState.opponentType == OpponentType.AI -> stringResource(R.string.status_your_turn)
        snapshot.currentPlayer == Player.X -> stringResource(R.string.status_player_x_turn)
        else -> stringResource(R.string.status_player_o_turn)
    }

    val dotColor = when {
        snapshot.endedByTimeout -> MaterialTheme.colorScheme.error
        snapshot.winner == Player.X -> MaterialTheme.colorScheme.primary
        snapshot.winner == Player.O -> MaterialTheme.colorScheme.tertiary
        snapshot.isDraw -> MaterialTheme.colorScheme.outline
        snapshot.currentPlayer == Player.X -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.tertiary
    }

    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f),
        border = BorderStroke(1.dp, dotColor.copy(alpha = 0.35f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (uiState.isAiThinking) {
                AiThinkingPulse(color = dotColor)
            } else {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor),
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (uiState.mode == GameMode.INFINITY && snapshot.expiringCell(uiState.mode) != null) {
                Text(
                    text = "• Fade Active",
                    style = MaterialTheme.typography.labelSmall,
                    color = dotColor,
                )
            } else if (uiState.mode == GameMode.MISERE && !snapshot.isGameOver) {
                Text(
                    text = "• Avoid 3-in-a-row",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun HintOrResultSection(
    uiState: GameUiState,
    onDismissHint: () -> Unit,
    onNewRound: () -> Unit,
    onOpenReplay: (MatchRecordEntity) -> Unit,
    onShareResult: () -> Unit,
) {
    AnimatedVisibility(
        visible = uiState.activeHint != null && !uiState.snapshot.isGameOver,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        uiState.activeHint?.let { hint ->
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.94f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Text(
                        text = hint.reason,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onDismissHint) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Dismiss hint",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
            }
        }
    }

    AnimatedVisibility(
        visible = uiState.snapshot.isGameOver,
        enter = fadeIn() + expandVertically(spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
        exit = fadeOut() + shrinkVertically(),
    ) {
        val resultAccent = when {
            uiState.snapshot.endedByTimeout -> MaterialTheme.colorScheme.error
            uiState.snapshot.winner == Player.X -> MaterialTheme.colorScheme.primary
            uiState.snapshot.winner == Player.O -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.outline
        }
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(1.2.dp, resultAccent.copy(alpha = 0.42f)),
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                resultAccent.copy(alpha = 0.12f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            val headline = when {
                                uiState.snapshot.endedByTimeout -> "Time's Up!"
                                uiState.snapshot.isDraw -> "Tie Game"
                                uiState.opponentType == OpponentType.AI && uiState.snapshot.winner == uiState.humanSide ->
                                    if (uiState.mode == GameMode.MISERE) "Trap Sprung — You Win!" else "Clean Victory!"
                                uiState.opponentType == OpponentType.AI ->
                                    if (uiState.mode == GameMode.MISERE) "Caught in 3-in-a-Row!" else "Outplayed — Try Again"
                                uiState.snapshot.winner == Player.X -> "Player X Wins!"
                                else -> "Player O Wins!"
                            }
                            Text(
                                text = headline,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            val modeName = when (uiState.mode) {
                                GameMode.INFINITY -> "Infinity"
                                GameMode.CLASSIC -> "Classic"
                                GameMode.MISERE -> "Misère"
                            }
                            Text(
                                text = "${uiState.moveHistory.size} moves  •  $modeName" +
                                    if (uiState.isBlitzEnabled) "  •  5s Blitz" else "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        FilledTonalIconButton(
                            onClick = onShareResult,
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("share_result_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Share,
                                contentDescription = stringResource(R.string.cd_share_match),
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }

                    // Accuracy & Elo Delta chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        uiState.lastAccuracyPercent?.let { acc ->
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer,
                            ) {
                                Text(
                                    text = "$acc% ACCURACY",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                )
                            }
                        }

                        uiState.lastEloDelta?.let { delta ->
                            val sign = if (delta >= 0) "+" else ""
                            Surface(
                                shape = CircleShape,
                                color = if (delta >= 0) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.tertiaryContainer
                                },
                            ) {
                                Text(
                                    text = "$sign$delta ELO",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (delta >= 0) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onTertiaryContainer
                                    },
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                )
                            }
                        }
                    }

                    // Newly unlocked Mastery Medal banner
                    if (uiState.newlyUnlockedMedals.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.EmojiEvents,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp),
                                )
                                Text(
                                    text = "Trophy Unlocked: ${uiState.newlyUnlockedMedals.joinToString(", ") { it.title }}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        val saved = uiState.lastSavedMatch
                        if (saved != null) {
                            FilledTonalButton(
                                onClick = { onOpenReplay(saved) },
                                shape = CircleShape,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("replay_last_match_button"),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PlayCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.action_replay))
                            }
                        }
                        Button(
                            onClick = onNewRound,
                            shape = CircleShape,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("play_again_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.action_play_again))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayActionDock(
    uiState: GameUiState,
    onRequestHint: () -> Unit,
    onNewRound: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        FilledTonalButton(
            onClick = onRequestHint,
            enabled = !uiState.isAiThinking && !uiState.snapshot.isGameOver,
            shape = CircleShape,
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("hint_button"),
        ) {
            Icon(
                imageVector = Icons.Rounded.Lightbulb,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.action_hint))
        }

        OutlinedButton(
            onClick = onNewRound,
            shape = CircleShape,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("new_round_button"),
        ) {
            Icon(
                imageVector = Icons.Rounded.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.action_reset))
        }
    }
}
