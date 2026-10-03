package io.github.perroabuelo.materialeleven.playback

import android.net.Uri
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import io.github.perroabuelo.materialeleven.core.library.Track
import io.github.perroabuelo.materialeleven.data.MediaStoreSource
import io.github.perroabuelo.materialeleven.data.TrackArtwork

/** How tracks travel through the media session: the media id is the MediaStore id. */
object TrackItems {
    private const val ARTWORK_SCHEME = "me-artwork"
    const val EXTRA_FORMAT = "format"
    const val EXTRA_FORMAT_FAMILY = "format_family"

    /**
     * A queue item for [track]. [unknownLabel] fills missing artist and album, so the notification
     * and the lock screen show the same text as the app.
     */
    fun mediaItem(track: Track, unknownLabel: String): MediaItem = MediaItem.Builder()
        .setMediaId(track.id.toString())
        .setUri(MediaStoreSource.uriOf(track.id))
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(track.title)
                .setArtist(track.artist ?: unknownLabel)
                .setAlbumTitle(track.album ?: unknownLabel)
                .setDurationMs(track.durationMs)
                .setArtworkUri(artworkUri(TrackArtwork(track.id, track.albumId)))
                .setExtras(
                    android.os.Bundle().apply {
                        putString(EXTRA_FORMAT, track.format.label)
                        putString(EXTRA_FORMAT_FAMILY, track.format.family.name)
                    },
                )
                .build(),
        )
        .build()

    /** Restores the playable URI, which does not survive the trip from a controller. */
    fun withUri(item: MediaItem): MediaItem {
        val id = item.mediaId.toLongOrNull() ?: return item
        return item.buildUpon().setUri(MediaStoreSource.uriOf(id)).build()
    }

    fun artworkUri(artwork: TrackArtwork): Uri =
        "$ARTWORK_SCHEME://track/${artwork.trackId}?album=${artwork.albumId}".toUri()

    fun artworkOf(uri: Uri?): TrackArtwork? {
        if (uri?.scheme != ARTWORK_SCHEME) return null
        val trackId = uri.lastPathSegment?.toLongOrNull() ?: return null
        val albumId = uri.getQueryParameter("album")?.toLongOrNull() ?: 0L
        return TrackArtwork(trackId, albumId)
    }

    fun artworkOf(item: MediaItem?): TrackArtwork? = artworkOf(item?.mediaMetadata?.artworkUri)
}
