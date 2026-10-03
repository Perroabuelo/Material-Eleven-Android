package io.github.perroabuelo.materialeleven.core.library

/**
 * A song as the app shows it. [artist] and [album] are null when the tags do not carry them; the
 * UI shows them as "Unknown" in the current language.
 */
data class Track(
    val id: Long,
    val title: String,
    val artist: String?,
    val album: String?,
    val albumId: Long,
    val durationMs: Long,
    val format: AudioFormat,
    val fileName: String,
    val dateAddedSeconds: Long,
)
