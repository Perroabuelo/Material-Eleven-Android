package io.github.perroabuelo.materialeleven

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.perroabuelo.materialeleven.ui.nowplaying.MiniPlayerHeight
import io.github.perroabuelo.materialeleven.ui.nowplaying.PlayerActions
import io.github.perroabuelo.materialeleven.ui.nowplaying.PlayerSheet
import io.github.perroabuelo.materialeleven.ui.permission.AudioPermissionStatus
import io.github.perroabuelo.materialeleven.ui.permission.PermissionScreen
import io.github.perroabuelo.materialeleven.ui.permission.rememberAudioPermissionState
import io.github.perroabuelo.materialeleven.ui.player.PlayerEvent
import io.github.perroabuelo.materialeleven.ui.player.PlayerViewModel
import io.github.perroabuelo.materialeleven.ui.songs.SongsScreen
import io.github.perroabuelo.materialeleven.ui.theme.AccentColors
import io.github.perroabuelo.materialeleven.ui.theme.MaterialElevenTheme

class MainActivity : ComponentActivity() {
    // Grows every time the notification asks to open Now Playing.
    private val nowPlayingRequests = mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        // Light system bar icons over the fixed dark background, in any system theme.
        val bars = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) handleIntent(intent)
        setContent {
            val player: PlayerViewModel = viewModel()
            val accentArgb by player.accent.collectAsStateWithLifecycle()
            val accent by animateColorAsState(Color(accentArgb), animationSpec = tween(ACCENT_FADE_MS), label = "accent")
            MaterialElevenTheme(accent = AccentColors.of(accent)) {
                val permission = rememberAudioPermissionState()
                if (permission.status == AudioPermissionStatus.GRANTED) {
                    MusicApp(player, nowPlayingRequests.intValue)
                } else {
                    PermissionScreen(permission)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(EXTRA_OPEN_NOW_PLAYING, false) == true) nowPlayingRequests.intValue++
    }

    companion object {
        /** Set by the notification: opens the app on Now Playing. */
        const val EXTRA_OPEN_NOW_PLAYING = "open_now_playing"

        private const val ACCENT_FADE_MS = 400
    }
}

@Composable
private fun MusicApp(player: PlayerViewModel, nowPlayingRequests: Int) {
    val state by player.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val nothingPlayable = stringResource(R.string.nothing_playable)
    LaunchedEffect(player) {
        player.events.collect { event ->
            when (event) {
                PlayerEvent.NOTHING_PLAYABLE -> snackbar.showSnackbar(nothingPlayable)
            }
        }
    }
    val actions = remember(player) {
        PlayerActions(
            togglePlay = player::togglePlay,
            next = player::next,
            previous = player::previous,
            seekTo = player::seekTo,
            toggleShuffle = player::toggleShuffle,
            toggleRepeat = player::toggleRepeat,
            skipTo = player::skipTo,
        )
    }
    val miniPlayerSpace = if (state.hasQueue) MiniPlayerHeight else 0.dp

    Box(Modifier.fillMaxSize()) {
        SongsScreen(
            currentTrackId = state.currentTrackId,
            bottomPadding = miniPlayerSpace,
            onPlay = player::playQueue,
        )
        SnackbarHost(
            snackbar,
            Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = miniPlayerSpace),
        )
        if (state.hasQueue) PlayerSheet(state, actions, nowPlayingRequests)
    }
}
