package com.guruprasad.mngexpense.domain

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Locale

enum class TimeFilter(val title: String) { DAY("Day"), WEEK("Week"), MONTH("Month"), YEAR("Year") }

/** One bar in a bar chart. */
data class Bucket(val label: String, val amount: Double)

/**
 * A concrete date range for a [TimeFilter], e.g. "the week containing 13 Nov 2026".
 * [start] is inclusive, [endExclusive] is exclusive. Weeks start on Monday.
 */
data class Period(val filter: TimeFilter, val start: LocalDate, val endExclusive: LocalDate) {

    val lengthDays: Long get() = ChronoUnit.DAYS.between(start, endExclusive)

    fun previous(): Period = of(filter, start.minusDays(1))
    fun next(): Period = of(filter, endExclusive)

    fun contains(date: LocalDate) = !date.isBefore(start) && date.isBefore(endExclusive)

    /** Days of this period up to and including [today] (the whole period if it is in the past). */
    fun elapsedDays(today: LocalDate): Long = when {
        today.isBefore(start) -> 0
        contains(today) -> ChronoUnit.DAYS.between(start, today) + 1
        else -> lengthDays
    }

    fun label(locale: Locale = Locale.getDefault()): String = when (filter) {
        TimeFilter.DAY -> start.format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy", locale))
        TimeFilter.WEEK -> {
            val end = endExclusive.minusDays(1)
            val startPattern = when {
                start.year != end.year -> "d MMM yyyy"
                start.month != end.month -> "d MMM"
                else -> "d"
            }
            start.format(DateTimeFormatter.ofPattern(startPattern, locale)) + " – " +
                    end.format(DateTimeFormatter.ofPattern("d MMM yyyy", locale))
        }
        TimeFilter.MONTH -> start.format(DateTimeFormatter.ofPattern("MMMM yyyy", locale))
        TimeFilter.YEAR -> start.year.toString()
    }

    /**
     * Splits [expenses] (already inside this period) into chart bars:
     * Day → by category, Week → Mon..Sun, Month → week 1..5, Year → Jan..Dec.
     */
    fun buckets(expenses: List<Expense>, locale: Locale = Locale.getDefault()): List<Bucket> = when (filter) {
        TimeFilter.DAY -> {
            val bars = Category.entries.map { c -> c to expenses.filter { it.category == c }.sumOf { it.amount } }
            // Up to 7 categories: one bar each, like before. More: only those with spending,
            // so many custom categories don't squeeze the chart into slivers
            (if (bars.size <= 7) bars else bars.filter { it.second > 0 }).map { (c, total) -> Bucket(c.shortLabel, total) }
        }
        TimeFilter.WEEK -> (0 until 7).map { i ->
            val day = start.plusDays(i.toLong())
            Bucket(
                day.dayOfWeek.getDisplayName(TextStyle.SHORT, locale),
                expenses.filter { it.date == day }.sumOf { it.amount }
            )
        }
        TimeFilter.MONTH -> {
            val weeks = ((start.lengthOfMonth() - 1) / 7) + 1
            (1..weeks).map { w ->
                Bucket("Wk $w", expenses.filter { (it.date.dayOfMonth - 1) / 7 + 1 == w }.sumOf { it.amount })
            }
        }
        TimeFilter.YEAR -> (1..12).map { m ->
            Bucket(
                java.time.Month.of(m).getDisplayName(TextStyle.SHORT, locale),
                expenses.filter { it.date.monthValue == m }.sumOf { it.amount }
            )
        }
    }

    companion object {
        fun of(filter: TimeFilter, anchor: LocalDate): Period = when (filter) {
            TimeFilter.DAY -> Period(filter, anchor, anchor.plusDays(1))
            TimeFilter.WEEK -> {
                val monday = anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                Period(filter, monday, monday.plusWeeks(1))
            }
            TimeFilter.MONTH -> {
                val first = anchor.withDayOfMonth(1)
                Period(filter, first, first.plusMonths(1))
            }
            TimeFilter.YEAR -> {
                val first = anchor.withDayOfYear(1)
                Period(filter, first, first.plusYears(1))
            }
        }
    }
}
