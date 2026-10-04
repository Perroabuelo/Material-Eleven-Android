package io.github.perroabuelo.materialeleven.core.accent

import kotlin.math.pow

/** What a cover gives to the accent. */
sealed interface CoverAccent {
    /** The cover has a dominant hue; [color] is its raw average colour (ARGB). */
    data class Chromatic(val color: Int) : CoverAccent

    /** The cover was read but carries too little colour (black and white, greyscale). */
    data object Achromatic : CoverAccent

    /** No cover, or one that could not be read. */
    data object None : CoverAccent
}

/**
 * The cover-art accent colour. A Kotlin translation of source/accent.c from Material-Eleven for
 * PS Vita, with the same constants, so both apps pick the same accent for the same cover.
 * Colours are ARGB ints.
 */
object Accent {
    const val FIXED: Int = 0xFFFF9166.toInt()
    const val NEUTRAL: Int = 0xFFE6E3EC.toInt()
    const val BACKGROUND: Int = 0xFF120F17.toInt()
    const val TEXT_PRIMARY: Int = 0xFFF4EFEA.toInt()

    // Cover-art sampling: roughly a 48x48 grid of samples, whatever the cover's real size.
    private const val SAMPLE_GRID = 48
    private const val HUE_BUCKETS = 24

    // A pixel needs this much saturation, and a lightness away from both extremes, before its hue
    // counts as a vote.
    private const val HUE_MIN_SAT = 0.18f
    private const val HUE_MIN_LUM = 0.10f
    private const val HUE_MAX_LUM = 0.92f

    // Share of samples that must carry a hue for the cover to count as chromatic. 1 % still
    // catches a small detail of colour on an otherwise black cover.
    private const val HUE_MIN_SHARE = 0.01f

    // The fixed accent #FF9166 sits at S 1.00 / L 0.70. This band brackets it, so a derived accent
    // lands in the same contrast range against the background.
    private const val ACCENT_MIN_SAT = 0.60f
    private const val ACCENT_MAX_SAT = 0.95f
    private const val ACCENT_MIN_LUM = 0.58f
    private const val ACCENT_MAX_LUM = 0.76f

    // Lightness is a poor stand-in for perceived luminance, so a second pass lifts the lightness
    // until the accent clears a real contrast floor over the background.
    const val MIN_CONTRAST = 4.5f
    private const val ACCENT_LUM_CEILING = 0.88f
    private const val ACCENT_LUM_STEP = 0.02f

    /**
     * Classifies a cover from its ARGB [pixels] ([width] x [height], row-major). Pixels with an
     * alpha under 128 carry no colour.
     */
    fun classify(pixels: IntArray, width: Int, height: Int): CoverAccent {
        if (width <= 0 || height <= 0 || pixels.size < width * height) return CoverAccent.None

        val weight = FloatArray(HUE_BUCKETS)
        val sumR = FloatArray(HUE_BUCKETS)
        val sumG = FloatArray(HUE_BUCKETS)
        val sumB = FloatArray(HUE_BUCKETS)
        val count = IntArray(HUE_BUCKETS)
        var sampled = 0
        var chromatic = 0

        val stepX = ((width + SAMPLE_GRID - 1) / SAMPLE_GRID).coerceAtLeast(1)
        val stepY = ((height + SAMPLE_GRID - 1) / SAMPLE_GRID).coerceAtLeast(1)
        val hsl = FloatArray(3)

        for (y in 0 until height step stepY) {
            for (x in 0 until width step stepX) {
                val pixel = pixels[y * width + x]
                if ((pixel ushr 24) < 128) continue

                val r = ((pixel shr 16) and 0xFF) / 255f
                val g = ((pixel shr 8) and 0xFF) / 255f
                val b = (pixel and 0xFF) / 255f
                sampled++
                rgbToHsl(r, g, b, hsl)
                val (hue, sat, lum) = Triple(hsl[0], hsl[1], hsl[2])

                // Flat black, flat white and grey carry no hue to vote with.
                if (sat < HUE_MIN_SAT || lum < HUE_MIN_LUM || lum > HUE_MAX_LUM) continue

                val bucket = (hue * HUE_BUCKETS).toInt().coerceIn(0, HUE_BUCKETS - 1)
                // Weighted by saturation, so a vivid minority outvotes a washed-out majority.
                weight[bucket] += sat
                sumR[bucket] += r
                sumG[bucket] += g
                sumB[bucket] += b
                count[bucket]++
                chromatic++
            }
        }

        if (sampled == 0 || chromatic.toFloat() < sampled.toFloat() * HUE_MIN_SHARE) {
            return CoverAccent.Achromatic
        }

        var best = 0
        for (i in 1 until HUE_BUCKETS) {
            if (weight[i] > weight[best]) best = i
        }
        if (count[best] == 0) return CoverAccent.Achromatic

        val inv = 1f / count[best]
        return CoverAccent.Chromatic(
            argb(
                (sumR[best] * inv * 255f + 0.5f).toInt(),
                (sumG[best] * inv * 255f + 0.5f).toInt(),
                (sumB[best] * inv * 255f + 0.5f).toInt(),
            ),
        )
    }

    /** The accent the interface uses for a cover. */
    fun accentFor(cover: CoverAccent): Int = when (cover) {
        is CoverAccent.Chromatic -> makeLegible(cover.color)
        CoverAccent.Achromatic -> NEUTRAL
        CoverAccent.None -> FIXED
    }

    /**
     * Keeps the hue of [color] and moves it into the accent band, then lifts its lightness until it
     * reaches [MIN_CONTRAST] over [background] (or the lightness ceiling).
     */
    fun makeLegible(color: Int, background: Int = BACKGROUND): Int {
        val hsl = FloatArray(3)
        rgbToHsl(
            ((color shr 16) and 0xFF) / 255f,
            ((color shr 8) and 0xFF) / 255f,
            (color and 0xFF) / 255f,
            hsl,
        )
        val hue = hsl[0]
        val sat = hsl[1].coerceIn(ACCENT_MIN_SAT, ACCENT_MAX_SAT)
        var lum = hsl[2].coerceIn(ACCENT_MIN_LUM, ACCENT_MAX_LUM)
        var accent = hslToArgb(hue, sat, lum)

        while (lum < ACCENT_LUM_CEILING && contrast(accent, background) < MIN_CONTRAST) {
            lum += ACCENT_LUM_STEP
            accent = hslToArgb(hue, sat, lum)
        }
        return accent
    }

    /** The colour for icons and text drawn on top of [accent]: whichever of the two reads better. */
    fun onAccent(accent: Int): Int =
        if (contrast(BACKGROUND, accent) >= contrast(TEXT_PRIMARY, accent)) BACKGROUND else TEXT_PRIMARY

    /** WCAG contrast ratio between two colours. */
    fun contrast(a: Int, b: Int): Float {
        val la = relativeLuminance(a)
        val lb = relativeLuminance(b)
        return if (la > lb) (la + 0.05f) / (lb + 0.05f) else (lb + 0.05f) / (la + 0.05f)
    }

    private fun relativeLuminance(color: Int): Float =
        0.2126f * srgbToLinear(((color shr 16) and 0xFF) / 255f) +
            0.7152f * srgbToLinear(((color shr 8) and 0xFF) / 255f) +
            0.0722f * srgbToLinear((color and 0xFF) / 255f)

    private fun srgbToLinear(c: Float): Float =
        if (c <= 0.03928f) c / 12.92f else ((c + 0.055f) / 1.055f).toDouble().pow(2.4).toFloat()

    private fun rgbToHsl(r: Float, g: Float, b: Float, out: FloatArray) {
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val span = max - min
        val lum = (max + min) / 2f
        out[2] = lum

        if (span < 0.0001f) {
            out[0] = 0f
            out[1] = 0f
            return
        }

        out[1] = if (lum > 0.5f) span / (2f - max - min) else span / (max + min)
        val hue = when (max) {
            r -> (g - b) / span + (if (g < b) 6f else 0f)
            g -> (b - r) / span + 2f
            else -> (r - g) / span + 4f
        }
        out[0] = hue / 6f
    }

    private fun hueToChannel(p: Float, q: Float, tIn: Float): Float {
        var t = tIn
        if (t < 0f) t += 1f
        if (t > 1f) t -= 1f
        return when {
            t < 1f / 6f -> p + (q - p) * 6f * t
            t < 1f / 2f -> q
            t < 2f / 3f -> p + (q - p) * (2f / 3f - t) * 6f
            else -> p
        }
    }

    private fun hslToArgb(h: Float, s: Float, l: Float): Int {
        val r: Float
        val g: Float
        val b: Float
        if (s < 0.0001f) {
            r = l; g = l; b = l
        } else {
            val q = if (l < 0.5f) l * (1f + s) else l + s - l * s
            val p = 2f * l - q
            r = hueToChannel(p, q, h + 1f / 3f)
            g = hueToChannel(p, q, h)
            b = hueToChannel(p, q, h - 1f / 3f)
        }
        return argb((r * 255f + 0.5f).toInt(), (g * 255f + 0.5f).toInt(), (b * 255f + 0.5f).toInt())
    }

    private fun argb(r: Int, g: Int, b: Int): Int =
        (0xFF shl 24) or ((r and 0xFF) shl 16) or ((g and 0xFF) shl 8) or (b and 0xFF)
}
