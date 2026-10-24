package com.akacaraka.herbal.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal object HerbColors {
    val ArchiveInk = Color(0xFF172019)
    val IvoryGround = Color(0xFFFBF3E3)
    val RicePaper = Color(0xFFFFFFF7)
    val MossPrimary = Color(0xFF314C2E)
    val MossSoft = Color(0xFFDDE6D2)
    val TerracottaAccent = Color(0xFF9D3F26)
    val TerracottaSoft = Color(0xFFF0D4C4)
    val RattanLine = Color(0xFFC9B893)
    val MutedInk = Color(0xFF555D50)
    val DeepForest = Color(0xFF253B27)
    val BrickError = Color(0xFF8C241A)
    val LeafSuccess = Color(0xFF2F6B3B)
    val TurmericWarning = Color(0xFFA86213)
    val RiverInfo = Color(0xFF2F5F70)
    val FocusGold = Color(0xFFC7932D)
    val ScrimInk = Color(0xB8172019)
    val Transparent = Color.Transparent
}

internal object HerbSpace {
    val Space1 = 4.dp
    val Space2 = 8.dp
    val Space3 = 12.dp
    val Space4 = 16.dp
    val Space5 = 20.dp
    val Space6 = 24.dp
    val Space8 = 32.dp
    val Space10 = 40.dp
    val TouchTarget = 52.dp
}

internal object HerbRadius {
    val Xs = RoundedCornerShape(6.dp)
    val Sm = RoundedCornerShape(10.dp)
    val Md = RoundedCornerShape(14.dp)
    val Lg = RoundedCornerShape(18.dp)
    val Xl = RoundedCornerShape(26.dp)
}

internal enum class WindowTier { Compact, Medium, Expanded }

internal data class AdaptiveSpec(
    val tier: WindowTier,
    val maxWidth: Dp,
    val authMaxWidth: Dp,
    val adminDetailMaxWidth: Dp,
    val pagePadding: Dp,
    val sectionGap: Dp,
    val panelPadding: Dp,
    val imageHeight: Dp
) {
    val isCompact: Boolean get() = tier == WindowTier.Compact
    val isExpanded: Boolean get() = tier == WindowTier.Expanded
}

@Composable
internal fun rememberAdaptiveSpec(maxWidth: Dp): AdaptiveSpec {
    return when {
        maxWidth < 600.dp -> AdaptiveSpec(WindowTier.Compact, 432.dp, 560.dp, 432.dp, HerbSpace.Space4, HerbSpace.Space3, HerbSpace.Space4, 172.dp)
        maxWidth < 840.dp -> AdaptiveSpec(WindowTier.Medium, 680.dp, 560.dp, 680.dp, HerbSpace.Space6, HerbSpace.Space5, HerbSpace.Space5, 210.dp)
        else -> AdaptiveSpec(WindowTier.Expanded, 920.dp, 560.dp, 1040.dp, HerbSpace.Space8, HerbSpace.Space6, HerbSpace.Space6, 260.dp)
    }
}

private val HerbColorScheme = lightColorScheme(
    primary = HerbColors.MossPrimary,
    onPrimary = HerbColors.RicePaper,
    primaryContainer = HerbColors.MossSoft,
    onPrimaryContainer = HerbColors.ArchiveInk,
    secondary = HerbColors.TerracottaAccent,
    onSecondary = HerbColors.RicePaper,
    secondaryContainer = HerbColors.TerracottaSoft,
    onSecondaryContainer = HerbColors.ArchiveInk,
    background = HerbColors.IvoryGround,
    onBackground = HerbColors.ArchiveInk,
    surface = HerbColors.RicePaper,
    surfaceVariant = HerbColors.IvoryGround,
    onSurface = HerbColors.ArchiveInk,
    onSurfaceVariant = HerbColors.MutedInk,
    outline = HerbColors.RattanLine,
    error = HerbColors.BrickError,
    onError = HerbColors.RicePaper,
    errorContainer = HerbColors.TerracottaSoft,
    onErrorContainer = HerbColors.BrickError
)

private fun herbTypography(compact: Boolean): Typography = Typography().run {
    copy(
        displaySmall = displaySmall.copy(fontSize = if (compact) 36.sp else 42.sp, lineHeight = if (compact) 42.sp else 48.sp, fontWeight = FontWeight.Bold),
        headlineSmall = headlineSmall.copy(fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold),
        titleLarge = titleLarge.copy(fontSize = 23.sp, lineHeight = 29.sp, fontWeight = FontWeight.Bold),
        titleMedium = titleMedium.copy(fontSize = 19.sp, lineHeight = 25.sp, fontWeight = FontWeight.Bold),
        bodyLarge = bodyLarge.copy(fontSize = 17.sp, lineHeight = 25.sp),
        bodyMedium = bodyMedium.copy(fontSize = 15.sp, lineHeight = 21.sp),
        bodySmall = bodySmall.copy(fontSize = 13.sp, lineHeight = 17.sp, fontWeight = FontWeight.Medium),
        labelMedium = labelMedium.copy(fontSize = 13.sp, lineHeight = 17.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.4.sp),
        labelLarge = labelLarge.copy(fontSize = 15.sp, lineHeight = 19.sp, fontWeight = FontWeight.ExtraBold)
    )
}

@Composable
internal fun HerbTheme(compact: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = HerbColorScheme, typography = herbTypography(compact), content = content)
}
