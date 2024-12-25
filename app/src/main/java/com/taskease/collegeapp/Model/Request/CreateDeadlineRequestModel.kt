package com.taskease.collegeapp.Model.Request

data class CreateDeadlineRequestModel(
    val description: String,
    val endDate: String,
    val startDate: String,
    val title: String
)