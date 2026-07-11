package com.convodoc.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.convodoc.app.Activities.Intro.HomeScreen
import com.convodoc.app.Activities.Intro.SplashScreen
import com.convodoc.app.presentation.imagecompressor.ImageCompressorRoute
import com.convodoc.app.presentation.imagecompressor.ImageCompressorViewModel
import com.convodoc.app.presentation.imagecompressor.result.CompressionResultScreen
import com.convodoc.app.utils.FileDeleteUtils
import com.convodoc.app.utils.FileOpener
import com.convodoc.app.utils.ShareUtils

@Composable
fun AppNavHost() {

    val navController = rememberNavController()

    val viewModel: ImageCompressorViewModel = viewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    NavHost(

        navController = navController,

        startDestination = AppDestination.Splash.route

    ) {

        composable(AppDestination.Splash.route) {

            SplashScreen {

                navController.navigate(AppDestination.Home.route) {

                    popUpTo(AppDestination.Splash.route) {

                        inclusive = true

                    }

                }

            }

        }

        composable(AppDestination.Home.route) {

            HomeScreen(

                onToolClick = {

                    if (it.id == 1) {

                        navController.navigate(
                            AppDestination.ImageCompressor.route
                        )

                    }

                }

            )

        }

        composable(AppDestination.ImageCompressor.route) {

            ImageCompressorRoute(

                viewModel = viewModel,

                onBack = {

                    navController.popBackStack()

                },

                onCompressionSuccess = {

                    navController.navigate(
                        AppDestination.CompressionResult.route
                    )

                }

            )

        }

        composable(AppDestination.CompressionResult.route) {
            if (state.compressionResult == null) {

                navController.popBackStack()

                return@composable

            }

            state.compressionResult?.let {

                CompressionResultScreen(

                    result = it,

                    onShare = {

                        ShareUtils.shareImage(

                            context,

                            it.outputUri

                        )

                    },

                    onOpen = {

                        FileOpener.open(

                            context,

                            it.outputUri

                        )

                    },

                    onDelete = {

                        FileDeleteUtils.delete(

                            context,

                            it.outputUri

                        )

                        viewModel.reset()
                        navController.navigate(AppDestination.ImageCompressor.route) {
                            popUpTo(AppDestination.CompressionResult.route) {
                                inclusive = true
                            }
                        }

                    },

                    onCompressAnother = {

                        viewModel.reset()

                        navController.popBackStack(
                            AppDestination.ImageCompressor.route,
                            false
                        )

                    }

                )

            }

        }

    }

}