package com.guruprasad.mngexpense.data.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.guruprasad.mngexpense.data.local.AppDatabase
import com.guruprasad.mngexpense.data.repository.CategoryRepository
import com.guruprasad.mngexpense.util.BudgetPrefs
import com.guruprasad.mngexpense.util.CurrencyPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/** A backup that was read and checked, ready to restore. */
data class LoadedBackup(val backup: BackupFile, val source: String) {
    val expenseCount get() = backup.expenses.size
    val createdAt get() = backup.createdAt
}

/**
 * Creates backups (everything: expenses, recurring rules, budgets and their settings) and
 * restores them.
 *
 * @param onRestored after a restore — refreshes widgets and adds recurring expenses now due.
 */
class BackupRepository(
    private val context: Context,
    private val db: AppDatabase,
    private val categories: CategoryRepository,
    private val onRestored: suspend () -> Unit = {}
) {
    /** A scheduled backup and "Back up now" must not write at the same moment as a restore. */
    private val lock = Mutex()

    suspend fun expenseCount(): Int = db.expenseDao().count()

    /** Writes a new backup file to the backup folder. Throws when that fails. */
    suspend fun backupNow(automatic: Boolean): File = lock.withLock {
        withContext(Dispatchers.IO) {
            val backup = snapshot()
            val file = BackupStorage.write(context, BackupCodec.encode(backup), automatic)
            if (automatic) BackupStorage.pruneAutomatic(context)
            BackupPrefs.recordSuccess(context, backup.createdAt)
            file
        }
    }

    /** The newest readable backup in the folder, or null if there is none. */
    suspend fun findLatest(): LoadedBackup? = withContext(Dispatchers.IO) {
        BackupStorage.list(context).firstNotNullOfOrNull { entry ->
            runCatching { load(entry) }.getOrNull() // skip a damaged file, try the next
        }
    }

    suspend fun load(entry: BackupEntry): LoadedBackup = withContext(Dispatchers.IO) {
        LoadedBackup(BackupCodec.decode(BackupStorage.read(entry)), entry.name)
    }

    suspend fun load(uri: Uri): LoadedBackup = withContext(Dispatchers.IO) {
        LoadedBackup(BackupCodec.decode(BackupStorage.read(context, uri)), uri.lastPathSegment ?: "file")
    }

    /**
     * Replaces all data with [loaded]'s. All or nothing: if anything fails, the current data stays.
     * The backup file itself is never changed or removed.
     */
    suspend fun restore(loaded: LoadedBackup) {
        val b = loaded.backup
        lock.withLock {
            db.withTransaction {
                val expenses = db.expenseDao()
                val recurring = db.recurringExpenseDao()
                val budgets = db.budgetDao()
                expenses.deleteAll()
                recurring.deleteAll()
                budgets.deleteAll()
                budgets.deleteAllAlerts()
                recurring.insertAll(b.recurring.map { it.toEntity() })
                expenses.insertAll(b.expenses.map { it.toEntity() })
                budgets.insertAll(b.budgets.map { it.toEntity() })
                budgets.insertAlerts(b.budgetAlerts.map { it.toEntity() })
                // Older backups have no categories: keep this install's
                if (b.categories.isNotEmpty()) {
                    db.categoryDao().deleteAll()
                    db.categoryDao().insertIgnore(b.categories.map { it.toEntity() })
                }
            }
            // Every category an expense uses must exist (and "Other" always)
            categories.repair()
            b.settings.homeCurrency?.let { CurrencyPrefs.setHomeCurrency(context, it) }
            CurrencyPrefs.replaceRates(context, b.settings.exchangeRates)
            BudgetPrefs.setAlertsEnabled(context, b.settings.budgetAlerts)
            // Turn scheduled backups back on (a new install starts with them off), but never
            // change a schedule the user has already picked on this install
            val schedule = BackupSchedule.fromName(b.settings.backupSchedule)
            if (schedule != BackupSchedule.OFF && BackupPrefs.schedule(context) == BackupSchedule.OFF) {
                BackupPrefs.setSchedule(context, schedule)
                BackupWorker.apply(context, changed = true)
            }
        }
        onRestored()
    }

    private suspend fun snapshot(): BackupFile = db.withTransaction {
        BackupFile(
            createdAt = System.currentTimeMillis(),
            expenses = db.expenseDao().getAll().map { it.toBackup() },
            recurring = db.recurringExpenseDao().getAll().map { it.toBackup() },
            budgets = db.budgetDao().getAll().map { it.toBackup() },
            budgetAlerts = db.budgetDao().getAllAlerts().map { it.toBackup() },
            categories = db.categoryDao().getAll().map { it.toBackup() },
            settings = BackupSettings(
                homeCurrency = CurrencyPrefs.homeCurrency(context),
                exchangeRates = CurrencyPrefs.allRates(context),
                budgetAlerts = BudgetPrefs.alertsEnabled(context),
                backupSchedule = BackupPrefs.schedule(context).name
            )
        )
    }
}
