package com.geniuskods.cosapp.Model.ResponseModel

data class LoginResponseModel(
    val data: LoginResponse,
    val message: String,
    val status: String
)

data class LoginResponse(
    val email: String,
    val fullName: String,
    val instituteId: Int,
    val isActive: Boolean,
    val profilePicture: String,
    val token: String,
    val userId: Int,
    val userRole: String
)