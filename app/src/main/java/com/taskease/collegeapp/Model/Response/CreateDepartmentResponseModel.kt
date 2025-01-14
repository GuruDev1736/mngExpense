package com.taskease.collegeapp.Model.Response

data class CreateDepartmentResponseModel(
    val CONTENT: departmentDetails,
    val MSG: String,
    val STS: String
)

data class departmentDetails(
    val description: String,
    val id: Int,
    val name: String
)