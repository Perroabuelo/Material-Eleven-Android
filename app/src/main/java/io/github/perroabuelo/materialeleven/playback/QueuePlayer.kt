package io.github.perroabuelo.materialeleven.playback

import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer

/**
 * The player the media session exposes. It keeps the queue circular, exposes repeat as "repeat
 * the track" on or off, and starts every shuffled order with the playing track.
 */
@UnstableApi
class QueuePlayer(private val exoPlayer: ExoPlayer) : ForwardingPlayer(exoPlayer) {
    private val wrappedListeners = mutableMapOf<Player.Listener, Player.Listener>()

    override fun getRepeatMode(): Int = QueueModes.exposedRepeatMode(exoPlayer.repeatMode)

    override fun setRepeatMode(repeatMode: Int) {
        exoPlayer.repeatMode = QueueModes.playerRepeatMode(repeatMode)
    }

    override fun setShuffleModeEnabled(shuffleModeEnabled: Boolean) {
        if (shuffleModeEnabled && !exoPlayer.shuffleModeEnabled) reshuffleFromCurrent()
        exoPlayer.shuffleModeEnabled = shuffleModeEnabled
    }

    override fun setMediaItems(mediaItems: MutableList<MediaItem>, startIndex: Int, startPositionMs: Long) {
        exoPlayer.setMediaItems(mediaItems, startIndex, startPositionMs)
        if (exoPlayer.shuffleModeEnabled) reshuffleFromCurrent()
    }

    override fun setMediaItems(mediaItems: MutableList<MediaItem>, resetPosition: Boolean) {
        exoPlayer.setMediaItems(mediaItems, resetPosition)
        if (exoPlayer.shuffleModeEnabled) reshuffleFromCurrent()
    }

    override fun addListener(listener: Player.Listener) {
        val wrapped = wrappedListeners.getOrPut(listener) { RepeatMappingListener(listener) }
        super.addListener(wrapped)
    }

    override fun removeListener(listener: Player.Listener) {
        wrappedListeners.remove(listener)?.let { super.removeListener(it) }
    }

    private fun reshuffleFromCurrent() {
        val size = exoPlayer.mediaItemCount
        if (size == 0) return
        exoPlayer.setShuffleOrder(QueueModes.shuffleOrder(size, exoPlayer.currentMediaItemIndex, System.nanoTime()))
    }

    // Listeners see the exposed repeat mode, never the player's REPEAT_MODE_ALL.
    private class RepeatMappingListener(private val delegate: Player.Listener) : Player.Listener by delegate {
        override fun onRepeatModeChanged(repeatMode: Int) {
            delegate.onRepeatModeChanged(QueueModes.exposedRepeatMode(repeatMode))
        }
    }
}
