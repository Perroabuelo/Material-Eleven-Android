package io.github.perroabuelo.materialeleven.ui.player

import android.app.Application
import android.content.ComponentName
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import io.github.perroabuelo.materialeleven.R
import io.github.perroabuelo.materialeleven.core.library.Track
import io.github.perroabuelo.materialeleven.core.queue.PlaybackOrder
import io.github.perroabuelo.materialeleven.playback.PlaybackEvents
import io.github.perroabuelo.materialeleven.playback.PlaybackService
import io.github.perroabuelo.materialeleven.playback.TrackItems
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** What the UI shows of playback. */
data class PlayerState(
    val current: MediaItem? = null,
    val currentIndex: Int = C.INDEX_UNSET,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0,
    val durationMs: Long = C.TIME_UNSET,
    val shuffle: Boolean = false,
    val repeatTrack: Boolean = false,
    /** The next items in the order they will play, wrapping around. */
    val upNext: List<IndexedItem> = emptyList(),
) {
    val hasQueue: Boolean get() = current != null
    val currentTrackId: Long? get() = current?.mediaId?.toLongOrNull()
}

data class IndexedItem(val index: Int, val item: MediaItem)

enum class PlayerEvent { NOTHING_PLAYABLE }

/** Connects the UI to the playback service through a media controller. */
@OptIn(UnstableApi::class)
class PlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    private val _events = Channel<PlayerEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var controller: MediaController? = null
    private val controllerFuture: ListenableFuture<MediaController>
    private var positionJob: Job? = null

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) = refresh()
    }

    init {
        val token = SessionToken(application, ComponentName(application, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(application, token)
            .setListener(
                object : MediaController.Listener {
                    override fun onCustomCommand(
                        controller: MediaController,
                        command: SessionCommand,
                        args: Bundle,
                    ): ListenableFuture<SessionResult> {
                        if (command.customAction == PlaybackEvents.NOTHING_PLAYABLE.customAction) {
                            _events.trySend(PlayerEvent.NOTHING_PLAYABLE)
                        }
                        return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
                    }
                },
            )
            .buildAsync()
        controllerFuture.addListener(
            {
                controller = controllerFuture.get().also { it.addListener(listener) }
                refresh()
            },
            ContextCompat.getMainExecutor(application),
        )
    }

    /** Replaces the queue with [tracks], in their order, and plays the one at [index]. */
    fun playQueue(tracks: List<Track>, index: Int) {
        val player = controller ?: return
        val unknown = getApplication<Application>().getString(R.string.unknown)
        player.setMediaItems(tracks.map { TrackItems.mediaItem(it, unknown) }, index, 0)
        player.prepare()
        player.play()
    }

    fun togglePlay() {
        val player = controller ?: return
        if (player.isPlaying) {
            player.pause()
        } else {
            if (player.playbackState == Player.STATE_IDLE) player.prepare()
            player.play()
        }
    }

    fun next() = controller?.seekToNext() ?: Unit

    fun previous() = controller?.seekToPrevious() ?: Unit

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
        refresh()
    }

    fun skipTo(index: Int) = controller?.seekTo(index, 0) ?: Unit

    fun toggleShuffle() {
        val player = controller ?: return
        player.shuffleModeEnabled = !player.shuffleModeEnabled
    }

    fun toggleRepeat() {
        val player = controller ?: return
        player.repeatMode = if (player.repeatMode == Player.REPEAT_MODE_ONE) Player.REPEAT_MODE_OFF else Player.REPEAT_MODE_ONE
    }

    private fun refresh() {
        val player = controller ?: return
        _state.value = PlayerState(
            current = player.currentMediaItem,
            currentIndex = player.currentMediaItemIndex,
            isPlaying = player.isPlaying,
            positionMs = player.currentPosition,
            durationMs = player.duration,
            shuffle = player.shuffleModeEnabled,
            repeatTrack = player.repeatMode == Player.REPEAT_MODE_ONE,
            upNext = upNext(player),
        )
        updatePositionTicker(player.isPlaying)
    }

    private fun updatePositionTicker(playing: Boolean) {
        if (!playing) {
            positionJob?.cancel()
            positionJob = null
            return
        }
        if (positionJob?.isActive == true) return
        positionJob = viewModelScope.launch {
            while (isActive) {
                delay(POSITION_TICK_MS)
                controller?.let { _state.value = _state.value.copy(positionMs = it.currentPosition) }
            }
        }
    }

    private fun upNext(player: Player): List<IndexedItem> {
        val timeline = player.currentTimeline
        if (timeline.isEmpty || player.currentMediaItemIndex == C.INDEX_UNSET) return emptyList()
        val order = orderOf(timeline, player.shuffleModeEnabled)
        val window = Timeline.Window()
        return order.upNext(player.currentMediaItemIndex, UP_NEXT_COUNT).map { index ->
            IndexedItem(index, timeline.getWindow(index, window).mediaItem)
        }
    }

    private fun orderOf(timeline: Timeline, shuffle: Boolean): PlaybackOrder {
        val items = IntArray(timeline.windowCount)
        var index = timeline.getFirstWindowIndex(shuffle)
        var position = 0
        while (index != C.INDEX_UNSET && position < items.size) {
            items[position++] = index
            index = timeline.getNextWindowIndex(index, Player.REPEAT_MODE_OFF, shuffle)
        }
        return PlaybackOrder(items)
    }

    override fun onCleared() {
        controller?.removeListener(listener)
        MediaController.releaseFuture(controllerFuture)
    }

    private companion object {
        const val POSITION_TICK_MS = 250L
        const val UP_NEXT_COUNT = 8
    }
}
