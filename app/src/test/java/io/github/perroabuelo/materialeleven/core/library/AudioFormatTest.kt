package io.github.perroabuelo.materialeleven.core.library

import org.junit.Assert.assertEquals
import org.junit.Test

class AudioFormatTest {
    @Test
    fun knownExtensionsMapToTheirFamily() {
        assertEquals(AudioFormat("FLAC", FormatFamily.LOSSLESS), AudioFormat.of("a.flac", null))
        assertEquals(AudioFormat("WAV", FormatFamily.LOSSLESS), AudioFormat.of("a.WAV", null))
        assertEquals(AudioFormat("MP3", FormatFamily.LOSSY), AudioFormat.of("a.mp3", null))
        assertEquals(AudioFormat("OGG", FormatFamily.LOSSY), AudioFormat.of("a.ogg", null))
        assertEquals(AudioFormat("OPUS", FormatFamily.LOSSY), AudioFormat.of("a.opus", null))
        assertEquals(AudioFormat("AAC", FormatFamily.LOSSY), AudioFormat.of("a.aac", null))
        assertEquals(AudioFormat("M4A", FormatFamily.LOSSY), AudioFormat.of("a.m4a", null))
    }

    @Test
    fun extensionWinsOverMimeType() {
        assertEquals("FLAC", AudioFormat.of("a.flac", "audio/mpeg").label)
    }

    @Test
    fun mimeTypeIsTheFallback() {
        assertEquals(AudioFormat("FLAC", FormatFamily.LOSSLESS), AudioFormat.of("track", "audio/x-flac"))
        assertEquals(AudioFormat("ALAC", FormatFamily.LOSSLESS), AudioFormat.of("a.alac", "audio/alac"))
        assertEquals(AudioFormat("MP3", FormatFamily.LOSSY), AudioFormat.of(null, "audio/mpeg"))
    }

    @Test
    fun unknownFormatKeepsItsExtension() {
        assertEquals(AudioFormat("MKA", FormatFamily.OTHER), AudioFormat.of("a.mka", "audio/x-matroska"))
        assertEquals(AudioFormat("?", FormatFamily.OTHER), AudioFormat.of("noextension", null))
    }
}
