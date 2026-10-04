package io.github.perroabuelo.materialeleven.core.accent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class AccentTest {
    private val size = 96

    private fun image(pixel: (x: Int, y: Int) -> Int): IntArray =
        IntArray(size * size) { i -> pixel(i % size, i / size) }

    private fun rgb(r: Int, g: Int, b: Int, a: Int = 255): Int = (a shl 24) or (r shl 16) or (g shl 8) or b

    private fun assertColorNear(expected: Int, actual: Int) {
        for (shift in intArrayOf(16, 8, 0)) {
            val e = (expected shr shift) and 0xFF
            val a = (actual shr shift) and 0xFF
            assertTrue(
                "expected %06X, got %06X".format(expected and 0xFFFFFF, actual and 0xFFFFFF),
                abs(e - a) <= 1,
            )
        }
    }

    private fun chromatic(cover: CoverAccent): Int {
        assertTrue("expected a chromatic cover, got $cover", cover is CoverAccent.Chromatic)
        return (cover as CoverAccent.Chromatic).color
    }

    // Reference values come from source/accent.c of Material-Eleven for PS Vita, run on the same
    // synthetic 96x96 images.
    @Test
    fun matchesTheVitaVersion() {
        val cases = listOf(
            Triple(image { x, y -> if (x in 40..49 && y in 40..49) rgb(0xE0, 0x20, 0x20) else rgb(0x10, 0x10, 0x10) }, 0xE02020, 0xE54343),
            Triple(image { _, _ -> rgb(0x3A, 0x1A, 0x6B) }, 0x3A1A6B, 0x9263D9),
            Triple(image { x, y -> rgb(x * 255 / (size - 1), y * 255 / (size - 1), 128) }, 0x25BC80, 0x4CDCA3),
            Triple(image { _, _ -> rgb(0x20, 0x50, 0xC0) }, 0x2050C0, 0x507CE2),
            Triple(image { _, _ -> rgb(0xF0, 0xE0, 0x40) }, 0xF0E040, 0xF0E040),
            Triple(image { x, _ -> rgb(0xC0, 0x20, 0x30, if (x < 48) 0 else 255) }, 0xC02030, 0xE04757),
        )
        for ((pixels, raw, legible) in cases) {
            val color = chromatic(Accent.classify(pixels, size, size))
            assertColorNear(raw, color)
            assertColorNear(legible, Accent.makeLegible(color))
        }
    }

    @Test
    fun aSmallRedDetailOnABlackCoverGivesARedAccent() {
        val pixels = image { x, y -> if (x in 40..49 && y in 40..49) rgb(0xE0, 0x20, 0x20) else rgb(0x10, 0x10, 0x10) }
        val accent = Accent.accentFor(Accent.classify(pixels, size, size))
        val r = (accent shr 16) and 0xFF
        val g = (accent shr 8) and 0xFF
        val b = accent and 0xFF
        assertTrue(r > 200 && g < 100 && b < 100)
    }

    @Test
    fun greyscaleCoverIsAchromaticAndUsesTheNeutralAccent() {
        val pixels = image { x, y -> ((x + y) * 255 / (2 * size - 2)).let { rgb(it, it, it) } }
        val cover = Accent.classify(pixels, size, size)
        assertEquals(CoverAccent.Achromatic, cover)
        assertEquals(Accent.NEUTRAL, Accent.accentFor(cover))
        assertEquals(Accent.BACKGROUND, Accent.onAccent(Accent.NEUTRAL))
    }

    @Test
    fun fullyTransparentCoverIsAchromaticLikeTheVitaVersion() {
        assertEquals(CoverAccent.Achromatic, Accent.classify(IntArray(size * size), size, size))
    }

    @Test
    fun missingCoverUsesTheFixedAccent() {
        assertEquals(CoverAccent.None, Accent.classify(IntArray(0), 0, 0))
        assertEquals(CoverAccent.None, Accent.classify(IntArray(10), 96, 96))
        assertEquals(Accent.FIXED, Accent.accentFor(CoverAccent.None))
    }

    @Test
    fun darkVioletIsLiftedToTheContrastFloor() {
        val color = chromatic(Accent.classify(image { _, _ -> rgb(0x3A, 0x1A, 0x6B) }, size, size))
        assertTrue(Accent.contrast(Accent.makeLegible(color), Accent.BACKGROUND) >= Accent.MIN_CONTRAST)
    }

    @Test
    fun everyHueEndsUpLegibleAndSoDoesWhatIsDrawnOnIt() {
        for (degrees in 0 until 360 step 5) {
            for (lightness in listOf(0.15f, 0.4f, 0.7f)) {
                val color = hsl(degrees / 360f, 0.8f, lightness)
                val accent = Accent.makeLegible(color)
                assertTrue("hue $degrees", Accent.contrast(accent, Accent.BACKGROUND) >= Accent.MIN_CONTRAST)
                assertTrue("on hue $degrees", Accent.contrast(Accent.onAccent(accent), accent) >= Accent.MIN_CONTRAST)
            }
        }
    }

    @Test
    fun lightYellowGetsDarkContentOnTop() {
        assertEquals(Accent.BACKGROUND, Accent.onAccent(rgb(0xF0, 0xE0, 0x40)))
    }

    private fun hsl(h: Float, s: Float, l: Float): Int {
        val q = if (l < 0.5f) l * (1 + s) else l + s - l * s
        val p = 2 * l - q
        fun channel(tIn: Float): Int {
            var t = tIn
            if (t < 0) t += 1
            if (t > 1) t -= 1
            val v = when {
                t < 1f / 6 -> p + (q - p) * 6 * t
                t < 1f / 2 -> q
                t < 2f / 3 -> p + (q - p) * (2f / 3 - t) * 6
                else -> p
            }
            return (v * 255 + 0.5f).toInt()
        }
        return rgb(channel(h + 1f / 3), channel(h), channel(h - 1f / 3))
    }
}
