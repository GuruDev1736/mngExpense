package com.guruprasad.mngexpense.ui.screens.budget

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.guruprasad.mngexpense.MngExpenseApp
import com.guruprasad.mngexpense.data.repository.BudgetRepository
import com.guruprasad.mngexpense.domain.Budget
import com.guruprasad.mngexpense.domain.BudgetStatus
import com.guruprasad.mngexpense.domain.Category
import com.guruprasad.mngexpense.util.BudgetPrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

/** One category on the Budgets screen: what was spent, and its budget status if it has one. */
data class CategoryBudgetRow(val category: Category, val spent: Double, val status: BudgetStatus?)

data class BudgetScreenState(
    val month: YearMonth = YearMonth.now(),
    val overall: BudgetStatus? = null,
    /** Spent this month in all categories (shown even without an overall budget). */
    val totalSpent: Double = 0.0,
    val categories: List<CategoryBudgetRow> = emptyList(),
    val loading: Boolean = true
) {
    /** Sum of the category budgets — warns when it exceeds the overall budget. */
    val categoryBudgetSum: Double get() = categories.sumOf { it.status?.budget?.amount ?: 0.0 }
}

class BudgetViewModel(
    private val repository: BudgetRepository,
    private val app: Application?,
    today: () -> LocalDate = LocalDate::now
) : ViewModel() {

    private val month = YearMonth.from(today())

    val state: StateFlow<BudgetScreenState> =
        combine(repository.observeStatuses(month), repository.observeSpending(month)) { statuses, spending ->
            BudgetScreenState(
                month = month,
                overall = statuses.firstOrNull { it.budget.category == null },
                totalSpent = spending.total,
                categories = Category.entries.map { c ->
                    CategoryBudgetRow(c, spending.of(c), statuses.firstOrNull { it.budget.category == c })
                },
                loading = false
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BudgetScreenState(month = month))

    private val _alertsEnabled = MutableStateFlow(app?.let(BudgetPrefs::alertsEnabled) ?: true)
    val alertsEnabled: StateFlow<Boolean> = _alertsEnabled.asStateFlow()

    fun setAlertsEnabled(enabled: Boolean) {
        app?.let { BudgetPrefs.setAlertsEnabled(it, enabled) }
        _alertsEnabled.value = enabled
    }

    /** [category] null = the overall monthly budget. */
    fun saveBudget(category: Category?, amount: Double) {
        if (amount <= 0) return
        viewModelScope.launch { repository.setBudget(category, amount) }
    }

    fun removeBudget(category: Category?) {
        viewModelScope.launch { repository.removeBudget(category) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MngExpenseApp
                BudgetViewModel(app.budgetRepository, app)
            }
        }

        /** For labels: "Food budget" / "Monthly budget". */
        fun labelOf(category: Category?) = Budget(category, 1.0).label
    }
}
