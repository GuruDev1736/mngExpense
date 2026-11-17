package com.guruprasad.mngexpense.ui.common

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/** "Today", "Tomorrow", "In 5 days", or a date like "Mon, 1 Dec" further out. */
fun relativeDay(date: LocalDate, today: LocalDate = LocalDate.now()): String {
    val days = ChronoUnit.DAYS.between(today, date)
    return when {
        days == 0L -> "Today"
        days == 1L -> "Tomorrow"
        days in 2..6 -> "In $days days"
        date.year == today.year -> date.format(DateTimeFormatter.ofPattern("EEE, d MMM"))
        else -> date.format(DateTimeFormatter.ofPattern("d MMM yyyy"))
    }
}
