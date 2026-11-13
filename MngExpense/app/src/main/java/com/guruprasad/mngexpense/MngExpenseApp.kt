package com.guruprasad.mngexpense

import android.app.Application
import com.guruprasad.mngexpense.data.export.ReportExporter
import com.guruprasad.mngexpense.data.local.AppDatabase
import com.guruprasad.mngexpense.data.repository.ExpenseRepository

/** Holds app-wide singletons (simple manual dependency injection). */
class MngExpenseApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.build(this) }
    val expenseRepository: ExpenseRepository by lazy { ExpenseRepository(database.expenseDao()) }
    val reportExporter: ReportExporter by lazy { ReportExporter(this, expenseRepository) }
}
