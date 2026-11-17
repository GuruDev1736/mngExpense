package com.guruprasad.mngexpense.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.guruprasad.mngexpense.MngExpenseApp
import android.net.Uri
import com.guruprasad.mngexpense.data.export.ExportFormat
import com.guruprasad.mngexpense.data.export.ReportExporter
import com.guruprasad.mngexpense.data.repository.ExpenseRepository
import com.guruprasad.mngexpense.data.repository.RecurringRepository
import com.guruprasad.mngexpense.domain.RecurringExpense
import com.guruprasad.mngexpense.domain.Bucket
import com.guruprasad.mngexpense.domain.Category
import com.guruprasad.mngexpense.domain.Expense
import com.guruprasad.mngexpense.domain.Frequency
import com.guruprasad.mngexpense.domain.Period
import com.guruprasad.mngexpense.domain.TimeFilter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/** One-off messages for the screen (shown as a snackbar). */
sealed interface DashboardEvent {
    data class ExportDone(val uri: Uri, val format: ExportFormat) : DashboardEvent
    data class ExportFailed(val message: String) : DashboardEvent
}

data class CategoryTotal(val category: Category, val amount: Double)

data class DashboardUiState(
    val period: Period = Period.of(TimeFilter.MONTH, LocalDate.now()),
    val total: Double = 0.0,
    val count: Int = 0,
    val averagePerDay: Double = 0.0,
    /** % change vs the previous period, or null when there is nothing to compare with. */
    val changePercent: Double? = null,
    val categories: List<CategoryTotal> = emptyList(),
    val bars: List<Bucket> = emptyList(),
    val recent: List<Expense> = emptyList(),
    val canGoNext: Boolean = false,
    val loading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val repository: ExpenseRepository,
    private val exporter: ReportExporter? = null,
    recurring: RecurringRepository? = null,
    private val today: () -> LocalDate = LocalDate::now
) : ViewModel() {

    private val period = MutableStateFlow(Period.of(TimeFilter.MONTH, today()))

    val state: StateFlow<DashboardUiState> = period
        .flatMapLatest { p ->
            val prev = p.previous()
            combine(
                repository.observeBetween(p.start, p.endExclusive),
                repository.observeBetween(prev.start, prev.endExclusive)
            ) { current, previous -> buildState(p, current, previous) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())

    /** Active (non-daily) recurring expenses due within the next week, soonest first. */
    val upcoming: StateFlow<List<RecurringExpense>> = (recurring?.observeAll() ?: flowOf(emptyList()))
        .map { rules ->
            val limit = today().plusDays(UPCOMING_DAYS)
            // Daily rules (milk, travel) would fill this card every day — leave them out
            rules.filter { it.isActive && it.frequency != Frequency.DAILY && !it.nextDate!!.isAfter(limit) }
                .sortedBy { it.nextDate }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _events = Channel<DashboardEvent>(Channel.BUFFERED)
    val events: Flow<DashboardEvent> = _events.receiveAsFlow()

    fun exportFileName(format: ExportFormat): String =
        exporter?.fileName(period.value, format) ?: "Expenses.${format.extension}"

    /** Writes the report for the period currently on screen to [uri]. */
    fun export(format: ExportFormat, uri: Uri) {
        val exporter = exporter ?: return
        val p = period.value
        viewModelScope.launch {
            val event = try {
                exporter.export(p, format, uri)
                DashboardEvent.ExportDone(uri, format)
            } catch (e: Exception) {
                DashboardEvent.ExportFailed(e.message ?: "Export failed")
            }
            _events.send(event)
        }
    }

    fun setFilter(filter: TimeFilter) = period.update { Period.of(filter, today()) }
    fun previousPeriod() = period.update { it.previous() }
    fun nextPeriod() = period.update { if (it.endExclusive.isAfter(today())) it else it.next() }

    private fun buildState(p: Period, current: List<Expense>, previous: List<Expense>): DashboardUiState {
        val total = current.sumOf { it.amount }
        val prevTotal = previous.sumOf { it.amount }
        val days = p.elapsedDays(today()).coerceAtLeast(1)
        return DashboardUiState(
            period = p,
            total = total,
            count = current.size,
            averagePerDay = total / days,
            changePercent = if (prevTotal > 0) (total - prevTotal) / prevTotal * 100 else null,
            categories = current.groupBy { it.category }
                .map { (c, list) -> CategoryTotal(c, list.sumOf { it.amount }) }
                .sortedByDescending { it.amount },
            bars = p.buckets(current),
            recent = current.take(5),
            canGoNext = !p.endExclusive.isAfter(today()),
            loading = false
        )
    }

    companion object {
        const val UPCOMING_DAYS = 7L

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MngExpenseApp
                DashboardViewModel(app.expenseRepository, app.reportExporter, app.recurringRepository)
            }
        }
    }
}
