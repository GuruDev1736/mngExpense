package com.taskease.collegeapp.Model.Response

data class CreateDeadlineResponseModel(
    val CONTENT: deadline,
    val MSG: String,
    val STS: String
)

data class deadline(
    val createdDate: Long,
    val description: String,
    val endDate: String,
    val id: Int,
    val startDate: String,
    val title: String,
    val user: creater
)

data class creater(
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
