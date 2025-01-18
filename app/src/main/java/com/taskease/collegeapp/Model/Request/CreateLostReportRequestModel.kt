package com.taskease.collegeapp.Model.Request

data class CreateLostReportRequestModel(
    val description: String,
    val link: String,
    val status: String,
    val title: String
)