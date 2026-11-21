package com.guruprasad.mngexpense.ui.screens.backup

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.guruprasad.mngexpense.MngExpenseApp
import com.guruprasad.mngexpense.data.backup.BackupEntry
import com.guruprasad.mngexpense.data.backup.BackupNotifier
import com.guruprasad.mngexpense.data.backup.BackupPrefs
import com.guruprasad.mngexpense.data.backup.BackupSchedule
import com.guruprasad.mngexpense.data.backup.BackupStorage
import com.guruprasad.mngexpense.data.backup.BackupWorker
import com.guruprasad.mngexpense.data.backup.InvalidBackupException
import com.guruprasad.mngexpense.data.backup.LoadedBackup
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class BackupScreenState(
    val hasAccess: Boolean = false,
    val schedule: BackupSchedule = BackupSchedule.OFF,
    val lastBackupAt: Long? = null,
    val lastError: String? = null,
    val backups: List<BackupEntry> = emptyList(),
    val folder: String = "",
    val busy: Boolean = false,
    /** Waiting for the user to confirm replacing their data with this backup. */
    val pendingRestore: LoadedBackup? = null,
    /** One-off message for a snackbar. */
    val message: String? = null
)

class BackupViewModel(private val app: MngExpenseApp) : ViewModel() {
    private val repository = app.backupRepository

    private val _state = MutableStateFlow(BackupScreenState(folder = BackupStorage.displayPath(app)))
    val state: StateFlow<BackupScreenState> = _state.asStateFlow()

    init {
        refresh()
        // A scheduled backup may finish while the screen is open: show its file
        viewModelScope.launch { BackupWorker.observe(app).drop(1).collect { refresh() } }
    }

    /** Re-reads access, status and the backup list (e.g. back from the "All files access" switch). */
    fun refresh() {
        viewModelScope.launch {
            val backups = withContext(Dispatchers.IO) { BackupStorage.list(app) }
            _state.update {
                it.copy(
                    hasAccess = BackupStorage.hasAccess(app),
                    schedule = BackupPrefs.schedule(app),
                    lastBackupAt = BackupPrefs.lastBackupAt(app),
                    lastError = BackupPrefs.lastError(app),
                    backups = backups
                )
            }
        }
    }

    fun setSchedule(schedule: BackupSchedule) {
        BackupPrefs.setSchedule(app, schedule)
        BackupWorker.apply(app, changed = true)
        _state.update { it.copy(schedule = schedule) }
    }

    fun backupNow() = runBusy {
        val file = repository.backupNow(automatic = false)
        BackupNotifier.cancelFailed(app)
        refresh()
        "Saved ${file.name}"
    }

    /** Opens the restore confirmation for a backup from the list. */
    fun pick(entry: BackupEntry) = runBusy {
        _state.update { it.copy(pendingRestore = repository.load(entry)) }
        null
    }

    /** Opens the restore confirmation for a file picked with the system file picker. */
    fun pick(uri: Uri) = runBusy {
        _state.update { it.copy(pendingRestore = repository.load(uri)) }
        null
    }

    fun cancelRestore() = _state.update { it.copy(pendingRestore = null) }

    fun confirmRestore() {
        val loaded = _state.value.pendingRestore ?: return
        _state.update { it.copy(pendingRestore = null) }
        runBusy {
            repository.restore(loaded)
            refresh() // the restore may have turned the schedule back on
            "Backup restored: ${loaded.expenseCount} expenses"
        }
    }

    fun messageShown() = _state.update { it.copy(message = null) }

    /** Runs [block] with the spinner on; its result, or the error, becomes the snackbar message. */
    private fun runBusy(block: suspend () -> String?) {
        if (_state.value.busy) return
        _state.update { it.copy(busy = true) }
        viewModelScope.launch {
            val message = try {
                block()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                if (e is InvalidBackupException) e.message else "Something went wrong: ${e.message ?: e.javaClass.simpleName}"
            }
            _state.update { it.copy(busy = false, message = message ?: it.message) }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { BackupViewModel(this[APPLICATION_KEY] as MngExpenseApp) }
        }
    }
}
