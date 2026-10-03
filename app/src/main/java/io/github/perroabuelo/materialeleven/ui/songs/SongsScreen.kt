package io.github.perroabuelo.materialeleven.ui.songs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
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
import io.github.perroabuelo.materialeleven.ui.theme.LocalAccent

@Composable
fun SongsScreen(currentTrackId: Long?, bottomPadding: Dp, onPlay: (tracks: List<Track>, index: Int) -> Unit) {
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
            SongsState.Loading -> CenteredMessage(stringResource(R.string.songs_loading), body = null, loading = true)
            is SongsState.Loaded -> if (current.tracks.isEmpty()) {
                CenteredMessage(stringResource(R.string.songs_empty_title), stringResource(R.string.songs_empty_body))
            } else {
                // Keeps the last row reachable above the mini player.
                LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = bottomPadding)) {
                    itemsIndexed(current.tracks, key = { _, track -> track.id }, contentType = { _, _ -> "song" }) { index, track ->
                        SongRow(track, isCurrent = track.id == currentTrackId) { onPlay(current.tracks, index) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SongRow(track: Track, isCurrent: Boolean, onClick: () -> Unit) {
    val accent = LocalAccent.current.accent
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
            Text(
                track.title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isCurrent) accent else LocalContentColor.current,
                fontWeight = if (isCurrent) FontWeight.Bold else null,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
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

@Composable
private fun CenteredMessage(title: String, body: String?, loading: Boolean = false) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (loading) CircularProgressIndicator(color = LocalAccent.current.accent)
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        if (body != null) {
            Text(body, style = MaterialTheme.typography.bodyLarge, color = ElevenColors.TextSecondary, textAlign = TextAlign.Center)
        }
    }
}
