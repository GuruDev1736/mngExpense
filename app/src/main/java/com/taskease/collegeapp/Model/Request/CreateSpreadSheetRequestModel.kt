package com.taskease.collegeapp.Model.Request

data class CreateSpreadSheetRequestModel(
    val category: String,
    val description: String,
    val imageLink: String,
    val link: String,
    val title: String
)