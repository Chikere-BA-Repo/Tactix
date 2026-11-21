package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.AiDifficulty
import com.example.domain.GameMode
import com.example.domain.OpponentType
import com.example.ui.components.CeramicIconButton
import com.example.ui.components.CeramicPiece
import com.example.ui.components.CeramicPrimaryButton
import com.example.ui.components.CeramicSwitchRow
import com.example.ui.components.FloatingPieces
import com.example.ui.components.MatchSetupSheet
import com.example.ui.components.ModeLook
import com.example.ui.components.TactixInteractiveSurface
import com.example.ui.components.modeLook
import com.example.ui.theme.Ceramic
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
    var setupMode by rememberSaveable { mutableStateOf<GameMode?>(null) }
    var showSettings by rememberSaveable { mutableStateOf(false) }

    val heroMode = gameState.mode
    val otherModes = GameMode.entries.filter { it != heroMode }
    val isResume = gameState.hasActiveUnfinishedMatch

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
                .padding(top = 10.dp)
                .padding(contentBottomPadding)
                .testTag("home_hub_screen"),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            HomeHeader(onOpenSettings = { showSettings = true })

            StatChips(
                elo = gameState.playerElo,
                dailyStreak = dailyStreak,
                medals = unlockedMedalCount,
                onOpenDaily = onOpenDaily,
                onOpenArchive = onOpenArchive,
            )

            HeroModeCard(
                look = modeLook(heroMode),
                isResume = isResume,
                resumeDetail = "Move ${gameState.moveHistory.size} in progress. Pick up where you left off.",
                isBlitz = gameState.isBlitzEnabled,
                onPrimary = {
                    if (isResume) onResumeActiveMatch() else setupMode = heroMode
                },
                onNewGame = { setupMode = heroMode },
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                otherModes.forEach { mode ->
                    ModeTile(
                        look = modeLook(mode),
                        onClick = { setupMode = mode },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            DailyPuzzlesCard(
                solvedCount = dailySolvedCount,
                onClick = onOpenDaily,
            )

            Text(
                text = stringResource(R.string.studio_crafted_by),
                style = MaterialTheme.typography.labelMedium,
                color = Ceramic.Muted,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 4.dp, bottom = 8.dp),
            )
        }
    }

    setupMode?.let { mode ->
        MatchSetupSheet(
            mode = mode,
            opponentType = gameState.opponentType,
            aiDifficulty = gameState.aiDifficulty,
            isBlitzEnabled = gameState.isBlitzEnabled,
            humanSide = gameState.humanSide,
            onConfigureOpponent = onConfigureOpponent,
            onConfigureDifficulty = onConfigureDifficulty,
            onToggleBlitz = onToggleBlitz,
            onToggleHumanSide = onToggleHumanSide,
            onStart = {
                setupMode = null
                onLaunchMode(mode)
            },
            onDismiss = { setupMode = null },
        )
    }

    if (showSettings) {
        SettingsSheet(
            hapticsEnabled = gameState.hapticsEnabled,
            onToggleHaptics = onToggleHaptics,
            onOpenTutorial = {
                showSettings = false
                onOpenTutorial()
            },
            onDismiss = { showSettings = false },
        )
    }
}

@Composable
private fun HomeHeader(onOpenSettings: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val iconShape = RoundedCornerShape(12.dp)
            Image(
                painter = painterResource(id = R.drawable.tactix_bold_icon_clean),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .shadow(8.dp, iconShape, clip = false)
                    .clip(iconShape),
            )
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                color = Ceramic.Obsidian,
            )
        }
        CeramicIconButton(
            icon = Icons.Rounded.Tune,
            contentDescription = "Settings",
            onClick = onOpenSettings,
            modifier = Modifier.testTag("home_settings_button"),
        )
    }
}

@Composable
private fun StatChips(
    elo: Int,
    dailyStreak: Int,
    medals: Int,
    onOpenDaily: () -> Unit,
    onOpenArchive: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatChip(label = "Rating", value = elo.toString(), onClick = onOpenArchive)
        StatChip(
            label = if (dailyStreak > 0) "day streak" else "Start a streak",
            value = if (dailyStreak > 0) dailyStreak.toString() else "",
            icon = Icons.Rounded.LocalFireDepartment,
            iconTint = Ceramic.Vermilion,
            valueFirst = true,
            onClick = onOpenDaily,
        )
        StatChip(
            label = if (medals == 1) "medal" else "medals",
            value = medals.toString(),
            icon = Icons.Rounded.EmojiEvents,
            iconTint = Ceramic.Cobalt,
            valueFirst = true,
            onClick = onOpenArchive,
        )
    }
}

@Composable
private fun StatChip(
    label: String,
    value: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    iconTint: Color = Ceramic.Obsidian,
    valueFirst: Boolean = false,
) {
    TactixInteractiveSurface(
        onClick = onClick,
        shape = CircleShape,
        color = Ceramic.Card,
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp),
                )
            }
            val valueText: @Composable () -> Unit = {
                if (value.isNotEmpty()) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                        color = Ceramic.Obsidian,
                    )
                }
            }
            val labelText: @Composable () -> Unit = {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                    fontWeight = FontWeight.Medium,
                    color = Ceramic.Muted,
                )
            }
            if (valueFirst) {
                valueText()
                labelText()
            } else {
                labelText()
                valueText()
            }
        }
    }
}

@Composable
private fun HeroModeCard(
    look: ModeLook,
    isResume: Boolean,
    resumeDetail: String,
    isBlitz: Boolean,
    onPrimary: () -> Unit,
    onNewGame: () -> Unit,
) {
    val shape = RoundedCornerShape(32.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(312.dp)
            .shadow(
                elevation = 22.dp,
                shape = shape,
                clip = false,
                ambientColor = look.background,
                spotColor = look.background,
            )
            .clip(shape)
            .background(look.background)
            .testTag("hub_hero_card"),
    ) {
        FloatingPieces(look = look, leadSize = 168.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = if (isResume) "IN PROGRESS" else "LAST PLAYED",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = look.content,
                )
                if (isBlitz) {
                    Text(
                        text = "BLITZ",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = look.background,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(look.content)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = look.title,
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 36.sp),
                color = look.content,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (isResume) resumeDetail else look.description,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = look.muted,
                modifier = Modifier.widthIn(max = 200.dp),
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CeramicPrimaryButton(
                    label = if (isResume) "Continue" else "Play",
                    onClick = onPrimary,
                    testTag = "hub_hero_play_button",
                    modifier = Modifier.weight(1f),
                )
                if (isResume) {
                    TactixInteractiveSurface(
                        onClick = onNewGame,
                        shape = RoundedCornerShape(20.dp),
                        color = Ceramic.Obsidian.copy(alpha = 0.22f),
                        modifier = Modifier
                            .height(58.dp)
                            .testTag("hub_hero_new_game_button"),
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 18.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "New",
                                style = MaterialTheme.typography.labelLarge,
                                color = look.content,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeTile(
    look: ModeLook,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(28.dp)
    TactixInteractiveSurface(
        onClick = onClick,
        shape = shape,
        color = look.background,
        shadowElevation = 8.dp,
        modifier = modifier
            .height(164.dp)
            .testTag("hub_mode_${look.mode.name.lowercase()}_card"),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            CeramicPiece(
                player = look.leadPiece,
                color = look.leadColor,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 14.dp, y = 16.dp)
                    .size(96.dp)
                    .rotate(-12f),
            )
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = look.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = look.content,
                )
                Text(
                    text = look.tagline,
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp),
                    color = look.muted,
                )
            }
        }
    }
}

@Composable
private fun DailyPuzzlesCard(
    solvedCount: Int,
    onClick: () -> Unit,
) {
    TactixInteractiveSurface(
        onClick = onClick,
        shape = RoundedCornerShape(26.dp),
        color = Ceramic.Card,
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hub_daily_card"),
    ) {
        Row(
            modifier = Modifier.padding(start = 20.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Today’s puzzles",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 15.sp),
                    color = Ceramic.Obsidian,
                )
                Text(
                    text = if (solvedCount >= 3) "All solved. Try Puzzle Rush." else "$solvedCount of 3 solved",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    color = Ceramic.Muted,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (i in 0 until 3) {
                    val solved = i < solvedCount
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (solved) Ceramic.Cobalt else Ceramic.Socket),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (solved) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsSheet(
    hapticsEnabled: Boolean,
    onToggleHaptics: () -> Unit,
    onOpenTutorial: () -> Unit,
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
                .padding(horizontal = 22.dp)
                .padding(bottom = 28.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineSmall,
                color = Ceramic.Obsidian,
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Ceramic.Paper)
                    .padding(horizontal = 16.dp, vertical = 2.dp),
            ) {
                CeramicSwitchRow(
                    title = "Vibration",
                    subtitle = "Feel each move and win",
                    checked = hapticsEnabled,
                    onToggle = onToggleHaptics,
                    testTag = "settings_haptics_switch",
                )
            }
            TactixInteractiveSurface(
                onClick = onOpenTutorial,
                shape = RoundedCornerShape(20.dp),
                color = Ceramic.Paper,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_tutorial_row"),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                        tint = Ceramic.Cobalt,
                        modifier = Modifier.size(22.dp),
                    )
                    Text(
                        text = "How to play",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                        color = Ceramic.Obsidian,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = Ceramic.Muted,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.width(1.dp))
        }
    }
}
