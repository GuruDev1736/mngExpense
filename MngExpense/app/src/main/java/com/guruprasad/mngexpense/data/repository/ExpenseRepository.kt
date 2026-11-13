package com.guruprasad.mngexpense.data.repository

import com.guruprasad.mngexpense.data.local.ExpenseDao
import com.guruprasad.mngexpense.data.local.toDomain
import com.guruprasad.mngexpense.data.local.toEntity
import com.guruprasad.mngexpense.domain.Expense
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class ExpenseRepository(private val dao: ExpenseDao) {

    /** Expenses from [start] (inclusive) to [endExclusive]. */
    fun observeBetween(start: LocalDate, endExclusive: LocalDate): Flow<List<Expense>> =
        dao.observeBetween(start.toEpochDay(), endExclusive.toEpochDay())
            .map { list -> list.map { it.toDomain() } }

    suspend fun get(id: Long): Expense? = dao.getById(id)?.toDomain()

    suspend fun save(expense: Expense): Long = dao.upsert(expense.toEntity())

    suspend fun delete(expense: Expense) = dao.delete(expense.toEntity())
}
