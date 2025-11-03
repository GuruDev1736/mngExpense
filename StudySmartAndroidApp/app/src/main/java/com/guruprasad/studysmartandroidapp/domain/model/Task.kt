package com.guruprasad.studysmartandroidapp.domain.model

data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val dueDate: Long,
    val priority: Int,
    val relatedToSubject: String,
    val taskSubjectId : Int,
    val isCompleted: Boolean
)
