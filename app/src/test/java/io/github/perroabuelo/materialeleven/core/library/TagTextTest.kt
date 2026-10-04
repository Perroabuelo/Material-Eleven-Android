package io.github.perroabuelo.materialeleven.core.library

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TagTextTest {
    @Test
    fun missingValuesAreNull() {
        assertNull(TagText.clean(null))
        assertNull(TagText.clean(""))
        assertNull(TagText.clean("   "))
        assertNull(TagText.clean("<unknown>"))
    }

    @Test
    fun presentValuesAreTrimmed() {
        assertEquals("Radiohead", TagText.clean("  Radiohead "))
    }

    @Test
    fun titleFallsBackToFileNameWithoutExtension() {
        assertEquals("demo-01", TagText.title(null, "demo-01.mp3"))
        assertEquals("demo-01", TagText.title(" ", "demo-01.mp3"))
        assertEquals("my.song", TagText.title(null, "my.song.flac"))
        assertEquals("README", TagText.title(null, "README"))
        assertEquals(".flac", TagText.title(null, ".flac"))
    }

    @Test
    fun titleTagWins() {
        assertEquals("Airbag", TagText.title("Airbag", "01.flac"))
    }
}
