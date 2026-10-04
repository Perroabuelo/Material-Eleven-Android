package io.github.perroabuelo.materialeleven.core.library

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryFilterTest {
    @Test
    fun musicFoldersAreListed() {
        assertTrue(LibraryFilter.isListed("Music/Música/LE SSERAFIM/", "01.flac", "audio/flac"))
        assertTrue(LibraryFilter.isListed("Download/", "song.mp3", "audio/mpeg"))
        assertTrue(LibraryFilter.isListed("Music/", "Specialist ringtone.m4a", "audio/mp4"))
        assertTrue(LibraryFilter.isListed(null, "song.mp3", "audio/mpeg"))
    }

    @Test
    fun otherAppsMediaIsExcluded() {
        assertFalse(LibraryFilter.isListed("Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Audio/", "AUD-20211105-WA0020.m4a", "audio/mpeg"))
        assertFalse(LibraryFilter.isListed("Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Documents/", "Kantata.mp3", "audio/mpeg"))
        assertFalse(LibraryFilter.isListed("Android/data/org.telegram.messenger/files/", "voice.ogg", "audio/ogg"))
    }

    @Test
    fun recordingFoldersAreExcludedAtAnyDepth() {
        assertFalse(LibraryFilter.isListed("Grabaciones/", "a.m4a", "audio/mp4"))
        assertFalse(LibraryFilter.isListed("Recordings/Call/", "a.m4a", "audio/mp4"))
        assertFalse(LibraryFilter.isListed("MIUI/sound_recorder/", "a.mp3", "audio/mpeg"))
        assertFalse(LibraryFilter.isListed("Music/Call recordings/", "a.mp3", "audio/mpeg"))
        assertFalse(LibraryFilter.isListed("Voice Recorder/", "a.m4a", "audio/mp4"))
    }

    @Test
    fun similarFolderNamesAreNotExcluded() {
        assertTrue(LibraryFilter.isListed("Music/Recordings of Bach/", "a.flac", "audio/flac"))
        assertTrue(LibraryFilter.isListed("Music/Plini - Impulse Voices/", "a.flac", "audio/flac"))
    }

    @Test
    fun unplayableFormatsAreExcluded() {
        assertFalse(LibraryFilter.isListed("Download/", "Broken Will.mid", "audio/midi"))
        assertFalse(LibraryFilter.isListed("Download/", "tune.midi", null))
        assertFalse(LibraryFilter.isListed("Download/", "noext", "audio/x-midi"))
    }

    @Test
    fun absolutePathsLoseTheirVolumePrefix() {
        assertTrue(LibraryFilter.isListed("/storage/emulated/0/Music/Album/", "a.flac", null))
        assertFalse(LibraryFilter.isListed("/storage/emulated/0/Android/media/com.whatsapp/", "a.opus", null))
        assertFalse(LibraryFilter.isListed("/storage/1234-ABCD/Recordings/", "a.m4a", null))
        assertTrue(LibraryFilter.isListed("/sdcard/Music/", "a.mp3", null))
    }
}
