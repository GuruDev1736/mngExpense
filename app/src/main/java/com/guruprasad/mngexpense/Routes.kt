package com.guruprasad.mngexpense

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object DashboardRoute : NavKey

/** Add a new expense when [id] is null, otherwise edit the expense with that id. */
@Serializable data class ExpenseRoute(val id: Long? = null) : NavKey

@Serializable data object SettingsRoute : NavKey

/** List of recurring expenses (rent, subscriptions, ...). */
@Serializable data object RecurringListRoute : NavKey

/** Add a recurring expense when [id] is null, otherwise edit it. */
@Serializable data class RecurringRoute(val id: Long? = null) : NavKey

/** Monthly budgets: overall + per category. */
@Serializable data object BudgetsRoute : NavKey

/**
 * All transactions with search and filters. Optional starting filters (from a dashboard chart):
 * [category] as its enum name, and a date range as epoch days (both inclusive).
 * [focusSearch] opens the keyboard on the search field.
 */
@Serializable data class TransactionsRoute(
    val category: String? = null,
    val startDay: Long? = null,
    val endDay: Long? = null,
    val focusSearch: Boolean = false
) : NavKey
