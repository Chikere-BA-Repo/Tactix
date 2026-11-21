package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.AiDifficulty
import com.example.domain.GameMode
import com.example.domain.OpponentType
import com.example.domain.Player
import com.example.ui.theme.Ceramic

/**
 * How each game mode looks across the app: its block colour, text colours and the two
 * decorative ceramic pieces that float on its cards, echoing the app icon.
 */
data class ModeLook(
    val mode: GameMode,
    val title: String,
    val tagline: String,
    val description: String,
    val background: Color,
    val content: Color,
    val muted: Color,
    val leadPiece: Player,
    val leadColor: Color,
    val accentPiece: Player,
    val accentColor: Color,
)

@Composable
fun modeLook(mode: GameMode): ModeLook = when (mode) {
    GameMode.INFINITY -> ModeLook(
        mode = mode,
        title = stringResource(R.string.mode_infinity),
        tagline = "Only 3 marks stay",
        description = "Only your 3 newest marks stay. Every game has a winner.",
        background = Ceramic.Cobalt,
        content = Color.White,
        muted = Color.White,
        leadPiece = Player.X,
        leadColor = Ceramic.Ivory,
        accentPiece = Player.O,
        accentColor = Ceramic.Vermilion,
    )
    GameMode.CLASSIC -> ModeLook(
        mode = mode,
        title = stringResource(R.string.mode_classic),
        tagline = "3 in a row wins",
        description = "The original. Line up three before your rival does.",
        background = Ceramic.Vermilion,
        content = Ceramic.Obsidian,
        muted = Ceramic.Obsidian,
        leadPiece = Player.X,
        leadColor = Ceramic.Ivory,
        accentPiece = Player.O,
        accentColor = Ceramic.Cobalt,
    )
    GameMode.MISERE -> ModeLook(
        mode = mode,
        title = stringResource(R.string.mode_misere),
        tagline = "3 in a row loses",
        description = "Reverse rules. Force your rival into three in a row.",
        background = Ceramic.Obsidian,
        content = Color.White,
        muted = Ceramic.OnDarkMuted,
        leadPiece = Player.O,
        leadColor = Ceramic.Cobalt,
        accentPiece = Player.X,
        accentColor = Ceramic.Ivory,
    )
}

/** White pill with a dark arrow tile, the main call to action on coloured cards. */
@Composable
fun CeramicPrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    container: Color = Color.White,
    content: Color = Ceramic.Obsidian,
    tile: Color = Ceramic.Obsidian,
    tileIconTint: Color = Color.White,
    icon: ImageVector = Icons.AutoMirrored.Rounded.ArrowForward,
    testTag: String = "ceramic_primary_button",
) {
    val shape = RoundedCornerShape(20.dp)
    TactixInteractiveSurface(
        onClick = onClick,
        shape = shape,
        color = container,
        shadowElevation = 6.dp,
        modifier = modifier
            .height(58.dp)
            .testTag(testTag),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 22.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 17.sp),
                color = content,
                maxLines = 1,
            )
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(tile),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tileIconTint,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

/** White rounded button with an icon, used for secondary actions such as Hint. */
@Composable
fun CeramicSecondaryButton(
    label: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    container: Color = Ceramic.Card,
    content: Color = Ceramic.Obsidian,
    testTag: String = "ceramic_secondary_button",
) {
    val shape = RoundedCornerShape(18.dp)
    TactixInteractiveSurface(
        onClick = { if (enabled) onClick() },
        shape = shape,
        color = container,
        shadowElevation = if (container == Ceramic.Card) 3.dp else 0.dp,
        modifier = modifier
            .height(56.dp)
            .testTag(testTag),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) iconTint else iconTint.copy(alpha = 0.35f),
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled) content else content.copy(alpha = 0.4f),
                maxLines = 1,
            )
        }
    }
}

/** Round icon button on a white disc, used in headers. */
@Composable
fun CeramicIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    container: Color = Ceramic.Card,
    tint: Color = Ceramic.Obsidian,
) {
    TactixInteractiveSurface(
        onClick = onClick,
        shape = CircleShape,
        color = container,
        shadowElevation = if (container == Ceramic.Card) 3.dp else 0.dp,
        modifier = modifier
            .size(44.dp)
            .semantics { this.contentDescription = contentDescription },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/** A labelled row with a cobalt switch. */
@Composable
fun CeramicSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "ceramic_switch_row",
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Switch, onClick = onToggle)
            .padding(vertical = 12.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                color = Ceramic.Obsidian,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = Ceramic.Muted,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Ceramic.Cobalt,
                checkedBorderColor = Color.Transparent,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Ceramic.Switch,
                uncheckedBorderColor = Color.Transparent,
            ),
        )
    }
}

/** Small uppercase section label. */
@Composable
fun CeramicSectionLabel(text: String, modifier: Modifier = Modifier, color: Color = Ceramic.Muted) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
        color = color,
        modifier = modifier,
    )
}

/** Two or more options in a sunken track; the chosen one rises as a white tile. */
@Composable
fun <T> CeramicSegmented(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Ceramic.Socket)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEach { (value, label) ->
            val isSelected = value == selected
            val shape = RoundedCornerShape(14.dp)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .then(
                        if (isSelected) {
                            Modifier.shadow(4.dp, shape, clip = false)
                        } else {
                            Modifier
                        },
                    )
                    .clip(shape)
                    .background(if (isSelected) Color.White else Color.Transparent)
                    .semantics { this.selected = isSelected }
                    .clickable(role = Role.Tab) { onSelect(value) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isSelected) Ceramic.Obsidian else Ceramic.Muted,
                    maxLines = 1,
                )
            }
        }
    }
}

/** Equal-width chips; the chosen one turns solid cobalt. */
@Composable
fun <T> CeramicChoiceChips(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { (value, label) ->
            val isSelected = value == selected
            val shape = RoundedCornerShape(14.dp)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .then(
                        if (isSelected) {
                            Modifier.shadow(
                                elevation = 8.dp,
                                shape = shape,
                                clip = false,
                                ambientColor = Ceramic.Cobalt,
                                spotColor = Ceramic.Cobalt,
                            )
                        } else {
                            Modifier
                        },
                    )
                    .clip(shape)
                    .background(if (isSelected) Ceramic.Cobalt else Ceramic.Socket)
                    .semantics { this.selected = isSelected }
                    .clickable(role = Role.RadioButton) { onSelect(value) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isSelected) Color.White else Ceramic.Obsidian,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/** Two floating ceramic pieces for a coloured block, placed off its top-right edge. */
@Composable
fun FloatingPieces(
    look: ModeLook,
    leadSize: Dp,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        CeramicPiece(
            player = look.leadPiece,
            color = look.leadColor,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = leadSize * 0.22f, y = -(leadSize * 0.12f))
                .size(leadSize)
                .rotate(10f),
        )
        CeramicPiece(
            player = look.accentPiece,
            color = look.accentColor,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = -(leadSize * 0.16f), y = leadSize * 0.66f)
                .size(leadSize * 0.66f)
                .rotate(-14f),
        )
    }
}

fun difficultyNote(difficulty: AiDifficulty): String = when (difficulty) {
    AiDifficulty.ADAPTIVE -> "Adjusts to your level as you play."
    AiDifficulty.CASUAL -> "Relaxed play. Good for learning the rules."
    AiDifficulty.TACTICAL -> "Plays sharp tactics and punishes slips."
    AiDifficulty.MASTER -> "Near-perfect play. Rarely beaten."
}

/**
 * Bottom sheet that sets up a game before it starts: opponent, difficulty, Blitz and sides.
 * The top of the sheet takes the mode's colour, with the floating ceramic pieces.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchSetupSheet(
    mode: GameMode,
    opponentType: OpponentType,
    aiDifficulty: AiDifficulty,
    isBlitzEnabled: Boolean,
    humanSide: Player,
    onConfigureOpponent: (OpponentType) -> Unit,
    onConfigureDifficulty: (AiDifficulty) -> Unit,
    onToggleBlitz: () -> Unit,
    onToggleHumanSide: () -> Unit,
    onStart: () -> Unit,
    onDismiss: () -> Unit,
    startLabel: String = "Start game",
) {
    val look = modeLook(mode)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp),
        containerColor = Ceramic.Card,
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .testTag("match_setup_sheet"),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(196.dp)
                    .background(look.background),
            ) {
                FloatingPieces(look = look, leadSize = 150.dp)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp)
                        .size(width = 40.dp, height = 5.dp)
                        .clip(CircleShape)
                        .background(look.content.copy(alpha = 0.35f)),
                )
                CeramicIconButton(
                    icon = Icons.Rounded.Close,
                    contentDescription = "Close",
                    onClick = onDismiss,
                    container = Ceramic.Obsidian.copy(alpha = 0.22f),
                    tint = look.content,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 18.dp, top = 22.dp),
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 24.dp, end = 24.dp, bottom = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "GAME SETUP",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = look.content,
                    )
                    Text(
                        text = look.title,
                        style = MaterialTheme.typography.displaySmall,
                        color = look.content,
                    )
                    Text(
                        text = look.description,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = look.muted,
                        modifier = Modifier.widthIn(max = 220.dp),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp)
                    .padding(top = 20.dp, bottom = 24.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    CeramicSectionLabel("Opponent")
                    CeramicSegmented(
                        options = listOf(
                            OpponentType.AI to "Computer",
                            OpponentType.PASS_AND_PLAY to "A friend",
                        ),
                        selected = opponentType,
                        onSelect = onConfigureOpponent,
                    )
                }

                if (opponentType == OpponentType.AI) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        CeramicSectionLabel("Difficulty")
                        CeramicChoiceChips(
                            options = listOf(
                                AiDifficulty.ADAPTIVE to stringResource(R.string.ai_adaptive),
                                AiDifficulty.CASUAL to stringResource(R.string.ai_casual),
                                AiDifficulty.TACTICAL to stringResource(R.string.ai_tactical),
                                AiDifficulty.MASTER to stringResource(R.string.ai_master),
                            ),
                            selected = aiDifficulty,
                            onSelect = onConfigureDifficulty,
                        )
                        Text(
                            text = difficultyNote(aiDifficulty),
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            color = Ceramic.Muted,
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
                        checked = isBlitzEnabled,
                        onToggle = onToggleBlitz,
                        testTag = "setup_blitz_switch",
                    )
                    if (opponentType == OpponentType.AI) {
                        CeramicSwitchRow(
                            title = "You go first",
                            subtitle = if (humanSide == Player.X) "You play X" else "You play O",
                            checked = humanSide == Player.X,
                            onToggle = onToggleHumanSide,
                            testTag = "setup_side_switch",
                        )
                    }
                }

                CeramicPrimaryButton(
                    label = startLabel,
                    onClick = onStart,
                    container = Ceramic.Obsidian,
                    content = Color.White,
                    tile = Ceramic.Vermilion,
                    tileIconTint = Ceramic.Obsidian,
                    testTag = "setup_start_button",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
