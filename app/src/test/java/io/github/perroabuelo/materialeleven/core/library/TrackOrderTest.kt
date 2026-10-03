package io.github.perroabuelo.materialeleven.core.library

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class TrackOrderTest {
    private fun track(id: Long, title: String, artist: String? = null) = Track(
        id = id, title = title, artist = artist, album = null, albumId = 0, durationMs = 0,
        format = AudioFormat("MP3", FormatFamily.LOSSY), fileName = "$title.mp3", dateAddedSeconds = 0,
    )

    private val spanish = Locale.forLanguageTag("es-CL")

    @Test
    fun titleOrderIgnoresCaseAndAccents() {
        val sorted = listOf(track(1, "zeta"), track(2, "Álamo"), track(3, "beta"))
            .sortedWith(TrackOrder.byTitle(spanish))
        assertEquals(listOf("Álamo", "beta", "zeta"), sorted.map { it.title })
    }

    @Test
    fun artistBreaksTiesAndUnknownGoesLast() {
        val sorted = listOf(track(1, "Intro", null), track(2, "Intro", "Muse"), track(3, "intro", "Blur"))
            .sortedWith(TrackOrder.byTitle(spanish))
        assertEquals(listOf(3L, 2L, 1L), sorted.map { it.id })
    }

    @Test
    fun identicalRowsKeepIdOrder() {
        val sorted = listOf(track(9, "Same", "A"), track(4, "Same", "A"))
            .sortedWith(TrackOrder.byTitle(Locale.ENGLISH))
        assertEquals(listOf(4L, 9L), sorted.map { it.id })
    }
}
