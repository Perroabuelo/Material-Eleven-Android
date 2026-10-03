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
    fun onlyRepeatOneRepeatsTheTrack() {
        assertEquals(Player.REPEAT_MODE_ONE, QueueModes.playerRepeatMode(Player.REPEAT_MODE_ONE))
        // Controllers see repeat off as REPEAT_MODE_ALL; asking for it keeps the queue circular.
        assertEquals(Player.REPEAT_MODE_ALL, QueueModes.playerRepeatMode(Player.REPEAT_MODE_ALL))
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
