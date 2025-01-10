package com.taskease.collegeapp.Model.Response

data class CreateStoreResponseModel(
    val CONTENT: storeObject,
    val MSG: String,
    val STS: String
)

data class storeObject(
    val category: String,
    val id: Int,
    val status: Boolean
)