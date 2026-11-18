package com.guruprasad.mngexpense.domain

import java.util.Currency
import kotlin.math.roundToLong

/** Currency list for the pickers, and conversion into the home currency. */
object Currencies {
    /** Shown first in pickers: the currencies Indian users most often travel or shop in. */
    val popular: List<String> = listOf(
        "INR", "USD", "EUR", "GBP", "AED", "SAR", "QAR", "SGD", "THB", "MYR",
        "JPY", "AUD", "CAD", "CHF", "CNY", "HKD", "NPR", "LKR", "BDT", "NZD"
    )

    /** Every real currency Java knows, without precious metals, funds and test codes (XAU, XTS...). */
    fun all(): List<String> = Currency.getAvailableCurrencies()
        .map { it.currencyCode }
        .filter { !it.startsWith("X") }
        .sorted()

    /** [original] × [rate], rounded to 2 decimals (paise / cents). */
    fun convert(original: Double, rate: Double): Double = (original * rate * 100).roundToLong() / 100.0
}
