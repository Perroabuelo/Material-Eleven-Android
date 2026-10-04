package io.github.perroabuelo.materialeleven.playback

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.media3.common.util.BitmapLoader
import androidx.media3.common.util.UnstableApi
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.ListeningExecutorService
import com.google.common.util.concurrent.MoreExecutors
import io.github.perroabuelo.materialeleven.data.TrackArtwork
import io.github.perroabuelo.materialeleven.data.loadArtwork
import java.io.IOException
import java.util.concurrent.Executors

/** Loads the covers the notification and the lock screen show, from the app's artwork URIs. */
@UnstableApi
class ArtworkBitmapLoader(context: Context) : BitmapLoader {
    private val context = context.applicationContext
    private val executor: ListeningExecutorService =
        MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor())

    override fun supportsMimeType(mimeType: String): Boolean = mimeType.startsWith("image/")

    override fun decodeBitmap(data: ByteArray): ListenableFuture<Bitmap> = executor.submit<Bitmap> {
        BitmapFactory.decodeByteArray(data, 0, data.size) ?: throw IOException("Undecodable artwork")
    }

    override fun loadBitmap(uri: Uri): ListenableFuture<Bitmap> = executor.submit<Bitmap> {
        val artwork = TrackItems.artworkOf(uri) ?: throw IOException("Unsupported artwork $uri")
        loadArtwork(context, artwork, NOTIFICATION_PX) ?: throw IOException("No artwork for $uri")
    }

    private companion object {
        const val NOTIFICATION_PX = TrackArtwork.LARGE_PX / 2
    }
}
