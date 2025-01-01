package com.taskease.collegeapp.Model.Response

data class GetAllBooksResponseModel(
    val CONTENT: List<Books>,
    val MSG: String,
    val STS: String
)

data class Books(
    val author: String,
    val bookLink: String,
    val description: String,
    val id: Int,
    val publisher: String,
    val title: String,
    val user: UserInfo
)

data class UserInfo(
    val address: String,
    val completeProfile: Boolean,
    val department: Any,
    val designation: String,
    val email: String,
    val enabled: Boolean,
    val fullName: String,
    val id: Int,
    val joinDate: Long,
    val phoneNo: String,
    val profile_pic: String
)