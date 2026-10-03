package io.github.perroabuelo.materialeleven.core.queue

enum class Direction { FORWARD, BACKWARD }

/** What to do when a track of the queue cannot be opened. */
object SkipPolicy {
    /**
     * The item to try after [failed] could not be opened, moving in [direction] through [order].
     * [failures] counts the consecutive failures, this one included. Returns null once a whole lap
     * has failed, meaning playback stops.
     */
    fun next(order: PlaybackOrder, failed: Int, direction: Direction, failures: Int): Int? {
        if (failures >= order.size) return null
        return order.step(failed, if (direction == Direction.FORWARD) 1 else -1)
    }
}
