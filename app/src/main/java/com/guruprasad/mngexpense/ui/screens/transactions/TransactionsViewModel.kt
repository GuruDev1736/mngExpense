package com.guruprasad.mngexpense.ui.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.guruprasad.mngexpense.MngExpenseApp
import com.guruprasad.mngexpense.data.repository.ExpenseRepository
import com.guruprasad.mngexpense.domain.Category
import com.guruprasad.mngexpense.domain.DateFilter
import com.guruprasad.mngexpense.domain.TransactionFilter
import com.guruprasad.mngexpense.domain.TransactionGroup
import com.guruprasad.mngexpense.domain.TransactionKind
import com.guruprasad.mngexpense.domain.TransactionSort
import com.guruprasad.mngexpense.domain.groupTransactions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate

data class TransactionsState(
    val groups: List<TransactionGroup> = emptyList(),
    val count: Int = 0,
    val total: Double = 0.0,
    /** True while nothing has loaded yet (not shown for later filter changes, to avoid flicker). */
    val loading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class TransactionsViewModel(
    private val repository: ExpenseRepository,
    initial: TransactionFilter,
    private val today: () -> LocalDate = LocalDate::now
) : ViewModel() {

    private val _filter = MutableStateFlow(initial)
    val filter: StateFlow<TransactionFilter> = _filter.asStateFlow()

    val state: StateFlow<TransactionsState> = run {
        // Only the date range needs a new database query; everything else filters in memory
        val expensesInRange = _filter
            .map { it.date }
            .distinctUntilChanged()
            .flatMapLatest { date ->
                val (start, end) = date.range(today())
                repository.observeRange(start, end)
            }
        // Typing in the search box shouldn't re-filter on every key press (clearing it is instant)
        val query = _filter
            .map { it.query }
            .distinctUntilChanged()
            .debounce { if (it.isEmpty()) 0L else SEARCH_DELAY_MS }
        combine(expensesInRange, _filter, query) { expenses, f, q ->
            val results = f.copy(query = q).apply(expenses)
            TransactionsState(
                groups = groupTransactions(results, f.sort),
                count = results.size,
                total = results.sumOf { it.amount },
                loading = false
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TransactionsState())

    fun setQuery(q: String) = _filter.update { it.copy(query = q) }
    fun setDate(d: DateFilter) = _filter.update { it.copy(date = d) }
    fun setCategories(c: Set<Category>) = _filter.update { it.copy(categories = c) }
    fun setAmountRange(min: Double?, max: Double?) = _filter.update { it.copy(minAmount = min, maxAmount = max) }
    fun setKind(k: TransactionKind) = _filter.update { it.copy(kind = k) }
    fun setSort(s: TransactionSort) = _filter.update { it.copy(sort = s) }

    /** Clears the filters but keeps the search text and sort order. */
    fun clearFilters() = _filter.update { TransactionFilter(query = it.query, sort = it.sort) }

    companion object {
        private const val SEARCH_DELAY_MS = 150L

        fun factory(category: Category?, start: LocalDate?, end: LocalDate?) = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MngExpenseApp
                TransactionsViewModel(
                    app.expenseRepository,
                    TransactionFilter(
                        categories = setOfNotNull(category),
                        date = if (start != null && end != null) DateFilter.Custom(start, end) else DateFilter.AllTime
                    )
                )
            }
        }
    }
}
