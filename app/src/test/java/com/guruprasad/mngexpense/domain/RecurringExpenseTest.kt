package com.guruprasad.mngexpense.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

class RecurringExpenseTest {

    private fun rule(
        start: LocalDate,
        frequency: Frequency = Frequency.MONTHLY,
        every: Int = 1,
        end: LocalDate? = null,
        nextIndex: Int = 0,
        paused: Boolean = false
    ) = RecurringExpense(
        title = "Rent", amount = 1200.0, category = Category.BILLS,
        frequency = frequency, every = every, startDate = start, endDate = end,
        nextIndex = nextIndex, paused = paused
    )

    private fun d(y: Int, m: Int, day: Int) = LocalDate.of(y, m, day)

    @Test
    fun monthlyOnThe31stClampsWithoutDrifting() {
        val r = rule(d(2026, 1, 31))
        assertEquals(
            listOf(d(2026, 1, 31), d(2026, 2, 28), d(2026, 3, 31), d(2026, 4, 30)),
            (0..3).map(r::occurrence)
        )
    }

    @Test
    fun yearlyOnLeapDay() {
        val r = rule(d(2028, 2, 29), Frequency.YEARLY)
        assertEquals(d(2029, 2, 28), r.occurrence(1))
        assertEquals(d(2032, 2, 29), r.occurrence(4))
    }

    @Test
    fun everyTwoWeeks() {
        val r = rule(d(2026, 11, 2), Frequency.WEEKLY, every = 2)
        assertEquals(d(2026, 11, 16), r.occurrence(1))
        assertEquals(d(2026, 11, 30), r.occurrence(2))
    }

    @Test
    fun dueDatesCatchUpOnMissedDays() {
        val r = rule(d(2026, 11, 15), Frequency.DAILY, nextIndex = 1) // 15th already added
        assertEquals(listOf(d(2026, 11, 16), d(2026, 11, 17), d(2026, 11, 18)), r.dueDates(d(2026, 11, 18)))
    }

    @Test
    fun startInPastAddsAllPastDatesIncludingToday() {
        val r = rule(d(2026, 9, 18))
        assertEquals(listOf(d(2026, 9, 18), d(2026, 10, 18), d(2026, 11, 18)), r.dueDates(d(2026, 11, 18)))
    }

    @Test
    fun futureStartHasNothingDue() {
        val r = rule(d(2026, 12, 1))
        assertTrue(r.dueDates(d(2026, 11, 18)).isEmpty())
        assertEquals(d(2026, 12, 1), r.nextDate)
    }

    @Test
    fun endDateIsInclusiveAndEndsTheRule() {
        val r = rule(d(2026, 1, 10), end = d(2026, 3, 10))
        assertEquals(3, r.dueDates(d(2026, 12, 31)).size)
        val done = r.copy(nextIndex = 3)
        assertNull(done.nextDate)
        assertTrue(done.hasEnded)
        assertTrue(done.dueDates(d(2027, 1, 1)).isEmpty())
    }

    @Test
    fun pausedRuleHasNothingDue() {
        assertTrue(rule(d(2026, 1, 1), paused = true).dueDates(d(2026, 11, 18)).isEmpty())
    }

    @Test
    fun catchUpIsCapped() {
        val r = rule(d(2000, 1, 1), Frequency.DAILY)
        assertEquals(RecurringExpense.MAX_CATCH_UP, r.dueDates(d(2026, 11, 18)).size)
    }

    @Test
    fun firstIndexAfterMatchesBruteForce() {
        val starts = listOf(d(2024, 1, 31), d(2025, 2, 28), d(2026, 11, 18))
        val dates = listOf(d(2023, 6, 1), d(2024, 1, 31), d(2024, 3, 30), d(2026, 11, 18), d(2030, 7, 4))
        for (start in starts) for (f in Frequency.entries) for (every in listOf(1, 3)) for (date in dates) {
            val r = rule(start, f, every)
            val expected = (0..20_000).first { r.occurrence(it).isAfter(date) }
            assertEquals("$f x$every from $start after $date", expected, r.firstIndexAfter(date))
        }
    }

    @Test
    fun editingAmountKeepsScheduleAndProgress() {
        val old = rule(d(2026, 1, 18), nextIndex = 11) // Jan–Nov added, next 18 Dec
        // The edit form shows the next payment date as its date
        val new = rescheduled(old, old.copy(amount = 1500.0, startDate = d(2026, 12, 18)))
        assertEquals(d(2026, 1, 18), new.startDate)
        assertEquals(11, new.nextIndex)
        assertEquals(1500.0, new.amount, 0.0)
    }

    @Test
    fun movingNextPaymentContinuesFromChosenDate() {
        // Paid on 18 Nov; the user moves the next payment from 18 Dec to 20 Dec
        val old = rule(d(2026, 9, 18), nextIndex = 3)
        val new = rescheduled(old, old.copy(startDate = d(2026, 12, 20)))
        assertEquals(d(2026, 12, 20), new.nextDate)
        assertTrue(new.dueDates(d(2026, 11, 18)).isEmpty()) // November is not charged twice
        assertEquals(d(2027, 1, 20), new.occurrence(new.nextIndex + 1))
    }

    @Test
    fun changingFrequencyRestartsFromNextPaymentDate() {
        val old = rule(d(2026, 9, 18), nextIndex = 3)
        val new = rescheduled(old, old.copy(startDate = d(2026, 12, 18), frequency = Frequency.WEEKLY))
        assertEquals(d(2026, 12, 18), new.nextDate)
        assertEquals(d(2026, 12, 25), new.occurrence(1))
    }

    @Test
    fun lastAddedDate() {
        assertNull(rule(d(2026, 9, 18)).lastAddedDate)
        assertEquals(d(2026, 11, 18), rule(d(2026, 9, 18), nextIndex = 3).lastAddedDate)
    }

    @Test
    fun resumeSkipsDatesMissedWhilePaused() {
        val r = rule(d(2026, 1, 5), nextIndex = 6, paused = true) // Jan–Jun added, then paused
        val back = resumed(r, d(2026, 11, 18))
        assertEquals(d(2026, 12, 5), back.nextDate)
        assertTrue(back.dueDates(d(2026, 11, 18)).isEmpty())
    }

    @Test
    fun resumeOnTheDueDayStillAddsToday() {
        val r = rule(d(2026, 1, 18), nextIndex = 3, paused = true)
        assertEquals(listOf(d(2026, 11, 18)), resumed(r, d(2026, 11, 18)).dueDates(d(2026, 11, 18)))
    }

    @Test
    fun monthlyEquivalent() {
        assertEquals(1200.0, rule(d(2026, 1, 1)).monthlyEquivalent, 0.001)
        assertEquals(100.0, rule(d(2026, 1, 1), Frequency.YEARLY).monthlyEquivalent, 0.001)
        assertEquals(600.0, rule(d(2026, 1, 1), every = 2).monthlyEquivalent, 0.001)
    }

    @Test
    fun scheduleText() {
        val l = Locale.UK
        assertEquals("Every month on the 1st", rule(d(2026, 12, 1)).scheduleText(l))
        assertEquals("Every 3 months on the 22nd", rule(d(2026, 12, 22), every = 3).scheduleText(l))
        assertEquals("Every 2 weeks on Monday", rule(d(2026, 11, 16), Frequency.WEEKLY, 2).scheduleText(l))
        assertEquals("Every day", rule(d(2026, 11, 16), Frequency.DAILY).scheduleText(l))
        assertEquals("Every year on 5 Mar", rule(d(2027, 3, 5), Frequency.YEARLY).scheduleText(l))
        assertEquals("11th", RecurringExpense.ordinal(11))
        assertEquals("23rd", RecurringExpense.ordinal(23))
    }
}
