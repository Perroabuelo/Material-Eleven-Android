package io.github.perroabuelo.materialeleven.playback

import androidx.media3.common.C
import androidx.media3.common.Player
import org.junit.Assert.assertEquals
import org.junit.Test

class QueueModesTest {
    @Test
    fun repeatOffKeepsTheQueueCircular() {
        assertEquals(Player.REPEAT_MODE_ALL, QueueModes.playerRepeatMode(Player.REPEAT_MODE_OFF))
    }

    @Test
    fun anyRepeatRequestRepeatsTheTrack() {
        assertEquals(Player.REPEAT_MODE_ONE, QueueModes.playerRepeatMode(Player.REPEAT_MODE_ONE))
        assertEquals(Player.REPEAT_MODE_ONE, QueueModes.playerRepeatMode(Player.REPEAT_MODE_ALL))
    }

    @Test
    fun controllersOnlySeeOffOrOne() {
        assertEquals(Player.REPEAT_MODE_OFF, QueueModes.exposedRepeatMode(Player.REPEAT_MODE_ALL))
        assertEquals(Player.REPEAT_MODE_OFF, QueueModes.exposedRepeatMode(Player.REPEAT_MODE_OFF))
        assertEquals(Player.REPEAT_MODE_ONE, QueueModes.exposedRepeatMode(Player.REPEAT_MODE_ONE))
    }

    @Test
    fun shuffleOrderStartsWithTheCurrentTrackAndCoversAll() {
        val order = QueueModes.shuffleOrder(size = 12, first = 7, seed = 3)
        assertEquals(7, order.firstIndex)
        val visited = mutableListOf<Int>()
        var index = order.firstIndex
        while (index != C.INDEX_UNSET) {
            visited += index
            index = order.getNextIndex(index)
        }
        assertEquals((0 until 12).toList(), visited.sorted())
    }
}
