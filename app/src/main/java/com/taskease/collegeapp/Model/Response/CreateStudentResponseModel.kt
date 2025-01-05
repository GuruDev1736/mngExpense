package com.taskease.collegeapp.Model.Response

data class CreateStudentResponseModel(
    val CONTENT: Student,
    val MSG: String,
    val STS: String
)

data class Student(
    val batch: allocatedBatch,
    val creationDate: Long,
    val department: allocatedDepartment,
    val email: String,
    val enrollment: String,
    val fullName: String,
    val hostelAdmission: Boolean,
    val id: Int,
    val password: String,
    val phone: String,
    val profile_pic: String,
    val year: allocatedYear
)

data class allocatedBatch(
    val batchDescription: String,
    val batchName: String,
    val id: Int
)

data class allocatedDepartment(
    val description: String,
    val id: Int,
    val name: String
)

data class allocatedYear(
    val id: Int,
    val year: String
)