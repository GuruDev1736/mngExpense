package com.guruprasad.mngexpense.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.guruprasad.mngexpense.domain.Budget

/** One row per budget: "OVERALL" or a category name. */
@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey @ColumnInfo(name = "budget_key") val key: String,
    val amount: Double
)

/** Remembers which alerts were sent, so each fires at most once per budget per month. */
@Entity(tableName = "budget_alerts", primaryKeys = ["month", "budget_key", "level"])
data class BudgetAlertEntity(
    /** "2026-11" — sorts and compares correctly as text. */
    val month: String,
    @ColumnInfo(name = "budget_key") val key: String,
    /** 80 or 100 */
    val level: Int
)

fun BudgetEntity.toDomain(): Budget? {
    val category = Budget.categoryOf(key)
    // A key that is neither OVERALL nor a known category (e.g. a removed category) is ignored
    if (category == null && key != Budget.OVERALL_KEY) return null
    return Budget(category, amount)
}

fun Budget.toEntity() = BudgetEntity(key = key, amount = amount)
