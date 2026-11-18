package com.guruprasad.mngexpense.domain

import java.time.LocalDate
import java.time.YearMonth

/**
 * A monthly spending limit: for all spending ([category] == null) or for one category.
 * The same amount applies every month until the user changes it.
 */
data class Budget(val category: Category?, val amount: Double) {
    /** Database key: "OVERALL" or the category's enum name. */
    val key: String get() = keyOf(category)

    /** "Monthly budget" / "Food budget" — used in titles and notifications. */
    val label: String get() = category?.let { "${it.label} budget" } ?: "Monthly budget"

    companion object {
        const val OVERALL_KEY = "OVERALL"

        fun keyOf(category: Category?): String = category?.name ?: OVERALL_KEY

        fun categoryOf(key: String): Category? =
            if (key == OVERALL_KEY) null else Category.entries.firstOrNull { it.name == key }
    }
}

enum class BudgetLevel { ON_TRACK, WARNING, OVER }

/**
 * How a [budget] is doing in [month], seen on [today].
 *
 * @param fixedSpent part of [spent] added by recurring expenses (rent, EMIs...)
 * @param upcomingFixed recurring expenses still due later this month (current month only)
 */
data class BudgetStatus(
    val budget: Budget,
    val spent: Double,
    val month: YearMonth,
    val today: LocalDate,
    val fixedSpent: Double = 0.0,
    val upcomingFixed: Double = 0.0
) {
    /** Money left; negative when over budget. */
    val left: Double get() = budget.amount - spent

    /** 0.0 = nothing spent, 1.0 = budget used up, above 1.0 = over. */
    val fraction: Double get() = if (budget.amount > 0) spent / budget.amount else 0.0

    val percent: Int get() = (fraction * 100).toInt()

    val level: BudgetLevel
        get() = when {
            BudgetAlerts.reached(spent, budget.amount, BudgetAlerts.LIMIT_PERCENT) -> BudgetLevel.OVER
            BudgetAlerts.reached(spent, budget.amount, BudgetAlerts.WARNING_PERCENT) -> BudgetLevel.WARNING
            else -> BudgetLevel.ON_TRACK
        }

    val isCurrentMonth: Boolean get() = YearMonth.from(today) == month

    /** Full days left in the month after today (current month only, else 0). */
    val daysLeft: Int get() = if (isCurrentMonth) month.lengthOfMonth() - today.dayOfMonth else 0

    /**
     * Estimated spending by the end of the month, or null for other months and during the first
     * few days, when a projection would mostly be noise.
     *
     * Only day-to-day spending is extrapolated. Fixed costs from recurring expenses are counted
     * once (a ₹15,000 rent on the 1st must not be read as ₹15,000 every 18 days), plus the
     * recurring payments still due this month.
     */
    val projected: Double?
        get() {
            if (!isCurrentMonth) return null
            val elapsed = today.dayOfMonth
            if (elapsed < MIN_DAYS_FOR_PACE) return null
            val variable = (spent - fixedSpent).coerceAtLeast(0.0)
            return fixedSpent + upcomingFixed + variable / elapsed * month.lengthOfMonth()
        }

    companion object {
        const val MIN_DAYS_FOR_PACE = 5
    }
}

/** Alert rules: one alert at 80 % and one at 100 % per budget per month. */
object BudgetAlerts {
    const val WARNING_PERCENT = 80
    const val LIMIT_PERCENT = 100
    val LEVELS = listOf(WARNING_PERCENT, LIMIT_PERCENT)

    /** True when [spent] reaches [percent] % of [amount] (compared without floating-point percent maths). */
    fun reached(spent: Double, amount: Double, percent: Int): Boolean =
        amount > 0 && spent * 100 >= amount * percent - 1e-6

    /** Alert levels reached by [spent] that have not been sent yet this month, lowest first. */
    fun newLevels(spent: Double, amount: Double, alreadySent: Set<Int>): List<Int> =
        LEVELS.filter { reached(spent, amount, it) && it !in alreadySent }
}

/** A notification to show: [status] just reached [percent] % (80 or 100). */
data class BudgetAlert(val status: BudgetStatus, val percent: Int)

/** Saving an expense would put [budget] over its limit by [overBy]. */
data class BudgetWarning(val budget: Budget, val overBy: Double)

/**
 * Would saving an expense of [amount] in [category] (dated in [month]) go over a budget?
 * The category's own budget is checked first, then the overall one.
 *
 * @param spentIn spending already recorded in [month]: for a category, or in total for null
 * @param original the saved version of the expense being edited (its old amount is already
 *   counted in [spentIn], so it is taken out first); null when adding
 */
fun budgetWarning(
    budgets: List<Budget>,
    spentIn: (Category?) -> Double,
    month: YearMonth,
    category: Category,
    amount: Double,
    original: Expense? = null
): BudgetWarning? {
    if (amount <= 0) return null
    val old = original?.takeIf { YearMonth.from(it.date) == month }
    fun totalAfterSave(c: Category?): Double {
        val oldPart = if (old != null && (c == null || old.category == c)) old.amount else 0.0
        return spentIn(c) - oldPart + amount
    }
    for (budget in listOfNotNull(budgets.firstOrNull { it.category == category }, budgets.firstOrNull { it.category == null })) {
        val total = totalAfterSave(budget.category)
        if (total > budget.amount + 1e-6) return BudgetWarning(budget, total - budget.amount)
    }
    return null
}
