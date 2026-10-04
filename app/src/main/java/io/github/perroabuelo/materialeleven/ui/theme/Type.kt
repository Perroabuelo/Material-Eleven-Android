package io.github.perroabuelo.materialeleven.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.github.perroabuelo.materialeleven.R

// Static weights: Compose does not apply the weight axis of a variable font loaded from
// resources, which left every style at the variable font's default ExtraLight.
/** Manrope. Glyphs it lacks fall back to the system font. */
val Manrope = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extrabold, FontWeight.ExtraBold),
)

/** IBM Plex Mono, for times and format badges: digits keep their width as they change. */
val PlexMono = FontFamily(Font(R.font.ibm_plex_mono_medium, FontWeight.Medium))

// The Vita's five sizes (badge, label, body, title, display), scaled for a phone.
// No colour here: text takes LocalContentColor, so components like buttons can set it.
private val base = TextStyle(fontFamily = Manrope)

val ElevenTypography = Typography(
    displaySmall = base.copy(fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold),
    headlineSmall = base.copy(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold),
    titleLarge = base.copy(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold),
    titleMedium = base.copy(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
    titleSmall = base.copy(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = base.copy(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium),
    bodyMedium = base.copy(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
    bodySmall = base.copy(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium),
    labelLarge = base.copy(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = base.copy(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold),
    labelSmall = base.copy(fontSize = 11.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold),
)

/** Mono styles outside the Material scale. */
object ElevenTextStyles {
    val Time = TextStyle(fontFamily = PlexMono, fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium)
    val Badge = TextStyle(
        fontFamily = PlexMono,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.5.sp,
    )
}
