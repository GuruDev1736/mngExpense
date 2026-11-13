package com.guruprasad.mngexpense.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    /** Expenses with startDay <= day < endDay, newest first. */
    @Query("SELECT * FROM expenses WHERE epoch_day >= :startDay AND epoch_day < :endDay ORDER BY epoch_day DESC, id DESC")
    fun observeBetween(startDay: Long, endDay: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getById(id: Long): ExpenseEntity?

    @Upsert
    suspend fun upsert(expense: ExpenseEntity): Long

    @Delete
    suspend fun delete(expense: ExpenseEntity)
}
