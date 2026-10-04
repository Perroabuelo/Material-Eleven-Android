package io.github.perroabuelo.materialeleven.core.queue

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class PlaybackOrderTest {
    @Test
    fun shuffledIsAPermutationStartingWithTheCurrentTrack() {
        for (seed in 0 until 50) {
            val order = PlaybackOrder.shuffled(20, first = 7, random = Random(seed))
            assertEquals(7, order[0])
            assertEquals((0 until 20).toList(), order.toList().sorted())
        }
    }

    @Test
    fun shuffledIsStableForTheSameSeed() {
        assertEquals(
            PlaybackOrder.shuffled(30, 3, Random(42)).toList(),
            PlaybackOrder.shuffled(30, 3, Random(42)).toList(),
        )
    }

    @Test
    fun shuffledActuallyShuffles() {
        val orders = (0 until 10).map { PlaybackOrder.shuffled(20, 0, Random(it)).toList() }.toSet()
        assertTrue(orders.size > 1)
        assertNotEquals((0 until 20).toList(), PlaybackOrder.shuffled(20, 0, Random(1)).toList())
    }

    @Test
    fun shuffledEdgeSizes() {
        assertEquals(emptyList<Int>(), PlaybackOrder.shuffled(0, 0, Random(1)).toList())
        assertEquals(listOf(0), PlaybackOrder.shuffled(1, 0, Random(1)).toList())
        assertEquals(listOf(1, 0), PlaybackOrder.shuffled(2, 1, Random(1)).toList())
    }

    @Test
    fun aFullLapPlaysEveryTrackOnce() {
        val order = PlaybackOrder.shuffled(20, 5, Random(9))
        var item = 5
        val played = mutableListOf(item)
        repeat(19) {
            item = order.step(item, 1)
            played += item
        }
        assertEquals((0 until 20).toList(), played.sorted())
        assertEquals(5, order.step(item, 1))
    }

    @Test
    fun stepWrapsAroundBothWays() {
        val order = PlaybackOrder.natural(4)
        assertEquals(0, order.step(3, 1))
        assertEquals(3, order.step(0, -1))
        assertEquals(2, order.step(1, 1))
    }

    @Test
    fun backwardsInAShuffledOrderGoesToThePreviouslyPlayedTrack() {
        val order = PlaybackOrder.shuffled(10, 4, Random(3))
        val next = order.step(4, 1)
        assertEquals(4, order.step(next, -1))
    }

    @Test
    fun upNextFollowsTheOrderAndWrapsAtTheEnd() {
        val natural = PlaybackOrder.natural(5)
        assertEquals(listOf(2, 3, 4), natural.upNext(1, 3))
        assertEquals(listOf(0, 1, 2), natural.upNext(4, 3))

        val shuffled = PlaybackOrder.shuffled(5, 2, Random(7))
        assertEquals(listOf(shuffled[1], shuffled[2]), shuffled.upNext(2, 2))
    }

    @Test
    fun upNextNeverRepeatsTheCurrentTrack() {
        assertEquals(listOf(1, 2), PlaybackOrder.natural(3).upNext(0, 10))
        assertEquals(emptyList<Int>(), PlaybackOrder.natural(1).upNext(0, 5))
    }
}
