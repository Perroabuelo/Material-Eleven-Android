package io.github.perroabuelo.materialeleven.ui.permission

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.perroabuelo.materialeleven.R
import io.github.perroabuelo.materialeleven.ui.theme.ElevenColors
import io.github.perroabuelo.materialeleven.ui.theme.LocalAccent

/** Explains why the app needs the audio permission, with one button to grant it. */
@Composable
fun PermissionScreen(state: AudioPermissionState) {
    val accent = LocalAccent.current
    val blocked = state.status == AudioPermissionStatus.BLOCKED
    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 28.dp, vertical = 24.dp),
    ) {
        Column(
            Modifier.align(Alignment.Center),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                Modifier
                    .size(72.dp)
                    .background(accent.accent, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painterResource(R.drawable.ic_note),
                    contentDescription = null,
                    tint = accent.onAccent,
                    modifier = Modifier.size(40.dp),
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.permission_title), style = MaterialTheme.typography.displaySmall)
            Text(
                stringResource(R.string.permission_body),
                style = MaterialTheme.typography.bodyLarge,
                color = ElevenColors.TextSecondary,
            )
            if (blocked) {
                Text(
                    stringResource(R.string.permission_blocked),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ElevenColors.TextSecondary,
                )
            }
        }
        Button(
            onClick = state.request,
            colors = ButtonDefaults.buttonColors(containerColor = accent.accent, contentColor = accent.onAccent),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(56.dp),
        ) {
            Text(
                stringResource(if (blocked) R.string.permission_open_settings else R.string.permission_allow),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
