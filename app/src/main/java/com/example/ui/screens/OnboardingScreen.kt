package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AllInclusive
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Flip
import androidx.compose.material.icons.rounded.Grid3x3
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.BoardSnapshot
import com.example.domain.GameEngine
import com.example.domain.GameMode
import com.example.domain.PieceSkin
import com.example.domain.Player
import com.example.ui.components.TactixAppEmblem
import com.example.ui.components.TactixBoard
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    onTapHaptic: () -> Unit,
    onCelebrationHaptic: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pageCount = 4
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .testTag("onboarding_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Clean top bar: step badge + Skip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp)
                .padding(top = 12.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            ) {
                Text(
                    text = "${pagerState.currentPage + 1} of $pageCount",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 6.dp),
                )
            }

            if (pagerState.currentPage < pageCount - 1) {
                TextButton(
                    onClick = onComplete,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("onboarding_skip_button"),
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_skip),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(64.dp))
            }
        }

        // Swipeable onboarding pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .widthIn(max = 560.dp),
        ) { page ->
            when (page) {
                0 -> WelcomeSlide()
                1 -> InteractiveInfinitySlide(
                    onTapHaptic = onTapHaptic,
                    onCelebrationHaptic = onCelebrationHaptic,
                )
                2 -> ModesContrastSlide()
                else -> MasteryAndDailySlide()
            }
        }

        // Bottom controls: animated pill dots + Back / Next / Let's Play
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp)
                .padding(top = 8.dp, bottom = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(pageCount) { index ->
                    val selected = pagerState.currentPage == index
                    val width by animateDpAsState(
                        targetValue = if (selected) 28.dp else 8.dp,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "dot_w_$index",
                    )
                    val color by animateColorAsState(
                        targetValue = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        },
                        animationSpec = tween(220),
                        label = "dot_c_$index",
                    )
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(color),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (pagerState.currentPage > 0) {
                    FilledTonalButton(
                        onClick = {
                            onTapHaptic()
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        },
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 15.dp),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("onboarding_back_button"),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.onboarding_back),
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.onboarding_back))
                    }
                }

                val isLastPage = pagerState.currentPage == pageCount - 1
                Button(
                    onClick = {
                        if (isLastPage) {
                            onComplete()
                        } else {
                            onTapHaptic()
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag(if (isLastPage) "onboarding_start_button" else "onboarding_next_button"),
                ) {
                    Text(
                        text = stringResource(
                            if (isLastPage) R.string.onboarding_start else R.string.onboarding_next,
                        ),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeSlide() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        TactixAppEmblem(
            size = 96.dp,
            cornerRadius = 26.dp,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "The Classic,\nRemade.",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.8).sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Easy to learn in seconds. Hard to put down. Think faster and play for keeps.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SimpleFeaturePill(
                icon = Icons.Rounded.AllInclusive,
                title = "3 Fresh Ways to Play",
                subtitle = "Infinity, Classic 3×3, and Reverse Misère.",
                accent = MaterialTheme.colorScheme.primary,
            )
            SimpleFeaturePill(
                icon = Icons.Rounded.Groups,
                title = "Play Solo or With a Friend",
                subtitle = "Challenge a rival that grows with you, or pass the phone.",
                accent = MaterialTheme.colorScheme.tertiary,
            )
            SimpleFeaturePill(
                icon = Icons.Rounded.WifiOff,
                title = "Ready Anywhere, Offline",
                subtitle = "No ads, no accounts — just open and play.",
                accent = MaterialTheme.colorScheme.secondary,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(R.string.studio_crafted_by),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Interactive "Learn-by-Tapping" tutorial for Infinity Mode powered by the real GameEngine.
 */
@Composable
private fun InteractiveInfinitySlide(
    onTapHaptic: () -> Unit,
    onCelebrationHaptic: () -> Unit,
) {
    fun buildDemoSnapshot(): BoardSnapshot {
        val board = MutableList<Player?>(9) { null }
        board[0] = Player.X
        board[1] = Player.O
        board[4] = Player.X
        board[8] = Player.O
        board[6] = Player.X
        return BoardSnapshot(
            board = board,
            currentPlayer = Player.X,
            xQueue = listOf(0, 4, 6),
            oQueue = listOf(1, 8),
        )
    }

    var demoSnapshot by remember { mutableStateOf(buildDemoSnapshot()) }
    var demoHintCell by remember { mutableStateOf<Int?>(2) }
    var demoFeedback by remember {
        mutableStateOf("Tap the glowing corner! Watch your pulsing top-left X vanish.")
    }
    var solvedDemo by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.AllInclusive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = "TRY IT NOW",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "No More Draws.",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "In Infinity Mode, only 3 of your marks stay on the board. Drop a 4th and your oldest one disappears.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.widthIn(max = 264.dp)) {
            TactixBoard(
                snapshot = demoSnapshot,
                mode = GameMode.INFINITY,
                skin = PieceSkin.CLASSIC,
                onCellClick = { cellIndex ->
                    if (demoSnapshot.isGameOver) return@TactixBoard
                    val next = GameEngine.applyMove(demoSnapshot, cellIndex, GameMode.INFINITY) ?: return@TactixBoard
                    if (next.winner == Player.X) {
                        demoSnapshot = next
                        demoHintCell = null
                        solvedDemo = true
                        demoFeedback = "Boom! Your oldest X vanished and you lined up three to win."
                        onCelebrationHaptic()
                    } else {
                        val nextPlayable = next.copy(currentPlayer = Player.X)
                        val nextWin = GameEngine.countImmediateWinningMoves(
                            state = nextPlayable,
                            player = Player.X,
                            mode = GameMode.INFINITY,
                        ).firstOrNull()
                        demoSnapshot = nextPlayable
                        demoHintCell = nextWin
                        demoFeedback = if (nextWin != null) {
                            "Your oldest X disappeared! Now tap the glowing square to win."
                        } else {
                            "See how your oldest X vanished? Keep tapping or reset the board."
                        }
                        onTapHaptic()
                    }
                },
                enabled = !demoSnapshot.isGameOver,
                hintCellIndex = if (solvedDemo) null else demoHintCell,
                testTagPrefix = "onboarding_demo_board",
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        AnimatedContent(targetState = demoFeedback, label = "demo_feedback") { message ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (solvedDemo) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                },
                border = BorderStroke(
                    width = 1.dp,
                    color = if (solvedDemo) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                    } else {
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    },
                ),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (solvedDemo) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (solvedDemo) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (solvedDemo) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = solvedDemo || demoSnapshot.xQueue != listOf(0, 4, 6),
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            TextButton(
                onClick = {
                    onTapHaptic()
                    demoSnapshot = buildDemoSnapshot()
                    demoHintCell = 2
                    solvedDemo = false
                    demoFeedback = "Tap the glowing corner! Watch your pulsing top-left X vanish."
                },
                modifier = Modifier.padding(top = 4.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Try again")
            }
        }
    }
}

@Composable
private fun ModesContrastSlide() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Play Straight —\nOr Flip It.",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Pick your mood from the Home screen whenever you want a fresh twist.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp),
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RuleContrastCard(
                title = "Classic",
                badge = "3 IN A ROW WINS",
                description = "The original showdown. Line up three first.",
                icon = Icons.Rounded.Grid3x3,
                accent = MaterialTheme.colorScheme.primary,
                isReverse = false,
                modifier = Modifier.weight(1f),
            )
            RuleContrastCard(
                title = "Misère",
                badge = "3 IN A ROW LOSES",
                description = "Everything is backwards! Make your rival connect three.",
                icon = Icons.Rounded.Flip,
                accent = MaterialTheme.colorScheme.error,
                isReverse = true,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.72f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "5-Second Blitz Clock",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                    Text(
                        text = "Turn on Blitz in any mode for fast 5-second turns and nonstop rush.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.88f),
                    )
                }
            }
        }
    }
}

@Composable
private fun RuleContrastCard(
    title: String,
    badge: String,
    description: String,
    icon: ImageVector,
    accent: Color,
    isReverse: Boolean,
    modifier: Modifier = Modifier,
) {
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
    val oColor = MaterialTheme.colorScheme.tertiary

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.94f),
        border = BorderStroke(1.2.dp, accent.copy(alpha = 0.35f)),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth(0.74f)
                    .aspectRatio(1f),
            ) {
                val cell = size.minDimension / 3f
                val gridStroke = 1.3.dp.toPx()
                for (i in 1..2) {
                    drawLine(gridColor, Offset(cell * i, 4f), Offset(cell * i, size.height - 4f), gridStroke)
                    drawLine(gridColor, Offset(4f, cell * i), Offset(size.width - 4f, cell * i), gridStroke)
                }

                val markStroke = 2.6.dp.toPx()
                fun drawMiniX(col: Int, row: Int, color: Color) {
                    val pad = cell * 0.28f
                    val x0 = col * cell + pad
                    val y0 = row * cell + pad
                    val x1 = (col + 1) * cell - pad
                    val y1 = (row + 1) * cell - pad
                    drawLine(color, Offset(x0, y0), Offset(x1, y1), markStroke, StrokeCap.Round)
                    drawLine(color, Offset(x1, y0), Offset(x0, y1), markStroke, StrokeCap.Round)
                }

                fun drawMiniO(col: Int, row: Int) {
                    val pad = cell * 0.27f
                    drawArc(
                        color = oColor.copy(alpha = 0.7f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = Offset(col * cell + pad, row * cell + pad),
                        size = Size(cell - pad * 2f, cell - pad * 2f),
                        style = Stroke(width = markStroke),
                    )
                }

                drawMiniX(0, 0, accent)
                drawMiniX(1, 1, accent)
                drawMiniX(2, 2, accent)
                drawMiniO(2, 0)
                drawMiniO(0, 2)

                drawLine(
                    color = accent.copy(alpha = if (isReverse) 0.85f else 0.78f),
                    start = Offset(cell * 0.22f, cell * 0.22f),
                    end = Offset(size.width - cell * 0.22f, size.height - cell * 0.22f),
                    strokeWidth = 3.2.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }

            Surface(
                shape = CircleShape,
                color = accent.copy(alpha = 0.14f),
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = accent,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    maxLines = 1,
                )
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MasteryAndDailySlide() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Daily Puzzles.\nReal Progress.",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Keep your mind sharp with fresh daily challenges and watch your skill climb.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 10.dp),
        )

        Spacer(modifier = Modifier.height(22.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SimpleFeaturePill(
                icon = Icons.Rounded.CalendarToday,
                title = "Daily Puzzles & Rush",
                subtitle = "Solve 3 fresh brain-teasers every day or chase a streak in endless Rush.",
                accent = MaterialTheme.colorScheme.primary,
            )
            SimpleFeaturePill(
                icon = Icons.Rounded.PlayCircle,
                title = "Move-by-Move Replays",
                subtitle = "Rewatch your matches, check your accuracy, and see where the winning move was.",
                accent = MaterialTheme.colorScheme.tertiary,
            )
            SimpleFeaturePill(
                icon = Icons.Rounded.EmojiEvents,
                title = "8 Trophies & 4 Board Styles",
                subtitle = "Win trophies to unlock Prism, Dual Halo, and Apex piece designs.",
                accent = MaterialTheme.colorScheme.secondary,
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.88f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        ) {
            Text(
                text = stringResource(R.string.studio_crafted_by),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            )
        }
    }
}

@Composable
private fun SimpleFeaturePill(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
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
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
