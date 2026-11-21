package com.guruprasad.mngexpense.util

import android.content.Context
import androidx.core.content.edit

/** Home currency and the last exchange rate used for each foreign currency. */
object CurrencyPrefs {
    private const val FILE = "currency_prefs"
    private const val KEY_HOME = "home_currency"
    private const val RATE_PREFIX = "rate_"

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun homeCurrency(ctx: Context): String = prefs(ctx).getString(KEY_HOME, null) ?: AppCurrency.DEFAULT

    /** Saves the choice and switches the whole app to it. */
    fun setHomeCurrency(ctx: Context, code: String) {
        prefs(ctx).edit { putString(KEY_HOME, code) }
        AppCurrency.code = code
    }

    /** Last rate used for [code] into [home], e.g. 1 USD = 83.2 INR, or null if never entered. */
    fun lastRate(ctx: Context, code: String, home: String): Double? =
        prefs(ctx).getString("$RATE_PREFIX${code}_$home", null)?.toDoubleOrNull()

    fun saveRate(ctx: Context, code: String, home: String, rate: Double) {
        prefs(ctx).edit { putString("$RATE_PREFIX${code}_$home", rate.toString()) }
    }

    /** Every saved rate as "USD_INR" → 83.2 (for backups). */
    fun allRates(ctx: Context): Map<String, Double> =
        prefs(ctx).all.mapNotNull { (key, value) ->
            if (!key.startsWith(RATE_PREFIX)) return@mapNotNull null
            val rate = (value as? String)?.toDoubleOrNull() ?: return@mapNotNull null
            key.removePrefix(RATE_PREFIX) to rate
        }.toMap()

    /** Replaces the saved rates with [rates] (from a backup). */
    fun replaceRates(ctx: Context, rates: Map<String, Double>) {
        prefs(ctx).edit {
            prefs(ctx).all.keys.filter { it.startsWith(RATE_PREFIX) }.forEach(::remove)
            rates.forEach { (pair, rate) -> putString("$RATE_PREFIX$pair", rate.toString()) }
        }
    }
}
