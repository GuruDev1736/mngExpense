package com.taskease.collegeapp.Model.Response

data class CreateYearResponseModel(
    val CONTENT: YearDetail,
    val MSG: String,
    val STS: String
)

data class YearDetail(
    val id: Int,
    val year: String
)