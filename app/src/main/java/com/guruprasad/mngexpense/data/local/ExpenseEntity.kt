package com.guruprasad.mngexpense.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.guruprasad.mngexpense.domain.Category
import com.guruprasad.mngexpense.domain.Expense
import java.time.LocalDate

@Entity(tableName = "expenses", indices = [Index("epoch_day")])
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val category: String,
    /** Date stored as days since 1970-01-01 (LocalDate.toEpochDay) — timezone-proof. */
    @ColumnInfo(name = "epoch_day") val epochDay: Long,
    val note: String = "",
    /** Set when a recurring expense added this row (no foreign key: history stays if the rule is deleted). */
    @ColumnInfo(name = "recurring_id") val recurringId: Long? = null
)

fun ExpenseEntity.toDomain() = Expense(
    id = id,
    title = title,
    amount = amount,
    category = Category.fromName(category),
    date = LocalDate.ofEpochDay(epochDay),
    note = note,
    recurringId = recurringId
)

fun Expense.toEntity() = ExpenseEntity(
    id = id,
    title = title,
    amount = amount,
    category = category.name,
    epochDay = date.toEpochDay(),
    note = note,
    recurringId = recurringId
)
