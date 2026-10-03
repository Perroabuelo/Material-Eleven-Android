package io.github.perroabuelo.materialeleven.core.time

import java.util.Locale

/** Formats playback positions and durations for the seek bar. */
object PlaybackTime {
    const val UNKNOWN = "--:--"

    /** "m:ss" below an hour, "h:mm:ss" from an hour on, [UNKNOWN] for a negative value. */
    fun format(ms: Long): String {
        if (ms < 0) return UNKNOWN
        val totalSeconds = ms / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.ROOT, "%d:%02d", minutes, seconds)
        }
    }

    /**
     * Time left, written with a leading minus ("-1:05"). [UNKNOWN] when the duration is not known
     * yet. A position past the end counts as nothing left.
     */
    fun remaining(positionMs: Long, durationMs: Long): String {
        if (durationMs < 0) return UNKNOWN
        val left = (durationMs - positionMs.coerceAtLeast(0)).coerceAtLeast(0)
        return "-" + format(left)
    }
}
