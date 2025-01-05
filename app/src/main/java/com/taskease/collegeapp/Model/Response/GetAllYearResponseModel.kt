package com.taskease.collegeapp.Model.Response

data class GetAllYearResponseModel(
    val CONTENT: List<years>,
    val MSG: String,
    val STS: String
)

data class years(
    val id: Int,
    val year: String
)