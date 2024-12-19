package com.taskease.collegeapp.Model.Response

data class BannerResponseModel(
    val CONTENT: List<images>,
    val MSG: String,
    val STS: String
)

data class images(
    val creationDate: Long,
    val id: Int,
    val imageLink: String,
    val title: String
)