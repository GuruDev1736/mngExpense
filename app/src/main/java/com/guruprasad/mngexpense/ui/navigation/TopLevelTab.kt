package com.guruprasad.mngexpense.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import com.guruprasad.mngexpense.BackupRoute
import com.guruprasad.mngexpense.BudgetsRoute
import com.guruprasad.mngexpense.CategoriesRoute
import com.guruprasad.mngexpense.DashboardRoute
import com.guruprasad.mngexpense.ExpenseRoute
import com.guruprasad.mngexpense.RecurringListRoute
import com.guruprasad.mngexpense.RecurringRoute
import com.guruprasad.mngexpense.SettingsRoute
import com.guruprasad.mngexpense.TransactionsRoute

/**
 * The main features, always one tap away: a bottom bar on phones, a side rail on tablets,
 * foldables and in landscape. Everything else lives under [SETTINGS].
 */
enum class TopLevelTab(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    /** The screen the tab opens. */
    val root: NavKey
) {
    HOME("Home", Icons.Outlined.Home, Icons.Filled.Home, DashboardRoute),
    TRANSACTIONS("Transactions", Icons.AutoMirrored.Outlined.ReceiptLong, Icons.AutoMirrored.Filled.ReceiptLong, TransactionsRoute()),
    BUDGETS("Budgets", Icons.Outlined.Savings, Icons.Filled.Savings, BudgetsRoute),
    RECURRING("Recurring", Icons.Outlined.EventRepeat, Icons.Filled.EventRepeat, RecurringListRoute),
    SETTINGS("Settings", Icons.Outlined.Settings, Icons.Filled.Settings, SettingsRoute);

    companion object {
        /** The tab a screen belongs to (highlighted while it shows), or null for a full-screen form. */
        fun of(route: NavKey?): TopLevelTab? = when (route) {
            DashboardRoute -> HOME
            is TransactionsRoute -> TRANSACTIONS
            BudgetsRoute -> BUDGETS
            RecurringListRoute -> RECURRING
            SettingsRoute, BackupRoute, CategoriesRoute -> SETTINGS
            is ExpenseRoute, is RecurringRoute -> null
            else -> null
        }

        /** Add / edit forms take the whole screen: no bar, more room for the keyboard. */
        fun showsNavigation(route: NavKey?): Boolean = of(route) != null
    }
}
