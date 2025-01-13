package com.taskease.collegeapp.Model.Response

data class TimeTableResponseModel(
    val CONTENT: List<timetable>,
    val MSG: String,
    val STS: String
)

data class timetable(
    val category: String,
    val description: String,
    val downloadUrl: String,
    val id: Int,
    val title: String
)