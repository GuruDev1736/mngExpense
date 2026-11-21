package com.guruprasad.mngexpense.data.backup

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.guruprasad.mngexpense.MngExpenseApp
import java.util.concurrent.TimeUnit

/** The scheduled backup: writes a backup file every day / week / month (see [BackupSchedule]). */
class BackupWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val ctx = applicationContext
        if (BackupPrefs.schedule(ctx) == BackupSchedule.OFF) return Result.success()
        if (!BackupStorage.hasAccess(ctx)) {
            // Retrying can't help until the user allows file access: tell them instead
            val message = "File access was turned off, so the scheduled backup couldn't be saved."
            BackupPrefs.recordError(ctx, message)
            BackupNotifier.notifyFailed(ctx, message)
            return Result.success()
        }
        return try {
            (ctx as MngExpenseApp).backupRepository.backupNow(automatic = true)
            BackupNotifier.cancelFailed(ctx)
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Scheduled backup failed", e)
            BackupPrefs.recordError(ctx, "The scheduled backup couldn't be saved: ${e.message ?: e.javaClass.simpleName}")
            if (runAttemptCount < 3) Result.retry()
            else {
                BackupNotifier.notifyFailed(ctx, BackupPrefs.lastError(ctx).orEmpty())
                Result.success()
            }
        }
    }

    companion object {
        private const val TAG = "BackupWorker"
        private const val WORK_NAME = "scheduled-backup"

        /** Emits whenever the scheduled backup changes state (e.g. a run just finished). */
        fun observe(context: Context) = WorkManager.getInstance(context).getWorkInfosForUniqueWorkFlow(WORK_NAME)

        /**
         * Starts, changes or stops the scheduled backup to match [BackupPrefs.schedule].
         * @param changed true when the user just picked a new schedule (replaces the old one);
         *   false on app start (keeps an already scheduled job and its timing).
         */
        fun apply(context: Context, changed: Boolean = false) {
            val wm = WorkManager.getInstance(context)
            val schedule = BackupPrefs.schedule(context)
            if (schedule == BackupSchedule.OFF) {
                wm.cancelUniqueWork(WORK_NAME)
                return
            }
            val request = PeriodicWorkRequestBuilder<BackupWorker>(schedule.days, TimeUnit.DAYS)
                .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
                .build()
            wm.enqueueUniquePeriodicWork(
                WORK_NAME,
                if (changed) ExistingPeriodicWorkPolicy.UPDATE else ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
