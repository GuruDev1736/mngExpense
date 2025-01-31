package com.taskease.collegeapp.Model.Response

data class LoginResponseModel(
    val CONTENT: CONTENT,
    val MSG: String,
    val STS: String
)

data class CONTENT(
    val batchId: Int,
    val batchName: String,
    val departmentId: Int,
    val departmentName: String,
    val fullName: String,
    val hostelAllowance: Boolean,
    val token: String,
    val userId: Int,
    val userName: String,
    val userProfilePic: String,
    val userRole: String,
    val yearId: Int,
    val yearName: String,
    val enrollement : String
)