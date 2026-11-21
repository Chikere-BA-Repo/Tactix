package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.domain.GameMode
import com.example.ui.components.TactixBoard
import com.example.ui.viewmodel.DailyUiState
import com.example.ui.viewmodel.PuzzlePlayStatus

@Composable
fun DailyScreen(
    uiState: DailyUiState,
    onSelectDailyPuzzle: (Int) -> Unit,
    onStartStreakRush: () -> Unit,
    onReturnToDailySet: () -> Unit,
    onCellTap: (Int) -> Unit,
    onRequestHint: () -> Unit,
    onRetry: () -> Unit,
    onNextPuzzle: () -> Unit,
    contentBottomPadding: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier,
) {
    val puzzle = uiState.activePuzzle ?: return

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
                ) {
                    TactixBoard(
                        snapshot = uiState.snapshot,
                        mode = puzzle.mode,
                        skin = uiState.selectedSkin,
                        onCellClick = onCellTap,
                        enabled = uiState.status == PuzzlePlayStatus.PLAYING,
                        hintCellIndex = uiState.activeHint?.cellIndex,
                        shakeTrigger = uiState.shakeCounter,
                        testTagPrefix = "daily_board",
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .widthIn(max = 440.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    DailyHeaderAndStreak(
                        uiState = uiState,
                        onStartStreakRush = onStartStreakRush,
                        onReturnToDailySet = onReturnToDailySet,
                    )
                    if (!uiState.isRushMode) {
                        DailyPuzzlePills(
                            uiState = uiState,
                            onSelectDailyPuzzle = onSelectDailyPuzzle,
                        )
                    }
                    PuzzleObjectiveCard(uiState = uiState)
                    DailyActionBar(
                        uiState = uiState,
                        onRequestHint = onRequestHint,
                        onRetry = onRetry,
                        onNextPuzzle = onNextPuzzle,
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
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                DailyHeaderAndStreak(
                    uiState = uiState,
                    onStartStreakRush = onStartStreakRush,
                    onReturnToDailySet = onReturnToDailySet,
                )

                if (!uiState.isRushMode) {
                    DailyPuzzlePills(
                        uiState = uiState,
                        onSelectDailyPuzzle = onSelectDailyPuzzle,
                    )
                }

                PuzzleObjectiveCard(uiState = uiState)

                TactixBoard(
                    snapshot = uiState.snapshot,
                    mode = puzzle.mode,
                    skin = uiState.selectedSkin,
                    onCellClick = onCellTap,
                    enabled = uiState.status == PuzzlePlayStatus.PLAYING,
                    hintCellIndex = uiState.activeHint?.cellIndex,
                    shakeTrigger = uiState.shakeCounter,
                    testTagPrefix = "daily_board",
                )

                DailyActionBar(
                    uiState = uiState,
                    onRequestHint = onRequestHint,
                    onRetry = onRetry,
                    onNextPuzzle = onNextPuzzle,
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun DailyHeaderAndStreak(
    uiState: DailyUiState,
    onStartStreakRush: () -> Unit,
    onReturnToDailySet: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = if (uiState.isRushMode) {
                        stringResource(R.string.streak_rush_title)
                    } else {
                        stringResource(R.string.daily_title)
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = if (uiState.isRushMode) {
                        "How many can you solve in a row?"
                    } else {
                        "3 fresh brain-teasers every day"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            FilledTonalButton(
                onClick = if (uiState.isRushMode) onReturnToDailySet else onStartStreakRush,
                shape = CircleShape,
                modifier = Modifier
                    .height(42.dp)
                    .testTag("toggle_rush_mode_button"),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Bolt,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (uiState.isRushMode) {
                        stringResource(R.string.action_back_to_daily)
                    } else {
                        "Rush"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        val heroAccent = if (uiState.isRushMode) {
            MaterialTheme.colorScheme.tertiary
        } else {
            MaterialTheme.colorScheme.primary
        }

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(1.dp, heroAccent.copy(alpha = 0.30f)),
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                heroAccent.copy(alpha = 0.14f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (uiState.isRushMode) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Bolt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                )
                            }
                            Column {
                                Text(
                                    text = "${uiState.rushStreak}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = "ACTIVE RUSH  •  BEST ${uiState.bestRushStreak}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                            Column {
                                Text(
                                    text = "${uiState.dailyStreak} ${stringResource(R.string.daily_streak_label)}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = if (uiState.isDailyComplete) {
                                        stringResource(R.string.daily_completed_banner)
                                    } else {
                                        val done = (0..2).count { uiState.isPuzzleSolved(it) }
                                        "$done of 3 solved today"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        // Modern 7-day capsule rhythm
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            uiState.sevenDayCompletion.forEachIndexed { idx, completed ->
                                val isToday = idx == uiState.sevenDayCompletion.lastIndex
                                Box(
                                    modifier = Modifier
                                        .width(if (isToday) 10.dp else 7.dp)
                                        .height(if (completed) 22.dp else 12.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                completed -> MaterialTheme.colorScheme.primary
                                                isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                                                else -> MaterialTheme.colorScheme.surfaceContainerHighest
                                            },
                                        ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyPuzzlePills(
    uiState: DailyUiState,
    onSelectDailyPuzzle: (Int) -> Unit,
) {
    val solvedCount = (0 until uiState.dailyPuzzles.size).count { uiState.isPuzzleSolved(it) }
    val progress by animateFloatAsState(
        targetValue = if (uiState.dailyPuzzles.isEmpty()) 0f else solvedCount.toFloat() / uiState.dailyPuzzles.size,
        animationSpec = tween(350),
        label = "daily_progress",
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(CircleShape),
            strokeCap = StrokeCap.Round,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            uiState.dailyPuzzles.forEachIndexed { index, puzzle ->
                val selected = uiState.selectedDailyIndex == index
                val solved = uiState.isPuzzleSolved(index)
                val shape = RoundedCornerShape(18.dp)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .clip(shape)
                        .testTag("daily_puzzle_pill_$index")
                        .clickable { onSelectDailyPuzzle(index) },
                    shape = shape,
                    color = when {
                        selected -> MaterialTheme.colorScheme.primaryContainer
                        solved -> MaterialTheme.colorScheme.surfaceContainerHigh
                        else -> MaterialTheme.colorScheme.surfaceContainerLow
                    },
                    border = BorderStroke(
                        width = if (selected) 1.5.dp else 1.dp,
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                        },
                    ),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 11.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (solved) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = "Solved",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                        } else {
                            Text(
                                text = "0${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                        }
                        Text(
                            text = puzzle.difficultyLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PuzzleObjectiveCard(uiState: DailyUiState) {
    val puzzle = uiState.activePuzzle ?: return
    val statusBg by animateColorAsState(
        targetValue = when (uiState.status) {
            PuzzlePlayStatus.SOLVED -> MaterialTheme.colorScheme.primaryContainer
            PuzzlePlayStatus.WRONG_MOVE -> MaterialTheme.colorScheme.tertiaryContainer
            else -> MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f)
        },
        animationSpec = tween(260),
        label = "puzzle_status_bg",
    )
    val statusOnColor = when (uiState.status) {
        PuzzlePlayStatus.SOLVED -> MaterialTheme.colorScheme.onPrimaryContainer
        PuzzlePlayStatus.WRONG_MOVE -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = statusBg,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = puzzle.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = statusOnColor,
                )
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                ) {
                    Text(
                        text = if (puzzle.mode == GameMode.INFINITY) "INFINITY" else "CLASSIC",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }

            Text(
                text = uiState.feedbackMessage ?: puzzle.objective,
                style = MaterialTheme.typography.bodyMedium,
                color = statusOnColor.copy(alpha = 0.9f),
            )

            AnimatedVisibility(
                visible = uiState.activeHint != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                uiState.activeHint?.let { hint ->
                    Text(
                        text = "Hint: ${hint.reason}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyActionBar(
    uiState: DailyUiState,
    onRequestHint: () -> Unit,
    onRetry: () -> Unit,
    onNextPuzzle: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when (uiState.status) {
            PuzzlePlayStatus.SOLVED -> {
                OutlinedButton(
                    onClick = onRetry,
                    shape = CircleShape,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("daily_retry_button"),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.action_retry))
                }
                Button(
                    onClick = onNextPuzzle,
                    shape = CircleShape,
                    modifier = Modifier
                        .weight(1.25f)
                        .height(48.dp)
                        .testTag("daily_next_button"),
                ) {
                    Text(stringResource(R.string.action_next_puzzle))
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            PuzzlePlayStatus.WRONG_MOVE -> {
                Button(
                    onClick = onRetry,
                    shape = CircleShape,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("daily_retry_button"),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.action_retry))
                }
            }
            else -> {
                FilledTonalButton(
                    onClick = onRequestHint,
                    enabled = uiState.status == PuzzlePlayStatus.PLAYING,
                    shape = CircleShape,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("daily_hint_button"),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lightbulb,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.action_hint))
                }
                OutlinedButton(
                    onClick = onRetry,
                    shape = CircleShape,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("daily_reset_button"),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.action_retry))
                }
            }
        }
    }
}
