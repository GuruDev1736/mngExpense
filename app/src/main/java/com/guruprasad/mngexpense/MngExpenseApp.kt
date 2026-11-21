package com.guruprasad.mngexpense

import android.app.Application
import android.util.Log
import androidx.glance.appwidget.updateAll
import com.guruprasad.mngexpense.data.backup.BackupRepository
import com.guruprasad.mngexpense.data.backup.BackupWorker
import com.guruprasad.mngexpense.data.budget.BudgetNotifier
import com.guruprasad.mngexpense.data.export.ReportExporter
import com.guruprasad.mngexpense.data.local.AppDatabase
import com.guruprasad.mngexpense.data.recurring.RecurringWorker
import com.guruprasad.mngexpense.data.repository.BudgetRepository
import com.guruprasad.mngexpense.data.repository.CategoryRepository
import com.guruprasad.mngexpense.data.repository.ExpenseRepository
import com.guruprasad.mngexpense.data.repository.RecurringRepository
import com.guruprasad.mngexpense.data.widget.TodayWidget
import com.guruprasad.mngexpense.util.AppCurrency
import com.guruprasad.mngexpense.util.BudgetPrefs
import com.guruprasad.mngexpense.util.CurrencyPrefs
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking

/** Holds app-wide singletons (simple manual dependency injection). */
class MngExpenseApp : Application() {
    /** For work that should finish even if the screen that started it closes. */
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database: AppDatabase by lazy { AppDatabase.build(this) }

    val budgetRepository: BudgetRepository by lazy { BudgetRepository(database) }

    /** After any change to expenses: refresh widgets and check budgets. */
    private val onExpensesChanged: suspend () -> Unit = {
        // Each step on its own: one failing must not stop the other (or the save)
        try {
            TodayWidget().updateAll(this)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.w(TAG, "Widget refresh failed", e)
        }
        try {
            checkBudgets()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.w(TAG, "Budget check failed", e)
        }
    }

    val expenseRepository: ExpenseRepository by lazy {
        ExpenseRepository(database.expenseDao(), onDataChanged = onExpensesChanged)
    }
    val recurringRepository: RecurringRepository by lazy {
        RecurringRepository(database, onDataChanged = onExpensesChanged)
    }
    val categoryRepository: CategoryRepository by lazy {
        CategoryRepository(database, onDataChanged = onExpensesChanged)
    }

    val reportExporter: ReportExporter by lazy { ReportExporter(this, expenseRepository) }

    val backupRepository: BackupRepository by lazy {
        BackupRepository(this, database, categoryRepository, onRestored = {
            onExpensesChanged()
            // Rules from the backup may have payments due since it was made
            try {
                recurringRepository.processDue()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.w(TAG, "Recurring catch-up after restore failed", e)
            }
        })
    }

    override fun onCreate() {
        super.onCreate()
        AppCurrency.code = CurrencyPrefs.homeCurrency(this)
        // Categories are needed by nearly everything (screens, widget, notifications): load them
        // before anything else runs. A small table; this also opens the database early.
        try {
            runBlocking(Dispatchers.IO) { categoryRepository.reload() }
        } catch (e: Exception) {
            Log.e(TAG, "Loading categories failed; using the built-in ones", e)
        }
        // Due recurring expenses are added by the daily worker (with a notification) and,
        // silently, whenever the app comes to the foreground (MainActivity.onResume).
        // Not here: this also runs when the worker itself wakes the app, and would add them
        // before the worker could tell the user.
        RecurringWorker.schedule(this)
        BackupWorker.apply(this)
    }

    /** Shows "80% used" / "exceeded" notifications for budgets that just crossed a threshold. */
    private suspend fun checkBudgets() {
        if (!BudgetPrefs.alertsEnabled(this)) return
        BudgetNotifier.notify(this, budgetRepository.checkAlerts())
    }

    private companion object {
        const val TAG = "MngExpenseApp"
    }
}
