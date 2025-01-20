package com.taskease.collegeapp.Model.Request

data class AddAdmissionRequestModel(
    val aadharLink: String,
    val address: String,
    val department: String,
    val diplomaPercentage: String,
    val parentPhoneNo: String,
    val schoolPercentage: String,
    val studentName: String,
    val studentPhoneNo: String,
    val year: String
)