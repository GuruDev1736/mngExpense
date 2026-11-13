package com.guruprasad.mngexpense

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object DashboardRoute : NavKey

/** Add a new expense when [id] is null, otherwise edit the expense with that id. */
@Serializable data class ExpenseRoute(val id: Long? = null) : NavKey

@Serializable data object SettingsRoute : NavKey
