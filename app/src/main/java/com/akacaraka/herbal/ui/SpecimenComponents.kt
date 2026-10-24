package com.akacaraka.herbal.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.akacaraka.herbal.data.HerbalItem

@Composable
internal fun SpecimenRow(item: HerbalItem, isFavorite: Boolean, isAdmin: Boolean, spec: AdaptiveSpec, onFavorite: () -> Unit, onOpen: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = HerbColors.RicePaper,
        shape = HerbRadius.Md,
        shadowElevation = 2.dp
    ) {
        if (spec.isCompact) {
            Column(Modifier.padding(spec.panelPadding), verticalArrangement = Arrangement.spacedBy(HerbSpace.Space3)) {
                Box { 
                    HerbImage(item, 148.dp)
                    if (isAdmin) FavoriteIconButton(isFavorite, onFavorite, Modifier.align(Alignment.TopEnd).padding(HerbSpace.Space2)) 
                }
                SpecimenCopy(item)
                HerbButton("Baca detail", onOpen, Modifier.fillMaxWidth(), tone = HerbButtonTone.Secondary)
            }
        } else {
            Row(Modifier.padding(spec.panelPadding), horizontalArrangement = Arrangement.spacedBy(spec.sectionGap), verticalAlignment = Alignment.CenterVertically) {
                HerbImage(item, if (spec.isExpanded) 176.dp else 150.dp, Modifier.width(if (spec.isExpanded) 228.dp else 188.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(HerbSpace.Space3)) {
                    SpecimenCopy(item)
                    Row(horizontalArrangement = Arrangement.spacedBy(HerbSpace.Space2), verticalAlignment = Alignment.CenterVertically) {
                        HerbButton("Baca detail", onOpen, tone = HerbButtonTone.Secondary)
                        if (isAdmin) FavoriteIconButton(isFavorite, onFavorite)
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecimenCopy(item: HerbalItem) {
    Column(verticalArrangement = Arrangement.spacedBy(HerbSpace.Space1)) {
        CategoryPill(item)
        Text(item.localName.ifBlank { "Herbal tanpa nama" }, style = MaterialTheme.typography.titleLarge, color = HerbColors.ArchiveInk, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text(item.scientificName.ifBlank { "Nama ilmiah belum tersedia" }, color = HerbColors.TerracottaAccent, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(item.benefits.ifBlank { "Khasiat belum ditulis." }, color = HerbColors.MutedInk, style = MaterialTheme.typography.bodyMedium, maxLines = 3, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun CategoryPill(item: HerbalItem) {
    Surface(color = HerbColors.MossSoft, contentColor = HerbColors.MossPrimary, shape = HerbRadius.Sm) {
        Row(Modifier.padding(horizontal = HerbSpace.Space2, vertical = HerbSpace.Space1), horizontalArrangement = Arrangement.spacedBy(HerbSpace.Space1), verticalAlignment = Alignment.CenterVertically) {
            Icon(categoryIcon(item.category), contentDescription = null, modifier = Modifier.size(HerbSpace.Space4))
            Text(item.category.label.uppercase(), style = MaterialTheme.typography.labelMedium, maxLines = 1)
        }
    }
}

@Composable
internal fun FavoriteIconButton(isFavorite: Boolean, onFavorite: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier, color = HerbColors.RicePaper, shape = HerbRadius.Sm, shadowElevation = 1.dp) {
        IconButton(onClick = onFavorite, modifier = Modifier.sizeIn(minWidth = HerbSpace.TouchTarget, minHeight = HerbSpace.TouchTarget)) {
            Icon(if (isFavorite) HerbIcons.FavoriteFilled() else HerbIcons.FavoriteOutlined(), contentDescription = if (isFavorite) "Hapus dari favorit" else "Simpan ke favorit", tint = HerbColors.TerracottaAccent)
        }
    }
}

@Composable
internal fun HerbImage(item: HerbalItem, height: Dp, modifier: Modifier = Modifier.fillMaxWidth()) {
    if (item.imageUrl.isNotBlank()) {
        AsyncImage(model = item.imageUrl, contentDescription = item.localName, modifier = modifier.height(height).clip(HerbRadius.Sm), contentScale = ContentScale.Crop)
    } else {
        SpecimenFallback(item.localName.ifBlank { "Herbal" }, modifier.height(height))
    }
}

@Composable
internal fun CompactAvatar(item: HerbalItem, modifier: Modifier = Modifier) {
    Box(modifier.height(56.dp).width(56.dp).clip(HerbRadius.Sm)) {
        if (item.imageUrl.isNotBlank()) {
            AsyncImage(model = item.imageUrl, contentDescription = item.localName, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else {
            SpecimenFallback(item.localName.ifBlank { item.category.label }, Modifier.fillMaxSize())
        }
    }
}
