package com.guruprasad.mngexpense.data.backup

import com.guruprasad.mngexpense.data.local.BudgetAlertEntity
import com.guruprasad.mngexpense.data.local.BudgetEntity
import com.guruprasad.mngexpense.data.local.CategoryEntity
import com.guruprasad.mngexpense.data.local.ExpenseEntity
import com.guruprasad.mngexpense.data.local.RecurringExpenseEntity
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * The backup file: everything needed to rebuild the app's data on a new install.
 *
 * Its own classes rather than the Room entities, so the file format stays the same when the
 * database changes. Add new fields with a default value: older backups then still load.
 * Bump [CURRENT_VERSION] only for a change older app versions cannot read.
 */
@Serializable
data class BackupFile(
    /** Marks the file as ours, so a random JSON file is refused. */
    val app: String = APP_ID,
    val version: Int = CURRENT_VERSION,
    /** When the backup was made, epoch milliseconds. */
    val createdAt: Long,
    val expenses: List<BackupExpense> = emptyList(),
    val recurring: List<BackupRecurring> = emptyList(),
    val budgets: List<BackupBudget> = emptyList(),
    val budgetAlerts: List<BackupBudgetAlert> = emptyList(),
    /** Empty in backups made before custom categories: those only used the built-in ones. */
    val categories: List<BackupCategory> = emptyList(),
    val settings: BackupSettings = BackupSettings()
) {
    companion object {
        const val APP_ID = "mngexpense"
        const val CURRENT_VERSION = 1
    }
}

@Serializable
data class BackupExpense(
    val id: Long,
    val title: String,
    val amount: Double,
    val category: String,
    /** LocalDate.toEpochDay, like the database. */
    val epochDay: Long,
    val note: String = "",
    val recurringId: Long? = null,
    val currency: String? = null,
    val originalAmount: Double? = null
)

@Serializable
data class BackupRecurring(
    val id: Long,
    val title: String,
    val amount: Double,
    val category: String,
    val note: String = "",
    val frequency: String,
    val every: Int,
    val startDay: Long,
    val endDay: Long? = null,
    val nextIndex: Int,
    val paused: Boolean = false
)

@Serializable
data class BackupCategory(val key: String, val name: String, val color: Long, val icon: String, val sortOrder: Int)

@Serializable
data class BackupBudget(val key: String, val amount: Double)

@Serializable
data class BackupBudgetAlert(val month: String, val key: String, val level: Int)

@Serializable
data class BackupSettings(
    val homeCurrency: String? = null,
    /** "USD_INR" → 83.2 : last exchange rate entered for each currency pair. */
    val exchangeRates: Map<String, Double> = emptyMap(),
    val budgetAlerts: Boolean = true,
    /** [BackupSchedule] name, so scheduled backups carry on after a restore on a new install. */
    val backupSchedule: String? = null
)

/** The backup can't be used: not our file, damaged, or made by a newer app version. */
class InvalidBackupException(message: String, cause: Throwable? = null) : Exception(message, cause)

object BackupCodec {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true // fields added by newer app versions
        encodeDefaults = true
    }

    fun encode(backup: BackupFile): String = json.encodeToString(BackupFile.serializer(), backup)

    /** @throws InvalidBackupException when [text] isn't a backup this version can restore */
    fun decode(text: String): BackupFile {
        val backup = try {
            json.decodeFromString(BackupFile.serializer(), text)
        } catch (e: Exception) {
            throw InvalidBackupException("This file isn't a MngExpense backup, or it is damaged.", e)
        }
        if (backup.app != BackupFile.APP_ID) {
            throw InvalidBackupException("This file isn't a MngExpense backup.")
        }
        if (backup.version > BackupFile.CURRENT_VERSION) {
            throw InvalidBackupException("This backup was made by a newer version of MngExpense. Update the app to restore it.")
        }
        return backup
    }
}

// ---- Entity ↔ backup ----

fun ExpenseEntity.toBackup() = BackupExpense(id, title, amount, category, epochDay, note, recurringId, currency, originalAmount)

fun BackupExpense.toEntity() = ExpenseEntity(
    id = id, title = title, amount = amount, category = category, epochDay = epochDay,
    note = note, recurringId = recurringId, currency = currency, originalAmount = originalAmount
)

fun RecurringExpenseEntity.toBackup() =
    BackupRecurring(id, title, amount, category, note, frequency, every, startDay, endDay, nextIndex, paused)

fun BackupRecurring.toEntity() = RecurringExpenseEntity(
    id = id, title = title, amount = amount, category = category, note = note, frequency = frequency,
    every = every, startDay = startDay, endDay = endDay, nextIndex = nextIndex, paused = paused
)

fun CategoryEntity.toBackup() = BackupCategory(key, name, color, icon, sortOrder)
fun BackupCategory.toEntity() = CategoryEntity(key, name, color, icon, sortOrder)

fun BudgetEntity.toBackup() = BackupBudget(key, amount)
fun BackupBudget.toEntity() = BudgetEntity(key, amount)

fun BudgetAlertEntity.toBackup() = BackupBudgetAlert(month, key, level)
fun BackupBudgetAlert.toEntity() = BudgetAlertEntity(month, key, level)
