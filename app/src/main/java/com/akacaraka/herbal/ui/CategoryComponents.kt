package com.akacaraka.herbal.ui

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.akacaraka.herbal.data.HerbalCategory

@Composable
internal fun CategoryChip(text: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        leadingIcon = { Icon(categoryIcon(text), contentDescription = null, modifier = Modifier.height(HerbSpace.Space4)) },
        label = { Text(text, style = MaterialTheme.typography.labelLarge, maxLines = 1) },
        modifier = Modifier.sizeIn(minHeight = HerbSpace.TouchTarget),
        shape = HerbRadius.Sm,
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = if (selected) HerbColors.MossPrimary else HerbColors.RattanLine,
            selectedBorderColor = HerbColors.MossPrimary,
            disabledBorderColor = HerbColors.RattanLine,
            disabledSelectedBorderColor = HerbColors.RattanLine,
            borderWidth = 1.dp,
            selectedBorderWidth = 1.dp
        ),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = HerbColors.RicePaper,
            labelColor = HerbColors.ArchiveInk,
            selectedContainerColor = HerbColors.MossPrimary,
            selectedLabelColor = HerbColors.RicePaper,
            selectedLeadingIconColor = HerbColors.RicePaper
        )
    )
}

internal fun categoryIcon(category: HerbalCategory): ImageVector = when (category) {
    HerbalCategory.TANAMAN -> HerbIcons.Plant()
    HerbalCategory.MINUMAN -> HerbIcons.Drink()
}

private fun categoryIcon(label: String): ImageVector = when (label) {
    HerbalCategory.MINUMAN.label -> HerbIcons.Drink()
    else -> HerbIcons.Plant()
}
