package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.AllInclusive
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Flip
import androidx.compose.material.icons.rounded.Grid3x3
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MobileOff
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.AiDifficulty
import com.example.domain.GameMode
import com.example.domain.MasteryMedal
import com.example.domain.OpponentType
import com.example.domain.Player
import com.example.ui.components.PillOption
import com.example.ui.components.TactixAppEmblem
import com.example.ui.components.TactixInteractiveSurface
import com.example.ui.components.TactixPillSwitcher
import com.example.ui.viewmodel.GameUiState

@Composable
fun HomeScreen(
    gameState: GameUiState,
    dailySolvedCount: Int,
    dailyStreak: Int,
    unlockedMedalCount: Int,
    onLaunchMode: (GameMode) -> Unit,
    onResumeActiveMatch: () -> Unit,
    onOpenDaily: () -> Unit,
    onOpenArchive: () -> Unit,
    onOpenTutorial: () -> Unit,
    onConfigureOpponent: (OpponentType) -> Unit,
    onConfigureDifficulty: (AiDifficulty) -> Unit,
    onToggleBlitz: () -> Unit,
    onToggleHumanSide: () -> Unit,
    onToggleHaptics: () -> Unit,
    contentBottomPadding: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 14.dp)
                .padding(contentBottomPadding)
                .testTag("home_hub_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            HomeHubHeader(
                hapticsEnabled = gameState.hapticsEnabled,
                onOpenTutorial = onOpenTutorial,
                onToggleHaptics = onToggleHaptics,
            )

            // Unified floating glass player bar
            UnifiedPlayerBar(
                elo = gameState.playerElo,
                dailyStreak = dailyStreak,
                unlockedMedalCount = unlockedMedalCount,
                onOpenDaily = onOpenDaily,
                onOpenArchive = onOpenArchive,
            )

            // Resume unfinished match banner (appears when user stepped back mid-game)
            AnimatedVisibility(
                visible = gameState.hasActiveUnfinishedMatch,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                ResumeMatchBanner(
                    gameState = gameState,
                    onResume = onResumeActiveMatch,
                )
            }

            // Section Title: Pick a Mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.home_choose_mode),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.4).sp,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                if (gameState.isBlitzEnabled) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Bolt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = "5s BLITZ",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                            )
                        }
                    }
                }
            }

            // Bold Full-Color Editorial Hero Card: Infinity Mode
            InfinityHeroModeCard(
                isBlitz = gameState.isBlitzEnabled,
                onClick = { onLaunchMode(GameMode.INFINITY) },
            )

            // 2-Column Bento Row: Classic & Misère
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SecondaryModeCard(
                    title = stringResource(R.string.mode_classic),
                    badge = "3×3",
                    tagline = "The original showdown. Line up three first.",
                    icon = Icons.Rounded.Grid3x3,
                    accent = MaterialTheme.colorScheme.primary,
                    onClick = { onLaunchMode(GameMode.CLASSIC) },
                    testTag = "hub_mode_classic_card",
                    modifier = Modifier.weight(1f),
                )
                SecondaryModeCard(
                    title = stringResource(R.string.mode_misere),
                    badge = "REVERSE",
                    tagline = "3 in a row loses. Outsmart your rival.",
                    icon = Icons.Rounded.Flip,
                    accent = MaterialTheme.colorScheme.tertiary,
                    onClick = { onLaunchMode(GameMode.MISERE) },
                    testTag = "hub_mode_misere_card",
                    modifier = Modifier.weight(1f),
                )
            }

            // Streamlined Game Setup Card
            HubMatchSetupCard(
                gameState = gameState,
                onConfigureOpponent = onConfigureOpponent,
                onConfigureDifficulty = onConfigureDifficulty,
                onToggleBlitz = onToggleBlitz,
                onToggleHumanSide = onToggleHumanSide,
            )

            // Daily Puzzles Banner
            DailyChallengeBanner(
                solvedCount = dailySolvedCount,
                dailyStreak = dailyStreak,
                onClick = onOpenDaily,
            )

            // Clean Studio Signature
            StudioFooter()
        }
    }
}

@Composable
private fun HomeHubHeader(
    hapticsEnabled: Boolean,
    onOpenTutorial: () -> Unit,
    onToggleHaptics: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TactixAppEmblem(
                size = 44.dp,
                cornerRadius = 14.dp,
            )
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = stringResource(R.string.splash_tagline),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilledTonalIconButton(
                onClick = onOpenTutorial,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                modifier = Modifier
                    .size(44.dp)
                    .minimumInteractiveComponentSize()
                    .testTag("home_how_to_play_button"),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.HelpOutline,
                    contentDescription = stringResource(R.string.cd_how_to_play),
                    modifier = Modifier.size(20.dp),
                )
            }

            FilledTonalIconButton(
                onClick = onToggleHaptics,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = if (hapticsEnabled) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                    contentColor = if (hapticsEnabled) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                ),
                modifier = Modifier
                    .size(44.dp)
                    .minimumInteractiveComponentSize()
                    .testTag("home_haptics_button"),
            ) {
                Icon(
                    imageVector = if (hapticsEnabled) Icons.Rounded.Vibration else Icons.Rounded.MobileOff,
                    contentDescription = stringResource(R.string.cd_toggle_haptics),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun UnifiedPlayerBar(
    elo: Int,
    dailyStreak: Int,
    unlockedMedalCount: Int,
    onOpenDaily: () -> Unit,
    onOpenArchive: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatSegment(
                label = "RATING",
                value = elo.toString(),
                accent = MaterialTheme.colorScheme.primary,
                onClick = onOpenArchive,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(26.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
            )
            StatSegment(
                label = "STREAK",
                value = "${dailyStreak}d",
                icon = Icons.Rounded.LocalFireDepartment,
                accent = MaterialTheme.colorScheme.tertiary,
                onClick = onOpenDaily,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(26.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
            )
            StatSegment(
                label = "TROPHIES",
                value = "$unlockedMedalCount/${MasteryMedal.entries.size}",
                icon = Icons.Rounded.EmojiEvents,
                accent = MaterialTheme.colorScheme.secondary,
                onClick = onOpenArchive,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatSegment(
    label: String,
    value: String,
    accent: Color,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(15.dp),
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 0.6.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ResumeMatchBanner(
    gameState: GameUiState,
    onResume: () -> Unit,
) {
    val modeLabel = when (gameState.mode) {
        GameMode.INFINITY -> stringResource(R.string.mode_infinity)
        GameMode.CLASSIC -> stringResource(R.string.mode_classic)
        GameMode.MISERE -> stringResource(R.string.mode_misere)
    }

    TactixInteractiveSurface(
        onClick = onResume,
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.94f),
        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home_resume_match_card"),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = stringResource(R.string.home_resume_match),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Text(
                        text = "$modeLabel  •  Move ${gameState.moveHistory.size}" +
                            if (gameState.isBlitzEnabled) "  •  5s Blitz" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f),
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun InfinityHeroModeCard(
    isBlitz: Boolean,
    onClick: () -> Unit,
) {
    val primary = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val tertiary = MaterialTheme.colorScheme.tertiary

    TactixInteractiveSurface(
        onClick = onClick,
        shape = RoundedCornerShape(30.dp),
        color = primary,
        border = BorderStroke(1.dp, onPrimary.copy(alpha = 0.16f)),
        shadowElevation = 10.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hub_mode_infinity_card"),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            primary,
                            primaryContainer.copy(alpha = 0.72f),
                            tertiary.copy(alpha = 0.88f),
                        ),
                    ),
                ),
        ) {
            // Subtle architectural 3x3 watermark art in top-right corner
            Canvas(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(144.dp)
                    .padding(12.dp),
            ) {
                val cell = size.minDimension / 3f
                val lineColor = onPrimary.copy(alpha = 0.14f)
                val gridStroke = 1.5.dp.toPx()
                for (i in 1..2) {
                    drawLine(lineColor, Offset(cell * i, 0f), Offset(cell * i, size.height), gridStroke)
                    drawLine(lineColor, Offset(0f, cell * i), Offset(size.width, cell * i), gridStroke)
                }
                val markStroke = 3.2.dp.toPx()
                // Decorative glowing X
                val pad = cell * 0.25f
                drawLine(
                    onPrimary.copy(alpha = 0.24f),
                    Offset(cell + pad, cell + pad),
                    Offset(cell * 2 - pad, cell * 2 - pad),
                    markStroke,
                    StrokeCap.Round,
                )
                drawLine(
                    onPrimary.copy(alpha = 0.24f),
                    Offset(cell * 2 - pad, cell + pad),
                    Offset(cell + pad, cell * 2 - pad),
                    markStroke,
                    StrokeCap.Round,
                )
                // Decorative glowing O
                drawArc(
                    color = onPrimary.copy(alpha = 0.22f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(cell * 2 + pad, pad),
                    size = Size(cell - pad * 2f, cell - pad * 2f),
                    style = Stroke(width = markStroke),
                )
            }

            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = CircleShape,
                        color = onPrimary.copy(alpha = 0.18f),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AllInclusive,
                                contentDescription = null,
                                tint = onPrimary,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = "FAN FAVORITE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = onPrimary,
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = onPrimary.copy(alpha = 0.14f),
                    ) {
                        Text(
                            text = "NO DRAWS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = onPrimary,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        text = stringResource(R.string.mode_infinity),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.8).sp,
                        color = onPrimary,
                    )
                    Text(
                        text = "Only 3 marks stay on the board. Drop a 4th and your oldest vanishes — every game ends with a winner.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = onPrimary.copy(alpha = 0.88f),
                    )
                }

                // Crisp high-contrast action pill
                Surface(
                    shape = CircleShape,
                    color = onPrimary,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = if (isBlitz) "Play Infinity Blitz" else stringResource(R.string.home_play_infinity),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = primary,
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            tint = primary,
                            modifier = Modifier.size(17.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SecondaryModeCard(
    title: String,
    badge: String,
    tagline: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    TactixInteractiveSurface(
        onClick = onClick,
        modifier = modifier.testTag(testTag),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.95f),
        border = BorderStroke(1.2.dp, accent.copy(alpha = 0.32f)),
        tonalElevation = 2.dp,
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
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(accent.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(22.dp),
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = accent.copy(alpha = 0.12f),
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = accent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = tagline,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    minLines = 2,
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = "Play",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun HubMatchSetupCard(
    gameState: GameUiState,
    onConfigureOpponent: (OpponentType) -> Unit,
    onConfigureDifficulty: (AiDifficulty) -> Unit,
    onToggleBlitz: () -> Unit,
    onToggleHumanSide: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
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
                    text = stringResource(R.string.home_match_setup),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (gameState.opponentType == OpponentType.AI) {
                        "Playing Solo"
                    } else {
                        "Same Device"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Solo vs 2 Players switcher
            TactixPillSwitcher(
                options = listOf(
                    PillOption(
                        value = OpponentType.AI,
                        label = stringResource(R.string.opponent_ai),
                        icon = Icons.Rounded.Person,
                        testTag = "hub_opponent_ai",
                    ),
                    PillOption(
                        value = OpponentType.PASS_AND_PLAY,
                        label = stringResource(R.string.opponent_pass_play),
                        icon = Icons.Rounded.Groups,
                        testTag = "hub_opponent_pvp",
                    ),
                ),
                selected = gameState.opponentType,
                onSelect = onConfigureOpponent,
                modifier = Modifier.fillMaxWidth(),
            )

            // Difficulty Tier Selector (Solo)
            AnimatedVisibility(
                visible = gameState.opponentType == OpponentType.AI,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    val tiers = listOf(
                        AiDifficulty.ADAPTIVE to stringResource(R.string.ai_adaptive),
                        AiDifficulty.CASUAL to stringResource(R.string.ai_casual),
                        AiDifficulty.TACTICAL to stringResource(R.string.ai_tactical),
                        AiDifficulty.MASTER to stringResource(R.string.ai_master),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        tiers.forEach { (difficulty, label) ->
                            val selected = gameState.aiDifficulty == difficulty
                            val pillBg by animateColorAsState(
                                targetValue = if (selected) {
                                    if (difficulty == AiDifficulty.MASTER) {
                                        MaterialTheme.colorScheme.tertiaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.secondaryContainer
                                    }
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.72f)
                                },
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                label = "hub_tier_bg_${difficulty.name}",
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
                                label = "hub_tier_fg_${difficulty.name}",
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .minimumInteractiveComponentSize()
                                    .clip(CircleShape)
                                    .background(pillBg)
                                    .border(
                                        width = 1.dp,
                                        color = if (selected) {
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.42f)
                                        } else {
                                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                        },
                                        shape = CircleShape,
                                    )
                                    .testTag("hub_tier_${difficulty.name.lowercase()}")
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = { onConfigureDifficulty(difficulty) },
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
                                            modifier = Modifier.size(12.dp),
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

                    val tierTip = when (gameState.aiDifficulty) {
                        AiDifficulty.ADAPTIVE -> "Matches your pace and turns up the heat as you win (${gameState.adaptiveIntensityLabel})."
                        AiDifficulty.CASUAL -> "A friendly warm-up with room to experiment."
                        AiDifficulty.TACTICAL -> "Reads ahead and punishes careless moves."
                        AiDifficulty.MASTER -> "Plays a near-perfect game. Bring your best."
                    }
                    Text(
                        text = tierTip,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }
            }

            // Quick Modifiers Row: 5s Blitz + Go First/Second
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ModifierToggleChip(
                    label = if (gameState.isBlitzEnabled) "5s Blitz: On" else "5s Blitz: Off",
                    icon = Icons.Rounded.Timer,
                    active = gameState.isBlitzEnabled,
                    activeBg = MaterialTheme.colorScheme.tertiaryContainer,
                    activeFg = MaterialTheme.colorScheme.onTertiaryContainer,
                    onClick = onToggleBlitz,
                    testTag = "hub_blitz_toggle",
                    modifier = Modifier.weight(1f),
                )

                if (gameState.opponentType == OpponentType.AI) {
                    val playingX = gameState.humanSide == Player.X
                    ModifierToggleChip(
                        label = if (playingX) "Go 1st (X)" else "Go 2nd (O)",
                        icon = Icons.Rounded.SwapHoriz,
                        active = !playingX,
                        activeBg = MaterialTheme.colorScheme.primaryContainer,
                        activeFg = MaterialTheme.colorScheme.onPrimaryContainer,
                        onClick = onToggleHumanSide,
                        testTag = "hub_side_toggle",
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun ModifierToggleChip(
    label: String,
    icon: ImageVector,
    active: Boolean,
    activeBg: Color,
    activeFg: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(42.dp)
            .minimumInteractiveComponentSize()
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        color = if (active) activeBg else MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.78f),
        border = BorderStroke(
            width = 1.dp,
            color = if (active) activeFg.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (active) activeFg else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                color = if (active) activeFg else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun DailyChallengeBanner(
    solvedCount: Int,
    dailyStreak: Int,
    onClick: () -> Unit,
) {
    TactixInteractiveSurface(
        onClick = onClick,
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hub_daily_banner_card"),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(13.dp),
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(MaterialTheme.colorScheme.tertiaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(22.dp),
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Daily Puzzles & Rush",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        repeat(3) { idx ->
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (idx < solvedCount) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                                        },
                                    ),
                            )
                        }
                        Text(
                            text = if (solvedCount >= 3) {
                                "Cleared today • ${dailyStreak}d streak"
                            } else {
                                "$solvedCount of 3 solved today"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = stringResource(R.string.home_open_daily),
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun StudioFooter() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
            modifier = Modifier.fillMaxWidth(0.48f),
        )
        Text(
            text = stringResource(R.string.studio_crafted_by),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
        )
    }
}
