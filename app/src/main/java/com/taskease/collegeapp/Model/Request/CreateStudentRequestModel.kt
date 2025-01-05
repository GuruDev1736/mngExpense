package com.taskease.collegeapp.Model.Request

data class CreateStudentRequestModel(
    val email: String,
    val enrollment: String,
    val fullName: String,
    val password: String,
    val phone: String,
    val profile_pic: String
)