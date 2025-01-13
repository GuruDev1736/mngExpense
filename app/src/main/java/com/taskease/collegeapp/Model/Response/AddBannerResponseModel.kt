package com.taskease.collegeapp.Model.Response

data class AddBannerResponseModel(
    val CONTENT: Banner,
    val MSG: String,
    val STS: String
)

data class Banner(
    val creationDate: Long,
    val id: Int,
    val imageLink: String,
    val title: String
)