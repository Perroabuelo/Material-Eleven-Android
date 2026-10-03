package io.github.perroabuelo.materialeleven.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import io.github.perroabuelo.materialeleven.R
import io.github.perroabuelo.materialeleven.data.TrackArtwork
import io.github.perroabuelo.materialeleven.ui.theme.ElevenColors

/** A track's cover clipped to [shape], with a note placeholder while loading or when it has none. */
@Composable
fun Artwork(artwork: TrackArtwork?, sizePx: Int, shape: Shape, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var loaded by remember(artwork) { mutableStateOf(false) }
    Box(modifier.clip(shape).background(ElevenColors.Surface2), contentAlignment = Alignment.Center) {
        if (!loaded) {
            Icon(
                painterResource(R.drawable.ic_note),
                contentDescription = null,
                tint = ElevenColors.TextTertiary,
                modifier = Modifier.fillMaxWidth(0.45f),
            )
        }
        if (artwork != null) {
            AsyncImage(
                model = remember(artwork, sizePx) {
                    ImageRequest.Builder(context).data(artwork).size(sizePx).crossfade(true).build()
                },
                contentDescription = null,
                contentScale = ContentScale.Crop,
                onSuccess = { loaded = true },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
