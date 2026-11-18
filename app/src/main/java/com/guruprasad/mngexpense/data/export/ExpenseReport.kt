package com.guruprasad.mngexpense.data.export

import com.guruprasad.mngexpense.domain.Category
import com.guruprasad.mngexpense.domain.Expense

/** Everything a report needs, independent of the output format. */
data class ExpenseReport(
    val title: String,          // e.g. "Expense report"
    val periodLabel: String,    // e.g. "November 2026"
    val expenses: List<Expense>, // oldest first
    /** Home currency code: every amount and total is in it. */
    val currency: String = "INR"
) {
    /** Some expenses were paid in another currency — reports then show what was actually paid. */
    val hasForeign: Boolean = expenses.any { it.isForeign }

    val total: Double = expenses.sumOf { it.amount }

    val byCategory: List<Pair<Category, Double>> = expenses
        .groupBy { it.category }
        .map { (c, list) -> c to list.sumOf { it.amount } }
        .sortedByDescending { it.second }
}
