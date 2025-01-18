package com.taskease.collegeapp.Model.Response

data class CreateLostReportResponseModel(
    val CONTENT: details,
    val MSG: String,
    val STS: String
)

data class details(
    val description: String,
    val id: Int,
    val link: String,
    val status: String,
    val title: String
)