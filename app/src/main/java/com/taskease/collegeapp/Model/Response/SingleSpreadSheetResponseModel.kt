package com.taskease.collegeapp.Model.Response

data class SingleSpreadSheetResponseModel(
    val CONTENT: sheet,
    val MSG: String,
    val STS: String
)

data class sheet(
    val category: String,
    val creationDate: Long,
    val description: String,
    val id: Int,
    val imageLink: String,
    val link: String,
    val title: String
)