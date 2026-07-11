package com.convodoc.app.presentation.imagecompressor.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ToolScaffold(
    topBar: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {

    Scaffold(

        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),

        topBar = topBar

    ) { padding ->

        content(padding)

    }

}