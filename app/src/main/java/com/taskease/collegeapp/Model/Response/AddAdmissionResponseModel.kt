package com.taskease.collegeapp.Model.Response

data class AddAdmissionResponseModel(
    val CONTENT: Admission?,
    val MSG: String,
    val STS: String
)

data class Admission(
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