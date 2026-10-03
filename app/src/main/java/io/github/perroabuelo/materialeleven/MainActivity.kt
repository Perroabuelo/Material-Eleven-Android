package io.github.perroabuelo.materialeleven

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.perroabuelo.materialeleven.ui.theme.ElevenColors
import io.github.perroabuelo.materialeleven.ui.theme.ElevenTextStyles
import io.github.perroabuelo.materialeleven.ui.theme.LocalAccent
import io.github.perroabuelo.materialeleven.ui.theme.MaterialElevenTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Light system bar icons over the fixed dark background, in any system theme.
        val bars = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)
        super.onCreate(savedInstanceState)
        setContent {
            MaterialElevenTheme {
                ThemePreview()
            }
        }
    }
}

// Temporary screen that shows the theme until the song list exists.
@Composable
private fun ThemePreview() {
    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Spacer(Modifier.height(16.dp))
            Text("Material Eleven", style = MaterialTheme.typography.displaySmall)
            Text("Songs", style = MaterialTheme.typography.titleLarge)
            Text("Body text in Manrope", style = MaterialTheme.typography.bodyLarge)
            Text("Secondary text", style = MaterialTheme.typography.bodyMedium, color = ElevenColors.TextSecondary)
            Text("Tertiary text", style = MaterialTheme.typography.bodySmall, color = ElevenColors.TextTertiary)
            Text("1:23  -2:37", style = ElevenTextStyles.Time, color = ElevenColors.TextSecondary)
            Text("音楽 · Музыка · 음악", style = MaterialTheme.typography.bodyLarge)
        }
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(56.dp)
                .background(LocalAccent.current.accent, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text("Bottom edge", color = LocalAccent.current.onAccent, style = MaterialTheme.typography.labelLarge)
        }
    }
}
