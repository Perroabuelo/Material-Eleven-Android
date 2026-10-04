package io.github.perroabuelo.materialeleven.ui.theme

import androidx.compose.ui.graphics.Color
import io.github.perroabuelo.materialeleven.core.accent.Accent
import io.github.perroabuelo.materialeleven.core.library.FormatFamily

/** Colour tokens of Material Eleven, taken from the PS Vita version (include/ui_theme.h). */
object ElevenColors {
    val Background = Color(Accent.BACKGROUND)
    val BackgroundElevated = Color(0xFF17141F)
    val Surface = Color(0xFF1C1826)
    val Surface2 = Color(0xFF241F30)

    val TextPrimary = Color(Accent.TEXT_PRIMARY)
    val TextSecondary = Color(0xFFB0A8C0)
    val TextTertiary = Color(0xFF756C89)
    val TextMuted = Color(0xFF6B6478)
    val Hairline = Color(0x0FFFFFFF)

    val AccentFixed = Color(Accent.FIXED)
    val AccentNeutral = Color(Accent.NEUTRAL)

    val Lossless = Color(0xFF7FE0C1)
    val Lossy = Color(0xFFB79CE8)

    // Badge washes use the Vita's alpha of 41/255.
    private const val WASH_ALPHA = 41f / 255f

    fun formatColor(family: FormatFamily): Color = when (family) {
        FormatFamily.LOSSLESS -> Lossless
        FormatFamily.LOSSY -> Lossy
        FormatFamily.OTHER -> TextSecondary
    }

    fun formatWash(family: FormatFamily): Color = formatColor(family).copy(alpha = WASH_ALPHA)
}
