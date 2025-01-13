package com.taskease.collegeapp.Model.Response

data class CreateTimeTableResponseModel(
    val CONTENT: timeTableDetail,
    val MSG: String,
    val STS: String
)

data class timeTableDetail(
    val category: String,
    val description: String,
    val downloadUrl: String,
    val id: Int,
    val title: String
)