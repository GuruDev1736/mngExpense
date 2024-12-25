package com.taskease.collegeapp.Model.Response

data class GetAllDeadlineResponseModel(
    val CONTENT: List<deadlines>,
    val MSG: String,
    val STS: String
)

data class deadlines(
    val createdDate: Long,
    val description: String,
    val endDate: String,
    val id: Int,
    val startDate: String,
    val title: String,
    val user: User
)

data class User(
    val address: String,
    val completeProfile: Boolean,
    val designation: String,
    val email: String,
    val enabled: Boolean,
    val fullName: String,
    val id: Int,
    val joinDate: Long,
    val phoneNo: String,
    val profile_pic: String
)