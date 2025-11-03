package com.guruprasad.studysmartandroidapp.presentation.Dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.guruprasad.studysmartandroidapp.R
import com.guruprasad.studysmartandroidapp.domain.model.Subject
import com.guruprasad.studysmartandroidapp.domain.model.Task
import com.guruprasad.studysmartandroidapp.presentation.Components.CountCard
import com.guruprasad.studysmartandroidapp.presentation.Components.SubjectCard
import com.guruprasad.studysmartandroidapp.presentation.Components.taskList

@Composable
fun DashboardActivity() {

    val subjectList = listOf(
        Subject(1, "Math", 50f, Subject.subjectCardColors[0]),
        Subject(2, "Science", 40f, Subject.subjectCardColors[1]),
        Subject(3, "History", 30f, Subject.subjectCardColors[2]),
        Subject(4, "Geography", 20f, Subject.subjectCardColors[3]),
        Subject(5, "English", 10f, Subject.subjectCardColors[4])
    )

    val taskList = listOf(
        Task(
            id = 1,
            title = "Complete Math Assignment",
            description = "Finish the assignment on algebra and submit it by tomorrow.",
            isCompleted = false,
            dueDate = 0L,
            priority = 1,
            relatedToSubject = "",
            taskSubjectId = 0
        ),
        Task(
            id = 2,
            title = "Complete Math Assignment",
            description = "Finish the assignment on algebra and submit it by tomorrow.",
            isCompleted = true,
            dueDate = 0L,
            priority = 1,
            relatedToSubject = "",
            taskSubjectId = 0
        )
    )

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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    subjectCount = "5",
                    studiedHours = "20",
                    goalStudyHours = "50"
                )
            }
            item {
                SubjectCardSelection(
                    modifier = Modifier.fillMaxWidth(), subjectList = subjectList
                )
            }
            item {
                Button(
                    onClick = {

                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp, vertical = 20.dp)
                ) {
                    Text(text = "Start Study Session")
                }
            }
            taskList(
                sessionTitle = "Upcoming Tasks", tasks = taskList, // Replace with actual task list
                emptyListText = "You don't have any tasks for today.\n Click the + button to add some tasks."
            )

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

@Composable
private fun SubjectCardSelection(
    modifier: Modifier,
    subjectList: List<Subject>,
    emptyListText: String = "You don't have any subjects yet.\n Click the + button to add some subjects to get started."
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "SUBJECTS",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 12.dp)
            )
            IconButton(onClick = {

            }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Subject",
                )
            }
        }

        if (subjectList.isEmpty()) {
            Image(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.CenterHorizontally),
                painter = painterResource(R.drawable.img_books),
                contentDescription = emptyListText,
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = emptyListText,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp)
        ) {
            items(subjectList) { subject ->
                SubjectCard(
                    subjectName = subject.name,
                    gradientColor = subject.colors,
                    onClick = { /* Handle subject click */ })
            }
        }
    }
}
