package com.taskease.collegeapp.Constants

import android.content.Context
import android.content.SharedPreferences

object SharedPreferencesManager {

    private const val PREF_NAME = "MyAppPreferences"

    // Keys for your shared preference values
    const val EMAIL = "email"
    const val PASSWORD = "password"
    const val TOKEN = "token"
    const val USERNAME = "username"
    const val DEPARTMENTID = "departmentId"
    const val PROFILEPIC = "profilepic"
    const val USERID = "userid"
    const val YEAR_NAME = "yearName"
    const val YEAR_ID = "yearId"
    const val DEPARTMENT_NAME = "departmentName"
    const val DEPARTMENT_ID = "departmentId"
    const val BATCH_ID = "batchId"
    const val BATCH_NAME = "batchName"
    const val HOSTEL_ALLOWANCE = "hostelAllowance"
    const val ROLE = "role"
    const val FULLNAME = "fullname"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // Function to save a String value
    fun saveString(context: Context, key: String, value: String) {
        getSharedPreferences(context).edit().putString(key, value).apply()
    }

    // Function to retrieve a String value
    fun getString(context: Context, key: String, defaultValue: String = ""): String {
        return getSharedPreferences(context).getString(key, defaultValue) ?: defaultValue
    }

    // Function to save a Boolean value
    fun saveBoolean(context: Context, key: String, value: Boolean) {
        getSharedPreferences(context).edit().putBoolean(key, value).apply()
    }

    // Function to retrieve a Boolean value
    fun getBoolean(context: Context, key: String, defaultValue: Boolean = false): Boolean {
        return getSharedPreferences(context).getBoolean(key, defaultValue)
    }

    // Function to save an Integer value
    fun saveInt(context: Context, key: String, value: Int) {
        getSharedPreferences(context).edit().putInt(key, value).apply()
    }

    // Function to retrieve an Integer value
    fun getInt(context: Context, key: String, defaultValue: Int = 0): Int {
        return getSharedPreferences(context).getInt(key, defaultValue)
    }

    // Function to clear specific data in SharedPreferences
    fun remove(context: Context, key: String) {
        getSharedPreferences(context).edit().remove(key).apply()
    }

    // Function to clear all data in SharedPreferences
    fun clearAll(context: Context) {
        getSharedPreferences(context).edit().clear().apply()
    }
}