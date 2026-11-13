package com.guruprasad.mngexpense.data.export

import com.guruprasad.mngexpense.domain.Category
import com.guruprasad.mngexpense.domain.Expense

/** Everything a report needs, independent of the output format. */
data class ExpenseReport(
    val title: String,          // e.g. "Expense report"
    val periodLabel: String,    // e.g. "November 2026"
    val expenses: List<Expense> // oldest first
) {
    val total: Double = expenses.sumOf { it.amount }

    val byCategory: List<Pair<Category, Double>> = expenses
        .groupBy { it.category }
        .map { (c, list) -> c to list.sumOf { it.amount } }
        .sortedByDescending { it.second }
}
