package com.guruprasad.mngexpense.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/** Total spent in one category (row of [ExpenseDao.categoryTotals]). */
data class CategoryTotalRow(val category: String, val total: Double)

@Dao
interface ExpenseDao {

    /** Expenses with startDay <= day < endDay, newest first. */
    @Query("SELECT * FROM expenses WHERE epoch_day >= :startDay AND epoch_day < :endDay ORDER BY epoch_day DESC, id DESC")
    fun observeBetween(startDay: Long, endDay: Long): Flow<List<ExpenseEntity>>

    /** Spending per category for startDay <= day < endDay. */
    @Query(
        "SELECT category, SUM(amount) AS total FROM expenses " +
            "WHERE epoch_day >= :startDay AND epoch_day < :endDay GROUP BY category"
    )
    suspend fun categoryTotals(startDay: Long, endDay: Long): List<CategoryTotalRow>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getById(id: Long): ExpenseEntity?

    @Upsert
    suspend fun upsert(expense: ExpenseEntity): Long

    @Delete
    suspend fun delete(expense: ExpenseEntity)

    /** Keeps the expenses but forgets which recurring rule added them (used when the rule is deleted). */
    @Query("UPDATE expenses SET recurring_id = NULL WHERE recurring_id = :recurringId")
    suspend fun unlinkRecurring(recurringId: Long)
}
