package com.guruprasad.mngexpense.ui.screens.backup

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.guruprasad.mngexpense.data.backup.BackupStorage
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** "21 Nov 2026, 9:30 am" in the phone's language and time zone. */
fun formatBackupTime(epochMillis: Long): String =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
        .format(Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()))

fun formatSize(bytes: Long): String = when {
    bytes >= 1_048_576 -> "%.1f MB".format(bytes / 1_048_576.0)
    bytes >= 1024 -> "${bytes / 1024} KB"
    else -> "$bytes B"
}

/**
 * Returns a function that asks for access to the backup folder. [onChecked] runs once the
 * answer is known: after the runtime dialog (Android 7–10), or when the user comes back from
 * the "All files access" switch (Android 11+). Also runs on every resume, so a change made in
 * Settings directly is picked up too.
 */
@Composable
fun rememberBackupAccessRequester(onChecked: (granted: Boolean) -> Unit): () -> Unit {
    val context = LocalContext.current
    val callback by rememberUpdatedState(onChecked)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        callback(BackupStorage.hasAccess(context))
    }
    LifecycleResumeEffect(Unit) {
        callback(BackupStorage.hasAccess(context))
        onPauseOrDispose { }
    }
    return { request(context, launcher::launch) }
}

private fun request(context: Context, launchRuntime: (Array<String>) -> Unit) {
    if (BackupStorage.accessNeedsSettings) BackupStorage.openAccessSettings(context)
    else launchRuntime(BackupStorage.legacyPermissions)
}
