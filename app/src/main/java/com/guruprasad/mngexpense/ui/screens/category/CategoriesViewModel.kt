package com.guruprasad.mngexpense.ui.screens.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.guruprasad.mngexpense.MngExpenseApp
import com.guruprasad.mngexpense.data.repository.CategoryRepository
import com.guruprasad.mngexpense.data.repository.CategoryUsage
import com.guruprasad.mngexpense.domain.Category
import com.guruprasad.mngexpense.domain.CategoryInfo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoryRow(val info: CategoryInfo, val expenses: Int)

/** Deleting [category]: what it affects and where its expenses go. */
data class DeleteRequest(val category: CategoryInfo, val usage: CategoryUsage, val moveTo: String = Category.OTHER.key)

/** The editor dialog: [editing] null = a new category. */
data class EditorState(val editing: CategoryInfo?, val saving: Boolean = false, val error: String? = null)

data class CategoriesUiState(
    val editor: EditorState? = null,
    val deleting: DeleteRequest? = null,
    val message: String? = null
)

class CategoriesViewModel(private val repository: CategoryRepository) : ViewModel() {

    val rows: StateFlow<List<CategoryRow>?> =
        combine(repository.observeAll(), repository.observeExpenseCounts()) { categories, counts ->
            categories.map { CategoryRow(it, counts[it.key] ?: 0) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _ui = MutableStateFlow(CategoriesUiState())
    val ui: StateFlow<CategoriesUiState> = _ui.asStateFlow()

    fun startCreate() = _ui.update { it.copy(editor = EditorState(editing = null)) }
    fun startEdit(info: CategoryInfo) = _ui.update { it.copy(editor = EditorState(editing = info)) }
    fun closeEditor() = _ui.update { it.copy(editor = null) }

    fun save(name: String, color: Long, icon: String) {
        val editor = _ui.value.editor ?: return
        _ui.update { it.copy(editor = editor.copy(saving = true, error = null)) }
        viewModelScope.launch {
            try {
                val editing = editor.editing
                if (editing == null) repository.add(name, color, icon)
                else repository.update(editing.key, name, color, icon)
                _ui.update { it.copy(editor = null, message = if (editing == null) "Added \"$name\"" else null) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _ui.update { it.copy(editor = editor.copy(saving = false, error = e.message ?: "Couldn't save")) }
            }
        }
    }

    fun move(info: CategoryInfo, delta: Int) = run { repository.move(info.key, delta) }

    fun requestDelete(info: CategoryInfo) {
        viewModelScope.launch {
            _ui.update { it.copy(editor = null, deleting = DeleteRequest(info, repository.usage(info.key))) }
        }
    }

    fun setMoveTo(key: String) = _ui.update { s -> s.copy(deleting = s.deleting?.copy(moveTo = key)) }
    fun cancelDelete() = _ui.update { it.copy(deleting = null) }

    fun confirmDelete() {
        val request = _ui.value.deleting ?: return
        _ui.update { it.copy(deleting = null) }
        run {
            repository.delete(request.category.key, request.moveTo)
            val moved = request.usage.expenses + request.usage.recurring
            "Deleted \"${request.category.name}\"" +
                if (moved > 0) " · moved to ${Category(request.moveTo).label}" else ""
        }
    }

    fun restoreDefaults() = run {
        repository.restoreDefaults()
        "Built-in categories restored"
    }

    fun messageShown() = _ui.update { it.copy(message = null) }

    /** Runs [block]; its result, or the error, becomes the snackbar message. */
    private fun run(block: suspend () -> Any?) {
        viewModelScope.launch {
            val message = try {
                block() as? String
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                e.message ?: "Something went wrong"
            }
            if (message != null) _ui.update { it.copy(message = message) }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { CategoriesViewModel((this[APPLICATION_KEY] as MngExpenseApp).categoryRepository) }
        }
    }
}
