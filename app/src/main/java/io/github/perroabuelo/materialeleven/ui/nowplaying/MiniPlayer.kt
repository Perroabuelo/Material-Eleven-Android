package io.github.perroabuelo.materialeleven.ui.nowplaying

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.perroabuelo.materialeleven.R
import io.github.perroabuelo.materialeleven.data.TrackArtwork
import io.github.perroabuelo.materialeleven.playback.TrackItems
import io.github.perroabuelo.materialeleven.ui.components.Artwork
import io.github.perroabuelo.materialeleven.ui.player.PlayerState
import io.github.perroabuelo.materialeleven.ui.theme.ElevenColors
import io.github.perroabuelo.materialeleven.ui.theme.LocalAccent

val MiniPlayerHeight = 68.dp

/** The docked player under the lists: cover, title, artist, play/pause, next and a progress line. */
@Composable
fun MiniPlayer(state: PlayerState, onTogglePlay: () -> Unit, onNext: () -> Unit, modifier: Modifier = Modifier) {
    val accent = LocalAccent.current.accent
    val metadata = state.current?.mediaMetadata
    Box(modifier.fillMaxWidth().height(MiniPlayerHeight)) {
        Row(
            Modifier
                .fillMaxHeight()
                .padding(start = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Artwork(
                TrackItems.artworkOf(state.current),
                sizePx = TrackArtwork.THUMBNAIL_PX,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(44.dp),
            )
            Column(Modifier.weight(1f)) {
                Text(
                    metadata?.title?.toString().orEmpty(),
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    metadata?.artist?.toString().orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = ElevenColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconButton(onClick = onTogglePlay) {
                Icon(
                    painterResource(if (state.isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                    contentDescription = stringResource(if (state.isPlaying) R.string.action_pause else R.string.action_play),
                )
            }
            IconButton(onClick = onNext) {
                Icon(painterResource(R.drawable.ic_skip_next), contentDescription = stringResource(R.string.action_next))
            }
        }
        // Progress line along the top edge.
        Box(
            Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth(state.progress)
                .height(2.dp)
                .background(accent),
        )
    }
}
