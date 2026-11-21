package com.guruprasad.mngexpense.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Query("SELECT * FROM budgets")
    fun observeAll(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets")
    suspend fun getAll(): List<BudgetEntity>

    @Upsert
    suspend fun upsert(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE budget_key = :key")
    suspend fun delete(key: String)

    @Query("SELECT level FROM budget_alerts WHERE month = :month AND budget_key = :key")
    suspend fun sentLevels(month: String, key: String): List<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun recordAlert(alert: BudgetAlertEntity)

    /** Housekeeping: forget alerts of months before [month]. */
    @Query("DELETE FROM budget_alerts WHERE month < :month")
    suspend fun deleteAlertsBefore(month: String)

    // ---- Backup & restore ----

    @Query("SELECT * FROM budget_alerts")
    suspend fun getAllAlerts(): List<BudgetAlertEntity>

    @Query("DELETE FROM budgets")
    suspend fun deleteAll()

    @Query("DELETE FROM budget_alerts")
    suspend fun deleteAllAlerts()

    @Insert
    suspend fun insertAll(budgets: List<BudgetEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAlerts(alerts: List<BudgetAlertEntity>)
}
