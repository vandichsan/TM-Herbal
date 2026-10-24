package com.akacaraka.herbal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.akacaraka.herbal.data.HerbalCategory
import com.akacaraka.herbal.data.HerbalItem
import com.akacaraka.herbal.data.Visitor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun ReportScreen(state: HerbalUiState, viewModel: HerbalViewModel, spec: AdaptiveSpec, modifier: Modifier = Modifier) {
    AppPageColumn(spec, modifier) {
        item {
            Column(Modifier.pageWidth(spec.adminDetailMaxWidth), verticalArrangement = Arrangement.spacedBy(spec.sectionGap)) {
                SectionTitle("Menu Laporan", "Pilih jenis laporan yang ingin Anda lihat atau cetak.")
                
                ReportType.entries.forEach { type ->
                    ReportMenuRow(type) { viewModel.openReport(type) }
                }
                
                Spacer(Modifier.height(HerbSpace.Space2))
                HerbTextAction("Kembali ke Admin", { viewModel.navigateTo(AppPage.ADMIN) }, HerbColors.MutedInk)
            }
        }
    }
}

@Composable
private fun ReportMenuRow(type: ReportType, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        color = HerbColors.RicePaper,
        shape = HerbRadius.Md,
        shadowElevation = 1.dp
    ) {
        Row(Modifier.padding(HerbSpace.Space4), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(HerbSpace.Space3)) {
            Box(Modifier.size(48.dp).background(HerbColors.MossSoft, HerbRadius.Sm), contentAlignment = Alignment.Center) {
                Icon(
                    when(type) {
                        ReportType.TANAMAN -> HerbIcons.Plant()
                        ReportType.MINUMAN -> HerbIcons.Drink()
                        ReportType.TAMU -> HerbIcons.PersonFilled()
                        ReportType.STATISTIK -> HerbIcons.Info()
                    },
                    contentDescription = null,
                    tint = HerbColors.MossPrimary
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(HerbSpace.Space1)) {
                Text(type.label, style = MaterialTheme.typography.titleMedium, color = HerbColors.ArchiveInk)
                Text(
                    when(type) {
                        ReportType.TANAMAN -> "Daftar seluruh koleksi tanaman herbal."
                        ReportType.MINUMAN -> "Daftar seluruh koleksi minuman herbal."
                        ReportType.TAMU -> "Rekap kunjungan pengguna mode tamu."
                        ReportType.STATISTIK -> "Analisis kelengkapan data & statistik."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = HerbColors.MutedInk
                )
            }
            Icon(HerbIcons.Forward(), contentDescription = null, tint = HerbColors.RattanLine)
        }
    }
}

@Composable
internal fun ReportDetailScreen(state: HerbalUiState, viewModel: HerbalViewModel, spec: AdaptiveSpec, modifier: Modifier = Modifier) {
    val type = state.selectedReportType ?: return
    val context = LocalContext.current

    AppPageColumn(spec, modifier) {
        item {
            Column(Modifier.pageWidth(spec.adminDetailMaxWidth), verticalArrangement = Arrangement.spacedBy(spec.sectionGap)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        SectionTitle(type.label, "Tampilan data untuk laporan.")
                    }
                    HerbButton(
                        text = "Cetak PDF",
                        onClick = {
                            when (type) {
                                ReportType.TANAMAN -> ReportPrinter.printHerbalReport(context, state.allItems.filter { it.category == HerbalCategory.TANAMAN }, HerbalCategory.TANAMAN)
                                ReportType.MINUMAN -> ReportPrinter.printHerbalReport(context, state.allItems.filter { it.category == HerbalCategory.MINUMAN }, HerbalCategory.MINUMAN)
                                ReportType.TAMU -> ReportPrinter.printVisitorReport(context, state.visitors)
                                ReportType.STATISTIK -> ReportPrinter.printStatReport(context, state.allItems)
                            }
                        },
                        tone = HerbButtonTone.Moss
                    )
                }

                ArchivePanel(spec, strong = true) {
                    ReportContent(state, type, spec)
                }
                
                HerbTextAction("Kembali ke Menu Laporan", { viewModel.navigateTo(AppPage.REPORT) }, HerbColors.MutedInk)
            }
        }
    }
}

@Composable
private fun ReportContent(state: HerbalUiState, type: ReportType, spec: AdaptiveSpec) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            text = type.label.uppercase(),
            style = MaterialTheme.typography.titleLarge,
            color = HerbColors.ArchiveInk,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Pratinjau data per ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())}",
            style = MaterialTheme.typography.bodySmall,
            color = HerbColors.MutedInk,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(HerbSpace.Space4))

        when (type) {
            ReportType.TANAMAN -> HerbalReportTable(state.allItems.filter { it.category == HerbalCategory.TANAMAN })
            ReportType.MINUMAN -> HerbalReportTable(state.allItems.filter { it.category == HerbalCategory.MINUMAN })
            ReportType.TAMU -> VisitorReportTable(state.visitors)
            ReportType.STATISTIK -> DetailedStatisticsContent(state.allItems, state.visitors)
        }
    }
}

@Composable
private fun DetailedStatisticsContent(items: List<HerbalItem>, visitors: List<Visitor>) {
    val totalTanaman = items.count { it.category == HerbalCategory.TANAMAN }
    val totalMinuman = items.count { it.category == HerbalCategory.MINUMAN }
    
    val missingPhoto = items.filter { it.imageUrl.isBlank() }
    val missingScientific = items.filter { it.scientificName.isBlank() }
    val missingDescription = items.filter { it.description.isBlank() }
    val missingSymptoms = items.filter { it.symptoms.isEmpty() }
    
    val now = System.currentTimeMillis()
    val visitorsLast24h = visitors.count { now - it.timestamp < 24 * 60 * 60 * 1000 }
    
    val totalFields = items.size * 7
    var filledFields = 0
    items.forEach { 
        if (it.localName.isNotBlank()) filledFields++
        if (it.scientificName.isNotBlank()) filledFields++
        if (it.description.isNotBlank()) filledFields++
        if (it.benefits.isNotBlank()) filledFields++
        if (it.dosage.isNotBlank()) filledFields++
        if (it.imageUrl.isNotBlank()) filledFields++
        if (it.symptoms.isNotEmpty()) filledFields++
    }
    val completeness = if (items.isNotEmpty()) (filledFields.toFloat() / totalFields * 100).toInt() else 100

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(HerbSpace.Space3)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(HerbSpace.Space2)) {
            StatCard("Skor Data", "$completeness%", Modifier.weight(1f))
            StatCard("Tamu (24j)", visitorsLast24h.toString(), Modifier.weight(1f))
            StatCard("Total Tamu", visitors.size.toString(), Modifier.weight(1f))
        }

        Spacer(Modifier.height(HerbSpace.Space1))
        Text("DISTRIBUSI KATEGORI", style = MaterialTheme.typography.labelLarge, color = HerbColors.ArchiveInk, fontWeight = FontWeight.Bold)
        ReportRow(listOf("Kategori", "Jumlah", "Persentase"), isHeader = true)
        ReportRow(listOf("Tanaman Herbal", totalTanaman.toString(), "${if(items.isNotEmpty()) (totalTanaman*100/items.size) else 0}%"))
        ReportRow(listOf("Minuman Herbal", totalMinuman.toString(), "${if(items.isNotEmpty()) (totalMinuman*100/items.size) else 0}%"))

        Spacer(Modifier.height(HerbSpace.Space2))
        Text("KELENGKAPAN DATA", style = MaterialTheme.typography.labelLarge, color = HerbColors.ArchiveInk, fontWeight = FontWeight.Bold)
        ReportRow(listOf("Data", "Terisi", "Kosong"), isHeader = true)
        ReportRow(listOf("Foto Utama", (items.size - missingPhoto.size).toString(), missingPhoto.size.toString()))
        ReportRow(listOf("Nama Ilmiah", (items.size - missingScientific.size).toString(), missingScientific.size.toString()))
        ReportRow(listOf("Deskripsi", (items.size - missingDescription.size).toString(), missingDescription.size.toString()))
        ReportRow(listOf("Gejala/Symptom", (items.size - missingSymptoms.size).toString(), missingSymptoms.size.toString()))

        if (missingPhoto.isNotEmpty()) {
            Spacer(Modifier.height(HerbSpace.Space2))
            Text("Perlu Tindakan Segera (Foto):", style = MaterialTheme.typography.labelMedium, color = HerbColors.BrickError, fontWeight = FontWeight.Bold)
            Text(missingPhoto.take(10).joinToString(", ") { it.localName } + if(missingPhoto.size > 10) "..." else "", style = MaterialTheme.typography.bodySmall, color = HerbColors.MutedInk)
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier) {
    Surface(modifier, color = HerbColors.MossSoft, shape = HerbRadius.Sm) {
        Column(Modifier.padding(HerbSpace.Space2), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = HerbColors.MutedInk)
            Text(value, style = MaterialTheme.typography.titleLarge, color = HerbColors.MossPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun HerbalReportTable(items: List<HerbalItem>) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(HerbSpace.Space1)) {
        ReportRow(listOf("No", "Nama Lokal", "Nama Ilmiah"), isHeader = true)
        items.forEachIndexed { index, item ->
            ReportRow(listOf((index + 1).toString(), item.localName, item.scientificName))
        }
        if (items.isEmpty()) {
            Text("Tidak ada data.", modifier = Modifier.padding(HerbSpace.Space4).fillMaxWidth(), textAlign = TextAlign.Center, color = HerbColors.MutedInk)
        }
    }
}

@Composable
private fun VisitorReportTable(visitors: List<Visitor>) {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(HerbSpace.Space1)) {
        ReportRow(listOf("No", "ID Pengunjung", "Waktu Kunjungan"), isHeader = true)
        visitors.sortedByDescending { it.timestamp }.forEachIndexed { index, visitor ->
            ReportRow(listOf((index + 1).toString(), visitor.id.take(8), sdf.format(Date(visitor.timestamp))))
        }
        if (visitors.isEmpty()) {
            Text("Tidak ada data.", modifier = Modifier.padding(HerbSpace.Space4).fillMaxWidth(), textAlign = TextAlign.Center, color = HerbColors.MutedInk)
        }
    }
}

@Composable
private fun ReportRow(cols: List<String>, isHeader: Boolean = false) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(if (isHeader) HerbColors.MossSoft else HerbColors.Transparent)
            .padding(vertical = HerbSpace.Space2, horizontal = HerbSpace.Space1),
        verticalAlignment = Alignment.CenterVertically
    ) {
        cols.forEachIndexed { index, text ->
            Text(
                text = text,
                modifier = Modifier.weight(
                    when (cols.size) {
                        3 -> {
                            val isListReport = cols[0] == "No" || cols[0].toIntOrNull() != null
                            if (isListReport) {
                                if (index == 0) 0.4f else 2f
                            } else {
                                if (index == 0) 2f else 1f
                            }
                        }
                        2 -> if (index == 0) 2f else 1f
                        else -> 1f
                    }
                ),
                style = if (isHeader) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodyMedium,
                color = if (isHeader) HerbColors.ArchiveInk else HerbColors.MutedInk,
                fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
