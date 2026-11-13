package com.guruprasad.mngexpense.domain

import com.guruprasad.mngexpense.util.compactRupees
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

class PeriodTest {
    private val fri = LocalDate.of(2026, 11, 13)
    private val en = Locale.ENGLISH

    @Test fun weekStartsOnMonday() {
        val p = Period.of(TimeFilter.WEEK, fri)
        assertEquals(LocalDate.of(2026, 11, 9), p.start)
        assertEquals(LocalDate.of(2026, 11, 16), p.endExclusive)
        assertEquals("9 – 15 Nov 2026", p.label(en))
    }

    @Test fun weekLabelAcrossMonthsAndYears() {
        assertEquals("30 Nov – 6 Dec 2026", Period.of(TimeFilter.WEEK, LocalDate.of(2026, 12, 2)).label(en))
        assertEquals("29 Dec 2025 – 4 Jan 2026", Period.of(TimeFilter.WEEK, LocalDate.of(2026, 1, 1)).label(en))
    }

    @Test fun previousAndNextMonth() {
        val p = Period.of(TimeFilter.MONTH, fri)
        assertEquals("November 2026", p.label(en))
        assertEquals("October 2026", p.previous().label(en))
        assertEquals("December 2026", p.next().label(en))
        assertEquals("December 2025", Period.of(TimeFilter.MONTH, LocalDate.of(2026, 1, 5)).previous().label(en))
    }

    @Test fun elapsedDays() {
        val p = Period.of(TimeFilter.MONTH, fri)
        assertEquals(13, p.elapsedDays(fri))
        assertEquals(31, p.previous().elapsedDays(fri)) // October is complete
        assertEquals(0, p.next().elapsedDays(fri))      // December hasn't started
    }

    @Test fun monthBuckets() {
        val p = Period.of(TimeFilter.MONTH, fri)
        val e = listOf(
            Expense(title = "a", amount = 100.0, category = Category.FOOD, date = LocalDate.of(2026, 11, 1)),
            Expense(title = "b", amount = 50.0, category = Category.FOOD, date = LocalDate.of(2026, 11, 8)),
            Expense(title = "c", amount = 25.0, category = Category.FOOD, date = LocalDate.of(2026, 11, 30))
        )
        val b = p.buckets(e, en)
        assertEquals(listOf("Wk 1", "Wk 2", "Wk 3", "Wk 4", "Wk 5"), b.map { it.label })
        assertEquals(listOf(100.0, 50.0, 0.0, 0.0, 25.0), b.map { it.amount })
    }

    @Test fun compactLabels() {
        assertEquals("₹850", compactRupees(850.0))
        assertEquals("₹2k", compactRupees(2000.0))
        assertEquals("₹10.5k", compactRupees(10500.0))
        assertEquals("₹1.2L", compactRupees(120000.0))
    }
}
