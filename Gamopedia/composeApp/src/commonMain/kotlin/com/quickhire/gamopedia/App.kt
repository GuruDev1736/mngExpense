package com.quickhire.gamopedia

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.quickhire.gamopedia.UI.Splash.SplashScreenVoyager

@Composable
fun App() {
    MaterialTheme {
        Navigator(SplashScreenVoyager()) { navigator ->
            SlideTransition(navigator)
        }
    }
}