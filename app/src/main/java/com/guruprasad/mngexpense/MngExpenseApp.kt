package com.guruprasad.mngexpense

import android.app.Application
import androidx.glance.appwidget.updateAll
import com.guruprasad.mngexpense.data.export.ReportExporter
import com.guruprasad.mngexpense.data.local.AppDatabase
import com.guruprasad.mngexpense.data.recurring.RecurringWorker
import com.guruprasad.mngexpense.data.repository.ExpenseRepository
import com.guruprasad.mngexpense.data.repository.RecurringRepository
import com.guruprasad.mngexpense.data.widget.TodayWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Holds app-wide singletons (simple manual dependency injection). */
class MngExpenseApp : Application() {
    /** For work that should finish even if the screen that started it closes. */
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database: AppDatabase by lazy { AppDatabase.build(this) }

    // Refresh every placed "Today's spending" widget after any change
    private val refreshWidgets: suspend () -> Unit = { TodayWidget().updateAll(this) }

    val expenseRepository: ExpenseRepository by lazy {
        ExpenseRepository(database.expenseDao(), onDataChanged = refreshWidgets)
    }
    val recurringRepository: RecurringRepository by lazy {
        RecurringRepository(database, onDataChanged = refreshWidgets)
    }
    val reportExporter: ReportExporter by lazy { ReportExporter(this, expenseRepository) }

    override fun onCreate() {
        super.onCreate()
        // Due recurring expenses are added by the daily worker (with a notification) and,
        // silently, whenever the app comes to the foreground (MainActivity.onResume).
        // Not here: this also runs when the worker itself wakes the app, and would add them
        // before the worker could tell the user.
        RecurringWorker.schedule(this)
    }
}
