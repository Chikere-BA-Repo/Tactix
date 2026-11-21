package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R

/**
 * Modern atmospheric mesh backdrop with layered dynamic spotlights and crisp architectural hairlines.
 */
@Composable
fun TactixAmbientBackdrop(
    accentPrimary: Color,
    accentSecondary: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val bgColor = MaterialTheme.colorScheme.background
    val gridColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.036f)

    val animatedPrimary by animateColorAsState(
        targetValue = accentPrimary,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "ambient_primary",
    )
    val animatedSecondary by animateColorAsState(
        targetValue = accentSecondary,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "ambient_secondary",
    )

    val drift = rememberInfiniteTransition(label = "ambient_drift")
    val orbShift by drift.animateFloat(
        initialValue = -0.05f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "orb_shift",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .drawBehind {
                // Top-left luminous spotlight
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            animatedPrimary.copy(alpha = 0.22f),
                            animatedPrimary.copy(alpha = 0.06f),
                            Color.Transparent,
                        ),
                        center = Offset(
                            x = size.width * (0.16f + orbShift),
                            y = size.height * 0.08f,
                        ),
                        radius = size.maxDimension * 0.62f,
                    ),
                )
                // Bottom-right warm complementary wash
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            animatedSecondary.copy(alpha = 0.16f),
                            animatedSecondary.copy(alpha = 0.04f),
                            Color.Transparent,
                        ),
                        center = Offset(
                            x = size.width * (0.86f - orbShift),
                            y = size.height * 0.82f,
                        ),
                        radius = size.maxDimension * 0.56f,
                    ),
                )
                // Subtle architectural cross-grid texture
                val spacing = 44.dp.toPx()
                val stroke = 0.8.dp.toPx()
                var x = spacing
                while (x < size.width) {
                    drawLine(
                        color = gridColor,
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = stroke,
                    )
                    x += spacing
                }
                var y = spacing
                while (y < size.height) {
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = stroke,
                    )
                    y += spacing
                }
            },
    ) {
        content()
    }
}

/**
 * Tactile micro-press spring scale wrapper for cards & hero actions.
 */
@Composable
fun TactixInteractiveSurface(
    onClick: () -> Unit,
    shape: androidx.compose.ui.graphics.Shape,
    color: Color,
    border: BorderStroke? = null,
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 0.dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.972f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "tactile_press_scale",
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = shape,
        color = color,
        border = border,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        content = content,
    )
}

/**
 * Bold sculpted Tactix app emblem matching the launcher icon for unified brand identity.
 */
@Composable
fun TactixAppEmblem(
    size: Dp = 42.dp,
    cornerRadius: Dp = 13.dp,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .size(size)
            .shadow(elevation = 6.dp, shape = shape, clip = false)
            .clip(shape)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                shape = shape,
            ),
    ) {
        Image(
            painter = painterResource(id = R.drawable.tactix_bold_icon_clean),
            contentDescription = "Tactix Emblem",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/**
 * Compact geometric X/O mark used in lightweight chrome spots.
 */
@Composable
fun TactixMiniEmblem(modifier: Modifier = Modifier) {
    val xColor = MaterialTheme.colorScheme.primary
    val oColor = MaterialTheme.colorScheme.tertiary
    val container = MaterialTheme.colorScheme.surfaceContainerHigh
    val rim = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)

    Box(
        modifier = modifier
            .size(34.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(container)
            .border(1.dp, rim, RoundedCornerShape(11.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(6.dp)) {
            val stroke = 2.4.dp.toPx()
            val half = size.minDimension * 0.44f
            drawLine(
                color = xColor,
                start = Offset(1f, 1f),
                end = Offset(half, half),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = xColor,
                start = Offset(half, 1f),
                end = Offset(1f, half),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
            val oStart = size.minDimension - half
            drawArc(
                color = oColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(oStart, oStart),
                size = Size(half, half),
                style = Stroke(width = stroke),
            )
        }
    }
}

data class PillOption<T>(
    val value: T,
    val label: String,
    val icon: ImageVector? = null,
    val testTag: String,
)

/**
 * Sleek modern floating capsule switcher with crisp elevated active thumb.
 */
@Composable
fun <T> TactixPillSwitcher(
    options: List<PillOption<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val trackShape = RoundedCornerShape(18.dp)
    val trackBorder = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)

    Surface(
        modifier = modifier.height(46.dp),
        shape = trackShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.78f),
        border = BorderStroke(1.dp, trackBorder),
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            options.forEach { option ->
                val isSelected = option.value == selected
                val itemBg by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color.Transparent
                    },
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "pill_bg_${option.label}",
                )
                val itemContent by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    animationSpec = tween(200),
                    label = "pill_fg_${option.label}",
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(14.dp))
                        .background(itemBg)
                        .testTag(option.testTag)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onSelect(option.value) },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        if (option.icon != null) {
                            Icon(
                                imageVector = option.icon,
                                contentDescription = null,
                                tint = itemContent,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                        Text(
                            text = option.label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = itemContent,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Vertically rolling animated score digit counter.
 */
@Composable
fun RollingScoreCounter(
    count: Int,
    color: Color,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = count,
        transitionSpec = {
            (slideInVertically { -it / 2 } + fadeIn(tween(180)))
                .togetherWith(slideOutVertically { it / 2 } + fadeOut(tween(140)))
        },
        label = "rolling_score",
        modifier = modifier,
    ) { target ->
        Text(
            text = target.toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}

/**
 * Subtle three-dot kinetic pulse shown while the opponent is choosing a move.
 */
@Composable
fun AiThinkingPulse(
    color: Color,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "ai_dots")
    val dot1 by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(420), RepeatMode.Reverse),
        label = "d1",
    )
    val dot2 by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(420, delayMillis = 140), RepeatMode.Reverse),
        label = "d2",
    )
    val dot3 by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(420, delayMillis = 280), RepeatMode.Reverse),
        label = "d3",
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        listOf(dot1, dot2, dot3).forEach { alpha ->
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = alpha)),
            )
        }
    }
}
