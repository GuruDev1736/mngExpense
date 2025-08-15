package com.quickhire.gamopedia.Logic.ViewModel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    fun startSplashSequence() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Simulate app initialization
            delay(1000) // Logo animation
            _uiState.value = _uiState.value.copy(showLogo = true)

            delay(500) // Show app name
            _uiState.value = _uiState.value.copy(showAppName = true)

            delay(500) // Show tagline
            _uiState.value = _uiState.value.copy(showTagline = true)

            delay(500) // Show loading
            _uiState.value = _uiState.value.copy(showLoading = true)

            // Actual initialization
            initializeApp()

            delay(1000) // Minimum splash time
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                shouldNavigateToMain = true
            )
        }
    }

    private suspend fun initializeApp() {
        // Add your initialization logic here:
        // - Check authentication status
        // - Load user preferences
        // - Initialize database
        // - Setup analytics
        // - Load configurations

        delay(1500) // Simulate actual initialization time
    }
}

data class SplashUiState(
    val isLoading: Boolean = false,
    val showLogo: Boolean = false,
    val showAppName: Boolean = false,
    val showTagline: Boolean = false,
    val showLoading: Boolean = false,
    val shouldNavigateToMain: Boolean = false,
    val error: String? = null
)