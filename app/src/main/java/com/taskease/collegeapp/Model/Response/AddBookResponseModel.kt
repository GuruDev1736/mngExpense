package com.taskease.collegeapp.Model.Response

data class AddBookResponseModel(
    val CONTENT: BookDetails,
    val MSG: String,
    val STS: String
)

data class BookDetails(
    val author: String,
    val bookLink: String,
    val description: String,
    val id: Int,
    val publisher: String,
    val title: String,
    val user: User
)

data class User(
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