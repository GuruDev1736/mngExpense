package com.taskease.collegeapp.Model.Response

data class CreateDeadlineResponseModel(
    val CONTENT: deadline,
    val MSG: String,
    val STS: String
)

data class deadline(
    val createdDate: Long,
    val description: String,
    val endDate: String,
    val id: Int,
    val startDate: String,
    val title: String,
)
