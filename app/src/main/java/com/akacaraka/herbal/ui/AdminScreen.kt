package com.akacaraka.herbal.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.akacaraka.herbal.data.HerbalCategory
import com.akacaraka.herbal.data.HerbalItem

@Composable
internal fun AdminScreen(state: HerbalUiState, viewModel: HerbalViewModel, spec: AdaptiveSpec, modifier: Modifier = Modifier) {
    val scrollState = rememberLazyListState()
    
    LaunchedEffect(state.editingItem.id) {
        if (state.editingItem.id.isNotBlank()) {
            scrollState.animateScrollToItem(0)
        }
    }

    AppPageColumn(spec, modifier, scrollState = scrollState) {
        item { Box(Modifier.pageWidth(spec.adminDetailMaxWidth)) { AdminSection(state, viewModel, spec) } }
    }
}

@Composable
private fun AdminSection(state: HerbalUiState, viewModel: HerbalViewModel, spec: AdaptiveSpec) {
    Column(verticalArrangement = Arrangement.spacedBy(spec.sectionGap)) {
        SectionTitle("Kelola data", "Pilih foto, isi data, lalu simpan. Foto baru dipasang otomatis.")
        if (!state.isAdminLoggedIn) AdminAccessPanel(state, viewModel, spec) else AdminCrudSection(state, viewModel, spec)
    }
}

@Composable
private fun AdminAccessPanel(state: HerbalUiState, viewModel: HerbalViewModel, spec: AdaptiveSpec) {
    ArchivePanel(spec, background = HerbColors.MossSoft) {
        SectionEyebrow("AKSES ADMIN")
        Text("Masuk dari form utama", color = HerbColors.ArchiveInk, style = MaterialTheme.typography.titleLarge)
        Text("Gunakan menu Masuk seperti pengguna biasa. Jika email itu terdaftar di admins, aplikasi langsung membuka ruang kelola data.", color = HerbColors.MutedInk, style = MaterialTheme.typography.bodyLarge)
        if (state.message.isNotBlank()) StatusNotice(state.message, StatusTone.Warning)
        HerbButton("Ke form masuk", { viewModel.navigateTo(AppPage.LOGIN) }, Modifier.buttonMaxWidth(spec), tone = HerbButtonTone.Moss)
    }
}

@Composable
private fun AdminCrudSection(state: HerbalUiState, viewModel: HerbalViewModel, spec: AdaptiveSpec) {
    val item = state.editingItem
    val canSave = item.localName.isNotBlank()
    val focusManager = LocalFocusManager.current
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> viewModel.selectImage(uri) }
    
    ArchivePanel(spec, strong = true) {
        AdminFormHeader()
        ImageUploadStudio(state, item, spec, { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, viewModel::clearSelectedImage)
        
        SectionTitle("Informasi herbal", "Data ini akan tampil pada pencarian dan halaman detail pengguna.")
        FlowActions { 
            HerbalCategory.entries.forEach { category -> 
                CategoryChip(category.label, item.category == category) { 
                    viewModel.updateEditingItem(item.copy(category = category)) 
                } 
            } 
        }
        
        AdminFields(item, viewModel, focusManager::clearFocus)
        
        if (state.message.isNotBlank() && state.message != state.imageUploadError) {
            StatusNotice(state.message, StatusTone.Info)
        }
        
        if (!canSave) {
            Text("Isi nama lokal untuk mengaktifkan tombol simpan.", color = HerbColors.MutedInk, style = MaterialTheme.typography.bodySmall)
        }
        
        HerbButton(
            text = if (item.id.isBlank()) "Simpan data baru" else "Perbarui data herbal",
            onClick = viewModel::saveEditingItem,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading && !state.isUploadingImage && canSave,
            loading = state.isLoading,
            tone = HerbButtonTone.Moss
        )
        
        if (item.id.isNotBlank()) {
            HerbTextAction("Batalkan edit", { viewModel.updateEditingItem(HerbalItem()) }, HerbColors.MutedInk)
        }
    }

    if (state.adminItems.isEmpty()) {
        EmptyState("ARSIP ADMIN KOSONG", "Belum ada data herbal yang bisa diedit. Isi formulir di atas untuk menambah catatan pertama.", spec)
    } else {
        SectionTitle("Arsip herbal", "Edit catatan tersimpan atau hapus data yang tidak lagi dipakai.")
        state.adminItems.forEach { adminItem -> AdminArchiveRow(adminItem, viewModel, spec) }
    }

    ArchivePanel(spec) {
        SectionEyebrow("SESI ADMIN")
        Text("Selesai mengelola data? Keluar dari akun admin agar akses kelola tidak tertinggal di perangkat ini.", color = HerbColors.MutedInk, style = MaterialTheme.typography.bodyMedium)
        HerbTextAction("Keluar dari akun admin", viewModel::logoutAdmin, HerbColors.TerracottaAccent)
    }
}

@Composable
private fun AdminFormHeader() {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(HerbSpace.Space3)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(HerbSpace.Space1)) {
            SectionEyebrow("FOTO HERBAL")
            Text("Pilih foto dari galeri; saat data disimpan, foto ikut terpasang.", color = HerbColors.MutedInk, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun AdminFields(item: HerbalItem, viewModel: HerbalViewModel, clearFocus: () -> Unit) {
    var symptomsText by remember(item.id) { mutableStateOf(item.symptoms.joinToString(", ")) }

    AdminField("Nama lokal", "Wajib diisi agar data bisa disimpan.") {
        HerbTextField(item.localName, "Contoh: Kunyit", { viewModel.updateEditingItem(item.copy(localName = it)) }, singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
    }
    AdminField("Nama ilmiah", "Opsional, isi bila tersedia.") {
        HerbTextField(item.scientificName, "Contoh: Curcuma longa", { viewModel.updateEditingItem(item.copy(scientificName = it)) }, singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
    }
    AdminField("Deskripsi kandungan") {
        HerbTextField(item.description, "Ringkas kandungan utama", { viewModel.updateEditingItem(item.copy(description = it)) }, minLines = 3, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
    }
    AdminField("Khasiat") {
        HerbTextField(item.benefits, "Manfaat yang akan dibaca pengguna", { viewModel.updateEditingItem(item.copy(benefits = it)) }, minLines = 3, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
    }
    AdminField("Takaran/cara pengolahan") {
        HerbTextField(item.dosage, "Cara pakai yang aman dan jelas", { viewModel.updateEditingItem(item.copy(dosage = it)) }, minLines = 3, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
    }
    AdminField("Gejala terkait", "Pisahkan dengan koma.") {
        HerbTextField(
            value = symptomsText,
            label = "Contoh: batuk, mual",
            onChange = { 
                symptomsText = it
                viewModel.updateEditingItem(item.copy(symptoms = parseSymptoms(it)))
            },
            minLines = 2,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { clearFocus() })
        )
    }
}

@Composable
private fun AdminField(label: String, helper: String = "", content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(HerbSpace.Space1)) {
        Text(label, color = HerbColors.ArchiveInk, style = MaterialTheme.typography.labelLarge)
        if (helper.isNotBlank()) Text(helper, color = HerbColors.MutedInk, style = MaterialTheme.typography.bodySmall)
        content()
    }
}

private fun parseSymptoms(text: String): List<String> = text.split(',').map { it.trim() }.filter { it.isNotBlank() }

@Composable
private fun ImageUploadStudio(state: HerbalUiState, item: HerbalItem, spec: AdaptiveSpec, onPickImage: () -> Unit, onClear: () -> Unit) {
    val previewModel: Any? = state.selectedImageUri ?: item.imageUrl.takeIf { it.isNotBlank() }
    if (spec.isExpanded) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spec.sectionGap), verticalAlignment = Alignment.Top) {
            ImageUploadPreview(state, item, previewModel, spec, onPickImage, Modifier.weight(1.15f))
            ImageUploadActions(state, previewModel, onPickImage, onClear, Modifier.weight(0.85f))
        }
    } else {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(HerbSpace.Space3)) {
            ImageUploadPreview(state, item, previewModel, spec, onPickImage, Modifier.fillMaxWidth())
            ImageUploadActions(state, previewModel, onPickImage, onClear, Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun ImageUploadPreview(state: HerbalUiState, item: HerbalItem, previewModel: Any?, spec: AdaptiveSpec, onPickImage: () -> Unit, modifier: Modifier) {
    val canPickImage = !state.isLoading && !state.isUploadingImage
    Box(modifier.height(if (spec.isCompact) 160.dp else 220.dp).background(if (previewModel == null) HerbColors.MossSoft else HerbColors.ArchiveInk, HerbRadius.Lg).clip(HerbRadius.Lg).archiveClickable("Pilih atau ganti foto herbal") { if (canPickImage) onPickImage() }, contentAlignment = Alignment.Center) {
        if (previewModel != null) {
            AsyncImage(model = previewModel, contentDescription = "Preview foto ${item.localName.ifBlank { "herbal" }}", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(HerbColors.Transparent, HerbColors.ScrimInk))))
            Column(Modifier.align(Alignment.BottomStart).padding(spec.panelPadding), verticalArrangement = Arrangement.spacedBy(HerbSpace.Space1)) {
                Text(if (state.selectedImageUri != null) "Foto baru siap disimpan" else "Foto aktif dalam data", color = HerbColors.RicePaper, style = MaterialTheme.typography.labelLarge)
                Text(if (state.selectedImageUri != null) "Aplikasi mengunggahnya saat data disimpan." else "Ketuk studio untuk mengganti foto.", color = HerbColors.TerracottaSoft, style = MaterialTheme.typography.bodySmall)
            }
        } else {
            Column(Modifier.padding(HerbSpace.Space4), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(HerbSpace.Space2)) {
                SectionEyebrow("FOTO UTAMA")
                Text("Tambahkan foto utama", color = HerbColors.ArchiveInk, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                Text("Pilih foto yang terang dan jelas.", color = HerbColors.MutedInk, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
            }
        }
        if (state.isUploadingImage) Box(Modifier.fillMaxSize().background(HerbColors.ScrimInk), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(HerbSpace.Space2)) {
                CircularProgressIndicator(color = HerbColors.TerracottaSoft, modifier = Modifier.size(HerbSpace.Space8))
                Text("Mengunggah foto", color = HerbColors.RicePaper, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun ImageUploadActions(state: HerbalUiState, previewModel: Any?, onPickImage: () -> Unit, onClear: () -> Unit, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(HerbSpace.Space2)) {
        SectionEyebrow("FOTO DATA HERBAL")
        Text("Ganti foto kapan saja sebelum menyimpan data.", color = HerbColors.MutedInk, style = MaterialTheme.typography.bodyMedium)
        HerbButton(if (previewModel == null) "Pilih foto" else "Ganti foto", onPickImage, Modifier.fillMaxWidth(), enabled = !state.isLoading && !state.isUploadingImage, tone = HerbButtonTone.Secondary)
        if (state.selectedImageUri != null && !state.isLoading && !state.isUploadingImage) HerbTextAction("Batalkan pilihan foto", onClear, HerbColors.TerracottaAccent)
        when {
            state.imageUploadError.isNotBlank() -> StatusNotice(state.imageUploadError, StatusTone.Error)
            state.selectedImageUri != null -> StatusNotice("Foto baru akan diunggah otomatis saat data disimpan.", StatusTone.Info)
            state.imageUploadSuccess -> StatusNotice("Foto sudah diunggah. Simpan data lagi jika perubahan belum tersimpan.", StatusTone.Success)
            previewModel != null -> Text("Tanpa foto baru, foto aktif tetap dipakai.", color = HerbColors.MutedInk, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun AdminArchiveRow(adminItem: HerbalItem, viewModel: HerbalViewModel, spec: AdaptiveSpec) {
    Surface(Modifier.fillMaxWidth(), color = HerbColors.RicePaper, shape = HerbRadius.Sm, shadowElevation = 1.dp) {
        if (spec.isCompact) {
            Column(Modifier.padding(HerbSpace.Space3), verticalArrangement = Arrangement.spacedBy(HerbSpace.Space2)) { 
                AdminArchiveIdentity(adminItem)
                AdminArchiveActions(adminItem, viewModel) 
            }
        } else {
            Row(Modifier.padding(HerbSpace.Space3), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(HerbSpace.Space3)) {
                CompactAvatar(adminItem)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(HerbSpace.Space1)) {
                    Text(adminItem.localName.ifBlank { "Herbal tanpa nama" }, color = HerbColors.ArchiveInk, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(adminItem.scientificName.ifBlank { "Nama ilmiah belum ditulis" }, color = HerbColors.MutedInk, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(adminItem.category.label, color = HerbColors.TerracottaAccent, style = MaterialTheme.typography.labelMedium)
                }
                AdminArchiveActions(adminItem, viewModel)
            }
        }
    }
}

@Composable
private fun AdminArchiveIdentity(adminItem: HerbalItem) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(HerbSpace.Space3)) {
        CompactAvatar(adminItem)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(HerbSpace.Space1)) {
            Text(adminItem.localName.ifBlank { "Herbal tanpa nama" }, color = HerbColors.ArchiveInk, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(adminItem.scientificName.ifBlank { "Nama ilmiah belum ditulis" }, color = HerbColors.MutedInk, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(adminItem.category.label, color = HerbColors.TerracottaAccent, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun AdminArchiveActions(adminItem: HerbalItem, viewModel: HerbalViewModel) {
    var confirmingDelete by remember(adminItem.id) { mutableStateOf(false) }
    FlowActions {
        HerbTextAction("Edit", { viewModel.editItem(adminItem) }, HerbColors.MossPrimary)
        HerbTextAction("Hapus", { confirmingDelete = true }, HerbColors.BrickError)
    }
    if (confirmingDelete) {
        AlertDialog(
            onDismissRequest = { confirmingDelete = false },
            title = { Text("Hapus data herbal?") },
            text = { Text("Data ${adminItem.localName.ifBlank { "herbal ini" }} akan dihapus dari arsip. Tindakan ini baru berjalan setelah kamu menekan Hapus data.") },
            confirmButton = {
                HerbTextAction("Hapus data", { confirmingDelete = false; viewModel.deleteItem(adminItem.id) }, HerbColors.BrickError)
            },
            dismissButton = {
                HerbTextAction("Batal", { confirmingDelete = false }, HerbColors.MossPrimary)
            },
            containerColor = HerbColors.RicePaper,
            titleContentColor = HerbColors.ArchiveInk,
            textContentColor = HerbColors.MutedInk
        )
    }
}
