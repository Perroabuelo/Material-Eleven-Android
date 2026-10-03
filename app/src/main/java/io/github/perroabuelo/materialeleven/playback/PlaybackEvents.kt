package io.github.perroabuelo.materialeleven.playback

import android.os.Bundle
import androidx.media3.session.SessionCommand

/** Events the playback service sends to the app's controllers. */
object PlaybackEvents {
    /** No track of the queue could be opened; playback stopped. */
    val NOTHING_PLAYABLE = SessionCommand("io.github.perroabuelo.materialeleven.NOTHING_PLAYABLE", Bundle.EMPTY)
}
