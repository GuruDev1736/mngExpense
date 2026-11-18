package com.guruprasad.mngexpense.domain

import java.time.LocalDate

data class Expense(
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: Category,
    val date: LocalDate,
    val note: String = "",
    /** Id of the [RecurringExpense] that added this expense automatically, if any. */
    val recurringId: Long? = null,
    /**
     * Set for an expense paid in another currency (e.g. "USD"); null = the home currency.
     * [amount] is then the converted home-currency value, which all totals use,
     * and [originalAmount] is what was actually paid in [currency].
     */
    val currency: String? = null,
    val originalAmount: Double? = null
) {
    val isForeign: Boolean get() = currency != null && originalAmount != null

    /** Rate used: 1 [currency] = [exchangeRate] home currency. */
    val exchangeRate: Double? get() = if (isForeign && originalAmount!! > 0) amount / originalAmount else null
}
