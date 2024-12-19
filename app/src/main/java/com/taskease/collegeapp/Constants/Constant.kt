package com.taskease.collegeapp.Constants

import android.content.Context
import android.widget.Toast

object Constant {

    fun toast(context: Context, message: String) {
        Toast.makeText(context,message,Toast.LENGTH_LONG).show()
    }
}