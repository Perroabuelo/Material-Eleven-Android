package io.github.perroabuelo.materialeleven.ui.nowplaying

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.perroabuelo.materialeleven.R
import io.github.perroabuelo.materialeleven.core.library.AudioFormat
import io.github.perroabuelo.materialeleven.core.library.FormatFamily
import io.github.perroabuelo.materialeleven.core.time.PlaybackTime
import io.github.perroabuelo.materialeleven.data.TrackArtwork
import io.github.perroabuelo.materialeleven.playback.TrackItems
import io.github.perroabuelo.materialeleven.ui.components.Artwork
import io.github.perroabuelo.materialeleven.ui.components.FormatBadge
import io.github.perroabuelo.materialeleven.ui.player.IndexedItem
import io.github.perroabuelo.materialeleven.ui.player.PlayerState
import io.github.perroabuelo.materialeleven.ui.theme.ElevenColors
import io.github.perroabuelo.materialeleven.ui.theme.ElevenTextStyles
import io.github.perroabuelo.materialeleven.ui.theme.LocalAccent

private const val COVER_WIDTH_FRACTION = 0.85f

/** What Now Playing can ask the player to do. */
class PlayerActions(
    val togglePlay: () -> Unit,
    val next: () -> Unit,
    val previous: () -> Unit,
    val seekTo: (Long) -> Unit,
    val toggleShuffle: () -> Unit,
    val toggleRepeat: () -> Unit,
    val skipTo: (Int) -> Unit,
)

/**
 * The full player. [dragHandle] goes on the part that drags the sheet down, so the up-next list
 * keeps its own scrolling.
 */
@Suppress("ModifierParameter") // dragHandle applies to a part of the screen, not to the whole.
@Composable
fun NowPlayingScreen(state: PlayerState, actions: PlayerActions, onCollapse: () -> Unit, dragHandle: Modifier) {
    val metadata = state.current?.mediaMetadata
    Column(Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        Column(dragHandle) {
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onCollapse) {
                    Icon(painterResource(R.drawable.ic_expand_more), contentDescription = stringResource(R.string.action_collapse))
                }
                Text(
                    stringResource(R.string.now_playing),
                    style = MaterialTheme.typography.labelLarge,
                    color = ElevenColors.TextSecondary,
                )
            }
            Spacer(Modifier.height(12.dp))
            // A bit narrower than the screen, so the up-next list keeps a few rows in view.
            Artwork(
                TrackItems.artworkOf(state.current),
                sizePx = TrackArtwork.LARGE_PX,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth(COVER_WIDTH_FRACTION).aspectRatio(1f).align(Alignment.CenterHorizontally),
            )
            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    metadata?.title?.toString().orEmpty(),
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                formatOf(state)?.let { FormatBadge(it) }
            }
            Text(
                metadata?.artist?.toString().orEmpty(),
                style = MaterialTheme.typography.bodyLarge,
                color = LocalAccent.current.accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                metadata?.albumTitle?.toString().orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = ElevenColors.TextTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        SeekBar(state, actions.seekTo)
        Transport(state, actions)
        Text(
            stringResource(R.string.up_next),
            style = MaterialTheme.typography.titleSmall,
            color = ElevenColors.TextSecondary,
            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
        )
        LazyColumn(Modifier.weight(1f)) {
            items(state.upNext, key = { "${it.index}" }) { UpNextRow(it) { actions.skipTo(it.index) } }
        }
    }
}

@Composable
private fun SeekBar(state: PlayerState, onSeek: (Long) -> Unit) {
    val accent = LocalAccent.current.accent
    val duration = state.durationMs.takeIf { it > 0 }
    // While dragging, the bar and the elapsed time show the target; the seek happens on release.
    var dragFraction by remember { mutableStateOf<Float?>(null) }
    val fraction = dragFraction ?: state.progress
    val shownPosition = if (dragFraction != null && duration != null) (dragFraction!! * duration).toLong() else state.positionMs
    Column(Modifier.padding(top = 12.dp)) {
        Slider(
            value = fraction,
            onValueChange = { dragFraction = it },
            onValueChangeFinished = {
                val target = dragFraction
                if (target != null && duration != null) onSeek((target * duration).toLong())
                dragFraction = null
            },
            enabled = duration != null,
            colors = SliderDefaults.colors(
                thumbColor = accent,
                activeTrackColor = accent,
                inactiveTrackColor = ElevenColors.Surface2,
            ),
        )
        Row(Modifier.fillMaxWidth()) {
            Text(PlaybackTime.format(shownPosition), style = ElevenTextStyles.Time, color = ElevenColors.TextSecondary)
            Spacer(Modifier.weight(1f))
            Text(
                duration?.let { PlaybackTime.remaining(shownPosition, it) } ?: PlaybackTime.UNKNOWN,
                style = ElevenTextStyles.Time,
                color = ElevenColors.TextSecondary,
            )
        }
    }
}

@Composable
private fun Transport(state: PlayerState, actions: PlayerActions) {
    val accent = LocalAccent.current
    Row(
        Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ToggleButton(R.drawable.ic_shuffle, R.string.action_shuffle, state.shuffle, actions.toggleShuffle)
        IconButton(onClick = actions.previous, modifier = Modifier.size(56.dp)) {
            Icon(painterResource(R.drawable.ic_skip_previous), stringResource(R.string.action_previous), Modifier.size(32.dp))
        }
        Box(
            Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(accent.accent)
                .clickable(onClick = actions.togglePlay),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(if (state.isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                contentDescription = stringResource(if (state.isPlaying) R.string.action_pause else R.string.action_play),
                tint = accent.onAccent,
                modifier = Modifier.size(40.dp),
            )
        }
        IconButton(onClick = actions.next, modifier = Modifier.size(56.dp)) {
            Icon(painterResource(R.drawable.ic_skip_next), stringResource(R.string.action_next), Modifier.size(32.dp))
        }
        ToggleButton(
            if (state.repeatTrack) R.drawable.ic_repeat_one else R.drawable.ic_repeat,
            R.string.action_repeat,
            state.repeatTrack,
            actions.toggleRepeat,
        )
    }
}

@Composable
private fun ToggleButton(icon: Int, label: Int, active: Boolean, onClick: () -> Unit) {
    val accent = LocalAccent.current.accent
    IconButton(onClick = onClick, modifier = Modifier.size(56.dp)) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                painterResource(icon),
                contentDescription = stringResource(label),
                tint = if (active) accent else ElevenColors.TextSecondary,
                modifier = Modifier.size(26.dp),
            )
            // A dot under an active toggle, so the state does not rely on colour alone.
            if (active) {
                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp)
                        .size(4.dp)
                        .background(accent, CircleShape),
                )
            }
        }
    }
}

@Composable
private fun UpNextRow(entry: IndexedItem, onClick: () -> Unit) {
    val metadata = entry.item.mediaMetadata
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Artwork(
            TrackItems.artworkOf(entry.item),
            sizePx = TrackArtwork.THUMBNAIL_PX,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.size(40.dp),
        )
        Column(Modifier.weight(1f)) {
            Text(metadata.title?.toString().orEmpty(), style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                metadata.artist?.toString().orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = ElevenColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun formatOf(state: PlayerState): AudioFormat? {
    val extras = state.current?.mediaMetadata?.extras ?: return null
    val label = extras.getString(TrackItems.EXTRA_FORMAT) ?: return null
    val family = extras.getString(TrackItems.EXTRA_FORMAT_FAMILY)
        ?.let { runCatching { FormatFamily.valueOf(it) }.getOrNull() } ?: FormatFamily.OTHER
    return AudioFormat(label, family)
}
