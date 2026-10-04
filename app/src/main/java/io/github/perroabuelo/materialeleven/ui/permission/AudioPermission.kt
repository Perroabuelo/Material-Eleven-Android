package io.github.perroabuelo.materialeleven.ui.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

/** The only storage permission the app asks for: audio files, and nothing else. */
val audioPermission: String =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

/** Where the permission stands, as the permission screen needs it. */
enum class AudioPermissionStatus {
    GRANTED,

    /** Not granted, and the system dialog can still be shown. */
    ASKABLE,

    /** Denied, and the system no longer shows the dialog: only the app's settings can grant it. */
    BLOCKED,
}

class AudioPermissionState(
    val status: AudioPermissionStatus,
    /** Shows the system dialog, or opens the app's settings when the dialog is blocked. */
    val request: () -> Unit,
)

private const val PREFS = "permission"
private const val KEY_DENIED_ONCE = "audio_denied_once"

/**
 * The audio permission, re-checked every time the app comes back to the foreground, so granting
 * it from the system settings is enough.
 */
@Composable
fun rememberAudioPermissionState(): AudioPermissionState {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val prefs = remember(context) { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE) }
    var status by remember { mutableStateOf(currentStatus(activity, prefs.getBoolean(KEY_DENIED_ONCE, false))) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted) prefs.edit { putBoolean(KEY_DENIED_ONCE, true) }
        status = currentStatus(activity, prefs.getBoolean(KEY_DENIED_ONCE, false))
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        status = currentStatus(activity, prefs.getBoolean(KEY_DENIED_ONCE, false))
    }

    return AudioPermissionState(status) {
        when (status) {
            AudioPermissionStatus.GRANTED -> Unit
            AudioPermissionStatus.ASKABLE -> launcher.launch(audioPermission)
            AudioPermissionStatus.BLOCKED -> activity.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", activity.packageName, null)),
            )
        }
    }
}

// Before the first denial the rationale flag is also false, so a stored "denied once" tells the
// two cases apart.
private fun currentStatus(activity: Activity, deniedOnce: Boolean): AudioPermissionStatus = when {
    ContextCompat.checkSelfPermission(activity, audioPermission) == PackageManager.PERMISSION_GRANTED ->
        AudioPermissionStatus.GRANTED
    deniedOnce && !activity.shouldShowRequestPermissionRationale(audioPermission) ->
        AudioPermissionStatus.BLOCKED
    else -> AudioPermissionStatus.ASKABLE
}

private tailrec fun Context.findActivity(): Activity = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> error("No activity in this context")
}
