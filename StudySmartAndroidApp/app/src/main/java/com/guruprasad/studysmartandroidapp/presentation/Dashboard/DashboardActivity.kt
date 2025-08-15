package com.guruprasad.studysmartandroidapp.presentation.Dashboard

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.guruprasad.studysmartandroidapp.Components.CountCard

@Composable
fun DashboardActivity() {
    Scaffold(
        topBar = {
            DashboardActivityTopBar()
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                CountCardsSection(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    subjectCount = "5",
                    studiedHours = "20",
                    goalStudyHours = "50"
                )
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardActivityTopBar() {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Study Smart", style = MaterialTheme.typography.headlineMedium

            )
        })

}

@Composable
private fun CountCardsSection(
    modifier: Modifier = Modifier,
    subjectCount: String,
    studiedHours: String,
    goalStudyHours: String
) {
    Row(modifier = modifier) {
        CountCard(
            modifier = Modifier.weight(1f), title = "Subject Count", count = subjectCount
        )
        Spacer(modifier = Modifier.width(10.dp))
        CountCard(
            title = "Studied Hours", count = studiedHours, modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(10.dp))
        CountCard(
            title = "Goal Study Hours", count = goalStudyHours, modifier = Modifier.weight(1f)
        )
    }
}
