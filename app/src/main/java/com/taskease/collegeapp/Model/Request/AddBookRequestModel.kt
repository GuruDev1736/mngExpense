package com.taskease.collegeapp.Model.Request

data class AddBookRequestModel(
    val author: String,
    val bookLink: String,
    val description: String,
    val publisher: String,
    val title: String
)