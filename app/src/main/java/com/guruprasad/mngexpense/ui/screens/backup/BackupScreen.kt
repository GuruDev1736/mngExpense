package com.guruprasad.mngexpense.ui.screens.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.guruprasad.mngexpense.data.backup.BackupEntry
import com.guruprasad.mngexpense.data.backup.BackupSchedule
import com.guruprasad.mngexpense.data.backup.BackupStorage
import com.guruprasad.mngexpense.data.backup.LoadedBackup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(onBack: () -> Unit, vm: BackupViewModel = viewModel(factory = BackupViewModel.Factory)) {
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val requestAccess = rememberBackupAccessRequester { vm.refresh() }
    val importFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(vm::pick)
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            vm.messageShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & restore") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AccessCard(hasAccess = state.hasAccess, folder = state.folder, onAllow = requestAccess)

            // ---- Back up ----
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Backup, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Back up now", style = MaterialTheme.typography.titleSmall)
                            Text(
                                state.lastBackupAt?.let { "Last backup: ${formatBackupTime(it)}" } ?: "No backup yet",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    state.lastError?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick = vm::backupNow, enabled = state.hasAccess && !state.busy) { Text("Back up now") }
                        if (state.busy) {
                            Spacer(Modifier.width(16.dp))
                            CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                        }
                    }
                }
            }

            // ---- Schedule ----
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Scheduled backup", style = MaterialTheme.typography.titleSmall)
                            Text(
                                if (state.schedule == BackupSchedule.OFF) "Off: back up by hand with the button above"
                                else "Runs in the background. The latest ${BackupStorage.KEEP_AUTOMATIC} scheduled " +
                                    "backups are kept; backups you make by hand are never removed.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    val options = BackupSchedule.entries
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        options.forEachIndexed { i, option ->
                            SegmentedButton(
                                selected = state.schedule == option,
                                onClick = { vm.setSchedule(option) },
                                enabled = state.hasAccess || option == BackupSchedule.OFF,
                                shape = SegmentedButtonDefaults.itemShape(i, options.size)
                            ) { Text(option.label) }
                        }
                    }
                }
            }

            // ---- Restore ----
            Spacer(Modifier.height(4.dp))
            Text("Restore", style = MaterialTheme.typography.titleMedium)
            Text(
                "Restoring replaces everything in the app with the backup. The backup file itself is kept.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (state.hasAccess && state.backups.isEmpty()) {
                Text("No backups in ${state.folder} yet.", style = MaterialTheme.typography.bodyMedium)
            }
            state.backups.forEach { entry ->
                BackupRow(entry, enabled = !state.busy, onClick = { vm.pick(entry) })
            }
            OutlinedButton(
                onClick = { importFile.launch(arrayOf("application/json", "text/*", "application/octet-stream")) },
                enabled = !state.busy
            ) {
                Icon(Icons.Filled.FileOpen, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Import a backup file…")
            }
            Text(
                "For a backup copied from another phone or saved somewhere else.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    state.pendingRestore?.let { loaded ->
        ConfirmRestoreDialog(loaded, onConfirm = vm::confirmRestore, onDismiss = vm::cancelRestore)
    }
}

@Composable
private fun AccessCard(hasAccess: Boolean, folder: String, onAllow: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (hasAccess) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(if (hasAccess) Icons.Filled.CheckCircle else Icons.Filled.Warning, contentDescription = null)
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        if (hasAccess) "Backups are saved in $folder" else "Allow file access to back up",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        if (hasAccess) "This folder stays on your phone even if you uninstall the app. " +
                            "When you install it again, MngExpense finds your backup there and offers to restore it."
                        else "Backups go to $folder, outside the app, so they survive an uninstall. " +
                            "Android only lets the app use that folder with " +
                            (if (BackupStorage.accessNeedsSettings) "\"All files access\"." else "the storage permission."),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            if (!hasAccess) {
                Spacer(Modifier.height(12.dp))
                Button(onClick = onAllow) { Text("Allow access") }
            }
        }
    }
}

@Composable
private fun BackupRow(entry: BackupEntry, enabled: Boolean, onClick: () -> Unit) {
    OutlinedCard(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Restore, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(formatBackupTime(entry.modifiedAt), style = MaterialTheme.typography.titleSmall)
                Text(
                    "${if (entry.automatic) "Scheduled" else "Manual"} · ${formatSize(entry.sizeBytes)} · ${entry.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ConfirmRestoreDialog(loaded: LoadedBackup, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val b = loaded.backup
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.Restore, contentDescription = null) },
        title = { Text("Restore this backup?") },
        text = {
            Text(
                "Made on ${formatBackupTime(b.createdAt)}: ${b.expenses.size} expenses, " +
                    "${b.recurring.size} recurring expenses and ${b.budgets.size} budgets.\n\n" +
                    "Everything currently in the app is replaced by this backup. The backup file is kept."
            )
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Restore") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
