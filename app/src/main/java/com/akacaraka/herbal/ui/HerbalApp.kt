package com.akacaraka.herbal.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HerbalApp(viewModel: HerbalViewModel) {
    val state by viewModel.state.collectAsState()
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val width = this.maxWidth
        HerbTheme(compact = width < 600.dp) {
            val spec = rememberAdaptiveSpec(width)
            BackHandler(enabled = viewModel.canNavigateBack(state)) { viewModel.navigateBack() }
            when (state.page) {
                AppPage.SPLASH -> SplashScreen(spec)
                AppPage.ONBOARDING -> OnboardingScreen(state, viewModel, spec)
                AppPage.LOGIN -> AuthScreen(state, viewModel, spec, isRegister = false)
                AppPage.REGISTER -> AuthScreen(state, viewModel, spec, isRegister = true)
                else -> HerbScaffold(state, viewModel, spec)
            }
        }
    }
}
