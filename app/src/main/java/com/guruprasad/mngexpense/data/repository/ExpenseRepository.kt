package com.guruprasad.mngexpense.data.repository

import com.guruprasad.mngexpense.data.local.ExpenseDao
import com.guruprasad.mngexpense.data.local.toDomain
import com.guruprasad.mngexpense.data.local.toEntity
import com.guruprasad.mngexpense.domain.Expense
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * @param onDataChanged called after every save/delete — used to refresh home-screen widgets.
 */
class ExpenseRepository(
    private val dao: ExpenseDao,
    private val onDataChanged: suspend () -> Unit = {}
) {

    /** Expenses from [start] (inclusive) to [endExclusive]. */
    fun observeBetween(start: LocalDate, endExclusive: LocalDate): Flow<List<Expense>> =
        dao.observeBetween(start.toEpochDay(), endExclusive.toEpochDay())
            .map { list -> list.map { it.toDomain() } }

    /** Expenses from [start] (inclusive) to [endExclusive]; a null end means no limit on that side. */
    fun observeRange(start: LocalDate?, endExclusive: LocalDate?): Flow<List<Expense>> =
        dao.observeBetween(start?.toEpochDay() ?: Long.MIN_VALUE, endExclusive?.toEpochDay() ?: Long.MAX_VALUE)
            .map { list -> list.map { it.toDomain() } }

    suspend fun get(id: Long): Expense? = dao.getById(id)?.toDomain()

    suspend fun save(expense: Expense): Long =
        dao.upsert(expense.toEntity()).also { notifyChanged() }

    suspend fun delete(expense: Expense) {
        dao.delete(expense.toEntity())
        notifyChanged()
    }

    private suspend fun notifyChanged() {
        // A widget refresh failing must never break saving an expense
        try {
            onDataChanged()
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            android.util.Log.w("ExpenseRepository", "Widget refresh failed", e)
        }
    }
}
