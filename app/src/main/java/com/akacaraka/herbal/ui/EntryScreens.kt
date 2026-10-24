package com.akacaraka.herbal.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.akacaraka.herbal.R

@Composable
internal fun SplashScreen(spec: AdaptiveSpec) {
    Box(
        Modifier
            .fillMaxSize()
            .background(HerbColors.IvoryGround)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(spec.pagePadding),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.pageWidth(spec.authMaxWidth),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(HerbSpace.Space4)
        ) {
            AppMark(large = true)
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(HerbSpace.Space1)) {
                Text("T&M Herbal", color = HerbColors.ArchiveInk, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
                Text("Menyiapkan sesi dan arsip herbal.", color = HerbColors.MutedInk, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
            }
            CircularProgressIndicator(color = HerbColors.MossPrimary, strokeWidth = 2.dp, modifier = Modifier.size(HerbSpace.Space8))
        }
    }
}

@Composable
internal fun OnboardingScreen(state: HerbalUiState, viewModel: HerbalViewModel, spec: AdaptiveSpec) {
    Box(
        Modifier
            .fillMaxSize()
            .background(HerbColors.IvoryGround)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(spec.pagePadding),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.pageWidth(spec.authMaxWidth),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(HerbSpace.Space5)
        ) {
            AppIdentityBlock(
                title = "T&M Herbal",
                body = entrySubtitle(state)
            )
            ArchivePanel(spec, strong = true) {
                SectionEyebrow("MASUK")
                Text("Pilih cara masuk", color = HerbColors.ArchiveInk, style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
                Text("Masuk untuk menyimpan akun, daftar bila baru pertama kali, atau jelajahi dulu sebagai tamu.", color = HerbColors.MutedInk, style = MaterialTheme.typography.bodyMedium)
                HerbButton("Masuk", { viewModel.navigateTo(AppPage.LOGIN) }, Modifier.fillMaxWidth())
                HerbButton("Daftar", { viewModel.navigateTo(AppPage.REGISTER) }, Modifier.fillMaxWidth(), tone = HerbButtonTone.Moss)
                HerbButton("Lanjut sebagai tamu", viewModel::continueAsGuest, Modifier.fillMaxWidth(), tone = HerbButtonTone.Secondary)
            }
        }
    }
}

@Composable
private fun AppIdentityBlock(title: String, body: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(HerbSpace.Space3)) {
        AppMark()
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(HerbSpace.Space1)) {
            Text(title, color = HerbColors.ArchiveInk, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center, modifier = Modifier.semantics { heading() })
            Text(body, color = HerbColors.MutedInk, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun AppMark(large: Boolean = false) {
    val size = if (large) 72.dp else 60.dp
    Surface(Modifier.size(size), color = HerbColors.RicePaper, shape = HerbRadius.Lg, shadowElevation = 2.dp) {
        Image(
            painter = painterResource(R.drawable.app_logo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().padding(if (large) HerbSpace.Space2 else HerbSpace.Space1),
            contentScale = ContentScale.Fit
        )
    }
}

private fun entrySubtitle(state: HerbalUiState): String = if (state.allItems.isEmpty()) {
    "Cari tanaman dan minuman herbal dari satu arsip yang ringkas."
} else {
    "${state.allItems.size} catatan herbal siap dicari dari nama, manfaat, atau keluhan."
}

@Composable
internal fun AuthScreen(state: HerbalUiState, viewModel: HerbalViewModel, spec: AdaptiveSpec, isRegister: Boolean) {
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current
    val keyboardVisible = WindowInsets.ime.getBottom(density) > 0
    val submit = if (isRegister) viewModel::registerUser else viewModel::loginUser
    Box(
        Modifier
            .fillMaxSize()
            .background(HerbColors.IvoryGround)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .imePadding()
            .padding(spec.pagePadding),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier
                .pageWidth(spec.authMaxWidth)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(HerbSpace.Space3)
        ) {
            ArchivePanel(spec, strong = true) {
                SectionEyebrow("AKUN")
                Text(if (isRegister) "Buat akun" else "Masuk", color = HerbColors.ArchiveInk, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.semantics { heading() })
                Text(if (isRegister) "Simpan favoritmu. Password min. 6 karakter." else "Lanjut ke etalase herbalmu.", color = HerbColors.MutedInk, style = MaterialTheme.typography.bodyMedium)
                if (isRegister) HerbTextField(state.authName, "Nama", viewModel::updateAuthName, singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
                HerbTextField(state.authEmail, "Email", viewModel::updateAuthEmail, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next))
                HerbTextField(state.authPassword, "Password", viewModel::updateAuthPassword, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(); submit() }), visualTransformation = PasswordVisualTransformation())
                if (state.authError.isNotBlank()) StatusNotice(state.authError, StatusTone.Error)
                HerbButton(if (isRegister) "Daftar" else "Masuk", submit, Modifier.fillMaxWidth(), enabled = !state.isLoading, loading = state.isLoading)
                if (!keyboardVisible) AuthSecondaryActions(isRegister, viewModel)
            }
        }
    }
}

@Composable
private fun AuthSecondaryActions(isRegister: Boolean, viewModel: HerbalViewModel) {
    val switchLabel = if (isRegister) "Sudah punya akun" else "Buat akun"
    val switchTarget = if (isRegister) AppPage.LOGIN else AppPage.REGISTER
    FlowActions {
        HerbTextAction(switchLabel, { viewModel.navigateTo(switchTarget) }, HerbColors.MossPrimary)
        HerbTextAction("Mode tamu", viewModel::continueAsGuest, HerbColors.TerracottaAccent)
        HerbTextAction("Kembali", { viewModel.navigateTo(AppPage.ONBOARDING) }, HerbColors.MossPrimary)
    }
}
