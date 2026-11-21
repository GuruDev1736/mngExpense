package com.guruprasad.mngexpense.ui.screens.backup

import android.widget.Toast
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.guruprasad.mngexpense.MngExpenseApp
import com.guruprasad.mngexpense.data.backup.BackupPrefs
import com.guruprasad.mngexpense.data.backup.BackupStorage
import com.guruprasad.mngexpense.data.backup.LoadedBackup
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface RestorePromptState {
    data object Hidden : RestorePromptState
    data object Checking : RestorePromptState
    /** No file access yet, so the backup folder can't be checked: ask for it. */
    data object AskAccess : RestorePromptState
    data class Found(val backup: LoadedBackup) : RestorePromptState
    data object Restoring : RestorePromptState
}

/**
 * The question shown on the first launch after an install, when the app is empty:
 * "Restore your backup?". Asked once; answering No keeps the file, so it can still be restored
 * later from Settings → Backup & restore.
 */
class RestorePromptViewModel(private val app: MngExpenseApp) : ViewModel() {
    private val repository = app.backupRepository

    private val _state = MutableStateFlow<RestorePromptState>(RestorePromptState.Checking)
    val state: StateFlow<RestorePromptState> = _state.asStateFlow()

    /** Shown once as a toast. */
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        viewModelScope.launch {
            _state.value = when {
                BackupPrefs.restorePromptHandled(app) -> RestorePromptState.Hidden
                // Already has data (e.g. an update, not a new install): nothing to ask
                repository.expenseCount() > 0 -> {
                    BackupPrefs.setRestorePromptHandled(app)
                    RestorePromptState.Hidden
                }
                BackupStorage.hasAccess(app) -> search()
                else -> RestorePromptState.AskAccess
            }
        }
    }

    /** Access may have just been granted: look for a backup now. */
    fun onAccessChecked(granted: Boolean) {
        if (!granted || _state.value != RestorePromptState.AskAccess) return
        _state.value = RestorePromptState.Checking
        viewModelScope.launch { _state.value = search() }
    }

    private suspend fun search(): RestorePromptState {
        val found = try {
            repository.findLatest()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            null
        }
        if (found == null) {
            // Nothing to restore: a new user. Don't ask again.
            BackupPrefs.setRestorePromptHandled(app)
            return RestorePromptState.Hidden
        }
        return RestorePromptState.Found(found)
    }

    fun restore(backup: LoadedBackup) {
        _state.value = RestorePromptState.Restoring
        viewModelScope.launch {
            _message.value = try {
                repository.restore(backup)
                BackupPrefs.setRestorePromptHandled(app)
                _state.value = RestorePromptState.Hidden
                "Backup restored: ${backup.expenseCount} expenses"
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _state.value = RestorePromptState.Found(backup) // let them try again or skip
                "Couldn't restore: ${e.message ?: e.javaClass.simpleName}"
            }
        }
    }

    /** "No" / "Start fresh": the backup file stays where it is. */
    fun startFresh() {
        BackupPrefs.setRestorePromptHandled(app)
        _state.value = RestorePromptState.Hidden
        _message.value = "You can restore a backup any time in Settings → Backup & restore"
    }

    fun messageShown() {
        _message.value = null
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { RestorePromptViewModel(this[APPLICATION_KEY] as MngExpenseApp) }
        }
    }
}

/** Put once at the top of the app; shows nothing unless a restore question is due. */
@Composable
fun RestorePrompt(vm: RestorePromptViewModel = viewModel(factory = RestorePromptViewModel.Factory)) {
    val context = LocalContext.current
    val state by vm.state.collectAsStateWithLifecycle()
    val message by vm.message.collectAsStateWithLifecycle()
    val requestAccess = rememberBackupAccessRequester(vm::onAccessChecked)

    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            vm.messageShown()
        }
    }

    // Only a button choice closes these: tapping outside must not skip the question by accident
    val properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    val folder = BackupStorage.displayPath(context)

    when (val s = state) {
        RestorePromptState.Hidden, RestorePromptState.Checking -> Unit

        RestorePromptState.AskAccess -> AlertDialog(
            onDismissRequest = {},
            properties = properties,
            icon = { Icon(Icons.Filled.Restore, contentDescription = null) },
            title = { Text("Used MngExpense before?") },
            text = {
                Text(
                    "MngExpense can look for a backup in $folder and restore your expenses.\n\n" +
                        "To check, allow " +
                        (if (BackupStorage.accessNeedsSettings) "\"All files access\" on the next screen, then come back."
                        else "access to files.")
                )
            },
            confirmButton = { TextButton(onClick = requestAccess) { Text("Check for backup") } },
            dismissButton = { TextButton(onClick = vm::startFresh) { Text("Start fresh") } }
        )

        is RestorePromptState.Found -> {
            val b = s.backup.backup
            AlertDialog(
                onDismissRequest = {},
                properties = properties,
                icon = { Icon(Icons.Filled.Restore, contentDescription = null) },
                title = { Text("Restore your backup?") },
                text = {
                    Text(
                        "A backup from ${formatBackupTime(b.createdAt)} was found, with ${b.expenses.size} expenses, " +
                            "${b.recurring.size} recurring expenses and ${b.budgets.size} budgets.\n\n" +
                            "If you choose No, the backup file is kept in $folder, so you can still restore it " +
                            "later from Settings → Backup & restore."
                    )
                },
                confirmButton = { TextButton(onClick = { vm.restore(s.backup) }) { Text("Yes, restore") } },
                dismissButton = { TextButton(onClick = vm::startFresh) { Text("No, start fresh") } }
            )
        }

        RestorePromptState.Restoring -> AlertDialog(
            onDismissRequest = {},
            properties = properties,
            title = { Text("Restoring…") },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(16.dp))
                    Text("Putting your expenses back")
                }
            },
            confirmButton = {}
        )
    }
}
