package com.guruprasad.mngexpense.domain

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

/** Date range choices on the Transactions screen. */
sealed interface DateFilter {
    data object AllTime : DateFilter
    data object ThisMonth : DateFilter
    data object LastMonth : DateFilter
    data object Last3Months : DateFilter
    data object ThisYear : DateFilter

    /** Both ends inclusive. */
    data class Custom(val start: LocalDate, val end: LocalDate) : DateFilter

    /** Inclusive start / exclusive end, or nulls for "no limit". */
    fun range(today: LocalDate): Pair<LocalDate?, LocalDate?> {
        val monthStart = today.withDayOfMonth(1)
        return when (this) {
            AllTime -> null to null
            ThisMonth -> monthStart to monthStart.plusMonths(1)
            LastMonth -> monthStart.minusMonths(1) to monthStart
            Last3Months -> monthStart.minusMonths(2) to monthStart.plusMonths(1) // this month + 2 before
            ThisYear -> today.with(TemporalAdjusters.firstDayOfYear()).let { it to it.plusYears(1) }
            is Custom -> start to end.plusDays(1)
        }
    }

    fun label(locale: Locale = Locale.getDefault()): String = when (this) {
        AllTime -> "All time"
        ThisMonth -> "This month"
        LastMonth -> "Last month"
        Last3Months -> "Last 3 months"
        ThisYear -> "This year"
        is Custom -> {
            val endText = end.format(DateTimeFormatter.ofPattern("d MMM yyyy", locale))
            if (start == end) endText
            else {
                val startPattern = if (start.year == end.year) "d MMM" else "d MMM yyyy"
                start.format(DateTimeFormatter.ofPattern(startPattern, locale)) + " – " + endText
            }
        }
    }

    companion object {
        // A getter, not a stored list: a stored one is built while this interface is still being
        // initialised and can capture its objects as null (crashed the Date sheet)
        val presets: List<DateFilter> get() = listOf(AllTime, ThisMonth, LastMonth, Last3Months, ThisYear)
    }
}

enum class TransactionKind(val label: String) {
    ALL("All"), RECURRING("Recurring only"), ONE_TIME("One-time only")
}

enum class TransactionSort(val label: String) {
    NEWEST("Newest first"), OLDEST("Oldest first"), HIGHEST("Highest amount"), LOWEST("Lowest amount")
}

/**
 * Everything the Transactions screen can search and filter by.
 * An empty [categories] set means "all categories"; [minAmount]/[maxAmount] are inclusive.
 */
data class TransactionFilter(
    val query: String = "",
    val date: DateFilter = DateFilter.AllTime,
    val categories: Set<Category> = emptySet(),
    val minAmount: Double? = null,
    val maxAmount: Double? = null,
    val kind: TransactionKind = TransactionKind.ALL,
    val sort: TransactionSort = TransactionSort.NEWEST
) {
    private val terms: List<String> = query.trim().lowercase().split(Regex("\\s+")).filter { it.isNotEmpty() }

    /**
     * Search matches when every word appears in the title or the note (any order, any case),
     * so "auto office" finds "Auto to office" and "Office trip" with note "auto".
     */
    fun matches(e: Expense): Boolean {
        if (terms.isNotEmpty()) {
            val text = (e.title + " " + e.note).lowercase()
            if (!terms.all { it in text }) return false
        }
        if (categories.isNotEmpty() && e.category !in categories) return false
        if (minAmount != null && e.amount < minAmount) return false
        if (maxAmount != null && e.amount > maxAmount) return false
        return when (kind) {
            TransactionKind.ALL -> true
            TransactionKind.RECURRING -> e.recurringId != null
            TransactionKind.ONE_TIME -> e.recurringId == null
        }
    }

    /** Filters (the date range is applied by the database query) and sorts [expenses]. */
    fun apply(expenses: List<Expense>): List<Expense> {
        val matching = expenses.filter(::matches)
        // Ties: newest entry first, so the order never jumps around
        return when (sort) {
            TransactionSort.NEWEST -> matching.sortedWith(compareByDescending<Expense> { it.date }.thenByDescending { it.id })
            TransactionSort.OLDEST -> matching.sortedWith(compareBy<Expense> { it.date }.thenBy { it.id })
            TransactionSort.HIGHEST -> matching.sortedWith(compareByDescending<Expense> { it.amount }.thenByDescending { it.date }.thenByDescending { it.id })
            TransactionSort.LOWEST -> matching.sortedWith(compareBy<Expense> { it.amount }.thenByDescending { it.date }.thenByDescending { it.id })
        }
    }

    /** Words to highlight in results. */
    val highlightTerms: List<String> get() = terms

    /** Number of filters (not search, not sort) that differ from the default — shown on "Clear". */
    val activeFilterCount: Int
        get() = listOf(
            date != DateFilter.AllTime,
            categories.isNotEmpty(),
            minAmount != null || maxAmount != null,
            kind != TransactionKind.ALL
        ).count { it }

    val isDefault: Boolean get() = this == TransactionFilter()
}

/** One section of the results: a day (when sorted by date) with its expenses and total. */
data class TransactionGroup(val date: LocalDate?, val expenses: List<Expense>) {
    val total: Double get() = expenses.sumOf { it.amount }
}

/**
 * Groups sorted results by day for the date sorts; amount sorts stay one flat list
 * (grouping by day would break the amount order).
 */
fun groupTransactions(sorted: List<Expense>, sort: TransactionSort): List<TransactionGroup> = when (sort) {
    TransactionSort.NEWEST, TransactionSort.OLDEST ->
        // sorted by date already, so consecutive runs are whole days
        sorted.fold(mutableListOf<MutableList<Expense>>()) { groups, e ->
            if (groups.lastOrNull()?.first()?.date == e.date) groups.last() += e else groups += mutableListOf(e)
            groups
        }.map { TransactionGroup(it.first().date, it) }
    TransactionSort.HIGHEST, TransactionSort.LOWEST ->
        if (sorted.isEmpty()) emptyList() else listOf(TransactionGroup(null, sorted))
}
