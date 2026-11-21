package com.guruprasad.mngexpense.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.guruprasad.mngexpense.domain.Category
import com.guruprasad.mngexpense.domain.Frequency
import com.guruprasad.mngexpense.domain.RecurringExpense
import java.time.LocalDate

@Entity(tableName = "recurring_expenses")
data class RecurringExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val note: String,
    val frequency: String,
    @ColumnInfo(name = "repeat_every") val every: Int,
    /** Dates stored as epoch days, like [ExpenseEntity.epochDay]. */
    @ColumnInfo(name = "start_day") val startDay: Long,
    @ColumnInfo(name = "end_day") val endDay: Long?,
    @ColumnInfo(name = "next_index") val nextIndex: Int,
    val paused: Boolean
)

fun RecurringExpenseEntity.toDomain() = RecurringExpense(
    id = id,
    title = title,
    amount = amount,
    category = Category.fromName(category),
    note = note,
    frequency = Frequency.fromName(frequency),
    every = every.coerceAtLeast(1),
    startDate = LocalDate.ofEpochDay(startDay),
    endDate = endDay?.let(LocalDate::ofEpochDay),
    nextIndex = nextIndex,
    paused = paused
)

fun RecurringExpense.toEntity() = RecurringExpenseEntity(
    id = id,
    title = title,
    amount = amount,
    category = category.key,
    note = note,
    frequency = frequency.name,
    every = every,
    startDay = startDate.toEpochDay(),
    endDay = endDate?.toEpochDay(),
    nextIndex = nextIndex,
    paused = paused
)
