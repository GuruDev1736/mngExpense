package com.taskease.collegeapp.Model.Response

data class LoginResponseModel(
    val CONTENT: CONTENT,
    val MSG: String,
    val STS: String
)

data class CONTENT(
    val fullName: String,
    val token: String,
    val userId: Int,
    val departmentId : Int,
    val userName: String,
    val userProfilePic: String,
    val userRole: String
)