package com.taskease.collegeapp.Model.Response

data class GetAllReportResponseModel(
    val CONTENT: List<Report>,
    val MSG: String,
    val STS: String
)

data class Report(
    val description: String,
    val id: Int,
    val link: String,
    val status: String,
    val student: stud,
    val title: String
)

data class stud(
    val batch: classs,
    val creationDate: Long,
    val department: depart,
    val email: String,
    val enrollment: String,
    val fullName: String,
    val hostelAdmission: Boolean,
    val id: Int,
    val password: String,
    val phone: String,
    val profile_pic: String,
    val year: yyear
)

data class classs(
    val batchDescription: String,
    val batchName: String,
    val id: Int
)

data class depart(
    val description: String,
    val id: Int,
    val name: String
)

data class yyear(
    val id: Int,
    val year: String
)