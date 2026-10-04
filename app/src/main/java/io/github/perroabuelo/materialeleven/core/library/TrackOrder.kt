package io.github.perroabuelo.materialeleven.core.library

import java.text.Collator
import java.util.Locale

/** Orders of the library views. */
object TrackOrder {
    /**
     * Title order that ignores case and accents ("Álamo", "beta", "zeta"), with the artist as the
     * tie-break and the id last, so equal rows keep a stable position.
     */
    fun byTitle(locale: Locale = Locale.getDefault()): Comparator<Track> {
        val collator = Collator.getInstance(locale).apply { strength = Collator.PRIMARY }
        return Comparator { a, b ->
            collator.compare(a.title, b.title).takeIf { it != 0 }
                ?: compareArtists(collator, a.artist, b.artist).takeIf { it != 0 }
                ?: a.id.compareTo(b.id)
        }
    }

    // Unknown artists go after the known ones.
    private fun compareArtists(collator: Collator, a: String?, b: String?): Int = when {
        a == null && b == null -> 0
        a == null -> 1
        b == null -> -1
        else -> collator.compare(a, b)
    }
}
