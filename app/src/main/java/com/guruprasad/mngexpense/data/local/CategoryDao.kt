package com.guruprasad.mngexpense.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY sort_order, name COLLATE NOCASE")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY sort_order, name COLLATE NOCASE")
    suspend fun getAll(): List<CategoryEntity>

    @Upsert
    suspend fun upsert(category: CategoryEntity)

    @Update
    suspend fun updateAll(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(categories: List<CategoryEntity>)

    @Query("DELETE FROM categories WHERE category_key = :key")
    suspend fun delete(key: String)

    @Query("DELETE FROM categories")
    suspend fun deleteAll()

    /** Number of expenses per category key. */
    @Query("SELECT category, COUNT(*) AS count FROM expenses GROUP BY category")
    fun observeExpenseCounts(): Flow<List<CategoryUsageRow>>

    @Query("SELECT COUNT(*) FROM expenses WHERE category = :key")
    suspend fun expenseCount(key: String): Int

    @Query("SELECT COUNT(*) FROM recurring_expenses WHERE category = :key")
    suspend fun recurringCount(key: String): Int

    /** Every category key used by an expense or recurring rule (to repair data after a restore). */
    @Query("SELECT category FROM expenses UNION SELECT category FROM recurring_expenses")
    suspend fun usedKeys(): List<String>

    // ---- Deleting a category: its expenses and rules move to another one ----

    @Query("UPDATE expenses SET category = :to WHERE category = :from")
    suspend fun moveExpenses(from: String, to: String)

    @Query("UPDATE recurring_expenses SET category = :to WHERE category = :from")
    suspend fun moveRecurring(from: String, to: String)

    @Query("DELETE FROM budgets WHERE budget_key = :key")
    suspend fun deleteBudget(key: String)

    @Query("DELETE FROM budget_alerts WHERE budget_key = :key")
    suspend fun deleteBudgetAlerts(key: String)
}
