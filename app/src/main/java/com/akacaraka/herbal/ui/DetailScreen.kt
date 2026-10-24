package com.akacaraka.herbal.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.akacaraka.herbal.data.HerbalItem

@Composable
internal fun DetailScreen(item: HerbalItem, isFavorite: Boolean, isAdmin: Boolean, spec: AdaptiveSpec, onFavorite: () -> Unit, onBack: () -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize().navigationBarsPadding().imePadding(),
        contentPadding = PaddingValues(
            start = spec.pagePadding,
            top = spec.sectionGap,
            end = spec.pagePadding,
            bottom = spec.sectionGap + BottomNavClearance
        ),
        verticalArrangement = Arrangement.spacedBy(spec.sectionGap),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item { Column(Modifier.pageWidth(spec.adminDetailMaxWidth), verticalArrangement = Arrangement.spacedBy(HerbSpace.Space2)) { HerbTextAction("Kembali", onBack, HerbColors.MossPrimary); DetailHeroPanel(item, isFavorite, isAdmin, spec, onFavorite) } }
        item { Box(Modifier.pageWidth(spec.adminDetailMaxWidth)) { DetailText("Deskripsi kandungan", item.description, spec) } }
        item { Box(Modifier.pageWidth(spec.adminDetailMaxWidth)) { DetailText("Manfaat / khasiat kesehatan", item.benefits, spec) } }
        item { Box(Modifier.pageWidth(spec.adminDetailMaxWidth)) { DetailText("Takaran & cara pengolahan", item.dosage, spec) } }
        item { Box(Modifier.pageWidth(spec.adminDetailMaxWidth)) { DetailText("Gejala terkait", item.symptoms.joinToString(", "), spec) } }
        item { Spacer(Modifier.height(BottomNavClearance)) }
    }
}

@Composable
private fun DetailHeroPanel(item: HerbalItem, isFavorite: Boolean, isAdmin: Boolean, spec: AdaptiveSpec, onFavorite: () -> Unit) {
    ArchivePanel(spec, strong = true) {
        if (spec.isCompact) Column(verticalArrangement = Arrangement.spacedBy(spec.sectionGap)) { HerbImage(item, 220.dp); DetailCopy(item, isFavorite, isAdmin, spec, onFavorite) } else Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spec.sectionGap), verticalAlignment = Alignment.CenterVertically) {
            HerbImage(item, if (spec.isExpanded) 300.dp else 250.dp, Modifier.weight(0.9f))
            Column(Modifier.weight(1.1f), verticalArrangement = Arrangement.spacedBy(HerbSpace.Space3)) { DetailCopy(item, isFavorite, isAdmin, spec, onFavorite) }
        }
    }
}

@Composable
private fun DetailCopy(item: HerbalItem, isFavorite: Boolean, isAdmin: Boolean, spec: AdaptiveSpec, onFavorite: () -> Unit) {
    SectionEyebrow(item.category.label.uppercase())
    HeroText(item.localName.ifBlank { "Herbal tanpa nama" })
    Text(item.scientificName.ifBlank { "Nama ilmiah belum tersedia" }, color = HerbColors.TerracottaAccent, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
    Text("Pelajari manfaat, kandungan, gejala terkait, serta cara pengolahan sebelum menggunakan herbal ini.", color = HerbColors.MutedInk, style = MaterialTheme.typography.bodyLarge)
    if (isAdmin) Row(horizontalArrangement = Arrangement.spacedBy(HerbSpace.Space2), verticalAlignment = Alignment.CenterVertically) {
        HerbButton(if (isFavorite) "Tersimpan" else "Simpan favorit", onFavorite, Modifier.buttonMaxWidth(spec), tone = if (isFavorite) HerbButtonTone.Destructive else HerbButtonTone.Moss)
        FavoriteIconButton(isFavorite, onFavorite)
    }
}

@Composable
private fun DetailText(title: String, body: String, spec: AdaptiveSpec) {
    ArchivePanel(spec) { SectionEyebrow(title.uppercase()); Text(body.ifBlank { "Belum ada catatan untuk bagian ini." }, color = HerbColors.MutedInk, style = MaterialTheme.typography.bodyLarge) }
}
