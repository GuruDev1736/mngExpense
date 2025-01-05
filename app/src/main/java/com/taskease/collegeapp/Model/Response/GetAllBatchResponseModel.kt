package com.taskease.collegeapp.Model.Response

data class GetAllBatchResponseModel(
    val CONTENT: List<batches>,
    val MSG: String,
    val STS: String
)

data class batches(
    val batchDescription: String,
    val batchName: String,
    val id: Int
)