package com.guruprasad.mngexpense.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class BudgetTest {

    private val nov = YearMonth.of(2026, 11)
    private val today = LocalDate.of(2026, 11, 18)
    private fun status(amount: Double, spent: Double, month: YearMonth = nov, day: LocalDate = today) =
        BudgetStatus(Budget(Category.FOOD, amount), spent, month, day)

    @Test
    fun keysAndLabels() {
        assertEquals("OVERALL", Budget(null, 1.0).key)
        assertEquals("FOOD", Budget(Category.FOOD, 1.0).key)
        assertEquals("Monthly budget", Budget(null, 1.0).label)
        assertEquals("Food budget", Budget(Category.FOOD, 1.0).label)
        assertNull(Budget.categoryOf("OVERALL"))
        assertEquals(Category.HEALTH, Budget.categoryOf("HEALTH"))
    }

    @Test
    fun levelsAtExactThresholds() {
        assertEquals(BudgetLevel.ON_TRACK, status(8000.0, 6399.0).level)
        assertEquals(BudgetLevel.WARNING, status(8000.0, 6400.0).level) // exactly 80 %
        assertEquals(BudgetLevel.WARNING, status(8000.0, 7999.0).level)
        assertEquals(BudgetLevel.OVER, status(8000.0, 8000.0).level) // used up counts as "over"
        assertEquals(BudgetLevel.OVER, status(8000.0, 8450.0).level)
        // 0.1 + 0.2 style rounding must not miss a threshold
        assertEquals(BudgetLevel.WARNING, status(0.3, 0.24000000000000002).level)
    }

    @Test
    fun leftPercentAndDays() {
        val s = status(30000.0, 21400.0)
        assertEquals(8600.0, s.left, 0.0)
        assertEquals(71, s.percent)
        assertEquals(12, s.daysLeft) // 18 Nov → 12 more days in a 30-day month
        assertEquals(0, status(1.0, 0.0, day = LocalDate.of(2026, 11, 30)).daysLeft)
        assertEquals(0, status(1.0, 0.0, month = YearMonth.of(2026, 10)).daysLeft)
    }

    @Test
    fun projectionOnlyForCurrentMonthAfterFewDays() {
        assertEquals(36000.0, status(30000.0, 21600.0).projected!!, 0.001) // 21,600 in 18 days → 36,000 in 30
        assertNull(status(30000.0, 5000.0, day = LocalDate.of(2026, 11, 3)).projected)
        assertNull(status(30000.0, 5000.0, month = YearMonth.of(2026, 10)).projected)
        assertFalse(status(30000.0, 5000.0, month = YearMonth.of(2026, 10)).isCurrentMonth)
    }

    @Test
    fun projectionCountsFixedCostsOnce() {
        // Rent ₹15,000 (recurring) on the 1st + ₹8,300 day-to-day in 18 days, ₹499 Netflix still due
        val s = BudgetStatus(Budget(null, 30000.0), 23300.0, nov, today, fixedSpent = 15000.0, upcomingFixed = 499.0)
        assertEquals(15000.0 + 499.0 + 8300.0 / 18 * 30, s.projected!!, 0.001)
    }

    @Test
    fun upcomingRecurringDatesWithinMonth() {
        val rule = RecurringExpense(
            title = "Water", amount = 40.0, category = Category.FOOD,
            frequency = Frequency.WEEKLY, startDate = LocalDate.of(2026, 11, 4), nextIndex = 3 // 4, 11, 18 added
        )
        assertEquals(
            listOf(LocalDate.of(2026, 11, 25)),
            rule.upcomingDates(today, nov.atEndOfMonth())
        )
        assertTrue(rule.copy(paused = true).upcomingDates(today, nov.atEndOfMonth()).isEmpty())
    }

    @Test
    fun alertLevelsFireOnceEach() {
        assertTrue(BudgetAlerts.newLevels(6000.0, 8000.0, emptySet()).isEmpty())
        assertEquals(listOf(80), BudgetAlerts.newLevels(6400.0, 8000.0, emptySet()))
        assertTrue(BudgetAlerts.newLevels(7000.0, 8000.0, setOf(80)).isEmpty())
        assertEquals(listOf(100), BudgetAlerts.newLevels(8100.0, 8000.0, setOf(80)))
        assertTrue(BudgetAlerts.newLevels(9000.0, 8000.0, setOf(80, 100)).isEmpty())
        // Jumping straight past 100 % reaches both at once (the notifier shows only "exceeded")
        assertEquals(listOf(80, 100), BudgetAlerts.newLevels(9000.0, 8000.0, emptySet()))
    }

    // ---- Warning on the add / edit form ----

    private val budgets = listOf(Budget(Category.FOOD, 8000.0), Budget(null, 30000.0))
    private val spent = mapOf(Category.FOOD to 7700.0, Category.BILLS to 20000.0)
    private val spentIn: (Category?) -> Double = { c -> if (c == null) spent.values.sum() else spent[c] ?: 0.0 }

    @Test
    fun warnsForCategoryBudgetFirst() {
        val w = budgetWarning(budgets, spentIn, nov, Category.FOOD, 750.0)!!
        assertEquals(Category.FOOD, w.budget.category)
        assertEquals(450.0, w.overBy, 0.001)
    }

    @Test
    fun warnsForOverallWhenCategoryHasRoom() {
        val w = budgetWarning(budgets, spentIn, nov, Category.SHOPPING, 2500.0)!!
        assertNull(w.budget.category)
        assertEquals(200.0, w.overBy, 0.001) // 27,700 + 2,500 = 30,200
    }

    @Test
    fun noWarningWithinBudgetOrForZero() {
        assertNull(budgetWarning(budgets, spentIn, nov, Category.FOOD, 300.0)) // exactly 8,000
        assertNull(budgetWarning(budgets, spentIn, nov, Category.FOOD, 0.0))
        assertNull(budgetWarning(emptyList(), spentIn, nov, Category.FOOD, 99999.0))
    }

    @Test
    fun editingDoesNotCountTheOldAmountTwice() {
        // A saved ₹700 Food expense (already inside the 7,700) is edited to ₹900 → 7,900, still fine
        val old = Expense(1, "Lunch", 700.0, Category.FOOD, LocalDate.of(2026, 11, 10))
        assertNull(budgetWarning(budgets, spentIn, nov, Category.FOOD, 900.0, old))
        // ...edited to ₹1,100 → 8,100: over by 100
        assertEquals(100.0, budgetWarning(budgets, spentIn, nov, Category.FOOD, 1100.0, old)!!.overBy, 0.001)
    }

    @Test
    fun editingIntoAnotherMonthCountsTheFullAmount() {
        val old = Expense(1, "Lunch", 700.0, Category.FOOD, LocalDate.of(2026, 10, 10)) // was in October
        assertEquals(400.0, budgetWarning(budgets, spentIn, nov, Category.FOOD, 700.0, old)!!.overBy, 0.001)
    }

    @Test
    fun movingToAnotherCategoryStillCountsOnceOverall() {
        // ₹700 Food expense changed to Shopping ₹2,500: overall 27,700 - 700 + 2,500 = 29,500 → fine
        val old = Expense(1, "Lunch", 700.0, Category.FOOD, LocalDate.of(2026, 11, 10))
        assertNull(budgetWarning(budgets, spentIn, nov, Category.SHOPPING, 2500.0, old))
    }
}
