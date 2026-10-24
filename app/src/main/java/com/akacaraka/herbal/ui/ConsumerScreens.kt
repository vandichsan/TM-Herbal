package com.akacaraka.herbal.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.akacaraka.herbal.R
import com.akacaraka.herbal.data.HerbalCategory
import com.akacaraka.herbal.data.HerbalItem

@Composable
internal fun HomeScreen(state: HerbalUiState, viewModel: HerbalViewModel, spec: AdaptiveSpec, modifier: Modifier = Modifier) {
    val featured = state.allItems.ifEmpty { state.searchResult.items }.take(4)
    AppPageColumn(spec, modifier) {
        item { Box(Modifier.pageWidth(spec.maxWidth)) { HomeMasthead(state, viewModel) } }
        item { Box(Modifier.pageWidth(spec.maxWidth)) { SectionTitle("Lanjutkan membaca", "Catatan herbal ringkas dari arsipmu.") } }
        if (featured.isEmpty()) {
            item { Box(Modifier.pageWidth(spec.maxWidth)) { EmptyState("ARSIP MASIH KOSONG", "Data herbal belum tersedia. Coba lagi nanti atau buka ruang admin bila kamu pengelola.", spec) } }
        } else {
            items(featured, key = { it.id }) { item -> Box(Modifier.pageWidth(spec.maxWidth)) { HomeHerbRow(item, state.favoriteIds.contains(item.id), state.isAdminLoggedIn, { viewModel.toggleFavorite(item.id) }, { viewModel.openDetail(item) }) } }
        }
    }
}

@Composable
private fun HomeMasthead(state: HerbalUiState, viewModel: HerbalViewModel) {
    Surface(Modifier.fillMaxWidth(), color = HerbColors.RicePaper, shape = HerbRadius.Md, shadowElevation = 1.dp) {
        Column(Modifier.padding(HerbSpace.Space3), verticalArrangement = Arrangement.spacedBy(HerbSpace.Space2)) {
            HomeHeader(state)
            HomeSearchEntry(state, viewModel)
            HomeSummary(state, viewModel)
        }
    }
}

@Composable
private fun HomeHeader(state: HerbalUiState) {
    val sessionLabel = when (state.session.type) {
        UserSessionType.ACCOUNT -> state.session.email.ifBlank { "Akun aktif" }
        UserSessionType.GUEST -> "Mode tamu aktif"
        UserSessionType.SIGNED_OUT -> "Belum masuk"
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(HerbSpace.Space2)) {
        Surface(Modifier.size(HerbSpace.Space10), color = HerbColors.MossSoft, shape = HerbRadius.Sm) {
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(R.drawable.app_logo),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().padding(HerbSpace.Space1),
                    contentScale = ContentScale.Fit
                )
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(HerbSpace.Space1)) {
            Text("Beranda herbal", color = HerbColors.ArchiveInk, style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
            Text(sessionLabel, color = HerbColors.MutedInk, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun HomeSearchEntry(state: HerbalUiState, viewModel: HerbalViewModel) {
    val focusManager = LocalFocusManager.current
    val search = {
        focusManager.clearFocus()
        viewModel.executeSearch()
        viewModel.navigateTo(AppPage.EXPLORE)
    }
    OutlinedTextField(
        value = state.keyword,
        onValueChange = viewModel::updateKeyword,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        label = { Text("Cari herbal") },
        leadingIcon = { Icon(HerbIcons.SearchOutlined(), contentDescription = null, tint = HerbColors.MutedInk) },
        trailingIcon = { HerbTextAction("Cari", search, HerbColors.MossPrimary) },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { search() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = HerbColors.FocusGold,
            focusedLabelColor = HerbColors.ArchiveInk,
            cursorColor = HerbColors.TerracottaAccent,
            unfocusedBorderColor = HerbColors.RattanLine,
            focusedContainerColor = HerbColors.RicePaper,
            unfocusedContainerColor = HerbColors.RicePaper
        ),
        shape = HerbRadius.Sm
    )
}

@Composable
private fun HomeSummary(state: HerbalUiState, viewModel: HerbalViewModel) {
    val isAdmin = state.isAdminLoggedIn
    Surface(Modifier.fillMaxWidth(), color = HerbColors.MossSoft, shape = HerbRadius.Sm, shadowElevation = 0.dp) {
        Row(Modifier.padding(horizontal = HerbSpace.Space3, vertical = HerbSpace.Space2), horizontalArrangement = Arrangement.spacedBy(HerbSpace.Space2), verticalAlignment = Alignment.CenterVertically) {
            Icon(HerbIcons.Info(), contentDescription = null, tint = HerbColors.MossPrimary, modifier = Modifier.size(HerbSpace.Space4))
            Text(
                if (isAdmin) "${state.allItems.size} catatan - ${state.favoriteIds.size} favorit" else "${state.allItems.size} catatan herbal tersedia",
                modifier = Modifier.weight(1f),
                color = HerbColors.ArchiveInk,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
            if (isAdmin) HerbTextAction("Favorit", { viewModel.navigateTo(AppPage.FAVORITES) }, HerbColors.MossPrimary)
        }
    }
}

@Composable
private fun HomeHerbRow(item: HerbalItem, isFavorite: Boolean, isAdmin: Boolean, onFavorite: () -> Unit, onOpen: () -> Unit) {
    val itemName = item.localName.ifBlank { "Herbal tanpa nama" }
    Surface(Modifier.fillMaxWidth(), color = HerbColors.RicePaper, shape = HerbRadius.Sm, shadowElevation = 1.dp) {
        Row(Modifier.padding(HerbSpace.Space3), horizontalArrangement = Arrangement.spacedBy(HerbSpace.Space3), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f).archiveClickable("Buka detail $itemName", onOpen), horizontalArrangement = Arrangement.spacedBy(HerbSpace.Space3), verticalAlignment = Alignment.CenterVertically) {
                CompactAvatar(item)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(HerbSpace.Space1)) {
                    Text(itemName, color = HerbColors.ArchiveInk, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(item.scientificName.ifBlank { item.category.label }, color = HerbColors.TerracottaAccent, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(item.benefits.ifBlank { "Ketuk untuk membaca catatan herbal." }, color = HerbColors.MutedInk, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
            if (isAdmin) FavoriteIconButton(isFavorite, onFavorite)
        }
    }
}

@Composable
internal fun ExploreScreen(state: HerbalUiState, viewModel: HerbalViewModel, spec: AdaptiveSpec, modifier: Modifier = Modifier) {
    AppPageColumn(spec, modifier) {
        item { Box(Modifier.pageWidth(spec.maxWidth)) { SearchPanel(state, viewModel, spec) } }
        item { Box(Modifier.pageWidth(spec.maxWidth)) { ResultsPanel(state, spec) } }
        if (state.isLoading) item { Box(Modifier.pageWidth(spec.maxWidth)) { LoadingState("Mencari arsip herbal") } }
        if (state.searchResult.items.isEmpty() && !state.isLoading) {
            item { Box(Modifier.pageWidth(spec.maxWidth)) { EmptyState("BELUM ADA HASIL", "Coba kata kunci lain, misalnya batuk, jahe, kunyit, atau pindah kategori.", spec) } }
        } else {
            items(state.searchResult.items, key = { it.id }) { item -> Box(Modifier.pageWidth(spec.maxWidth)) { SpecimenRow(item, state.favoriteIds.contains(item.id), state.isAdminLoggedIn, spec, { viewModel.toggleFavorite(item.id) }, { viewModel.openDetail(item) }) } }
        }
    }
}

@Composable
private fun SearchPanel(state: HerbalUiState, viewModel: HerbalViewModel, spec: AdaptiveSpec) {
    val focusManager = LocalFocusManager.current
    val search = {
        focusManager.clearFocus()
        viewModel.executeSearch()
    }
    ArchivePanel(spec) {
        SectionTitle("Cari herbal", "Ketik nama atau keluhan.")
        HerbTextField(state.keyword, "Kata kunci", viewModel::updateKeyword, singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search), keyboardActions = KeyboardActions(onSearch = { search() }), leadingIcon = HerbIcons.SearchOutlined())
        FlowActions { HerbalCategory.entries.forEach { category -> CategoryChip(category.label, state.category == category) { viewModel.updateCategory(category) } } }
        HerbButton("Cari sekarang", search, Modifier.buttonMaxWidth(spec), loading = state.isLoading)
    }
}

@Composable
private fun ResultsPanel(state: HerbalUiState, spec: AdaptiveSpec) {
    ArchivePanel(spec, background = HerbColors.IvoryGround) {
        SectionTitle("Rekomendasi", "DFS selesai dalam ${state.searchResult.elapsedMs} ms.")
        if (state.message.isNotBlank()) StatusNotice(state.message, StatusTone.Info)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(HerbSpace.Space2)) {
            ThinMetric("Hasil", state.searchResult.items.size.toString(), Modifier.weight(1f))
            ThinMetric("Node DFS", state.searchResult.visitedNodes.toString(), Modifier.weight(1f))
            ThinMetric("DFS", "${state.searchResult.elapsedMs} ms", Modifier.weight(1f))
        }
        Text("Kategori: ${state.category.label}", color = HerbColors.MutedInk, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
internal fun FavoritesScreen(state: HerbalUiState, viewModel: HerbalViewModel, spec: AdaptiveSpec, modifier: Modifier = Modifier) {
    val favorites = state.allItems.filter { state.favoriteIds.contains(it.id) }
    AppPageColumn(spec, modifier) {
        item { Box(Modifier.pageWidth(spec.maxWidth)) { SectionTitle("Favoritmu", "Herbal yang kamu tandai berkumpul sebagai rak bacaan pribadi.") } }
        if (favorites.isEmpty()) {
            item { Box(Modifier.pageWidth(spec.maxWidth)) { EmptyState("BELUM ADA FAVORIT", "Tandai herbal dari Beranda, Jelajah, atau Detail untuk membuat rak pribadimu.", spec) { HerbButton("Jelajahi herbal", { viewModel.navigateTo(AppPage.EXPLORE) }, Modifier.buttonMaxWidth(spec)) } } }
        } else {
            items(favorites, key = { it.id }) { item -> Box(Modifier.pageWidth(spec.maxWidth)) { SpecimenRow(item, true, state.isAdminLoggedIn, spec, { viewModel.toggleFavorite(item.id) }, { viewModel.openDetail(item) }) } }
        }
    }
}

@Composable
internal fun ProfileScreen(state: HerbalUiState, viewModel: HerbalViewModel, spec: AdaptiveSpec, modifier: Modifier = Modifier) {
    val isSignedIn = state.session.type != UserSessionType.SIGNED_OUT
    val name = when (state.session.type) {
        UserSessionType.ACCOUNT -> state.session.email.ifBlank { "Akun Herbal" }
        UserSessionType.GUEST -> "Tamu Herbal"
        UserSessionType.SIGNED_OUT -> "Belum masuk"
    }
    AppPageColumn(spec, modifier) {
        item { Box(Modifier.pageWidth(spec.maxWidth)) { ProfilePanel(state, viewModel, spec, name, isSignedIn) } }
        if (state.isAdminLoggedIn) {
            item { Box(Modifier.pageWidth(spec.maxWidth)) { AdminEntryPanel(viewModel, spec) } }
        }
    }
}

@Composable
private fun ProfilePanel(state: HerbalUiState, viewModel: HerbalViewModel, spec: AdaptiveSpec, name: String, isSignedIn: Boolean) {
    ArchivePanel(spec, strong = true) {
        SectionEyebrow("PROFIL")
        HeroText(name)
        Text(if (isSignedIn) "Kelola sesi." else "Masuk untuk menyimpan favorit.", color = HerbColors.MutedInk, style = MaterialTheme.typography.bodyLarge)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(HerbSpace.Space2)) {
            ThinMetric("Favorit", state.favoriteIds.size.toString(), Modifier.weight(1f))
            ThinMetric("Koleksi", state.allItems.size.toString(), Modifier.weight(1f))
            ThinMetric("Mode", if (state.session.type == UserSessionType.GUEST) "Tamu" else if (isSignedIn) "Admin" else "-", Modifier.weight(1f))
        }
        if (isSignedIn) HerbButton("Keluar", viewModel::logoutUser, Modifier.buttonMaxWidth(spec), tone = HerbButtonTone.Destructive) else HerbButton("Masuk", { viewModel.navigateTo(AppPage.LOGIN) }, Modifier.buttonMaxWidth(spec))
    }
}

@Composable
private fun AdminEntryPanel(viewModel: HerbalViewModel, spec: AdaptiveSpec) {
    ArchivePanel(spec, background = HerbColors.MossSoft) {
        SectionTitle("Ruang admin", "Tambah atau perbarui informasi dan foto herbal untuk pengguna.")
        HerbButton("Buka admin", { viewModel.navigateTo(AppPage.ADMIN) }, Modifier.buttonMaxWidth(spec), tone = HerbButtonTone.Moss)
    }
}
