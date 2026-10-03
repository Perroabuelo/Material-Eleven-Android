package io.github.perroabuelo.materialeleven

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import io.github.perroabuelo.materialeleven.data.TrackArtworkFetcher
import io.github.perroabuelo.materialeleven.data.TrackArtworkKeyer

class ElevenApplication : Application(), SingletonImageLoader.Factory {
    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components {
                add(TrackArtworkKeyer())
                add(TrackArtworkFetcher.Factory(context))
            }
            .build()
}
