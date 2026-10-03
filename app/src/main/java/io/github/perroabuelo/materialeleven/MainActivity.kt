package io.github.perroabuelo.materialeleven

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.perroabuelo.materialeleven.ui.permission.AudioPermissionStatus
import io.github.perroabuelo.materialeleven.ui.permission.PermissionScreen
import io.github.perroabuelo.materialeleven.ui.permission.rememberAudioPermissionState
import io.github.perroabuelo.materialeleven.ui.player.PlayerViewModel
import io.github.perroabuelo.materialeleven.ui.songs.SongsScreen
import io.github.perroabuelo.materialeleven.ui.theme.MaterialElevenTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Light system bar icons over the fixed dark background, in any system theme.
        val bars = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)
        super.onCreate(savedInstanceState)
        setContent {
            MaterialElevenTheme {
                val permission = rememberAudioPermissionState()
                if (permission.status == AudioPermissionStatus.GRANTED) {
                    val player: PlayerViewModel = viewModel()
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
    }
}
