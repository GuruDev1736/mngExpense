package com.taskease.collegeapp.Model


import androidx.appcompat.app.AppCompatActivity

data class DashboardItem(
    val image : Int,
    val title : String,
    val activityClass: Class<out AppCompatActivity>
)