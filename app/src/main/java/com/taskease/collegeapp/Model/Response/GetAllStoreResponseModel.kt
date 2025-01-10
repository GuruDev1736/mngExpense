package com.taskease.collegeapp.Model.Response

data class GetAllStoreResponseModel(
    val CONTENT: List<stores>,
    val MSG: String,
    val STS: String
)

data class stores(
    val category: String,
    val id: Int,
    val status: Boolean
)