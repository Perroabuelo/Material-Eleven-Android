package io.github.perroabuelo.materialeleven.playback

import androidx.media3.common.C
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import io.github.perroabuelo.materialeleven.core.queue.Direction
import io.github.perroabuelo.materialeleven.core.queue.PlaybackOrder
import io.github.perroabuelo.materialeleven.core.queue.SkipPolicy

/**
 * The player the media session exposes. It keeps the queue circular (repeat off is REPEAT_MODE_ALL,
 * which controllers see as such; repeat on is REPEAT_MODE_ONE), starts every shuffled order with the
 * playing track, and skips tracks that cannot be opened, calling [onNothingPlayable] when a whole
 * lap fails.
 */
@UnstableApi
class QueuePlayer(
    private val exoPlayer: ExoPlayer,
    private val onNothingPlayable: () -> Unit,
) : ForwardingPlayer(exoPlayer) {
    private var direction = Direction.FORWARD
    private var consecutiveFailures = 0

    init {
        exoPlayer.addListener(
            object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) = skipUnplayable()

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) consecutiveFailures = 0
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    // A track that ends on its own moves forward.
                    if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) direction = Direction.FORWARD
                }
            },
        )
    }

    override fun seekToNext() {
        direction = Direction.FORWARD
        super.seekToNext()
    }

    override fun seekToNextMediaItem() {
        direction = Direction.FORWARD
        super.seekToNextMediaItem()
    }

    override fun seekToPrevious() {
        direction = Direction.BACKWARD
        super.seekToPrevious()
    }

    override fun seekToPreviousMediaItem() {
        direction = Direction.BACKWARD
        super.seekToPreviousMediaItem()
    }

    override fun seekTo(mediaItemIndex: Int, positionMs: Long) {
        direction = Direction.FORWARD
        super.seekTo(mediaItemIndex, positionMs)
    }

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

    private fun skipUnplayable() {
        val count = exoPlayer.mediaItemCount
        if (count == 0) return
        consecutiveFailures++
        val next = SkipPolicy.next(currentOrder(), exoPlayer.currentMediaItemIndex, direction, consecutiveFailures)
        if (next == null) {
            consecutiveFailures = 0
            exoPlayer.stop()
            onNothingPlayable()
            return
        }
        exoPlayer.seekTo(next, 0)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    // The order in effect, natural or shuffled, read from the timeline.
    private fun currentOrder(): PlaybackOrder {
        val timeline = exoPlayer.currentTimeline
        val shuffle = exoPlayer.shuffleModeEnabled
        val items = IntArray(timeline.windowCount)
        var index = timeline.getFirstWindowIndex(shuffle)
        var position = 0
        while (index != C.INDEX_UNSET && position < items.size) {
            items[position++] = index
            index = timeline.getNextWindowIndex(index, Player.REPEAT_MODE_OFF, shuffle)
        }
        return PlaybackOrder(items)
    }

    private fun reshuffleFromCurrent() {
        val size = exoPlayer.mediaItemCount
        if (size == 0) return
        exoPlayer.setShuffleOrder(QueueModes.shuffleOrder(size, exoPlayer.currentMediaItemIndex, System.nanoTime()))
    }
}
