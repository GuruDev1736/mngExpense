package com.guruprasad.mngexpense.data.backup

import android.content.Context
import androidx.core.content.edit

/** How often the scheduled backup runs. */
enum class BackupSchedule(val label: String, val days: Long) {
    OFF("Off", 0),
    DAILY("Daily", 1),
    WEEKLY("Weekly", 7),
    MONTHLY("Monthly", 30);

    companion object {
        fun fromName(name: String?) = entries.firstOrNull { it.name == name } ?: OFF
    }
}

/**
 * Backup settings and status.
 *
 * Left out of Android's own cloud backup (see backup_rules.xml and data_extraction_rules.xml): restored on a new install,
 * [restorePromptHandled] would hide the "Restore your backup?" question there.
 */
object BackupPrefs {
    private const val FILE = "backup_prefs"
    private const val KEY_SCHEDULE = "schedule"
    private const val KEY_LAST_BACKUP = "last_backup_at"
    private const val KEY_LAST_ERROR = "last_error"
    private const val KEY_PROMPT_HANDLED = "restore_prompt_handled"

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun schedule(ctx: Context) = BackupSchedule.fromName(prefs(ctx).getString(KEY_SCHEDULE, null))
    fun setSchedule(ctx: Context, schedule: BackupSchedule) = prefs(ctx).edit { putString(KEY_SCHEDULE, schedule.name) }

    /** Epoch millis of the last successful backup, or null if none yet. */
    fun lastBackupAt(ctx: Context): Long? = prefs(ctx).getLong(KEY_LAST_BACKUP, 0L).takeIf { it > 0 }

    /** Why the last scheduled backup failed; null after a success. */
    fun lastError(ctx: Context): String? = prefs(ctx).getString(KEY_LAST_ERROR, null)

    fun recordSuccess(ctx: Context, at: Long) = prefs(ctx).edit {
        putLong(KEY_LAST_BACKUP, at)
        remove(KEY_LAST_ERROR)
    }

    fun recordError(ctx: Context, message: String) = prefs(ctx).edit { putString(KEY_LAST_ERROR, message) }

    /** The "Restore your backup?" question at first launch was answered (or isn't needed). */
    fun restorePromptHandled(ctx: Context) = prefs(ctx).getBoolean(KEY_PROMPT_HANDLED, false)
    fun setRestorePromptHandled(ctx: Context) = prefs(ctx).edit { putBoolean(KEY_PROMPT_HANDLED, true) }
}
