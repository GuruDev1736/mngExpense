package com.quickhire.gamopedia.UI.Splash


import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quickhire.gamopedia.Logic.ViewModel.SplashUiState

@Composable
fun SplashScreenContent(uiState: SplashUiState) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1976D2),
                        Color(0xFF42A5F5)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo with Animation
            AnimatedVisibility(
                visible = uiState.showLogo,
                enter = fadeIn(animationSpec = tween(800)) +
                        scaleIn(animationSpec = tween(800))
            ) {
                AppLogo()
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App Name with Animation
            AnimatedVisibility(
                visible = uiState.showAppName,
                enter = fadeIn(animationSpec = tween(600)) +
                        slideInVertically(animationSpec = tween(600)) { it / 2 }
            ) {
                Text(
                    text = "Your KMP App",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tagline with Animation
            AnimatedVisibility(
                visible = uiState.showTagline,
                enter = fadeIn(animationSpec = tween(600))
            ) {
                Text(
                    text = "Powered by Kotlin Multiplatform",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Light
                )
            }

            Spacer(modifier = Modifier.height(64.dp))

            // Loading Indicator with Animation
            AnimatedVisibility(
                visible = uiState.showLoading,
                enter = fadeIn(animationSpec = tween(400))
            ) {
                LoadingIndicator()
            }
        }

        // Version info at bottom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            AnimatedVisibility(
                visible = uiState.showLoading,
                enter = fadeIn(animationSpec = tween(600, delayMillis = 800))
            ) {
                Text(
                    text = "Version 1.0.0",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun AppLogo() {
    Card(
        modifier = Modifier.size(120.dp),
        shape = androidx.compose.foundation.shape.CircleShape,
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🚀",
                fontSize = 48.sp
            )
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(40.dp),
            color = Color.White,
            strokeWidth = 3.dp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Loading...",
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}