package io.github.perroabuelo.materialeleven.core.library

/**
 * Which audio files the library lists, beyond what the system flags as music. Some vendors flag
 * every audio file as music, voice notes and recordings included.
 */
object LibraryFilter {
    // Other apps' media folders: WhatsApp and Telegram audio, documents and voice notes.
    private val excludedRoots = listOf("android/media/", "android/data/")

    // Folders that hold recordings, matched at any depth.
    private val recordingFolders = setOf(
        "recordings",
        "grabaciones",
        "call recordings",
        "callrecordings",
        "sound_recorder",
        "voice recorder",
        "voicerecorder",
    )

    // Formats the player cannot play: MIDI and ringtone formats.
    private val unplayableMimeTypes = setOf("audio/midi", "audio/x-midi", "audio/mid", "audio/sp-midi", "audio/imelody")
    private val unplayableExtensions = setOf("mid", "midi", "xmf", "mxmf", "rtttl", "rtx", "ota", "imy")

    /**
     * Whether a file is listed. [folder] is its folder relative to the storage volume
     * ("Music/Album/"); an absolute path is accepted too and its volume prefix is ignored.
     */
    fun isListed(folder: String?, fileName: String, mimeType: String?): Boolean {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        if (extension in unplayableExtensions || mimeType?.lowercase() in unplayableMimeTypes) return false

        val relative = relativeFolder(folder ?: return true).lowercase()
        if (excludedRoots.any { relative.startsWith(it) }) return false
        return relative.split('/').none { it in recordingFolders }
    }

    // "/storage/emulated/0/Music/" and "/storage/1234-ABCD/Music/" become "Music/".
    private fun relativeFolder(path: String): String {
        val normalized = path.replace('\\', '/')
        if (!normalized.startsWith("/")) return normalized
        val parts = normalized.trimStart('/').split('/')
        val volumeDepth = when {
            parts.size >= 3 && parts[0] == "storage" && parts[1] == "emulated" -> 3
            parts.size >= 2 && parts[0] == "storage" -> 2
            parts.size >= 1 && parts[0] == "sdcard" -> 1
            else -> 0
        }
        return parts.drop(volumeDepth).joinToString("/")
    }
}
