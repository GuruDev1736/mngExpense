package com.taskease.collegeapp.Model.Response

data class GetAllDepartmentResponseModel(
    val CONTENT: List<department>,
    val MSG: String,
    val STS: String
)

data class department(
    val description: String,
    val id: Int,
    val name: String
)