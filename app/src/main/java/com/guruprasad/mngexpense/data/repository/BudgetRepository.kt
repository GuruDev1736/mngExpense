package com.guruprasad.mngexpense.data.repository

import androidx.room.withTransaction
import com.guruprasad.mngexpense.data.local.AppDatabase
import com.guruprasad.mngexpense.data.local.BudgetAlertEntity
import com.guruprasad.mngexpense.data.local.toDomain
import com.guruprasad.mngexpense.data.local.toEntity
import com.guruprasad.mngexpense.domain.Budget
import com.guruprasad.mngexpense.domain.BudgetAlert
import com.guruprasad.mngexpense.domain.BudgetAlerts
import com.guruprasad.mngexpense.domain.BudgetStatus
import com.guruprasad.mngexpense.domain.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate
import java.time.YearMonth

/**
 * Spending of one month, per category (null = all categories).
 * @param fixed part added by recurring expenses
 * @param upcoming recurring expenses still due later in the month
 */
data class MonthSpending(
    val byCategory: Map<Category, Double>,
    val fixed: Map<Category, Double> = emptyMap(),
    val upcoming: Map<Category, Double> = emptyMap()
) {
    val total: Double get() = byCategory.values.sum()
    fun of(category: Category?): Double = sum(byCategory, category)
    fun fixedOf(category: Category?): Double = sum(fixed, category)
    fun upcomingOf(category: Category?): Double = sum(upcoming, category)

    private fun sum(map: Map<Category, Double>, category: Category?) =
        if (category == null) map.values.sum() else map[category] ?: 0.0
}

/** Monthly budgets, their progress, and the "80 % / exceeded" alert bookkeeping. */
class BudgetRepository(
    private val db: AppDatabase,
    private val today: () -> LocalDate = LocalDate::now
) {
    private val dao = db.budgetDao()
    private val expenseDao = db.expenseDao()

    /** Saves and alert checks can come from the UI and the recurring worker at the same time. */
    private val alertLock = Mutex()

    fun observeBudgets(): Flow<List<Budget>> =
        dao.observeAll().map { rows -> rows.mapNotNull { it.toDomain() } }

    /** Live spending of [month], grouped by category, with fixed and upcoming recurring costs. */
    fun observeSpending(month: YearMonth): Flow<MonthSpending> =
        combine(
            expenseDao.observeBetween(month.atDay(1).toEpochDay(), month.plusMonths(1).atDay(1).toEpochDay()),
            db.recurringExpenseDao().observeAll()
        ) { rows, rules ->
            val day = today()
            val upcoming = if (YearMonth.from(day) != month) emptyMap() else rules
                .map { it.toDomain() }
                .flatMap { rule -> rule.upcomingDates(day, month.atEndOfMonth()).map { rule.category to rule.amount } }
                .groupBy({ it.first }, { it.second })
                .mapValues { (_, amounts) -> amounts.sum() }
            MonthSpending(
                byCategory = rows.groupBy { Category.fromName(it.category) }
                    .mapValues { (_, list) -> list.sumOf { it.amount } },
                fixed = rows.filter { it.recurringId != null }
                    .groupBy { Category.fromName(it.category) }
                    .mapValues { (_, list) -> list.sumOf { it.amount } },
                upcoming = upcoming
            )
        }

    /** Every budget with its progress in [month]. Overall budget first, then categories in order. */
    fun observeStatuses(month: YearMonth): Flow<List<BudgetStatus>> =
        combine(observeBudgets(), observeSpending(month)) { budgets, spending ->
            val day = today()
            budgets
                .sortedBy { it.category?.order ?: -1 }
                .map {
                    BudgetStatus(
                        it, spending.of(it.category), month, day,
                        fixedSpent = spending.fixedOf(it.category),
                        upcomingFixed = spending.upcomingOf(it.category)
                    )
                }
        }

    /**
     * Creates or changes a budget. Thresholds the month's spending already passes are marked as
     * alerted without a notification: the user is looking at the numbers right now.
     */
    suspend fun setBudget(category: Category?, amount: Double) {
        require(amount > 0) { "Budget must be greater than 0" }
        alertLock.withLock {
            db.withTransaction {
                val budget = Budget(category, amount)
                dao.upsert(budget.toEntity())
                findNewAlerts(listOf(budget))
            }
        }
    }

    suspend fun removeBudget(category: Category?) = dao.delete(Budget.keyOf(category))

    /**
     * Checks this month's spending against every budget and returns the alerts that should be
     * shown now — each threshold at most once per budget per month. Call after spending changed.
     */
    suspend fun checkAlerts(): List<BudgetAlert> = alertLock.withLock {
        db.withTransaction {
            dao.deleteAlertsBefore(YearMonth.from(today()).minusMonths(12).toString())
            findNewAlerts(dao.getAll().mapNotNull { it.toDomain() })
        }
    }

    /** Records newly reached thresholds of [budgets] in the current month; returns one alert per budget. */
    private suspend fun findNewAlerts(budgets: List<Budget>): List<BudgetAlert> {
        if (budgets.isEmpty()) return emptyList()
        val day = today()
        val month = YearMonth.from(day)
        val spending = MonthSpending(
            expenseDao.categoryTotals(month.atDay(1).toEpochDay(), month.plusMonths(1).atDay(1).toEpochDay())
                .groupBy { Category.fromName(it.category) }
                .mapValues { (_, rows) -> rows.sumOf { it.total } }
        )
        val alerts = mutableListOf<BudgetAlert>()
        for (budget in budgets) {
            val spent = spending.of(budget.category)
            val sent = dao.sentLevels(month.toString(), budget.key).toSet()
            val reached = BudgetAlerts.newLevels(spent, budget.amount, sent)
            if (reached.isEmpty()) continue
            reached.forEach { dao.recordAlert(BudgetAlertEntity(month.toString(), budget.key, it)) }
            // Jumping from 50 % straight past 100 % shows only "exceeded", not both
            alerts += BudgetAlert(BudgetStatus(budget, spent, month, day), reached.max())
        }
        return alerts
    }
}
