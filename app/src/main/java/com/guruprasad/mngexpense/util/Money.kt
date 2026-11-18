package com.guruprasad.mngexpense.util

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import kotlin.math.abs
import kotlin.math.round

/**
 * The home currency: every total, chart, budget and report is in it.
 *
 * Compose snapshot state, so any screen that formats money while composing redraws by itself
 * when the user picks another currency. Set at app start from [CurrencyPrefs].
 */
object AppCurrency {
    const val DEFAULT = "INR"

    var code: String by mutableStateOf(DEFAULT)
}

private val INDIA = Locale.forLanguageTag("en-IN")

/** Java's currency for [code], or the default one for an unknown code. */
fun currencyOf(code: String): Currency =
    runCatching { Currency.getInstance(code) }.getOrElse { Currency.getInstance(AppCurrency.DEFAULT) }

/** "₹", "$", "€", "AED" — US-English symbols keep "$" plain instead of "US$". */
fun currencySymbol(code: String = AppCurrency.code): String = currencyOf(code).getSymbol(Locale.US)

/** "Indian Rupee" */
fun currencyName(code: String, locale: Locale = Locale.getDefault()): String = currencyOf(code).getDisplayName(locale)

/**
 * ₹12,400 / $4.50 / AED 250 / ¥1,200.
 * Whole amounts drop the decimals; others show the currency's usual number of digits.
 * Rupees use Indian grouping (₹12,34,567).
 * @param rounded always show a whole number — for estimates and averages ("about ₹28,833")
 */
fun money(v: Double, code: String = AppCurrency.code, rounded: Boolean = false): String {
    val currency = currencyOf(code)
    val digits = currency.defaultFractionDigits.coerceAtLeast(0)
    val whole = rounded || abs(v - round(v)) < 0.005
    val number = NumberFormat.getNumberInstance(if (code == "INR") INDIA else Locale.US).apply {
        minimumFractionDigits = if (whole) 0 else digits
        maximumFractionDigits = if (whole) 0 else digits
    }
    val symbol = currencySymbol(code)
    // "AED 250" reads better than "AED250"; "$250" stays tight
    val gap = if (symbol.last().isLetter()) " " else ""
    return (if (v < 0) "-" else "") + symbol + gap + number.format(abs(v))
}

/** ₹1.2L / ₹3.5k / $1.2M / $850 — short labels for chart bars. Rupees use lakh and crore. */
fun compactMoney(v: Double, code: String = AppCurrency.code): String {
    val symbol = currencySymbol(code)
    fun short(x: Double, suffix: String) = symbol + String.format(Locale.ROOT, "%.1f", x).removeSuffix(".0") + suffix
    return if (code == "INR") when {
        v >= 10_000_000 -> short(v / 10_000_000, "Cr")
        v >= 100_000 -> short(v / 100_000, "L")
        v >= 1_000 -> short(v / 1_000, "k")
        else -> symbol + v.toInt()
    } else when {
        v >= 1_000_000 -> short(v / 1_000_000, "M")
        v >= 1_000 -> short(v / 1_000, "k")
        else -> symbol + v.toInt()
    }
}
