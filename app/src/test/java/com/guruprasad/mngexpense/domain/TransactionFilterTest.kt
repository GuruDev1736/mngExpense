package com.guruprasad.mngexpense.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

class TransactionFilterTest {

    private val today = LocalDate.of(2026, 11, 18)
    private fun d(m: Int, day: Int, y: Int = 2026) = LocalDate.of(y, m, day)

    private val expenses = listOf(
        Expense(1, "Auto to office", 120.0, Category.TRANSPORT, d(11, 18)),
        Expense(2, "Lunch", 320.0, Category.FOOD, d(11, 18), note = "with office team"),
        Expense(3, "Rent", 15000.0, Category.BILLS, d(11, 1), recurringId = 7),
        Expense(4, "Groceries", 2400.0, Category.FOOD, d(11, 14)),
        Expense(5, "Office chair", 2400.0, Category.SHOPPING, d(10, 30)),
        Expense(6, "Netflix", 499.0, Category.ENTERTAINMENT, d(10, 20), recurringId = 9)
    )

    private fun ids(f: TransactionFilter) = f.apply(expenses).map { it.id }

    @Test
    fun noFilterReturnsAllNewestFirst() {
        assertEquals(listOf(2L, 1L, 4L, 3L, 5L, 6L), ids(TransactionFilter())) // same day: newer id first
    }

    @Test
    fun searchIsCaseInsensitiveAndLooksInNotes() {
        assertEquals(listOf(2L, 1L, 5L), ids(TransactionFilter(query = "OFFICE")))
        assertEquals(listOf(3L), ids(TransactionFilter(query = "  rent ")))
    }

    @Test
    fun everySearchWordMustMatchInAnyOrder() {
        assertEquals(listOf(1L), ids(TransactionFilter(query = "office auto")))
        assertTrue(ids(TransactionFilter(query = "office pizza")).isEmpty())
    }

    @Test
    fun categoryFilterAllowsSeveral() {
        assertEquals(listOf(2L, 4L), ids(TransactionFilter(categories = setOf(Category.FOOD))))
        assertEquals(listOf(2L, 1L, 4L), ids(TransactionFilter(categories = setOf(Category.FOOD, Category.TRANSPORT))))
    }

    @Test
    fun amountBoundsAreInclusive() {
        assertEquals(listOf(4L, 5L), ids(TransactionFilter(minAmount = 2400.0, maxAmount = 2400.0)))
        assertEquals(listOf(2L, 1L, 6L), ids(TransactionFilter(maxAmount = 499.0)))
        assertEquals(listOf(3L), ids(TransactionFilter(minAmount = 2400.01)))
    }

    @Test
    fun recurringOrOneTime() {
        assertEquals(listOf(3L, 6L), ids(TransactionFilter(kind = TransactionKind.RECURRING)))
        assertEquals(listOf(2L, 1L, 4L, 5L), ids(TransactionFilter(kind = TransactionKind.ONE_TIME)))
    }

    @Test
    fun filtersCombine() {
        val f = TransactionFilter(query = "o", categories = setOf(Category.FOOD, Category.SHOPPING), maxAmount = 2400.0)
        assertEquals(listOf(2L, 4L, 5L), ids(f)) // Lunch (note), Groceries, Office chair
    }

    @Test
    fun sortOrdersWithStableTies() {
        assertEquals(listOf(6L, 5L, 3L, 4L, 1L, 2L), ids(TransactionFilter(sort = TransactionSort.OLDEST)))
        // 2,400 twice: the newer one (Groceries, 14 Nov) first
        assertEquals(listOf(3L, 4L, 5L, 6L, 2L, 1L), ids(TransactionFilter(sort = TransactionSort.HIGHEST)))
        assertEquals(listOf(1L, 2L, 6L, 4L, 5L, 3L), ids(TransactionFilter(sort = TransactionSort.LOWEST)))
    }

    @Test
    fun datePresetRanges() {
        assertEquals(null to null, DateFilter.AllTime.range(today))
        assertEquals(d(11, 1) to d(12, 1), DateFilter.ThisMonth.range(today))
        assertEquals(d(10, 1) to d(11, 1), DateFilter.LastMonth.range(today))
        assertEquals(d(9, 1) to d(12, 1), DateFilter.Last3Months.range(today))
        assertEquals(d(1, 1) to d(1, 1, 2027), DateFilter.ThisYear.range(today))
        assertEquals(d(11, 3) to d(11, 10), DateFilter.Custom(d(11, 3), d(11, 9)).range(today)) // end inclusive
        // January: "last month" is December of the previous year
        assertEquals(d(12, 1, 2025) to d(1, 1), DateFilter.LastMonth.range(d(1, 15)))
    }

    @Test
    fun presetsAreAllPresent() {
        // Regression: a stored list in the companion captured null objects
        assertEquals(listOf("All time", "This month", "Last month", "Last 3 months", "This year"),
            DateFilter.presets.map { it.label(Locale.UK) })
    }

    @Test
    fun customRangeLabels() {
        val l = Locale.UK
        assertEquals("3 Nov – 9 Nov 2026", DateFilter.Custom(d(11, 3), d(11, 9)).label(l))
        assertEquals("28 Dec 2025 – 3 Jan 2026", DateFilter.Custom(d(12, 28, 2025), d(1, 3)).label(l))
        assertEquals("9 Nov 2026", DateFilter.Custom(d(11, 9), d(11, 9)).label(l))
    }

    @Test
    fun activeFilterCountIgnoresSearchAndSort() {
        assertEquals(0, TransactionFilter(query = "x", sort = TransactionSort.HIGHEST).activeFilterCount)
        assertEquals(
            4,
            TransactionFilter(
                date = DateFilter.ThisMonth, categories = setOf(Category.FOOD),
                minAmount = 1.0, kind = TransactionKind.RECURRING
            ).activeFilterCount
        )
        assertTrue(TransactionFilter().isDefault)
        assertFalse(TransactionFilter(query = "a").isDefault)
    }

    @Test
    fun groupsByDayForDateSortsOnly() {
        val newest = TransactionFilter().apply(expenses)
        val groups = groupTransactions(newest, TransactionSort.NEWEST)
        assertEquals(listOf(d(11, 18), d(11, 14), d(11, 1), d(10, 30), d(10, 20)), groups.map { it.date })
        assertEquals(440.0, groups.first().total, 0.0)

        val byAmount = groupTransactions(TransactionFilter(sort = TransactionSort.HIGHEST).apply(expenses), TransactionSort.HIGHEST)
        assertEquals(1, byAmount.size)
        assertNull(byAmount.single().date)
        assertTrue(groupTransactions(emptyList(), TransactionSort.HIGHEST).isEmpty())
    }
}
