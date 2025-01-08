package com.taskease.collegeapp.Model.Request

data class CreateUserRequestModel(
    val email: String,
    val fullName: String,
    val password: String,
    val phoneNo: String,
    val address : String,
    val profile_pic : String
)