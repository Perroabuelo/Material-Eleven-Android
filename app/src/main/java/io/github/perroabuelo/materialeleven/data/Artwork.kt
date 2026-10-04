package io.github.perroabuelo.materialeleven.data

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Build
import android.util.Size
import androidx.core.net.toUri
import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DataSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.ImageFetchResult
import coil3.key.Keyer
import coil3.request.Options
import coil3.size.Dimension
import coil3.size.pxOrElse

/** The cover art of a track, loaded through Coil. */
data class TrackArtwork(val trackId: Long, val albumId: Long) {
    companion object {
        /** Size for list thumbnails. */
        const val THUMBNAIL_PX = 128

        /** Size for the large cover on Now Playing. */
        const val LARGE_PX = 1024

        /** Size the accent colour is computed from. */
        const val ACCENT_PX = 96
    }
}

/** Caches by track and requested size, so a thumbnail never stands in for the large cover. */
class TrackArtworkKeyer : Keyer<TrackArtwork> {
    override fun key(data: TrackArtwork, options: Options): String {
        val width = options.size.width.let { if (it is Dimension.Pixels) it.px else 0 }
        return "track-art:${data.trackId}:$width"
    }
}

class TrackArtworkFetcher(
    private val context: Context,
    private val data: TrackArtwork,
    private val options: Options,
) : Fetcher {
    override suspend fun fetch(): FetchResult? {
        val px = options.size.width.pxOrElse { TrackArtwork.LARGE_PX }
        val bitmap = loadArtwork(context, data, px) ?: return null
        return ImageFetchResult(image = bitmap.asImage(), isSampled = true, dataSource = DataSource.DISK)
    }

    class Factory(private val context: Context) : Fetcher.Factory<TrackArtwork> {
        override fun create(data: TrackArtwork, options: Options, imageLoader: ImageLoader): Fetcher =
            TrackArtworkFetcher(context.applicationContext, data, options)
    }
}

/**
 * The cover of a track at about [px] pixels, or null when it has none. From API 29 the system
 * extracts the embedded or album cover; before it, the album art table is tried first and the
 * embedded picture second.
 */
fun loadArtwork(context: Context, artwork: TrackArtwork, px: Int): Bitmap? {
    val resolver = context.contentResolver
    val trackUri = MediaStoreSource.uriOf(artwork.trackId)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        return runCatching { resolver.loadThumbnail(trackUri, Size(px, px), null) }.getOrNull()
    }
    val albumArt = ContentUris.withAppendedId("content://media/external/audio/albumart".toUri(), artwork.albumId)
    runCatching { resolver.openInputStream(albumArt)?.use { decodeSampled(it.readBytes(), px) } }
        .getOrNull()?.let { return it }
    return runCatching {
        MediaMetadataRetriever().run {
            try {
                setDataSource(context, trackUri)
                embeddedPicture?.let { decodeSampled(it, px) }
            } finally {
                release()
            }
        }
    }.getOrNull()
}

private fun decodeSampled(bytes: ByteArray, px: Int): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
    var sample = 1
    while (bounds.outWidth / (sample * 2) >= px && bounds.outHeight / (sample * 2) >= px) sample *= 2
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample })
}
