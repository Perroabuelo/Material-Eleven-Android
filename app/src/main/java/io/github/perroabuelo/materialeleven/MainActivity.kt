package io.github.perroabuelo.materialeleven

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.perroabuelo.materialeleven.ui.permission.AudioPermissionStatus
import io.github.perroabuelo.materialeleven.ui.permission.PermissionScreen
import io.github.perroabuelo.materialeleven.ui.permission.rememberAudioPermissionState
import io.github.perroabuelo.materialeleven.ui.player.PlayerViewModel
import io.github.perroabuelo.materialeleven.ui.songs.SongsScreen
import io.github.perroabuelo.materialeleven.ui.theme.AccentColors
import io.github.perroabuelo.materialeleven.ui.theme.MaterialElevenTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Light system bar icons over the fixed dark background, in any system theme.
        val bars = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)
        super.onCreate(savedInstanceState)
        setContent {
            val player: PlayerViewModel = viewModel()
            val accentArgb by player.accent.collectAsStateWithLifecycle()
            val accent by animateColorAsState(Color(accentArgb), animationSpec = tween(ACCENT_FADE_MS), label = "accent")
            MaterialElevenTheme(accent = AccentColors.of(accent)) {
                val permission = rememberAudioPermissionState()
                if (permission.status == AudioPermissionStatus.GRANTED) {
                    val playerState by player.state.collectAsStateWithLifecycle()
                    SongsScreen(currentTrackId = playerState.currentTrackId, onPlay = player::playQueue)
                } else {
                    PermissionScreen(permission)
                }
            }
        }
    }

    companion object {
        /** Set by the notification: opens the app on Now Playing. */
        const val EXTRA_OPEN_NOW_PLAYING = "open_now_playing"

        private const val ACCENT_FADE_MS = 400
    }
}
