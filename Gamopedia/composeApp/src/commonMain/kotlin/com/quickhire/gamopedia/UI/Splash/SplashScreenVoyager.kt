package com.quickhire.gamopedia.UI.Splash

import androidx.compose.runtime.*
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.quickhire.gamopedia.Logic.ViewModel.SplashViewModel
import com.quickhire.gamopedia.UI.Main.MainScreenVoyager

class SplashScreenVoyager : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = remember { SplashViewModel() }
        SplashContent(
            viewModel = viewModel,
            onNavigateToMain = {
                navigator.replace(MainScreenVoyager())
            }
        )
    }
}

@Composable
private fun SplashContent(
    viewModel: SplashViewModel,
    onNavigateToMain: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle navigation
    LaunchedEffect(uiState.shouldNavigateToMain) {
        if (uiState.shouldNavigateToMain) {
            onNavigateToMain()
        }
    }

    // Start splash sequence when screen loads
    LaunchedEffect(Unit) {
        viewModel.startSplashSequence()
    }

    // Your existing splash content composable
    SplashScreenContent(uiState = uiState)
}