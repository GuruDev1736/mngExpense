package com.taskease.collegeapp.Model.Response

data class SpreadSheetResponseModel(
    val CONTENT: List<spreadSheet>,
    val MSG: String,
    val STS: String
)

data class spreadSheet(
    val category: String,
    val creationDate: Long,
    val description: String,
    val id: Int,
    val imageLink: String,
    val link: String,
    val title: String
)