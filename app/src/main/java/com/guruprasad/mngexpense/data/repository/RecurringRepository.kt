package com.guruprasad.mngexpense.data.repository

import androidx.room.withTransaction
import com.guruprasad.mngexpense.data.local.AppDatabase
import com.guruprasad.mngexpense.data.local.RecurringHistory
import com.guruprasad.mngexpense.data.local.toDomain
import com.guruprasad.mngexpense.data.local.toEntity
import com.guruprasad.mngexpense.domain.Expense
import com.guruprasad.mngexpense.domain.Frequency
import com.guruprasad.mngexpense.domain.RecurringExpense
import com.guruprasad.mngexpense.domain.rescheduled
import com.guruprasad.mngexpense.domain.resumed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate

/**
 * Recurring expense rules, and the "catch-up" that turns due dates into real expenses.
 *
 * @param onDataChanged called after expenses were added — refreshes home-screen widgets.
 */
class RecurringRepository(
    private val db: AppDatabase,
    private val onDataChanged: suspend () -> Unit = {},
    private val today: () -> LocalDate = LocalDate::now
) {
    private val dao = db.recurringExpenseDao()
    private val expenseDao = db.expenseDao()

    /** App start, the daily worker and a save can all trigger a catch-up — never run two at once. */
    private val processLock = Mutex()

    fun observeAll(): Flow<List<RecurringExpense>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeHistory(id: Long): Flow<RecurringHistory?> = dao.observeHistory(id)

    suspend fun get(id: Long): RecurringExpense? = dao.getById(id)?.toDomain()

    /**
     * Creates or updates a rule, then adds any dates that are already due.
     * When editing, [rule].startDate is the next payment date (see [rescheduled]); expenses
     * already added are kept.
     * @return expenses added right away (e.g. a start date in the past)
     */
    suspend fun save(rule: RecurringExpense): List<Expense> {
        db.withTransaction {
            val toSave = if (rule.id == 0L) rule.copy(nextIndex = 0) else {
                val old = dao.getById(rule.id)?.toDomain()
                if (old == null) rule.copy(id = 0, nextIndex = 0)
                else rescheduled(old, rule.copy(paused = old.paused))
            }
            dao.upsert(toSave.toEntity())
        }
        return processDue()
    }

    /**
     * Saves [expense] and a rule that repeats it from its date on (the "Repeat" switch on the add form).
     * @return the expenses added for past dates after the first one
     */
    suspend fun saveRepeating(expense: Expense, frequency: Frequency, every: Int = 1): List<Expense> {
        db.withTransaction {
            val ruleId = dao.upsert(
                RecurringExpense(
                    title = expense.title,
                    amount = expense.amount,
                    category = expense.category,
                    note = expense.note,
                    frequency = frequency,
                    every = every,
                    startDate = expense.date,
                    nextIndex = 1 // occurrence 0 is the expense saved below
                ).toEntity()
            )
            expenseDao.upsert(expense.copy(id = 0, recurringId = ruleId).toEntity())
        }
        val added = processDue()
        if (added.isEmpty()) notifyChanged() // the first expense still needs a widget refresh
        return added
    }

    suspend fun setPaused(id: Long, paused: Boolean) {
        val rule = get(id) ?: return
        dao.upsert((if (paused) rule.copy(paused = true) else resumed(rule, today())).toEntity())
        if (!paused) processDue()
    }

    /** Deletes the rule. Expenses it already added stay in the history. */
    suspend fun delete(id: Long) {
        db.withTransaction {
            expenseDao.unlinkRecurring(id)
            dao.delete(id)
        }
    }

    /**
     * Adds an expense for every due date of every active rule (also catching up on days
     * the app did not run). Safe to call often: each date is added exactly once.
     * @return the expenses that were added
     */
    suspend fun processDue(): List<Expense> = processLock.withLock {
        val day = today()
        val added = mutableListOf<Expense>()
        db.withTransaction {
            for (entity in dao.getUnpaused()) {
                val rule = entity.toDomain()
                val dates = rule.dueDates(day)
                if (dates.isEmpty()) continue
                for (date in dates) {
                    val expense = Expense(
                        title = rule.title,
                        amount = rule.amount,
                        category = rule.category,
                        date = date,
                        note = rule.note,
                        recurringId = rule.id
                    )
                    expenseDao.upsert(expense.toEntity())
                    added += expense
                }
                dao.upsert(entity.copy(nextIndex = rule.nextIndex + dates.size))
            }
        }
        if (added.isNotEmpty()) notifyChanged()
        added
    }

    private suspend fun notifyChanged() {
        try {
            onDataChanged()
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            android.util.Log.w("RecurringRepository", "Widget refresh failed", e)
        }
    }
}
