package com.guruprasad.mngexpense

import android.app.Application
import com.guruprasad.mngexpense.data.export.ReportExporter
import com.guruprasad.mngexpense.data.local.AppDatabase
import com.guruprasad.mngexpense.data.repository.ExpenseRepository
import com.guruprasad.mngexpense.data.widget.TodayWidget
import androidx.glance.appwidget.updateAll

/** Holds app-wide singletons (simple manual dependency injection). */
class MngExpenseApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.build(this) }
    val expenseRepository: ExpenseRepository by lazy {
        // Refresh every placed "Today's spending" widget after any change
        ExpenseRepository(database.expenseDao(), onDataChanged = { TodayWidget().updateAll(this) })
    }
    val reportExporter: ReportExporter by lazy { ReportExporter(this, expenseRepository) }
}
