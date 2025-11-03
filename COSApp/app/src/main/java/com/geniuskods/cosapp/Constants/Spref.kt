package com.geniuskods.cosapp.Utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object Spref {

    private const val PREF_NAME = "COS_APP_PREFS"
    public const val EMAIL = "email"
    public const val PASSWORD = "password"
    private lateinit var sharedPref: SharedPreferences

    // Initialize once in Application class or first Activity
    fun init(context: Context) {
        sharedPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // Save String
    fun putString(key: String, value: String) {
        sharedPref.edit { putString(key, value) }
    }

    // Get String
    fun getString(key: String, defaultValue: String = ""): String {
        return sharedPref.getString(key, defaultValue) ?: defaultValue
    }

    // Save Boolean
    fun putBoolean(key: String, value: Boolean) {
        sharedPref.edit { putBoolean(key, value) }
    }

    // Get Boolean
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return sharedPref.getBoolean(key, defaultValue)
    }

    // Save Int
    fun putInt(key: String, value: Int) {
        sharedPref.edit { putInt(key, value) }
    }

    // Get Int
    fun getInt(key: String, defaultValue: Int = 0): Int {
        return sharedPref.getInt(key, defaultValue)
    }

    // Remove a specific key
    fun remove(key: String) {
        sharedPref.edit { remove(key) }
    }

    // Clear all data
    fun clear() {
        sharedPref.edit { clear() }
    }

    // Check if key exists
    fun contains(key: String): Boolean {
        return sharedPref.contains(key)
    }
}
