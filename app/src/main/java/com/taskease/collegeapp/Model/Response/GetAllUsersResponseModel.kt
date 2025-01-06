package com.taskease.collegeapp.Model.Response

data class GetAllUsersResponseModel(
    val CONTENT: List<Users>,
    val MSG: String,
    val STS: String
)

data class Users(
    val address: Any,
    val completeProfile: Boolean,
    val department: Any,
    val designation: Any,
    val email: String,
    val enabled: Boolean,
    val fullName: String,
    val id: Int,
    val joinDate: Long,
    val phoneNo: String,
    val profile_pic: Any
)