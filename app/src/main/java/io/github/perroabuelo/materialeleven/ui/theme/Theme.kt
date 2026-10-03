package io.github.perroabuelo.materialeleven.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import io.github.perroabuelo.materialeleven.core.accent.Accent

/** The accent colour of the moment and the colour for content drawn on top of it. */
@Immutable
data class AccentColors(val accent: Color, val onAccent: Color) {
    companion object {
        fun of(accent: Color) = AccentColors(accent, Color(Accent.onAccent(accent.toArgb())))
        val Fixed = of(ElevenColors.AccentFixed)
    }
}

val LocalAccent = staticCompositionLocalOf { AccentColors.Fixed }

/**
 * The Material Eleven theme: always the dark Vita palette, whatever the system theme or wallpaper,
 * with [accent] as the primary colour.
 */
@Composable
fun MaterialElevenTheme(accent: AccentColors = AccentColors.Fixed, content: @Composable () -> Unit) {
    val scheme = darkColorScheme(
        primary = accent.accent,
        onPrimary = accent.onAccent,
        secondary = accent.accent,
        onSecondary = accent.onAccent,
        background = ElevenColors.Background,
        onBackground = ElevenColors.TextPrimary,
        surface = ElevenColors.Background,
        onSurface = ElevenColors.TextPrimary,
        surfaceVariant = ElevenColors.Surface,
        onSurfaceVariant = ElevenColors.TextSecondary,
        surfaceContainerLowest = ElevenColors.Background,
        surfaceContainerLow = ElevenColors.BackgroundElevated,
        surfaceContainer = ElevenColors.Surface,
        surfaceContainerHigh = ElevenColors.Surface2,
        surfaceContainerHighest = ElevenColors.Surface2,
        outline = ElevenColors.TextTertiary,
        outlineVariant = ElevenColors.Hairline,
    )
    CompositionLocalProvider(LocalAccent provides accent) {
        MaterialTheme(colorScheme = scheme, typography = ElevenTypography, content = content)
    }
}
