package io.github.perroabuelo.materialeleven.playback

import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.source.ShuffleOrder
import io.github.perroabuelo.materialeleven.core.queue.PlaybackOrder
import kotlin.random.Random

/**
 * The queue's modes as the app exposes them, over the real player modes. The queue always wraps
 * around, so "repeat off" is the player's REPEAT_MODE_ALL and "repeat on" repeats the track.
 */
@UnstableApi
object QueueModes {
    /** The player mode for a mode asked by any controller: anything but OFF repeats the track. */
    fun playerRepeatMode(requested: @Player.RepeatMode Int): @Player.RepeatMode Int =
        if (requested == Player.REPEAT_MODE_OFF) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_ONE

    /** The mode controllers see for a player mode. */
    fun exposedRepeatMode(player: @Player.RepeatMode Int): @Player.RepeatMode Int =
        if (player == Player.REPEAT_MODE_ONE) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF

    /** A shuffle order with [first] in the first place, so a lap from the playing track covers all. */
    fun shuffleOrder(size: Int, first: Int, seed: Long): ShuffleOrder =
        ShuffleOrder.DefaultShuffleOrder(
            PlaybackOrder.shuffled(size, first.coerceIn(0, maxOf(size - 1, 0)), Random(seed)).toIntArray(),
            seed,
        )
}
