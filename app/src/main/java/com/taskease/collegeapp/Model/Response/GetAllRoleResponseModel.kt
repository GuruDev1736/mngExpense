package com.taskease.collegeapp.Model.Response

data class GetAllRoleResponseModel(
    val CONTENT: List<Role>,
    val MSG: String,
    val STS: String
)

data class Role(
    val id: Int,
    val roleName: String
)