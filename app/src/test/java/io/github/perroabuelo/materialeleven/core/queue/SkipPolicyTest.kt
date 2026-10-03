package io.github.perroabuelo.materialeleven.core.queue

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.random.Random

class SkipPolicyTest {
    @Test
    fun skipsForwardWhenMovingForward() {
        assertEquals(3, SkipPolicy.next(PlaybackOrder.natural(5), failed = 2, Direction.FORWARD, failures = 1))
    }

    @Test
    fun skipsBackwardWhenMovingBackward() {
        assertEquals(1, SkipPolicy.next(PlaybackOrder.natural(5), failed = 2, Direction.BACKWARD, failures = 1))
    }

    @Test
    fun skipWrapsAround() {
        assertEquals(0, SkipPolicy.next(PlaybackOrder.natural(5), failed = 4, Direction.FORWARD, failures = 1))
        assertEquals(4, SkipPolicy.next(PlaybackOrder.natural(5), failed = 0, Direction.BACKWARD, failures = 1))
    }

    @Test
    fun skipFollowsTheShuffledOrder() {
        val order = PlaybackOrder.shuffled(8, 3, Random(5))
        assertEquals(order[1], SkipPolicy.next(order, failed = 3, Direction.FORWARD, failures = 1))
    }

    @Test
    fun stopsAfterAWholeLapOfFailures() {
        val order = PlaybackOrder.natural(4)
        assertEquals(0, SkipPolicy.next(order, failed = 3, Direction.FORWARD, failures = 3))
        assertNull(SkipPolicy.next(order, failed = 0, Direction.FORWARD, failures = 4))
    }

    @Test
    fun singleUnreadableTrackStops() {
        assertNull(SkipPolicy.next(PlaybackOrder.natural(1), failed = 0, Direction.FORWARD, failures = 1))
    }
}
