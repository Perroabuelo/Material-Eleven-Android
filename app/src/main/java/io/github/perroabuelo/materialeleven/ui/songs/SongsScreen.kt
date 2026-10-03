package io.github.perroabuelo.materialeleven.ui.songs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.perroabuelo.materialeleven.R
import io.github.perroabuelo.materialeleven.core.library.Track
import io.github.perroabuelo.materialeleven.data.MediaStoreSource
import io.github.perroabuelo.materialeleven.data.TrackArtwork
import io.github.perroabuelo.materialeleven.ui.components.Artwork
import io.github.perroabuelo.materialeleven.ui.components.FormatBadge
import io.github.perroabuelo.materialeleven.ui.theme.ElevenColors

@Composable
fun SongsScreen() {
    val context = LocalContext.current
    val viewModel: SongsViewModel = viewModel { SongsViewModel(MediaStoreSource(context)) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Text(
            stringResource(R.string.songs),
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
        )
        when (val current = state) {
            SongsState.Loading -> Unit
            is SongsState.Loaded -> LazyColumn(Modifier.fillMaxSize()) {
                items(current.tracks, key = { it.id }) { SongRow(it) }
            }
        }
    }
}

@Composable
private fun SongRow(track: Track) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Artwork(
            TrackArtwork(track.id, track.albumId),
            sizePx = TrackArtwork.THUMBNAIL_PX,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.size(48.dp),
        )
        Column(Modifier.weight(1f)) {
            Text(track.title, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                track.artist ?: stringResource(R.string.unknown),
                style = MaterialTheme.typography.bodyMedium,
                color = ElevenColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        FormatBadge(track.format)
    }
}
