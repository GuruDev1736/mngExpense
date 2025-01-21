package com.taskease.collegeapp.Model.Response

data class ShowAllAdmissionResponseModel(
    val CONTENT: List<admissions>,
    val MSG: String,
    val STS: String
)

data class admissions(
    val aadharLink: String,
    val address: String,
    val department: String,
    val diplomaPercentage: String,
    val enabled: Boolean,
    val id: Int,
    val parentPhoneNo: String,
    val schoolPercentage: String,
    val studentName: String,
    val studentPhoneNo: String,
    val year: String
)