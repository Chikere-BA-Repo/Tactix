package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.GameMode
import com.example.domain.MatchOutcome
import com.example.domain.MoveGrade
import com.example.domain.OpponentType
import com.example.domain.PieceSkin
import com.example.domain.Player
import com.example.ui.viewmodel.ReplayStudioState
import kotlin.math.roundToInt

@Composable
fun ReplayStudioModal(
    studio: ReplayStudioState,
    skin: PieceSkin,
    onClose: () -> Unit,
    onTogglePlay: () -> Unit,
    onStepBack: () -> Unit,
    onStepForward: () -> Unit,
    onSeek: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onClose)

    val totalPlies = (studio.snapshots.size - 1).coerceAtLeast(0)
    val snapshot = studio.activeSnapshot
    val lastMove = snapshot.lastMove
    val moveAnalysis = studio.currentMoveAnalysis

    val modeLabel = when (studio.mode) {
        GameMode.INFINITY -> "Infinity"
        GameMode.CLASSIC -> "Classic"
        GameMode.MISERE -> "Misère"
    }
    val opponentLabel = if (studio.match.opponentType == OpponentType.AI.name) {
        val level = when (studio.match.aiDifficulty) {
            "CASUAL" -> "Easy"
            "TACTICAL" -> "Hard"
            "MASTER" -> "Master"
            else -> "Adaptive"
        }
        "Solo • $level"
    } else {
        "2 Players"
    }
    val outcomeLabel = when (studio.match.outcome) {
        MatchOutcome.X_WIN.name -> "X Won"
        MatchOutcome.O_WIN.name -> "O Won"
        else -> "Tie Game"
    }

    val moveCaption = if (lastMove == null) {
        stringResource(R.string.replay_start_board)
    } else {
        val coord = cellCoordinate(lastMove.cellIndex)
        val fadePart = lastMove.expiredCellIndex?.let { " • ${cellCoordinate(it)} faded" } ?: ""
        "Move ${lastMove.ply}/$totalPlies • ${lastMove.player.symbol} → $coord$fadePart"
    }

    val bestAltHint = if (moveAnalysis != null &&
        (moveAnalysis.grade == MoveGrade.INACCURACY || moveAnalysis.grade == MoveGrade.BLUNDER) &&
        moveAnalysis.bestCell != moveAnalysis.playedCell
    ) {
        moveAnalysis.bestCell
    } else {
        null
    }

    val activeDotColor = when (lastMove?.player) {
        Player.X -> MaterialTheme.colorScheme.primary
        Player.O -> MaterialTheme.colorScheme.tertiary
        null -> MaterialTheme.colorScheme.outline
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.64f))
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 472.dp),
            shape = RoundedCornerShape(34.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 14.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.replay_studio_title),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                            ) {
                                Text(
                                    text = "${studio.match.accuracyPercent}% ACC",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                )
                            }
                        }
                        Text(
                            text = "$modeLabel  •  $opponentLabel  •  $outcomeLabel",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    FilledTonalIconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("replay_close_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.cd_close_replay),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TactixBoard(
                    snapshot = snapshot,
                    mode = studio.mode,
                    skin = skin,
                    onCellClick = {},
                    enabled = false,
                    hintCellIndex = bestAltHint,
                    testTagPrefix = "replay_board",
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(activeDotColor),
                                )
                                Text(
                                    text = moveCaption,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                )
                            }
                            if (moveAnalysis != null) {
                                MoveGradeBadge(grade = moveAnalysis.grade)
                            }
                        }

                        if (bestAltHint != null) {
                            Text(
                                text = "Stronger play was ${cellCoordinate(bestAltHint)} (highlighted).",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (totalPlies > 0) {
                    Slider(
                        value = studio.currentStep.toFloat(),
                        onValueChange = { onSeek(it.roundToInt()) },
                        valueRange = 0f..totalPlies.toFloat(),
                        steps = (totalPlies - 1).coerceAtLeast(0),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("replay_scrubber_slider"),
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FilledTonalIconButton(
                        onClick = onStepBack,
                        enabled = studio.currentStep > 0,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("replay_step_back_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipPrevious,
                            contentDescription = stringResource(R.string.cd_step_back),
                        )
                    }

                    FilledIconButton(
                        onClick = onTogglePlay,
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        modifier = Modifier
                            .size(58.dp)
                            .testTag("replay_play_pause_button"),
                    ) {
                        Icon(
                            imageVector = if (studio.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = stringResource(R.string.cd_play_pause_replay),
                            modifier = Modifier.size(28.dp),
                        )
                    }

                    FilledTonalIconButton(
                        onClick = onStepForward,
                        enabled = studio.currentStep < totalPlies,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("replay_step_forward_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = stringResource(R.string.cd_step_forward),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MoveGradeBadge(grade: MoveGrade) {
    val (bg, fg) = when (grade) {
        MoveGrade.BRILLIANT -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        MoveGrade.BEST -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        MoveGrade.GOOD -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        MoveGrade.INACCURACY -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        MoveGrade.BLUNDER -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    }
    Surface(
        shape = CircleShape,
        color = bg,
    ) {
        Text(
            text = grade.label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = fg,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

private fun cellCoordinate(cellIndex: Int): String {
    val col = when (cellIndex % 3) {
        0 -> "A"
        1 -> "B"
        else -> "C"
    }
    val row = (cellIndex / 3) + 1
    return "$col$row"
}
