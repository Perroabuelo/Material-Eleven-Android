package io.github.perroabuelo.materialeleven.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.perroabuelo.materialeleven.core.library.AudioFormat
import io.github.perroabuelo.materialeleven.core.library.FormatFamily
import io.github.perroabuelo.materialeleven.ui.theme.ElevenColors
import io.github.perroabuelo.materialeleven.ui.theme.ElevenTextStyles
import io.github.perroabuelo.materialeleven.ui.theme.MaterialElevenTheme

/** The format name in its family's colour over a faint wash of that colour, without an outline. */
@Composable
fun FormatBadge(format: AudioFormat, modifier: Modifier = Modifier) {
    Text(
        text = format.label,
        style = ElevenTextStyles.Badge,
        color = ElevenColors.formatColor(format.family),
        maxLines = 1,
        overflow = TextOverflow.Clip,
        modifier = modifier
            .background(ElevenColors.formatWash(format.family), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

@Preview(backgroundColor = 0xFF120F17, showBackground = true)
@Composable
private fun FormatBadgePreview() {
    MaterialElevenTheme {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FormatBadge(AudioFormat("FLAC", FormatFamily.LOSSLESS))
            FormatBadge(AudioFormat("MP3", FormatFamily.LOSSY))
            FormatBadge(AudioFormat("MKA", FormatFamily.OTHER))
        }
    }
}
