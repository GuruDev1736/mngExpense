package com.taskease.collegeapp.Model.Response

data class AddBatchResponseModel(
    val CONTENT: batch,
    val MSG: String,
    val STS: String
)

data class batch(
    val batchDescription: String,
    val batchName: String,
    val id: Int
)