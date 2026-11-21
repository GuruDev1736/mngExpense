package com.guruprasad.mngexpense.data.backup

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.ContextCompat
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** A backup file found in the backup folder. */
data class BackupEntry(val file: File, val automatic: Boolean) {
    val name: String get() = file.name
    val modifiedAt: Long get() = file.lastModified()
    val sizeBytes: Long get() = file.length()
}

/**
 * Backup files live in shared storage, **Documents/MngExpense/Backups**, not in the app's own
 * folder: Android deletes the app's folder on uninstall, but leaves this one alone. After a
 * reinstall the app finds them there again.
 *
 * Android 11+ only lets an app read files in shared storage that it didn't create in this
 * install with "All files access" (MANAGE_EXTERNAL_STORAGE); Android 7–10 use the older
 * storage permission instead. See [hasAccess].
 */
object BackupStorage {
    private const val PREFIX_MANUAL = "MngExpense-backup-"
    private const val PREFIX_AUTO = "MngExpense-auto-"
    private const val EXTENSION = ".json"

    /** Scheduled backups kept; older ones are removed. Backups made with "Back up now" are never removed. */
    const val KEEP_AUTOMATIC = 10

    private val NAME_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")

    /** The debug build keeps its own folder, so test data never mixes with real backups. */
    private fun folderName(context: Context) =
        if (context.packageName.endsWith(".debug")) "MngExpense Debug" else "MngExpense"

    @Suppress("DEPRECATION") // still the way to reach Documents/ with direct file access
    fun directory(context: Context): File =
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "${folderName(context)}/Backups")

    /** For showing the user where the files are: "Documents/MngExpense/Backups". */
    fun displayPath(context: Context) = "Documents/${folderName(context)}/Backups"

    /** True when the app may read and write the backup folder. */
    fun hasAccess(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Environment.isExternalStorageManager()
        else ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED

    /** Android 11+: true when access is granted in system Settings rather than with a runtime dialog. */
    val accessNeedsSettings: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

    /** Android 7–10: the runtime permissions to request. */
    val legacyPermissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    /** Android 11+: opens the "All files access" switch for this app. */
    fun openAccessSettings(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return
        val forApp = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:${context.packageName}"))
        try {
            context.startActivity(forApp)
        } catch (_: ActivityNotFoundException) {
            // Some phones only have the list of all apps
            context.startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
        }
    }

    /** Backups in the folder, newest first. Empty without access or when there are none. */
    fun list(context: Context): List<BackupEntry> {
        if (!hasAccess(context)) return emptyList()
        val files = directory(context).listFiles() ?: return emptyList()
        return files
            .filter { it.isFile && it.name.endsWith(EXTENSION) && (it.name.startsWith(PREFIX_MANUAL) || it.name.startsWith(PREFIX_AUTO)) }
            .map { BackupEntry(it, automatic = it.name.startsWith(PREFIX_AUTO)) }
            .sortedByDescending { it.modifiedAt }
    }

    /**
     * Writes [content] as a new backup file and returns it. Written to a temporary file first
     * and then renamed, so a crash half-way never leaves a damaged backup behind.
     */
    fun write(context: Context, content: String, automatic: Boolean, now: LocalDateTime = LocalDateTime.now()): File {
        val dir = directory(context)
        if (!dir.isDirectory && !dir.mkdirs()) throw java.io.IOException("Couldn't create ${displayPath(context)}")
        val base = (if (automatic) PREFIX_AUTO else PREFIX_MANUAL) + now.format(NAME_TIME)
        var target = File(dir, base + EXTENSION)
        var n = 2
        while (target.exists()) target = File(dir, "$base-${n++}$EXTENSION")
        val temp = File(dir, target.name + ".tmp")
        temp.writeText(content)
        if (!temp.renameTo(target)) {
            temp.delete()
            throw java.io.IOException("Couldn't save ${target.name}")
        }
        return target
    }

    /** Removes the oldest scheduled backups beyond [KEEP_AUTOMATIC]. */
    fun pruneAutomatic(context: Context) {
        list(context).filter { it.automatic }.drop(KEEP_AUTOMATIC).forEach { it.file.delete() }
    }

    fun read(entry: BackupEntry): String = entry.file.readText()

    /** A file the user picked with the system file picker (e.g. a backup copied from another phone). */
    fun read(context: Context, uri: Uri): String =
        context.contentResolver.openInputStream(uri)?.use { it.bufferedReader().readText() }
            ?: throw java.io.IOException("Couldn't open the file")
}
