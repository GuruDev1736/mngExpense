package com.guruprasad.mngexpense.ui.screens.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.guruprasad.mngexpense.MngExpenseApp
import com.guruprasad.mngexpense.data.repository.ExpenseRepository
import com.guruprasad.mngexpense.domain.Category
import com.guruprasad.mngexpense.domain.Expense
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ExpenseFormState(
    val id: Long? = null,
    val title: String = "",
    val amount: String = "",
    val category: Category = Category.FOOD,
    val date: LocalDate = LocalDate.now(),
    val note: String = "",
    val titleError: String? = null,
    val amountError: String? = null,
    val loading: Boolean = false,
    /** True once saved or deleted: the screen should close. */
    val finished: Boolean = false
) {
    val isEditing get() = id != null
}

class ExpenseEditViewModel(
    private val repository: ExpenseRepository,
    private val expenseId: Long?
) : ViewModel() {

    private val _state = MutableStateFlow(ExpenseFormState(id = expenseId, loading = expenseId != null))
    val state: StateFlow<ExpenseFormState> = _state.asStateFlow()

    init {
        if (expenseId != null) viewModelScope.launch {
            val e = repository.get(expenseId)
            _state.update {
                if (e == null) it.copy(loading = false, finished = true) // deleted elsewhere
                else it.copy(
                    title = e.title,
                    amount = formatAmount(e.amount),
                    category = e.category,
                    date = e.date,
                    note = e.note,
                    loading = false
                )
            }
        }
    }

    fun onTitleChange(v: String) = _state.update { it.copy(title = v, titleError = null) }
    fun onCategoryChange(c: Category) = _state.update { it.copy(category = c) }
    fun onDateChange(d: LocalDate) = _state.update { it.copy(date = d) }
    fun onNoteChange(v: String) = _state.update { it.copy(note = v) }

    fun onAmountChange(v: String) {
        // Allow only digits with at most one decimal point and two decimals
        if (v.isEmpty() || AMOUNT_INPUT.matches(v)) _state.update { it.copy(amount = v, amountError = null) }
    }

    fun save() {
        val s = _state.value
        val amount = s.amount.toDoubleOrNull()
        val titleError = if (s.title.isBlank()) "Enter a title" else null
        val amountError = if (amount == null || amount <= 0) "Enter an amount greater than 0" else null
        if (titleError != null || amountError != null) {
            _state.update { it.copy(titleError = titleError, amountError = amountError) }
            return
        }
        viewModelScope.launch {
            repository.save(
                Expense(
                    id = s.id ?: 0,
                    title = s.title.trim(),
                    amount = amount!!,
                    category = s.category,
                    date = s.date,
                    note = s.note.trim()
                )
            )
            _state.update { it.copy(finished = true) }
        }
    }

    fun delete() {
        val id = _state.value.id ?: return
        viewModelScope.launch {
            repository.get(id)?.let { repository.delete(it) }
            _state.update { it.copy(finished = true) }
        }
    }

    companion object {
        private val AMOUNT_INPUT = Regex("""\d{0,9}(\.\d{0,2})?""")

        private fun formatAmount(v: Double): String =
            if (v % 1.0 == 0.0) v.toLong().toString() else "%.2f".format(java.util.Locale.ROOT, v)

        fun factory(expenseId: Long?) = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MngExpenseApp
                ExpenseEditViewModel(app.expenseRepository, expenseId)
            }
        }
    }
}
