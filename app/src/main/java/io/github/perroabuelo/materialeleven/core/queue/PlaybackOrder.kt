package io.github.perroabuelo.materialeleven.core.queue

import kotlin.random.Random

/**
 * A playback order: the queue's item indices in the order they play. It is the natural order
 * (0, 1, 2, ...) or a shuffled one, and it is walked circularly.
 */
class PlaybackOrder(private val items: IntArray) {
    private val positionOf = IntArray(items.size).also { positions ->
        items.forEachIndexed { position, item -> positions[item] = position }
    }

    val size: Int get() = items.size

    operator fun get(position: Int): Int = items[position]

    fun positionOf(item: Int): Int = positionOf[item]

    fun toList(): List<Int> = items.toList()

    fun toIntArray(): IntArray = items.copyOf()

    /** The item [steps] places after [item], wrapping around. Negative steps go backwards. */
    fun step(item: Int, steps: Int): Int = items[Math.floorMod(positionOf[item] + steps, size)]

    /**
     * The next [count] items after [current], wrapping around past the end, without repeating
     * [current] itself. This is what the "Up next" preview shows.
     */
    fun upNext(current: Int, count: Int): List<Int> =
        (1..minOf(count, size - 1)).map { step(current, it) }

    companion object {
        fun natural(size: Int): PlaybackOrder = PlaybackOrder(IntArray(size) { it })

        /**
         * A random permutation of [size] items with [first] in the first place, so a full lap from
         * the playing track goes through every track once.
         */
        fun shuffled(size: Int, first: Int, random: Random): PlaybackOrder {
            require(size == 0 || first in 0 until size) { "first=$first out of 0 until $size" }
            val items = IntArray(size) { it }
            if (size == 0) return PlaybackOrder(items)
            items[first] = 0
            items[0] = first
            // Fisher-Yates over everything after the first place.
            for (i in size - 1 downTo 2) {
                val j = 1 + random.nextInt(i)
                val tmp = items[i]
                items[i] = items[j]
                items[j] = tmp
            }
            return PlaybackOrder(items)
        }
    }
}
