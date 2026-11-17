package com.guruprasad.mngexpense.ui.screens.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.guruprasad.mngexpense.MngExpenseApp
import com.guruprasad.mngexpense.data.local.RecurringHistory
import com.guruprasad.mngexpense.data.repository.RecurringRepository
import com.guruprasad.mngexpense.domain.Category
import com.guruprasad.mngexpense.domain.Frequency
import com.guruprasad.mngexpense.domain.RecurringExpense
import com.guruprasad.mngexpense.domain.rescheduled
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Locale

data class RecurringFormState(
    val id: Long? = null,
    val title: String = "",
    val amount: String = "",
    val category: Category = Category.BILLS,
    val note: String = "",
    val frequency: Frequency = Frequency.MONTHLY,
    val every: Int = 1,
    /** First payment when adding; the *next* payment when editing. */
    val startDate: LocalDate = LocalDate.now(),
    /** When editing: the earliest allowed next payment (day after the last added one). */
    val minStartDate: LocalDate? = null,
    val hasEndDate: Boolean = false,
    val endDate: LocalDate = LocalDate.now().plusYears(1),
    val paused: Boolean = false,
    val titleError: String? = null,
    val amountError: String? = null,
    val startDateError: String? = null,
    val endDateError: String? = null,
    val loading: Boolean = false,
    /** True once saved or deleted: the screen should close. */
    val finished: Boolean = false
) {
    val isEditing get() = id != null
}

/** What will happen on save — shown live under the form. */
data class RecurringPreview(
    val scheduleText: String,
    /** Expenses that will be added right away because their dates already passed. */
    val pastCount: Int,
    val nextDate: LocalDate?,
    /** e.g. "≈ ₹1,083 / month" for a yearly or weekly rule; null for a plain monthly one. */
    val monthlyEquivalent: Double?
)

class RecurringEditViewModel(
    private val repository: RecurringRepository,
    private val ruleId: Long?,
    private val today: () -> LocalDate = LocalDate::now
) : ViewModel() {

    private val _state = MutableStateFlow(RecurringFormState(id = ruleId, loading = ruleId != null))
    val state: StateFlow<RecurringFormState> = _state.asStateFlow()

    /** The rule as loaded from the database (null when adding). */
    private var original: RecurringExpense? = null

    val history: StateFlow<RecurringHistory?> =
        (if (ruleId != null) repository.observeHistory(ruleId) else flowOf(null))
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        if (ruleId != null) viewModelScope.launch {
            val r = repository.get(ruleId)
            original = r
            _state.update {
                if (r == null) it.copy(loading = false, finished = true) // deleted elsewhere
                else it.copy(
                    title = r.title,
                    amount = formatAmount(r.amount),
                    category = r.category,
                    note = r.note,
                    frequency = r.frequency,
                    every = r.every,
                    startDate = r.occurrence(r.nextIndex),
                    minStartDate = r.lastAddedDate?.plusDays(1),
                    hasEndDate = r.endDate != null,
                    endDate = r.endDate ?: r.occurrence(r.nextIndex).plusYears(1),
                    paused = r.paused,
                    loading = false
                )
            }
        }
    }

    fun onTitleChange(v: String) = _state.update { it.copy(title = v, titleError = null) }
    fun onCategoryChange(c: Category) = _state.update { it.copy(category = c) }
    fun onNoteChange(v: String) = _state.update { it.copy(note = v) }
    fun onFrequencyChange(f: Frequency) = _state.update { it.copy(frequency = f) }
    fun onEveryChange(n: Int) = _state.update { it.copy(every = n.coerceIn(1, MAX_EVERY)) }
    fun onStartDateChange(d: LocalDate) = _state.update {
        it.copy(
            startDate = d, startDateError = null, endDateError = null,
            endDate = if (it.endDate.isBefore(d)) d.plusYears(1) else it.endDate
        )
    }
    fun onHasEndDateChange(v: Boolean) = _state.update { it.copy(hasEndDate = v, endDateError = null) }
    fun onEndDateChange(d: LocalDate) = _state.update { it.copy(endDate = d, endDateError = null) }

    fun onAmountChange(v: String) {
        if (v.isEmpty() || AMOUNT_INPUT.matches(v)) _state.update { it.copy(amount = v, amountError = null) }
    }

    /** The rule the form describes (amount 0 while the field is still empty). */
    private fun RecurringFormState.toRule() = RecurringExpense(
        id = id ?: 0,
        title = title.trim(),
        amount = amount.toDoubleOrNull() ?: 0.0,
        category = category,
        note = note.trim(),
        frequency = frequency,
        every = every,
        startDate = startDate,
        endDate = if (hasEndDate) endDate else null,
        paused = paused
    )

    fun preview(s: RecurringFormState): RecurringPreview {
        val formRule = s.toRule()
        val old = original
        val rule = if (old == null) formRule else rescheduled(old, formRule)
        val past = rule.dueDates(today())
        val next = rule.occurrence(rule.nextIndex + past.size)
            .takeIf { rule.endDate == null || !it.isAfter(rule.endDate) }
        return RecurringPreview(
            scheduleText = rule.scheduleText(Locale.getDefault()),
            pastCount = if (s.paused) 0 else past.size,
            nextDate = next,
            monthlyEquivalent = rule.monthlyEquivalent.takeIf {
                rule.amount > 0 && !(rule.frequency == Frequency.MONTHLY && rule.every == 1)
            }
        )
    }

    fun save() {
        val s = _state.value
        val amount = s.amount.toDoubleOrNull()
        val titleError = if (s.title.isBlank()) "Enter a title" else null
        val amountError = if (amount == null || amount <= 0) "Enter an amount greater than 0" else null
        val startDateError = s.minStartDate?.takeIf { s.startDate.isBefore(it) }?.let {
            "Pick a date after the last payment (${it.minusDays(1).format(java.time.format.DateTimeFormatter.ofPattern("d MMM"))})"
        }
        val endDateError = if (s.hasEndDate && s.endDate.isBefore(s.startDate)) "End date must be after the start date" else null
        if (titleError != null || amountError != null || startDateError != null || endDateError != null) {
            _state.update {
                it.copy(titleError = titleError, amountError = amountError, startDateError = startDateError, endDateError = endDateError)
            }
            return
        }
        viewModelScope.launch {
            repository.save(s.toRule())
            _state.update { it.copy(finished = true) }
        }
    }

    fun delete() {
        val id = _state.value.id ?: return
        viewModelScope.launch {
            repository.delete(id)
            _state.update { it.copy(finished = true) }
        }
    }

    companion object {
        const val MAX_EVERY = 30
        private val AMOUNT_INPUT = Regex("""\d{0,9}(\.\d{0,2})?""")

        private fun formatAmount(v: Double): String =
            if (v % 1.0 == 0.0) v.toLong().toString() else "%.2f".format(Locale.ROOT, v)

        fun factory(ruleId: Long?) = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MngExpenseApp
                RecurringEditViewModel(app.recurringRepository, ruleId)
            }
        }
    }
}
