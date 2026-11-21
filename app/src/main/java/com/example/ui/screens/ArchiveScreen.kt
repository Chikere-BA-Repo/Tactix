package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.MatchRecordEntity
import com.example.domain.GameEngine
import com.example.domain.GameMode
import com.example.domain.MasteryMedal
import com.example.domain.MatchOutcome
import com.example.domain.OpponentType
import com.example.domain.PieceSkin
import com.example.domain.Player
import com.example.ui.components.PillOption
import com.example.ui.components.TactixPillSwitcher
import com.example.ui.components.drawStyledPiece
import com.example.ui.viewmodel.ArchiveUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ArchiveScreen(
    uiState: ArchiveUiState,
    onSelectFilter: (GameMode?) -> Unit,
    onSelectPieceSkin: (PieceSkin) -> Unit,
    onOpenReplay: (MatchRecordEntity) -> Unit,
    onDeleteMatch: (Long) -> Unit,
    contentBottomPadding: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier,
) {
    val profile = uiState.profile
    val totalSoloGames = profile.totalWins + profile.totalLosses + profile.totalDraws
    val winRateFloat = if (totalSoloGames > 0) profile.totalWins.toFloat() / totalSoloGames else 0f
    val winRatePct = (winRateFloat * 100f).toInt()
    val animatedWinRate by animateFloatAsState(
        targetValue = winRateFloat,
        animationSpec = tween(durationMillis = 700),
        label = "win_rate_ring",
    )

    val rankTier = when {
        profile.eloRating >= 1250 -> "Legend"
        profile.eloRating >= 1120 -> "Master"
        profile.eloRating >= 1020 -> "Rising Star"
        else -> "Challenger"
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 620.dp),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 14.dp,
                bottom = 16.dp + contentBottomPadding.calculateBottomPadding(),
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.archive_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = "Your trophies, board styles, and match replays",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Asymmetric Hero Bento Telemetry Card
            item {
                val primary = MaterialTheme.colorScheme.primary
                val primaryContainer = MaterialTheme.colorScheme.primaryContainer
                val onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer

                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = primaryContainer,
                    border = BorderStroke(1.dp, primary.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        primary.copy(alpha = 0.14f),
                                        Color.Transparent,
                                    ),
                                ),
                            ),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f),
                                ) {
                                    Text(
                                        text = rankTier.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                    )
                                }
                                Text(
                                    text = profile.eloRating.toString(),
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = onPrimaryContainer,
                                )
                                Text(
                                    text = "${profile.totalWins}W  •  ${profile.totalLosses}L  •  ${profile.totalDraws}D",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = onPrimaryContainer.copy(alpha = 0.80f),
                                )
                            }

                            Box(
                                modifier = Modifier.size(86.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val stroke = 7.dp.toPx()
                                    val inset = stroke / 2f
                                    val arcSize = Size(size.width - stroke, size.height - stroke)
                                    drawArc(
                                        color = onPrimaryContainer.copy(alpha = 0.16f),
                                        startAngle = 0f,
                                        sweepAngle = 360f,
                                        useCenter = false,
                                        topLeft = Offset(inset, inset),
                                        size = arcSize,
                                        style = Stroke(width = stroke),
                                    )
                                    drawArc(
                                        color = primary,
                                        startAngle = -90f,
                                        sweepAngle = 360f * animatedWinRate,
                                        useCenter = false,
                                        topLeft = Offset(inset, inset),
                                        size = arcSize,
                                        style = Stroke(width = stroke, cap = StrokeCap.Round),
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$winRatePct%",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = onPrimaryContainer,
                                    )
                                    Text(
                                        text = "WIN",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = onPrimaryContainer.copy(alpha = 0.7f),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Secondary Bento Tiles
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    CompactBentoTile(
                        label = "Best Win Streak",
                        value = profile.bestWinStreak.toString(),
                        icon = Icons.Rounded.LocalFireDepartment,
                        iconTint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                    )
                    CompactBentoTile(
                        label = stringResource(R.string.stat_best_rush),
                        value = profile.bestStreakRush.toString(),
                        icon = Icons.Rounded.Bolt,
                        iconTint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // Unlockable Piece Styles Shelf
            item {
                PieceStyleShelf(
                    selectedSkin = uiState.selectedSkin,
                    unlockedMedalCount = uiState.unlockedMedalCount,
                    onSelectSkin = onSelectPieceSkin,
                )
            }

            // Skill Mastery Medals Constellation
            item {
                MasteryMedalsCard(
                    unlockedMask = profile.unlockedMedalsMask,
                    unlockedCount = uiState.unlockedMedalCount,
                )
            }

            // 4-Way Capsule Filter Switcher
            item {
                TactixPillSwitcher(
                    options = listOf(
                        PillOption(null, stringResource(R.string.filter_all), testTag = "archive_filter_all"),
                        PillOption(GameMode.INFINITY, "Infinity", testTag = "archive_filter_infinity"),
                        PillOption(GameMode.CLASSIC, "Classic", testTag = "archive_filter_classic"),
                        PillOption(GameMode.MISERE, "Misère", testTag = "archive_filter_misere"),
                    ),
                    selected = uiState.selectedFilter,
                    onSelect = onSelectFilter,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (uiState.filteredMatches.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(26.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 34.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(26.dp),
                                )
                            }
                            Text(
                                text = stringResource(R.string.archive_empty_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                text = stringResource(R.string.archive_empty_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            } else {
                items(items = uiState.filteredMatches, key = { it.id }) { match ->
                    MatchHistoryCard(
                        match = match,
                        onReplay = { onOpenReplay(match) },
                        onDelete = { onDeleteMatch(match.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun PieceStyleShelf(
    selectedSkin: PieceSkin,
    unlockedMedalCount: Int,
    onSelectSkin: (PieceSkin) -> Unit,
) {
    val xColor = MaterialTheme.colorScheme.primary
    val oColor = MaterialTheme.colorScheme.tertiary

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Board Pieces",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Earn trophies to unlock",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PieceSkin.entries.forEach { skin ->
                    val unlocked = unlockedMedalCount >= skin.unlockMedalsRequired
                    val selected = selectedSkin == skin
                    val shape = RoundedCornerShape(18.dp)

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .clip(shape)
                            .alpha(if (unlocked) 1f else 0.58f)
                            .testTag("skin_option_${skin.id.lowercase()}")
                            .clickable(enabled = unlocked) { onSelectSkin(skin) },
                        shape = shape,
                        color = if (selected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        },
                        border = BorderStroke(
                            width = if (selected) 1.5.dp else 1.dp,
                            color = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            },
                        ),
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Canvas(modifier = Modifier.size(20.dp)) {
                                    drawStyledPiece(Player.X, skin, xColor, progress = 1f)
                                }
                                Canvas(modifier = Modifier.size(20.dp)) {
                                    drawStyledPiece(Player.O, skin, oColor, progress = 1f)
                                }
                            }
                            Text(
                                text = skin.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                maxLines = 1,
                            )
                            if (!unlocked) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Lock,
                                        contentDescription = "Locked",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(10.dp),
                                    )
                                    Text(
                                        text = "${skin.unlockMedalsRequired}m",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MasteryMedalsCard(
    unlockedMask: Int,
    unlockedCount: Int,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.EmojiEvents,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = "Trophies",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Text(
                        text = "$unlockedCount / ${MasteryMedal.entries.size}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                    )
                }
            }

            val rows = MasteryMedal.entries.chunked(2)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                rows.forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        pair.forEach { medal ->
                            val isUnlocked = (unlockedMask and medal.bitMask) != 0
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(16.dp),
                                color = if (isUnlocked) {
                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.65f)
                                },
                                border = if (isUnlocked) {
                                    BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
                                } else {
                                    null
                                },
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(3.dp),
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    ) {
                                        Icon(
                                            imageVector = if (isUnlocked) Icons.Rounded.CheckCircle else Icons.Rounded.Lock,
                                            contentDescription = if (isUnlocked) "Unlocked" else "Locked",
                                            tint = if (isUnlocked) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                                            },
                                            modifier = Modifier.size(14.dp),
                                        )
                                        Text(
                                            text = medal.title,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isUnlocked) {
                                                MaterialTheme.colorScheme.onSecondaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            maxLines = 1,
                                        )
                                    }
                                    Text(
                                        text = medal.description,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            alpha = if (isUnlocked) 0.9f else 0.7f,
                                        ),
                                        maxLines = 2,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactBentoTile(
    label: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MatchHistoryCard(
    match: MatchRecordEntity,
    onReplay: () -> Unit,
    onDelete: () -> Unit,
) {
    val mode = runCatching { GameMode.valueOf(match.mode) }.getOrDefault(GameMode.CLASSIC)
    val starter = runCatching { Player.valueOf(match.startingPlayer) }.getOrDefault(Player.X)
    val finalSnapshot = remember(match.id, match.moveSequenceCsv) {
        GameEngine.reconstructReplaySnapshots(mode, starter, match.moveSequenceCsv).last()
    }
    val dateLabel = remember(match.playedAtMillis) {
        SimpleDateFormat("MMM d • HH:mm", Locale.getDefault()).format(Date(match.playedAtMillis))
    }

    val outcomeTitle = when (match.outcome) {
        MatchOutcome.X_WIN.name -> "X Won"
        MatchOutcome.O_WIN.name -> "O Won"
        else -> "Tie Game"
    }
    val modeText = when (mode) {
        GameMode.INFINITY -> "Infinity"
        GameMode.CLASSIC -> "Classic"
        GameMode.MISERE -> "Misère"
    }
    val oppText = if (match.opponentType == OpponentType.AI.name) {
        val level = when (match.aiDifficulty) {
            "CASUAL" -> "Easy"
            "TACTICAL" -> "Hard"
            "MASTER" -> "Master"
            else -> "Adaptive"
        }
        "Solo • $level"
    } else {
        "2 Players"
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            MiniBoardThumbnail(
                board = finalSnapshot.board,
                winningLine = finalSnapshot.winningLine,
                winner = finalSnapshot.winner,
                modifier = Modifier.size(58.dp),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = outcomeTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (match.isBlitz) {
                        Icon(
                            imageVector = Icons.Rounded.Timer,
                            contentDescription = "Blitz match",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                    ) {
                        Text(
                            text = "${match.accuracyPercent}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                        )
                    }
                    if (match.opponentType == OpponentType.AI.name && match.eloDelta != 0) {
                        val sign = if (match.eloDelta > 0) "+" else ""
                        Surface(
                            shape = CircleShape,
                            color = if (match.eloDelta > 0) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.tertiaryContainer
                            },
                        ) {
                            Text(
                                text = "$sign${match.eloDelta}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (match.eloDelta > 0) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onTertiaryContainer
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                            )
                        }
                    }
                }
                Text(
                    text = "$modeText  •  $oppText  •  ${match.moveCount} moves",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = dateLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                FilledTonalButton(
                    onClick = onReplay,
                    shape = CircleShape,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier
                        .height(40.dp)
                        .testTag("archive_replay_button_${match.id}"),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = stringResource(R.string.action_replay),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("archive_delete_button_${match.id}"),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = stringResource(R.string.cd_delete_match),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniBoardThumbnail(
    board: List<Player?>,
    winningLine: List<Int>?,
    winner: Player?,
    modifier: Modifier = Modifier,
) {
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)
    val xColor = MaterialTheme.colorScheme.primary
    val oColor = MaterialTheme.colorScheme.tertiary

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(7.dp),
        ) {
            val thirdW = size.width / 3f
            val thirdH = size.height / 3f
            val gridStroke = 1.dp.toPx()

            for (i in 1..2) {
                drawLine(
                    color = gridColor,
                    start = Offset(thirdW * i, 0f),
                    end = Offset(thirdW * i, size.height),
                    strokeWidth = gridStroke,
                )
                drawLine(
                    color = gridColor,
                    start = Offset(0f, thirdH * i),
                    end = Offset(size.width, thirdH * i),
                    strokeWidth = gridStroke,
                )
            }

            val markStroke = 2.2.dp.toPx()
            val inset = thirdW * 0.24f
            for (index in 0 until 9) {
                val mark = board.getOrNull(index) ?: continue
                val row = index / 3
                val col = index % 3
                val left = col * thirdW + inset
                val top = row * thirdH + inset
                val right = (col + 1) * thirdW - inset
                val bottom = (row + 1) * thirdH - inset

                if (mark == Player.X) {
                    drawLine(
                        color = xColor,
                        start = Offset(left, top),
                        end = Offset(right, bottom),
                        strokeWidth = markStroke,
                        cap = StrokeCap.Round,
                    )
                    drawLine(
                        color = xColor,
                        start = Offset(right, top),
                        end = Offset(left, bottom),
                        strokeWidth = markStroke,
                        cap = StrokeCap.Round,
                    )
                } else {
                    drawArc(
                        color = oColor,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(left, top),
                        size = Size(right - left, bottom - top),
                        style = Stroke(width = markStroke),
                    )
                }
            }

            if (winningLine != null && winningLine.size == 3 && winner != null) {
                fun center(cellIndex: Int): Offset {
                    val r = cellIndex / 3
                    val c = cellIndex % 3
                    return Offset(c * thirdW + thirdW / 2f, r * thirdH + thirdH / 2f)
                }
                val lineColor = if (winner == Player.X) xColor else oColor
                drawLine(
                    color = lineColor.copy(alpha = 0.55f),
                    start = center(winningLine.first()),
                    end = center(winningLine.last()),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}
