package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val TactixDarkColorScheme = darkColorScheme(
    primary = CobaltPrimaryDark,
    onPrimary = CobaltOnPrimaryDark,
    primaryContainer = CobaltPrimaryContainerDark,
    onPrimaryContainer = CobaltOnPrimaryContainerDark,
    inversePrimary = CobaltPrimaryLight,
    secondary = CeramicSecondaryDark,
    onSecondary = CeramicOnSecondaryDark,
    secondaryContainer = CeramicSecondaryContainerDark,
    onSecondaryContainer = CeramicOnSecondaryContainerDark,
    tertiary = VermilionTertiaryDark,
    onTertiary = VermilionOnTertiaryDark,
    tertiaryContainer = VermilionTertiaryContainerDark,
    onTertiaryContainer = VermilionOnTertiaryContainerDark,
    error = CrimsonErrorDark,
    onError = CrimsonOnErrorDark,
    errorContainer = CrimsonErrorContainerDark,
    onErrorContainer = CrimsonOnErrorContainerDark,
    background = ObsidianBackgroundDark,
    onBackground = IvoryOnSurfaceDark,
    surface = ObsidianSurfaceDark,
    onSurface = IvoryOnSurfaceDark,
    surfaceVariant = ObsidianSurfaceVariantDark,
    onSurfaceVariant = SilverOnSurfaceVariantDark,
    surfaceContainerLowest = ObsidianSurfaceContainerLowestDark,
    surfaceContainerLow = ObsidianSurfaceContainerLowDark,
    surfaceContainer = ObsidianSurfaceContainerDark,
    surfaceContainerHigh = ObsidianSurfaceContainerHighDark,
    surfaceContainerHighest = ObsidianSurfaceContainerHighestDark,
    inverseSurface = IvoryOnSurfaceDark,
    inverseOnSurface = ObsidianBackgroundDark,
    outline = HairlineOutlineDark,
    outlineVariant = HairlineOutlineVariantDark,
    scrim = ObsidianScrimDark,
)

private val TactixLightColorScheme = lightColorScheme(
    primary = CobaltPrimaryLight,
    onPrimary = CobaltOnPrimaryLight,
    primaryContainer = CobaltPrimaryContainerLight,
    onPrimaryContainer = CobaltOnPrimaryContainerLight,
    inversePrimary = CobaltPrimaryDark,
    secondary = ObsidianSecondaryLight,
    onSecondary = ObsidianOnSecondaryLight,
    secondaryContainer = ObsidianSecondaryContainerLight,
    onSecondaryContainer = ObsidianOnSecondaryContainerLight,
    tertiary = VermilionTertiaryLight,
    onTertiary = VermilionOnTertiaryLight,
    tertiaryContainer = VermilionTertiaryContainerLight,
    onTertiaryContainer = VermilionOnTertiaryContainerLight,
    error = CrimsonErrorLight,
    onError = CrimsonOnErrorLight,
    errorContainer = CrimsonErrorContainerLight,
    onErrorContainer = CrimsonOnErrorContainerLight,
    background = CeramicBackgroundLight,
    onBackground = ObsidianOnSurfaceLight,
    surface = CeramicSurfaceLight,
    onSurface = ObsidianOnSurfaceLight,
    surfaceVariant = CeramicSurfaceVariantLight,
    onSurfaceVariant = SlateOnSurfaceVariantLight,
    surfaceContainerLowest = CeramicSurfaceContainerLowestLight,
    surfaceContainerLow = CeramicSurfaceContainerLowLight,
    surfaceContainer = CeramicSurfaceContainerLight,
    surfaceContainerHigh = CeramicSurfaceContainerHighLight,
    surfaceContainerHighest = CeramicSurfaceContainerHighestLight,
    inverseSurface = ObsidianBackgroundDark,
    inverseOnSurface = IvoryOnSurfaceDark,
    outline = HairlineOutlineLight,
    outlineVariant = HairlineOutlineVariantLight,
    scrim = ObsidianScrimDark,
)

val TactixShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun TactixTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> TactixDarkColorScheme
        else -> TactixLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !darkTheme
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TactixTypography,
        shapes = TactixShapes,
        content = content,
    )
}
