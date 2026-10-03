package io.github.perroabuelo.materialeleven.playback

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CacheBitmapLoader
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import io.github.perroabuelo.materialeleven.MainActivity
import io.github.perroabuelo.materialeleven.R

/**
 * Owns the player and the media session. The app, the notification, the lock screen and
 * headphone buttons all control playback through this session.
 */
@UnstableApi
class PlaybackService : MediaSessionService() {
    private var session: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        // Leave the foreground as soon as playback pauses, so the notification can be dismissed.
        setForegroundServiceTimeoutMs(0)
        setMediaNotificationProvider(
            DefaultMediaNotificationProvider.Builder(this)
                .setChannelName(R.string.playback_channel)
                .build()
                .apply { setSmallIcon(R.drawable.ic_note) },
        )
        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                /* handleAudioFocus = */ true,
            )
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            // Previous restarts the track after its first 3 seconds, as Android players do.
            .setMaxSeekToPreviousPositionMs(MAX_SEEK_TO_PREVIOUS_MS)
            .build()
            .apply { repeatMode = ExoPlayer.REPEAT_MODE_ALL }

        val openNowPlaying = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra(MainActivity.EXTRA_OPEN_NOW_PLAYING, true),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val queuePlayer = QueuePlayer(player) {
            session?.broadcastCustomCommand(PlaybackEvents.NOTHING_PLAYABLE, Bundle.EMPTY)
        }
        session = MediaSession.Builder(this, queuePlayer)
            .setSessionActivity(openNowPlaying)
            .setBitmapLoader(CacheBitmapLoader(ArtworkBitmapLoader(this)))
            .setCallback(SessionCallback())
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    // Removing the app from recents while paused, or with nothing queued, ends the service and
    // its notification. While music plays, it keeps playing.
    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = session?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            pauseAllPlayersAndStopSelf()
        }
    }

    override fun onDestroy() {
        session?.run {
            player.release()
            release()
        }
        session = null
        super.onDestroy()
    }

    private class SessionCallback : MediaSession.Callback {
        // Items sent by a controller lose their URI on the way; the media id restores it.
        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>,
        ): ListenableFuture<MutableList<MediaItem>> =
            Futures.immediateFuture(mediaItems.map(TrackItems::withUri).toMutableList())
    }

    private companion object {
        const val MAX_SEEK_TO_PREVIOUS_MS = 3_000L
    }
}
