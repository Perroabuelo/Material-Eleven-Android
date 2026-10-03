package io.github.perroabuelo.materialeleven.core.library

/** Fallbacks for tag values that are missing, blank or set to MediaStore's placeholder. */
object TagText {
    private const val MEDIA_STORE_UNKNOWN = "<unknown>"

    /** The tag value, or null when it carries nothing to show. */
    fun clean(value: String?): String? {
        val trimmed = value?.trim()
        return if (trimmed.isNullOrEmpty() || trimmed == MEDIA_STORE_UNKNOWN) null else trimmed
    }

    /** The title to show: the tag when present, otherwise the file name without its extension. */
    fun title(tagTitle: String?, fileName: String): String =
        clean(tagTitle) ?: fileName.substringBeforeLast('.').ifEmpty { fileName }
}
