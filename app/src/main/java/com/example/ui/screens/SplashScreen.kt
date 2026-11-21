package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    isReady: Boolean,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val emblemScale = remember { Animatable(0.72f) }
    val emblemAlpha = remember { Animatable(0f) }
    val haloProgress = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val creditAlpha = remember { Animatable(0f) }

    var minDurationReached by remember { mutableStateOf(false) }
    var allowSkip by remember { mutableStateOf(false) }
    var didFinish by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        launch {
            emblemAlpha.animateTo(1f, tween(340, easing = FastOutSlowInEasing))
        }
        launch {
            emblemScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
            )
        }
        launch {
            haloProgress.animateTo(1f, tween(850, easing = FastOutSlowInEasing))
        }
        delay(280)
        launch {
            titleAlpha.animateTo(1f, tween(380, easing = FastOutSlowInEasing))
        }
        delay(240)
        allowSkip = true
        creditAlpha.animateTo(1f, tween(360, easing = FastOutSlowInEasing))
        delay(820)
        minDurationReached = true
    }

    LaunchedEffect(minDurationReached, isReady) {
        if (minDurationReached && isReady && !didFinish) {
            didFinish = true
            onFinished()
        }
    }

    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("splash_screen")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                if (allowSkip && isReady && !didFinish) {
                    didFinish = true
                    onFinished()
                }
            }
            .padding(horizontal = 28.dp, vertical = 24.dp),
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier.size(164.dp),
                contentAlignment = Alignment.Center,
            ) {
                // Expanding luminous ring & ambient aura behind the bold sculpted emblem
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val maxRadius = size.minDimension * 0.5f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primary.copy(alpha = 0.28f * haloProgress.value),
                                tertiary.copy(alpha = 0.14f * haloProgress.value),
                                Color.Transparent,
                            ),
                        ),
                        radius = maxRadius * (0.65f + 0.35f * haloProgress.value),
                    )
                    if (haloProgress.value > 0.05f) {
                        drawCircle(
                            color = primary.copy(alpha = (1f - haloProgress.value) * 0.45f),
                            radius = maxRadius * (0.66f + 0.34f * haloProgress.value),
                            style = Stroke(width = 2.dp.toPx()),
                        )
                    }
                }

                val emblemShape = RoundedCornerShape(32.dp)
                Surface(
                    shape = emblemShape,
                    color = Color(0xFF0D0E12),
                    border = BorderStroke(1.5.dp, primary.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .size(112.dp)
                        .scale(emblemScale.value)
                        .alpha(emblemAlpha.value)
                        .shadow(elevation = 18.dp, shape = emblemShape),
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.tactix_bold_icon_clean),
                        contentDescription = stringResource(R.string.app_name),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(emblemShape),
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1.2).sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.alpha(titleAlpha.value),
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.splash_tagline),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(titleAlpha.value),
            )
        }

        // Clean studio signature pinned at bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
                .alpha(creditAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "FROM",
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 2.2.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
            Text(
                text = stringResource(R.string.studio_name),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.3.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .width(34.dp)
                    .height(3.dp)
                    .background(
                        Brush.horizontalGradient(listOf(primary, tertiary)),
                        RoundedCornerShape(50),
                    ),
            )
        }
    }
}
