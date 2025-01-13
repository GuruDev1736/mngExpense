package com.taskease.collegeapp.Model.Request

data class CreateTimeTableRequestModel(
    val category: String,
    val description: String,
    val downloadUrl: String,
    val title: String
)