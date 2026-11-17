package com.guruprasad.mngexpense.domain

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

/** How often a recurring expense repeats. The enum name is what gets stored in the database. */
enum class Frequency(val label: String, private val unit: String) {
    DAILY("Daily", "day"),
    WEEKLY("Weekly", "week"),
    MONTHLY("Monthly", "month"),
    YEARLY("Yearly", "year");

    fun unit(count: Int) = if (count == 1) unit else "${unit}s"

    companion object {
        fun fromName(name: String): Frequency = entries.firstOrNull { it.name == name } ?: MONTHLY
    }
}

/**
 * A rule such as "Rent, ₹15,000, every month on the 1st".
 *
 * Occurrence dates are always computed from [startDate] (never by adding to the previous date),
 * so a rule starting on 31 Jan gives 28/29 Feb, then 31 Mar again — no drift.
 *
 * @param every repeat every N units (e.g. 2 + WEEKLY = every 2 weeks)
 * @param nextIndex how many occurrences have already been added as expenses
 * @param endDate last day an occurrence may fall on (inclusive), or null to repeat forever
 */
data class RecurringExpense(
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: Category,
    val note: String = "",
    val frequency: Frequency = Frequency.MONTHLY,
    val every: Int = 1,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val nextIndex: Int = 0,
    val paused: Boolean = false
) {
    init {
        require(every >= 1) { "every must be at least 1" }
    }

    /** Date of occurrence number [n] (0 = the start date). */
    fun occurrence(n: Int): LocalDate {
        val steps = n.toLong() * every
        return when (frequency) {
            Frequency.DAILY -> startDate.plusDays(steps)
            Frequency.WEEKLY -> startDate.plusWeeks(steps)
            Frequency.MONTHLY -> startDate.plusMonths(steps)
            Frequency.YEARLY -> startDate.plusYears(steps)
        }
    }

    private fun withinEnd(date: LocalDate) = endDate == null || !date.isAfter(endDate)

    /** The next date an expense will be added, or null when the rule has ended. */
    val nextDate: LocalDate? get() = occurrence(nextIndex).takeIf(::withinEnd)

    val hasEnded: Boolean get() = nextDate == null

    /** Date of the most recent expense this rule added, or null if none yet. */
    val lastAddedDate: LocalDate? get() = if (nextIndex > 0) occurrence(nextIndex - 1) else null

    /** Not paused and not ended. */
    val isActive: Boolean get() = !paused && !hasEnded

    /** Dates that are due (on or before [today]) but not added yet, oldest first. */
    fun dueDates(today: LocalDate, limit: Int = MAX_CATCH_UP): List<LocalDate> {
        if (paused) return emptyList()
        val result = mutableListOf<LocalDate>()
        var n = nextIndex
        while (result.size < limit) {
            val d = occurrence(n)
            if (d.isAfter(today) || !withinEnd(d)) break
            result += d
            n++
        }
        return result
    }

    /** Smallest occurrence index whose date is after [date]. */
    fun firstIndexAfter(date: LocalDate): Int {
        if (startDate.isAfter(date)) return 0
        // Jump close to the answer first so daily rules spanning years stay fast
        val unitsBetween = when (frequency) {
            Frequency.DAILY -> ChronoUnit.DAYS.between(startDate, date)
            Frequency.WEEKLY -> ChronoUnit.WEEKS.between(startDate, date)
            Frequency.MONTHLY -> ChronoUnit.MONTHS.between(startDate, date)
            Frequency.YEARLY -> ChronoUnit.YEARS.between(startDate, date)
        }
        var n = (unitsBetween / every).toInt().coerceAtLeast(1) - 1
        while (n > 0 && occurrence(n).isAfter(date)) n--
        while (!occurrence(n).isAfter(date)) n++
        return n
    }

    /** Average cost per month — used for the "fixed costs per month" summary. */
    val monthlyEquivalent: Double
        get() = amount * when (frequency) {
            Frequency.DAILY -> 365.25 / 12
            Frequency.WEEKLY -> 52.1775 / 12
            Frequency.MONTHLY -> 1.0
            Frequency.YEARLY -> 1.0 / 12
        } / every

    /** e.g. "Every month on the 1st", "Every 2 weeks on Monday", "Every year on 5 Mar". */
    fun scheduleText(locale: Locale = Locale.getDefault()): String {
        val head = if (every == 1) "Every ${frequency.unit(1)}" else "Every $every ${frequency.unit(every)}"
        return when (frequency) {
            Frequency.DAILY -> head
            Frequency.WEEKLY -> "$head on ${startDate.dayOfWeek.getDisplayName(TextStyle.FULL, locale)}"
            Frequency.MONTHLY -> "$head on the ${ordinal(startDate.dayOfMonth)}"
            Frequency.YEARLY -> "$head on ${startDate.format(DateTimeFormatter.ofPattern("d MMM", locale))}"
        }
    }

    companion object {
        /** Safety cap for one catch-up run (e.g. a daily rule started years ago). */
        const val MAX_CATCH_UP = 1000

        fun ordinal(n: Int): String {
            val suffix = if (n % 100 in 11..13) "th" else when (n % 10) {
                1 -> "st"; 2 -> "nd"; 3 -> "rd"; else -> "th"
            }
            return "$n$suffix"
        }
    }
}

/**
 * Applies an edit to a rule that may already have added expenses.
 *
 * When editing, the form's date is the *next payment* date (not the original start), so the user
 * decides exactly when the edited schedule continues. [edited].startDate is that date.
 * If date, frequency and interval are unchanged, the original schedule and progress are kept;
 * otherwise the schedule restarts from the chosen date. Expenses already added are never touched.
 */
fun rescheduled(old: RecurringExpense, edited: RecurringExpense): RecurringExpense {
    val sameSchedule = edited.startDate == old.occurrence(old.nextIndex) &&
            edited.frequency == old.frequency && edited.every == old.every
    return if (sameSchedule) edited.copy(startDate = old.startDate, nextIndex = old.nextIndex)
    else edited.copy(nextIndex = 0)
}

/** On resume, skip the dates that passed while the rule was paused. */
fun resumed(rule: RecurringExpense, today: LocalDate): RecurringExpense =
    rule.copy(paused = false, nextIndex = maxOf(rule.nextIndex, rule.firstIndexAfter(today.minusDays(1))))
