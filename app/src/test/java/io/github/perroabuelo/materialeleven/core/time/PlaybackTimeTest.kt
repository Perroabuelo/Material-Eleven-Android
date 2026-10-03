package io.github.perroabuelo.materialeleven.core.time

import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackTimeTest {
    @Test
    fun formatsBelowAnHour() {
        assertEquals("0:00", PlaybackTime.format(0))
        assertEquals("0:00", PlaybackTime.format(999))
        assertEquals("0:59", PlaybackTime.format(59_000))
        assertEquals("4:00", PlaybackTime.format(240_000))
        assertEquals("59:59", PlaybackTime.format(3_599_999))
    }

    @Test
    fun formatsFromAnHourOn() {
        assertEquals("1:00:00", PlaybackTime.format(3_600_000))
        assertEquals("1:02:03", PlaybackTime.format(3_723_000))
    }

    @Test
    fun negativeIsUnknown() {
        assertEquals(PlaybackTime.UNKNOWN, PlaybackTime.format(-1))
        assertEquals(PlaybackTime.UNKNOWN, PlaybackTime.format(Long.MIN_VALUE + 1))
    }

    @Test
    fun remainingTime() {
        assertEquals("-4:00", PlaybackTime.remaining(0, 240_000))
        assertEquals("-2:00", PlaybackTime.remaining(120_000, 240_000))
        assertEquals("-0:00", PlaybackTime.remaining(300_000, 240_000))
        assertEquals("-4:00", PlaybackTime.remaining(-5, 240_000))
        assertEquals(PlaybackTime.UNKNOWN, PlaybackTime.remaining(0, -1))
    }
}
