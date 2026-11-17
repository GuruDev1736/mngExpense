package com.guruprasad.mngexpense.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/** How many expenses a recurring rule has added so far, and their total. */
data class RecurringHistory(
    @androidx.room.ColumnInfo(name = "recurring_id") val recurringId: Long,
    val count: Int,
    val total: Double
)

@Dao
interface RecurringExpenseDao {

    @Query("SELECT * FROM recurring_expenses ORDER BY paused, title COLLATE NOCASE")
    fun observeAll(): Flow<List<RecurringExpenseEntity>>

    @Query("SELECT * FROM recurring_expenses WHERE id = :id")
    suspend fun getById(id: Long): RecurringExpenseEntity?

    @Query("SELECT * FROM recurring_expenses WHERE paused = 0")
    suspend fun getUnpaused(): List<RecurringExpenseEntity>

    @Upsert
    suspend fun upsert(rule: RecurringExpenseEntity): Long

    @Query("DELETE FROM recurring_expenses WHERE id = :id")
    suspend fun delete(id: Long)

    @Query(
        "SELECT recurring_id, COUNT(*) AS count, SUM(amount) AS total FROM expenses " +
            "WHERE recurring_id = :id GROUP BY recurring_id"
    )
    fun observeHistory(id: Long): Flow<RecurringHistory?>
}
