package com.guruprasad.mngexpense.util

import android.content.Context

/** Saves the shake on/off switch and sensitivity. */
object ShakePrefs {
    private const val FILE = "shake_prefs"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_SENSITIVITY = "sensitivity"

    private fun sp(ctx: Context) = ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun isEnabled(ctx: Context) = sp(ctx).getBoolean(KEY_ENABLED, false)
    fun setEnabled(ctx: Context, value: Boolean) = sp(ctx).edit().putBoolean(KEY_ENABLED, value).apply()

    /** 1 = hard shake needed … 5 = light shake is enough */
    fun sensitivity(ctx: Context) = sp(ctx).getInt(KEY_SENSITIVITY, 3)
    fun setSensitivity(ctx: Context, value: Int) =
        sp(ctx).edit().putInt(KEY_SENSITIVITY, value.coerceIn(1, 5)).apply()

    fun thresholdG(ctx: Context): Float = when (sensitivity(ctx)) {
        1 -> 3.4f
        2 -> 3.0f
        3 -> 2.7f
        4 -> 2.4f
        else -> 2.1f
    }
}