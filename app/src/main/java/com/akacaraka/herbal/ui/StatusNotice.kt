package com.akacaraka.herbal.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

internal enum class StatusTone { Success, Warning, Error, Info }

@Composable
internal fun StatusNotice(message: String, tone: StatusTone, modifier: Modifier = Modifier) {
    val displayMessage = userFacingNotice(message, tone)
    val labeledMessage = "${statusLabel(tone)}: $displayMessage"
    val background = statusBackground(tone)
    val foreground = statusForeground(tone)
    Surface(
        modifier
            .fillMaxWidth()
            .semantics { liveRegion = if (tone == StatusTone.Error || tone == StatusTone.Warning) LiveRegionMode.Assertive else LiveRegionMode.Polite },
        color = background,
        shape = HerbRadius.Sm,
        shadowElevation = 1.dp
    ) {
        Row(Modifier.padding(HerbSpace.Space3), horizontalArrangement = Arrangement.spacedBy(HerbSpace.Space2), verticalAlignment = Alignment.CenterVertically) {
            Icon(statusIcon(tone), contentDescription = statusLabel(tone), tint = foreground)
            Text(labeledMessage, color = foreground, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun statusIcon(tone: StatusTone): ImageVector = when (tone) {
    StatusTone.Success -> HerbIcons.Success()
    StatusTone.Warning -> HerbIcons.Warning()
    StatusTone.Error -> HerbIcons.Error()
    StatusTone.Info -> HerbIcons.Info()
}

private fun statusLabel(tone: StatusTone): String = when (tone) {
    StatusTone.Success -> "Berhasil"
    StatusTone.Warning -> "Peringatan"
    StatusTone.Error -> "Gagal"
    StatusTone.Info -> "Informasi"
}

private fun statusBackground(tone: StatusTone): Color = when (tone) {
    StatusTone.Success -> HerbColors.MossSoft
    StatusTone.Warning -> HerbColors.TerracottaSoft
    StatusTone.Error -> HerbColors.TerracottaSoft
    StatusTone.Info -> HerbColors.MossSoft
}

private fun statusForeground(tone: StatusTone): Color = when (tone) {
    StatusTone.Success -> HerbColors.LeafSuccess
    StatusTone.Warning -> HerbColors.TurmericWarning
    StatusTone.Error -> HerbColors.BrickError
    StatusTone.Info -> HerbColors.RiverInfo
}
