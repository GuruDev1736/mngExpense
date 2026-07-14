package com.convodoc.app.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.convodoc.app.presentation.Intro.HomeScreen
import com.convodoc.app.presentation.Intro.SplashScreen
import com.convodoc.app.presentation.history.HistoryScreen
import com.convodoc.app.presentation.history.HistoryViewModel
import com.convodoc.app.presentation.imagecompressor.ImageCompressorRoute
import com.convodoc.app.presentation.imagecompressor.ImageCompressorViewModel

@Composable
fun AppNavHost() {

    val navController = rememberNavController()

    val imageCompressorViewModel: ImageCompressorViewModel = viewModel()
    val historyViewModel: HistoryViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = AppDestination.Splash.route
    ) {

        // Splash Screen
        composable(AppDestination.Splash.route) {

            SplashScreen {

                navController.navigate(AppDestination.Home.route) {

                    popUpTo(AppDestination.Splash.route) {
                        inclusive = true
                    }

                    launchSingleTop = true
                }

            }

        }

        // Home Screen
        composable(AppDestination.Home.route) {

            HomeScreen(

                onToolClick = { tool ->

                    if (tool.id == 1) {

                        navController.navigate(
                            AppDestination.ImageCompressor.route
                        ) {

                            launchSingleTop = true

                        }

                    }

                },
                
                onViewAllHistoryClick = {
                    navController.navigate(AppDestination.History.route) {
                        launchSingleTop = true
                    }
                },
                
                historyViewModel = historyViewModel

            )

        }

        // Image Compressor
        composable(AppDestination.ImageCompressor.route) {

            ImageCompressorRoute(

                viewModel = imageCompressorViewModel,

                onBack = {

                    imageCompressorViewModel.reset()

                    navController.popBackStack()

                }

            )

        }
        
        // History Screen
        composable(AppDestination.History.route) {
            
            HistoryScreen(
                
                onBackClick = {
                    navController.popBackStack()
                },
                
                viewModel = historyViewModel
                
            )
            
        }

    }

}