package io.github.perroabuelo.materialeleven.core.library

enum class FormatFamily { LOSSLESS, LOSSY, OTHER }

/** A file format as shown on the format badge: a short label and the family that colours it. */
data class AudioFormat(val label: String, val family: FormatFamily) {
    companion object {
        private val FLAC = AudioFormat("FLAC", FormatFamily.LOSSLESS)
        private val WAV = AudioFormat("WAV", FormatFamily.LOSSLESS)
        private val AIFF = AudioFormat("AIFF", FormatFamily.LOSSLESS)
        private val ALAC = AudioFormat("ALAC", FormatFamily.LOSSLESS)
        private val MP3 = AudioFormat("MP3", FormatFamily.LOSSY)
        private val OGG = AudioFormat("OGG", FormatFamily.LOSSY)
        private val OPUS = AudioFormat("OPUS", FormatFamily.LOSSY)
        private val AAC = AudioFormat("AAC", FormatFamily.LOSSY)
        private val M4A = AudioFormat("M4A", FormatFamily.LOSSY)

        private val byExtension = mapOf(
            "flac" to FLAC,
            "wav" to WAV,
            "aif" to AIFF,
            "aiff" to AIFF,
            "mp3" to MP3,
            "ogg" to OGG,
            "oga" to OGG,
            "opus" to OPUS,
            "aac" to AAC,
            "m4a" to M4A,
        )

        private val byMimeType = mapOf(
            "audio/flac" to FLAC,
            "audio/x-flac" to FLAC,
            "audio/wav" to WAV,
            "audio/x-wav" to WAV,
            "audio/vnd.wave" to WAV,
            "audio/aiff" to AIFF,
            "audio/x-aiff" to AIFF,
            "audio/alac" to ALAC,
            "audio/mpeg" to MP3,
            "audio/ogg" to OGG,
            "audio/vorbis" to OGG,
            "audio/opus" to OPUS,
            "audio/aac" to AAC,
            "audio/mp4" to M4A,
        )

        /**
         * The format of a file, from its extension first and its MIME type second. An unknown
         * format keeps its extension as the label, in the OTHER family.
         */
        fun of(fileName: String?, mimeType: String?): AudioFormat {
            val extension = fileName?.substringAfterLast('.', "")?.lowercase().orEmpty()
            byExtension[extension]?.let { return it }
            mimeType?.lowercase()?.let { mime -> byMimeType[mime]?.let { return it } }
            return AudioFormat(extension.uppercase().ifEmpty { "?" }, FormatFamily.OTHER)
        }
    }
}
