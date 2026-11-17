package com.guruprasad.mngexpense.ui.screens.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.guruprasad.mngexpense.MngExpenseApp
import com.guruprasad.mngexpense.data.repository.RecurringRepository
import com.guruprasad.mngexpense.domain.RecurringExpense
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RecurringListState(
    val rules: List<RecurringExpense> = emptyList(),
    /** Average fixed costs per month of the active rules. */
    val monthlyTotal: Double = 0.0,
    val activeCount: Int = 0,
    val loading: Boolean = true
)

class RecurringListViewModel(private val repository: RecurringRepository) : ViewModel() {

    val state: StateFlow<RecurringListState> = repository.observeAll()
        .map { rules ->
            // Active first, soonest first; then paused; ended last
            val sorted = rules.sortedWith(
                compareBy<RecurringExpense>({ if (it.isActive) 0 else if (it.paused && !it.hasEnded) 1 else 2 })
                    .thenBy { it.nextDate }
                    .thenBy { it.title.lowercase() }
            )
            val active = rules.filter { it.isActive }
            RecurringListState(
                rules = sorted,
                monthlyTotal = active.sumOf { it.monthlyEquivalent },
                activeCount = active.size,
                loading = false
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecurringListState())

    fun setPaused(rule: RecurringExpense, paused: Boolean) {
        viewModelScope.launch { repository.setPaused(rule.id, paused) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MngExpenseApp
                RecurringListViewModel(app.recurringRepository)
            }
        }
    }
}
