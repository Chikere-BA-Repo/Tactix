package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import com.example.ui.theme.Ceramic
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.BoardSnapshot
import com.example.domain.GameMode
import com.example.domain.PieceSkin
import com.example.domain.Player
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun TactixBoard(
    snapshot: BoardSnapshot,
    mode: GameMode,
    onCellClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    skin: PieceSkin = PieceSkin.CLASSIC,
    enabled: Boolean = true,
    hintCellIndex: Int? = null,
    shakeTrigger: Int = 0,
    testTagPrefix: String = "board",
) {
    val expiringCell = snapshot.expiringCell(mode)
    val winningLine = snapshot.winningLine
    val xColor = Ceramic.Cobalt
    val oColor = Ceramic.Vermilion
    val hintColor = Ceramic.Cobalt
    val errorColor = MaterialTheme.colorScheme.error

    val targetAmbient = when {
        snapshot.winner == Player.X -> xColor
        snapshot.winner == Player.O -> oColor
        snapshot.currentPlayer == Player.X -> xColor
        else -> oColor
    }
    val ambientColor by animateColorAsState(
        targetValue = targetAmbient,
        animationSpec = tween(420),
        label = "board_ambient_color",
    )

    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(shakeTrigger) {
        if (shakeTrigger > 0) {
            val sequence = listOf(-18f, 18f, -12f, 12f, -6f, 0f)
            for (target in sequence) {
                shakeOffset.animateTo(
                    targetValue = target,
                    animationSpec = tween(durationMillis = 44, easing = FastOutSlowInEasing),
                )
            }
        }
    }

    val winLineProgress = remember { Animatable(0f) }
    val sparkProgress = remember { Animatable(0f) }
    LaunchedEffect(winningLine) {
        if (winningLine != null) {
            winLineProgress.snapTo(0f)
            sparkProgress.snapTo(0f)
            winLineProgress.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
            )
            sparkProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 720, easing = LinearOutSlowInEasing),
            )
        } else {
            winLineProgress.snapTo(0f)
            sparkProgress.snapTo(0f)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "board_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.34f,
        targetValue = 0.80f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 760, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "expiring_alpha",
    )
    val hintScale by infiniteTransition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 720, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "hint_scale",
    )

    val rimBrush = Brush.linearGradient(
        colors = listOf(
            ambientColor.copy(alpha = 0.36f),
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
            ambientColor.copy(alpha = 0.16f),
        ),
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 396.dp)
            .offset { IntOffset(shakeOffset.value.roundToInt(), 0) },
        contentAlignment = Alignment.Center,
    ) {
        val boardShape = RoundedCornerShape(34.dp)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .shadow(
                    elevation = 22.dp,
                    shape = boardShape,
                    clip = false,
                    ambientColor = Ceramic.Obsidian.copy(alpha = 0.18f),
                    spotColor = Ceramic.Obsidian.copy(alpha = 0.28f),
                ),
            shape = boardShape,
            color = Ceramic.Card,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    for (row in 0 until 3) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            for (col in 0 until 3) {
                                val index = row * 3 + col
                                val mark = snapshot.board[index]
                                val isWinningCell = winningLine?.contains(index) == true
                                val isMisereTrapLine = mode == GameMode.MISERE && isWinningCell
                                val isExpiring = expiringCell == index
                                val isHinted = hintCellIndex == index && mark == null
                                val isLastMove = snapshot.lastMove?.cellIndex == index

                                val lifePips = if (mode == GameMode.INFINITY && mark != null) {
                                    val queue = if (mark == Player.X) snapshot.xQueue else snapshot.oQueue
                                    val pos = queue.indexOf(index)
                                    if (pos >= 0) (3 - (queue.lastIndex - pos)).coerceIn(1, 3) else null
                                } else {
                                    null
                                }

                                TactixCell(
                                    index = index,
                                    row = row,
                                    col = col,
                                    mark = mark,
                                    skin = skin,
                                    xColor = xColor,
                                    oColor = oColor,
                                    hintColor = hintColor,
                                    isWinningCell = isWinningCell,
                                    isMisereTrapLine = isMisereTrapLine,
                                    isExpiring = isExpiring,
                                    lifePips = lifePips,
                                    isHinted = isHinted,
                                    isLastMove = isLastMove,
                                    pulseAlpha = pulseAlpha,
                                    hintScale = hintScale,
                                    enabled = enabled && mark == null && !snapshot.isGameOver,
                                    testTag = "${testTagPrefix}_cell_$index",
                                    onClick = { onCellClick(index) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize(),
                                )
                            }
                        }
                    }
                }

                if (winningLine != null && winningLine.size == 3 && winLineProgress.value > 0f) {
                    val lineColor = when {
                        mode == GameMode.MISERE -> errorColor
                        snapshot.winner == Player.X -> xColor
                        else -> oColor
                    }
                    val sparkSecondary = if (snapshot.winner == Player.X) oColor else xColor
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val gapPx = 10.dp.toPx()
                        val cellWidth = (size.width - gapPx * 2f) / 3f
                        val cellHeight = (size.height - gapPx * 2f) / 3f

                        fun centerOf(cellIndex: Int): Offset {
                            val r = cellIndex / 3
                            val c = cellIndex % 3
                            return Offset(
                                x = c * (cellWidth + gapPx) + cellWidth / 2f,
                                y = r * (cellHeight + gapPx) + cellHeight / 2f,
                            )
                        }

                        val start = centerOf(winningLine.first())
                        val mid = centerOf(winningLine[1])
                        val end = centerOf(winningLine.last())
                        val progress = winLineProgress.value.coerceIn(0f, 1f)
                        val currentEnd = Offset(
                            x = start.x + (end.x - start.x) * progress,
                            y = start.y + (end.y - start.y) * progress,
                        )

                        drawLine(
                            color = lineColor.copy(alpha = 0.35f),
                            start = start + Offset(0f, 3.dp.toPx()),
                            end = currentEnd + Offset(0f, 3.dp.toPx()),
                            strokeWidth = 9.dp.toPx(),
                            cap = StrokeCap.Round,
                        )
                        drawLine(
                            color = Ceramic.Ivory,
                            start = start,
                            end = currentEnd,
                            strokeWidth = 6.dp.toPx(),
                            cap = StrokeCap.Round,
                        )

                        val sp = sparkProgress.value
                        if (sp in 0.01f..0.99f) {
                            val fade = (1f - sp).coerceIn(0f, 1f)
                            val particleCount = 12
                            for (i in 0 until particleCount) {
                                val angle = (i * (360f / particleCount)) * (PI / 180.0)
                                val dist = (26.dp.toPx() + 86.dp.toPx() * sp) * if (i % 2 == 0) 1f else 0.72f
                                val px = mid.x + (cos(angle) * dist).toFloat()
                                val py = mid.y + (sin(angle) * dist).toFloat()
                                val pColor = if (i % 3 == 0) sparkSecondary else lineColor
                                if (i % 2 == 0) {
                                    drawCircle(
                                        color = pColor.copy(alpha = fade * 0.85f),
                                        radius = (4.5f * fade + 1.5f).dp.toPx(),
                                        center = Offset(px, py),
                                    )
                                } else {
                                    drawCircle(
                                        color = pColor.copy(alpha = fade * 0.75f),
                                        radius = (5.dp.toPx()) * (0.6f + 0.5f * sp),
                                        center = Offset(px, py),
                                        style = Stroke(width = 1.8.dp.toPx()),
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
private fun TactixCell(
    index: Int,
    row: Int,
    col: Int,
    mark: Player?,
    skin: PieceSkin,
    xColor: Color,
    oColor: Color,
    hintColor: Color,
    isWinningCell: Boolean,
    isMisereTrapLine: Boolean,
    isExpiring: Boolean,
    lifePips: Int?,
    isHinted: Boolean,
    isLastMove: Boolean,
    pulseAlpha: Float,
    hintScale: Float,
    enabled: Boolean,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.91f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "cell_press_scale",
    )

    val drawProgress = remember(mark, index) { Animatable(0f) }
    LaunchedEffect(mark) {
        if (mark != null) {
            drawProgress.snapTo(0f)
            drawProgress.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            )
        } else {
            drawProgress.snapTo(0f)
        }
    }

    val cellShape = RoundedCornerShape(24.dp)
    val targetBg = when {
        isMisereTrapLine -> MaterialTheme.colorScheme.errorContainer
        isWinningCell && mark == Player.X -> xColor
        isWinningCell && mark == Player.O -> oColor
        isHinted -> Ceramic.CobaltTint
        isLastMove && mark == Player.X -> Ceramic.CobaltTint
        isLastMove && mark == Player.O -> Ceramic.VermilionTint
        else -> Ceramic.Socket
    }
    val cellBgColor by animateColorAsState(
        targetValue = targetBg,
        animationSpec = tween(260),
        label = "cell_bg_$index",
    )
    val solidWinCell = isWinningCell && !isMisereTrapLine
    val expiringOutline = if (mark == Player.O) oColor else xColor

    val coordLabel = remember(row, col) {
        val colChar = ('A'.code + col).toChar()
        "$colChar${row + 1}"
    }

    val cellDescription = buildString {
        append("Square $coordLabel, ")
        when (mark) {
            Player.X -> append("X")
            Player.O -> append("O")
            null -> append("empty")
        }
        if (isExpiring) append(", fades on next move")
        if (isHinted) append(", recommended tactical hint")
    }

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .scale(pressScale)
            .clip(cellShape)
            .background(cellBgColor)
            .drawBehind {
                val radius = CornerRadius(24.dp.toPx())
                if (!solidWinCell) {
                    // Soft inner shade along the top edge so each square reads as a sunken socket.
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Ceramic.Obsidian.copy(alpha = 0.07f), Color.Transparent),
                            startY = 0f,
                            endY = 12.dp.toPx(),
                        ),
                        cornerRadius = radius,
                    )
                }
                if (isExpiring) {
                    val inset = 1.5.dp.toPx()
                    drawRoundRect(
                        color = expiringOutline,
                        topLeft = Offset(inset, inset),
                        size = Size(size.width - inset * 2f, size.height - inset * 2f),
                        cornerRadius = CornerRadius(22.dp.toPx()),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f),
                        ),
                    )
                }
                if (isHinted) {
                    drawRoundRect(
                        color = hintColor,
                        cornerRadius = radius,
                        style = Stroke(width = 2.dp.toPx()),
                    )
                }
            }
            .semantics { contentDescription = cellDescription }
            .testTag(testTag)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = ripple(bounded = true),
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (mark != null) {
            val markColor = when {
                solidWinCell -> Ceramic.Ivory
                mark == Player.X -> xColor
                else -> oColor
            }
            val effectiveAlpha = if (isExpiring) pulseAlpha else 1f
            val markScale = if (isExpiring) 0.86f else 1f

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .scale(markScale)
                    .alpha(effectiveAlpha),
            ) {
                val progress = drawProgress.value.coerceIn(0f, 1.08f)
                drawStyledPiece(
                    player = mark,
                    skin = skin,
                    color = markColor,
                    progress = progress,
                )
            }

            if (!isExpiring && lifePips != null && !solidWinCell) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 7.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    for (pip in 1..3) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(
                                    if (pip <= lifePips) {
                                        markColor.copy(alpha = 0.72f)
                                    } else {
                                        markColor.copy(alpha = 0.16f)
                                    },
                                ),
                        )
                    }
                }
            }
        } else if (isHinted) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(19.dp)
                    .scale(hintScale),
            ) {
                drawCircle(
                    color = hintColor.copy(alpha = 0.22f),
                    radius = size.minDimension * 0.45f,
                )
                drawCircle(
                    color = hintColor.copy(alpha = 0.82f),
                    radius = size.minDimension * 0.43f,
                    style = Stroke(
                        width = 2.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 9f), 0f),
                    ),
                )
                drawCircle(
                    color = hintColor,
                    radius = 5.5.dp.toPx(),
                )
            }
        }
    }
}

/**
 * Shared custom Canvas piece renderer supporting all 4 unlockable minimalist PieceSkins.
 */
fun DrawScope.drawStyledPiece(
    player: Player,
    skin: PieceSkin,
    color: Color,
    progress: Float = 1f,
) {
    val strokePx = (size.minDimension * 0.165f).coerceIn(6f, 26f)
    val glowPx = strokePx * 2.0f
    val pad = size.minDimension * 0.11f
    val left = pad
    val top = pad
    val right = size.width - pad
    val bottom = size.height - pad
    val cx = size.width / 2f
    val cy = size.height / 2f

    when (skin) {
        PieceSkin.CLASSIC -> drawSculptedPiece(player, color, progress)

        PieceSkin.PRISM -> {
            val scale = progress.coerceAtMost(1f)
            if (player == Player.X) {
                val r = (size.minDimension * 0.39f) * scale
                // Crisp beveled architectural X with central diamond core
                drawLine(color.copy(alpha = 0.18f), Offset(cx - r, cy - r), Offset(cx + r, cy + r), glowPx * 0.85f, StrokeCap.Square)
                drawLine(color.copy(alpha = 0.18f), Offset(cx + r, cy - r), Offset(cx - r, cy + r), glowPx * 0.85f, StrokeCap.Square)
                drawLine(color, Offset(cx - r, cy - r), Offset(cx + r, cy + r), strokePx * 0.85f, StrokeCap.Square)
                drawLine(color, Offset(cx + r, cy - r), Offset(cx - r, cy + r), strokePx * 0.85f, StrokeCap.Square)
            } else {
                // Faceted geometric diamond
                val r = (size.minDimension * 0.42f) * scale
                val path = Path().apply {
                    moveTo(cx, cy - r)
                    lineTo(cx + r, cy)
                    lineTo(cx, cy + r)
                    lineTo(cx - r, cy)
                    close()
                }
                drawPath(path, color.copy(alpha = 0.18f), style = Stroke(glowPx * 0.85f, join = StrokeJoin.Miter))
                drawPath(path, color, style = Stroke(strokePx * 0.85f, join = StrokeJoin.Miter))
            }
        }

        PieceSkin.HALO -> {
            val scale = progress.coerceAtMost(1f)
            val thin = strokePx * 0.46f
            if (player == Player.X) {
                val r = (size.minDimension * 0.38f) * scale
                val offset = strokePx * 0.34f
                // Dual parallel neon-studio strokes
                drawLine(color.copy(alpha = 0.24f), Offset(cx - r, cy - r), Offset(cx + r, cy + r), glowPx, StrokeCap.Round)
                drawLine(color.copy(alpha = 0.24f), Offset(cx + r, cy - r), Offset(cx - r, cy + r), glowPx, StrokeCap.Round)
                drawLine(color, Offset(cx - r - offset * 0.5f, cy - r + offset * 0.5f), Offset(cx + r - offset * 0.5f, cy + r + offset * 0.5f), thin, StrokeCap.Round)
                drawLine(color, Offset(cx - r + offset * 0.5f, cy - r - offset * 0.5f), Offset(cx + r + offset * 0.5f, cy + r - offset * 0.5f), thin, StrokeCap.Round)
                drawLine(color, Offset(cx + r - offset * 0.5f, cy - r - offset * 0.5f), Offset(cx - r - offset * 0.5f, cy + r - offset * 0.5f), thin, StrokeCap.Round)
                drawLine(color, Offset(cx + r + offset * 0.5f, cy - r + offset * 0.5f), Offset(cx - r + offset * 0.5f, cy + r + offset * 0.5f), thin, StrokeCap.Round)
            } else {
                // Concentric dual halo rings
                val outerR = (size.minDimension * 0.42f) * scale
                val innerR = (size.minDimension * 0.23f) * scale
                drawCircle(color.copy(alpha = 0.18f), radius = outerR, style = Stroke(glowPx * 0.7f))
                drawCircle(color, radius = outerR, style = Stroke(thin))
                drawCircle(color.copy(alpha = 0.85f), radius = innerR, style = Stroke(thin))
            }
        }

        PieceSkin.APEX -> {
            val scale = progress.coerceAtMost(1f)
            val arm = (size.minDimension * 0.40f) * scale
            if (player == Player.X) {
                // Sculpted chevron star-cross with radiant center core
                val gap = size.minDimension * 0.12f * scale
                val dirs = listOf(
                    Offset(-1f, -1f),
                    Offset(1f, -1f),
                    Offset(1f, 1f),
                    Offset(-1f, 1f),
                )
                dirs.forEach { d ->
                    val s = Offset(cx + d.x * gap, cy + d.y * gap)
                    val e = Offset(cx + d.x * arm, cy + d.y * arm)
                    drawLine(color.copy(alpha = 0.22f), s, e, glowPx * 0.85f, StrokeCap.Round)
                    drawLine(color, s, e, strokePx * 0.82f, StrokeCap.Round)
                }
                drawCircle(color = color, radius = strokePx * 0.45f * scale, center = Offset(cx, cy))
            } else {
                // Orbital 4-arc segmented ring + precision center dot
                val arcSize = Size(right - left, bottom - top)
                val segSweep = 62f * scale
                for (q in 0 until 4) {
                    val startAngle = -76f + q * 90f
                    drawArc(color.copy(alpha = 0.20f), startAngle, segSweep, false, Offset(left, top), arcSize, style = Stroke(glowPx * 0.78f, cap = StrokeCap.Round))
                    drawArc(color, startAngle, segSweep, false, Offset(left, top), arcSize, style = Stroke(strokePx * 0.85f, cap = StrokeCap.Round))
                }
                drawCircle(color = color.copy(alpha = 0.75f), radius = strokePx * 0.34f * scale, center = Offset(cx, cy))
            }
        }
    }
}

/**
 * Ceramic pieces in the style of the app icon: chunky strokes with a light-to-deep gradient,
 * a soft drop shadow underneath and a thin highlight along the top edge.
 */
private fun DrawScope.drawSculptedPiece(
    player: Player,
    color: Color,
    progress: Float,
) {
    val dim = size.minDimension
    val strokePx = dim * 0.24f
    val pad = dim * 0.17f
    val left = pad
    val top = pad
    val right = size.width - pad
    val bottom = size.height - pad
    val light = lerp(color, Color.White, 0.38f)
    val deep = lerp(color, Color.Black, 0.12f)
    val body = Brush.linearGradient(
        colors = listOf(light, deep),
        start = Offset(0f, 0f),
        end = Offset(size.width, size.height),
    )
    val shadow = lerp(color, Color.Black, 0.55f).copy(alpha = 0.26f)
    val drop = Offset(0f, dim * 0.07f)
    val shine = Offset(-dim * 0.025f, -dim * 0.035f)
    val highlight = Color.White.copy(alpha = 0.45f)

    if (player == Player.X) {
        val p1 = (progress * 2f).coerceIn(0f, 1f)
        val p2 = ((progress - 0.32f) / 0.68f).coerceIn(0f, 1f)
        val s1 = Offset(left, top)
        val e1 = Offset(left + (right - left) * p1, top + (bottom - top) * p1)
        val s2 = Offset(right, top)
        val e2 = Offset(right - (right - left) * p2, top + (bottom - top) * p2)
        if (p1 > 0f) drawLine(shadow, s1 + drop, e1 + drop, strokePx, StrokeCap.Round)
        if (p2 > 0f) drawLine(shadow, s2 + drop, e2 + drop, strokePx, StrokeCap.Round)
        if (p1 > 0f) drawLine(body, s1, e1, strokePx, StrokeCap.Round)
        if (p2 > 0f) drawLine(body, s2, e2, strokePx, StrokeCap.Round)
        if (p1 > 0f) drawLine(highlight, s1 + shine, e1 + shine, strokePx * 0.2f, StrokeCap.Round)
        if (p2 > 0f) drawLine(highlight, s2 + shine, e2 + shine, strokePx * 0.2f, StrokeCap.Round)
    } else {
        val inset = strokePx * 0.15f
        val topLeft = Offset(left + inset, top + inset)
        val arcSize = Size(right - left - inset * 2f, bottom - top - inset * 2f)
        val sweep = 360f * progress.coerceIn(0f, 1f)
        drawArc(
            color = shadow,
            startAngle = -90f,
            sweepAngle = sweep,
            useCenter = false,
            topLeft = topLeft + drop,
            size = arcSize,
            style = Stroke(width = strokePx, cap = StrokeCap.Round),
        )
        drawArc(
            brush = body,
            startAngle = -90f,
            sweepAngle = sweep,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokePx, cap = StrokeCap.Round),
        )
        if (progress >= 0.95f) {
            drawArc(
                color = highlight,
                startAngle = 165f,
                sweepAngle = 75f,
                useCenter = false,
                topLeft = topLeft + shine,
                size = arcSize,
                style = Stroke(width = strokePx * 0.2f, cap = StrokeCap.Round),
            )
        }
    }
}

/**
 * A single sculpted ceramic piece, used for decoration on cards and sheets.
 */
@Composable
fun CeramicPiece(
    player: Player,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        drawSculptedPiece(player = player, color = color, progress = 1f)
    }
}
