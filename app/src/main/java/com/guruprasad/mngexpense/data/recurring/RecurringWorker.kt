package com.guruprasad.mngexpense.data.recurring

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.guruprasad.mngexpense.MngExpenseApp
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

/**
 * Runs once a day, shortly after midnight, and adds the recurring expenses that are due.
 * (The app also catches up every time it starts, so a late or skipped run loses nothing.)
 */
class RecurringWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = try {
        val app = applicationContext as MngExpenseApp
        val added = app.recurringRepository.processDue()
        if (added.isNotEmpty()) RecurringNotifier.notifyAdded(applicationContext, added)
        Result.success()
    } catch (e: Exception) {
        Log.e(TAG, "Adding recurring expenses failed", e)
        Result.retry()
    }

    companion object {
        private const val TAG = "RecurringWorker"
        private const val WORK_NAME = "recurring-expenses-daily"

        /** Safe to call on every app start: an already scheduled job is kept. */
        fun schedule(context: Context) {
            val now = LocalDateTime.now()
            val nextRun = LocalDate.now().plusDays(1).atTime(LocalTime.of(0, 5))
            val request = PeriodicWorkRequestBuilder<RecurringWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(Duration.between(now, nextRun).toMillis(), TimeUnit.MILLISECONDS)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }
    }
}
