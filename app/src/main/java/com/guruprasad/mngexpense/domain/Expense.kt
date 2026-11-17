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
    val recurringId: Long? = null
)
