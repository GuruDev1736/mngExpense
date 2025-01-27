package com.taskease.collegeapp.Model.Response

data class GetAllBonafideResponseModel(
    val CONTENT: List<Bonafide>,
    val MSG: String,
    val STS: String
)

data class Bonafide(
    val description: String,
    val id: Int,
    val student: Student,
    val title: String
)