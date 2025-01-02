package com.taskease.collegeapp.Model.Response

data class GetAllStocksResponseModel(
    val CONTENT: List<Stocks>,
    val MSG: String,
    val STS: String
)

data class Stocks(
    val description: String,
    val id: Int,
    val price: String,
    val status: Boolean,
    val title: String
)