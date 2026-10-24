package com.akacaraka.herbal.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

private val MainPages = listOf(AppPage.HOME, AppPage.EXPLORE, AppPage.FAVORITES, AppPage.PROFILE)

@Composable
internal fun HerbScaffold(state: HerbalUiState, viewModel: HerbalViewModel, spec: AdaptiveSpec) {
    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = HerbColors.IvoryGround,
        contentWindowInsets = WindowInsets(0),
        topBar = { if (state.page == AppPage.DETAIL || state.page == AppPage.ADMIN || state.page == AppPage.REPORT || state.page == AppPage.REPORT_DETAIL) ArchiveTopBar(pageTitle(state), state.page, viewModel::navigateBack) },
        bottomBar = { if (state.page in MainPages || state.page == AppPage.REPORT) ArchiveBottomNav(state.page, state, viewModel, spec) }
    ) { padding ->
        val pageModifier = Modifier.padding(padding)
        when (state.page) {
            AppPage.HOME -> HomeScreen(state, viewModel, spec, pageModifier)
            AppPage.EXPLORE -> ExploreScreen(state, viewModel, spec, pageModifier)
            AppPage.FAVORITES -> FavoritesScreen(state, viewModel, spec, pageModifier)
            AppPage.PROFILE -> ProfileScreen(state, viewModel, spec, pageModifier)
            AppPage.REPORT -> ReportScreen(state, viewModel, spec, pageModifier)
            AppPage.REPORT_DETAIL -> ReportDetailScreen(state, viewModel, spec, pageModifier)
            AppPage.DETAIL -> state.selectedItem?.let {
                DetailScreen(it, state.favoriteIds.contains(it.id), state.isAdminLoggedIn, spec, { viewModel.toggleFavorite(it.id) }, viewModel::navigateBack, pageModifier)
            } ?: HomeScreen(state, viewModel, spec, pageModifier)
            AppPage.ADMIN -> AdminScreen(state, viewModel, spec, pageModifier)
            else -> HomeScreen(state, viewModel, spec, pageModifier)
        }
    }
}

private fun pageTitle(state: HerbalUiState): String = when (state.page) {
    AppPage.EXPLORE -> "Jelajah"
    AppPage.FAVORITES -> "Favorit"
    AppPage.PROFILE -> "Profil"
    AppPage.REPORT -> "Menu Laporan"
    AppPage.REPORT_DETAIL -> state.selectedReportType?.label ?: "Laporan"
    AppPage.DETAIL -> state.selectedItem?.localName ?: "Detail"
    AppPage.ADMIN -> "Admin"
    else -> "Beranda"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArchiveTopBar(title: String, page: AppPage, onBack: () -> Unit) {
    TopAppBar(
        navigationIcon = {
            if (page == AppPage.DETAIL || page == AppPage.ADMIN || page == AppPage.REPORT || page == AppPage.REPORT_DETAIL) {
                IconButton(onClick = onBack, modifier = Modifier.width(HerbSpace.TouchTarget).height(HerbSpace.TouchTarget)) {
                    Icon(HerbIcons.Back(), contentDescription = "Kembali", tint = HerbColors.ArchiveInk)
                }
            }
        },
        title = {
            Text(title, color = HerbColors.ArchiveInk, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.semantics { heading() })
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = HerbColors.IvoryGround)
    )
}

@Composable
private fun ArchiveBottomNav(currentPage: AppPage, state: HerbalUiState, viewModel: HerbalViewModel, spec: AdaptiveSpec) {
    val baseItems = if (state.isAdminLoggedIn) {
        NavItems + NavItem(AppPage.REPORT, "Laporan", HerbIcons.Report(), HerbIcons.Report())
    } else {
        NavItems.filter { it.page != AppPage.FAVORITES }
    }
    Box(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = spec.pagePadding, end = spec.pagePadding, top = HerbSpace.Space2, bottom = HerbSpace.Space3),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.widthIn(max = if (spec.isCompact) spec.maxWidth else 600.dp).fillMaxWidth(),
            color = HerbColors.RicePaper,
            shape = HerbRadius.Xl,
            tonalElevation = 1.dp,
            shadowElevation = 8.dp,
            border = BorderStroke(1.dp, HerbColors.RattanLine)
        ) {
            Row(
                Modifier.height(BottomDockHeight).padding(horizontal = HerbSpace.Space2, vertical = HerbSpace.Space2),
                horizontalArrangement = Arrangement.spacedBy(HerbSpace.Space1),
                verticalAlignment = Alignment.CenterVertically
            ) {
                baseItems.forEach { item ->
                    ArchiveDockItem(
                        item = item,
                        selected = currentPage == item.page,
                        onClick = { viewModel.navigateTo(item.page) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

private data class NavItem(val page: AppPage, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector)

private val NavItems = listOf(
    NavItem(AppPage.HOME, "Beranda", HerbIcons.HomeOutlined(), HerbIcons.HomeFilled()),
    NavItem(AppPage.EXPLORE, "Jelajah", HerbIcons.SearchOutlined(), HerbIcons.SearchFilled()),
    NavItem(AppPage.FAVORITES, "Favorit", HerbIcons.FavoriteOutlined(), HerbIcons.FavoriteFilled()),
    NavItem(AppPage.PROFILE, "Profil", HerbIcons.PersonOutlined(), HerbIcons.PersonFilled())
)

@Composable
private fun ArchiveDockItem(item: NavItem, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val container = if (selected) HerbColors.MossPrimary else HerbColors.Transparent
    val content = if (selected) HerbColors.RicePaper else HerbColors.MutedInk
    Surface(
        modifier = modifier
            .height(72.dp)
            .sizeIn(minWidth = HerbSpace.TouchTarget, minHeight = HerbSpace.TouchTarget)
            .clickable(role = Role.Tab, onClick = onClick)
            .semantics {
                role = Role.Tab
                this.selected = selected
                contentDescription = if (selected) "${item.label}, dipilih" else item.label
            },
        color = container,
        shape = HerbRadius.Lg,
        tonalElevation = if (selected) 1.dp else 0.dp
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = HerbSpace.Space1, vertical = HerbSpace.Space2),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(if (selected) item.selectedIcon else item.icon, contentDescription = null, tint = content, modifier = Modifier.size(HerbSpace.Space5))
            Text(item.label, color = content, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
