package com.guruprasad.mngexpense.util

import android.content.Context
import androidx.core.content.edit

/** "Budget alerts" on/off switch (on by default). */
object BudgetPrefs {
    private const val FILE = "budget_prefs"
    private const val KEY_ALERTS = "alerts_enabled"

    fun alertsEnabled(ctx: Context): Boolean =
        ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE).getBoolean(KEY_ALERTS, true)

    fun setAlertsEnabled(ctx: Context, enabled: Boolean) {
        ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit { putBoolean(KEY_ALERTS, enabled) }
    }
}
