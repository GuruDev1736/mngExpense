package com.taskease.collegeapp.Model.Response

data class AddStockResponseModel(
    val CONTENT: StockItem,
    val MSG: String,
    val STS: String
)

data class StockItem(
    val description: String,
    val id: Int,
    val price: String,
    val status: Boolean,
    val title: String
)