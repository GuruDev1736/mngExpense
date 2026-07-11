package com.convodoc.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.convodoc.app.navigation.AppNavHost
import com.convodoc.app.presentation.ui.theme.ConvodocTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ConvodocTheme {
                AppNavHost()
            }
        }
    }
}