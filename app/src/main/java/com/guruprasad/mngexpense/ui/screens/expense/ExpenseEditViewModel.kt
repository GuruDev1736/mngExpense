package com.guruprasad.mngexpense.ui.screens.expense

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.guruprasad.mngexpense.MngExpenseApp
import com.guruprasad.mngexpense.data.repository.BudgetRepository
import com.guruprasad.mngexpense.data.repository.ExpenseRepository
import com.guruprasad.mngexpense.data.repository.MonthSpending
import com.guruprasad.mngexpense.data.repository.RecurringRepository
import com.guruprasad.mngexpense.domain.Budget
import com.guruprasad.mngexpense.domain.budgetWarning
import com.guruprasad.mngexpense.domain.Category
import com.guruprasad.mngexpense.domain.Currencies
import com.guruprasad.mngexpense.domain.Expense
import com.guruprasad.mngexpense.domain.Frequency
import com.guruprasad.mngexpense.util.AppCurrency
import com.guruprasad.mngexpense.util.CurrencyPrefs
import com.guruprasad.mngexpense.util.money
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class ExpenseFormState(
    val id: Long? = null,
    val title: String = "",
    val amount: String = "",
    val category: Category = Category.FOOD,
    val date: LocalDate = LocalDate.now(),
    val note: String = "",
    /** Set when a recurring expense added this one automatically. */
    val recurringId: Long? = null,
    /** "Repeat" switch (new expenses only): also create a recurring rule from this expense. */
    val repeat: Boolean = false,
    val repeatFrequency: Frequency = Frequency.MONTHLY,
    /** Currency the amount is entered in; differs from [home] for a foreign expense. */
    val currency: String = AppCurrency.code,
    val home: String = AppCurrency.code,
    /** Exchange rate: 1 [currency] = [rate] [home] (foreign expenses only). */
    val rate: String = "",
    val titleError: String? = null,
    val amountError: String? = null,
    val rateError: String? = null,
    val loading: Boolean = false,
    /** True once saved or deleted: the screen should close. */
    val finished: Boolean = false
) {
    val isEditing get() = id != null

    val isForeign get() = currency != home

    /** The amount in the home currency, or null while the amount or rate is incomplete. */
    val homeAmount: Double?
        get() {
            val original = amount.toDoubleOrNull() ?: return null
            if (!isForeign) return original
            val r = rate.toDoubleOrNull()?.takeIf { it > 0 } ?: return null
            return Currencies.convert(original, r)
        }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseEditViewModel(
    private val repository: ExpenseRepository,
    private val recurringRepository: RecurringRepository?,
    private val expenseId: Long?,
    private val budgetRepository: BudgetRepository? = null,
    /** For remembering exchange rates; null in previews and tests. */
    private val app: Application? = null
) : ViewModel() {

    private val _state = MutableStateFlow(ExpenseFormState(id = expenseId, loading = expenseId != null))
    val state: StateFlow<ExpenseFormState> = _state.asStateFlow()

    /** The expense as loaded (editing only) — its old amount must not be counted twice. */
    private val original = MutableStateFlow<Expense?>(null)

    private data class HintInput(val category: Category, val month: YearMonth, val amount: Double)

    /** "This puts Food over its ₹8,000 budget by ₹450", or null. */
    val budgetHint: StateFlow<String?> =
        if (budgetRepository == null) MutableStateFlow(null)
        else _state
            .map { HintInput(it.category, YearMonth.from(it.date), it.homeAmount ?: 0.0) }
            .distinctUntilChanged()
            .flatMapLatest { input ->
                combine(
                    budgetRepository.observeBudgets(),
                    budgetRepository.observeSpending(input.month),
                    original
                ) { budgets, spending, old ->
                    budgetHint(input, budgets, spending, old)
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private fun budgetHint(input: HintInput, budgets: List<Budget>, spending: MonthSpending, old: Expense?): String? {
        val warning = budgetWarning(
            budgets = budgets,
            spentIn = spending::of,
            month = input.month,
            category = input.category,
            amount = input.amount,
            original = old
        ) ?: return null
        val b = warning.budget
        return if (b.category != null) "This puts ${b.category.label} over its ${money(b.amount)} budget by ${money(warning.overBy)}"
        else "This puts you over your ${money(b.amount)} monthly budget by ${money(warning.overBy)}"
    }

    init {
        if (expenseId != null) viewModelScope.launch {
            val e = repository.get(expenseId)
            original.value = e
            _state.update {
                if (e == null) it.copy(loading = false, finished = true) // deleted elsewhere
                else it.copy(
                    title = e.title,
                    // An expense paid in what is now the home currency (the user switched) is
                    // edited as a plain home-currency amount, so its stored total never changes
                    amount = formatAmount(if (e.isForeign && e.currency != it.home) e.originalAmount!! else e.amount),
                    currency = if (e.isForeign && e.currency != it.home) e.currency!! else it.home,
                    rate = if (e.isForeign && e.currency != it.home) e.exchangeRate?.let(::formatRate) ?: "" else "",
                    category = e.category,
                    date = e.date,
                    note = e.note,
                    recurringId = e.recurringId,
                    loading = false
                )
            }
        }
    }

    fun onTitleChange(v: String) = _state.update { it.copy(title = v, titleError = null) }
    fun onCategoryChange(c: Category) = _state.update { it.copy(category = c) }
    fun onDateChange(d: LocalDate) = _state.update { it.copy(date = d) }
    fun onNoteChange(v: String) = _state.update { it.copy(note = v) }
    fun onRepeatChange(v: Boolean) = _state.update { it.copy(repeat = v) }
    fun onRepeatFrequencyChange(f: Frequency) = _state.update { it.copy(repeatFrequency = f) }

    /** Switches the currency the amount is entered in; suggests the last rate used for it. */
    fun onCurrencyChange(code: String) = _state.update {
        val last = app?.let { a -> CurrencyPrefs.lastRate(a, code, it.home) }
        it.copy(
            currency = code,
            rate = if (code == it.home) "" else last?.let(::formatRate) ?: "",
            rateError = null,
            // Repeating is home-currency only (the recurring amount is fixed in home money)
            repeat = if (code != it.home) false else it.repeat
        )
    }

    fun onRateChange(v: String) {
        if (v.isEmpty() || RATE_INPUT.matches(v)) _state.update { it.copy(rate = v, rateError = null) }
    }

    fun onAmountChange(v: String) {
        // Allow only digits with at most one decimal point and two decimals
        if (v.isEmpty() || AMOUNT_INPUT.matches(v)) _state.update { it.copy(amount = v, amountError = null) }
    }

    fun save() {
        val s = _state.value
        val amount = s.amount.toDoubleOrNull()
        val titleError = if (s.title.isBlank()) "Enter a title" else null
        val amountError = if (amount == null || amount <= 0) "Enter an amount greater than 0" else null
        val rate = s.rate.toDoubleOrNull()
        val rateError = if (s.isForeign && (rate == null || rate <= 0)) "Enter the exchange rate" else null
        if (titleError != null || amountError != null || rateError != null) {
            _state.update { it.copy(titleError = titleError, amountError = amountError, rateError = rateError) }
            return
        }
        if (s.isForeign) app?.let { CurrencyPrefs.saveRate(it, s.currency, s.home, rate!!) }
        val expense = Expense(
            id = s.id ?: 0,
            title = s.title.trim(),
            amount = if (s.isForeign) Currencies.convert(amount!!, rate!!) else amount!!,
            currency = if (s.isForeign) s.currency else null,
            originalAmount = if (s.isForeign) amount else null,
            category = s.category,
            date = s.date,
            note = s.note.trim(),
            recurringId = s.recurringId
        )
        viewModelScope.launch {
            if (!s.isEditing && s.repeat && !s.isForeign && recurringRepository != null) {
                recurringRepository.saveRepeating(expense, s.repeatFrequency)
            } else {
                repository.save(expense)
            }
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
        private val RATE_INPUT = Regex("""\d{0,7}(\.\d{0,6})?""")

        /** Up to 4 decimals, no trailing zeros: 83.2, 0.0112, 1 */
        private fun formatRate(v: Double): String =
            java.math.BigDecimal(v).setScale(4, java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()

        private fun formatAmount(v: Double): String =
            if (v % 1.0 == 0.0) v.toLong().toString() else "%.2f".format(java.util.Locale.ROOT, v)

        fun factory(expenseId: Long?) = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MngExpenseApp
                ExpenseEditViewModel(app.expenseRepository, app.recurringRepository, expenseId, app.budgetRepository, app)
            }
        }
    }
}
