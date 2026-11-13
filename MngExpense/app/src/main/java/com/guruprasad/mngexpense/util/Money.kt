package com.guruprasad.mngexpense.util

import java.text.NumberFormat
import java.util.Locale

private val inr = Locale.forLanguageTag("en-IN")

/** ₹12,400 */
fun rupees(v: Double): String =
    NumberFormat.getCurrencyInstance(inr).apply { maximumFractionDigits = 0 }.format(v)

/** ₹1.2L, ₹3.5k, ₹850 — short labels for chart bars. */
fun compactRupees(v: Double): String {
    fun short(x: Double, suffix: String): String {
        val s = String.format(Locale.ROOT, "%.1f", x).removeSuffix(".0")
        return "₹$s$suffix"
    }
    return when {
        v >= 100_000 -> short(v / 100_000, "L")
        v >= 1_000 -> short(v / 1_000, "k")
        else -> "₹${v.toInt()}"
    }
}
