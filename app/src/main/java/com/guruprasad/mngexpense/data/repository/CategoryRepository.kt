package com.guruprasad.mngexpense.data.repository

import androidx.room.withTransaction
import com.guruprasad.mngexpense.data.local.AppDatabase
import com.guruprasad.mngexpense.data.local.CategoryEntity
import com.guruprasad.mngexpense.data.local.toDomain
import com.guruprasad.mngexpense.data.local.toEntity
import com.guruprasad.mngexpense.domain.Category
import com.guruprasad.mngexpense.domain.CategoryInfo
import com.guruprasad.mngexpense.domain.CategoryRules
import com.guruprasad.mngexpense.domain.CategoryStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

/** What deleting a category affects (shown in the confirmation). */
data class CategoryUsage(val expenses: Int, val recurring: Int, val budget: Double?) {
    val isUsed get() = expenses > 0 || recurring > 0 || budget != null
}

/** Rejected change, with a message for the user. */
class CategoryException(message: String) : Exception(message)

/**
 * Add, edit, reorder and delete categories. After every change [CategoryStore] is refreshed, so
 * the whole app sees it right away.
 *
 * @param onDataChanged after a delete moved expenses — refreshes widgets and budget alerts.
 */
class CategoryRepository(
    private val db: AppDatabase,
    private val onDataChanged: suspend () -> Unit = {}
) {
    private val dao = db.categoryDao()
    private val lock = Mutex()

    fun observeAll(): Flow<List<CategoryInfo>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    /** Expenses per category key. */
    fun observeExpenseCounts(): Flow<Map<String, Int>> =
        dao.observeExpenseCounts().map { rows -> rows.associate { it.category to it.count } }

    /** Loads the categories into [CategoryStore]. Called at app start and after every change. */
    suspend fun reload() {
        CategoryStore.set(dao.getAll().map { it.toDomain() })
    }

    /** Creates a category at the end of the list and returns it. */
    suspend fun add(name: String, color: Long, icon: String): Category = lock.withLock {
        val all = dao.getAll().map { it.toDomain() }
        CategoryRules.nameError(name, all)?.let { throw CategoryException(it) }
        val key = "c_" + UUID.randomUUID().toString().replace("-", "").take(12)
        dao.upsert(CategoryEntity(key, name.trim(), color, icon, (all.maxOfOrNull { it.sortOrder } ?: -1) + 1))
        reload()
        Category(key)
    }

    /** Renames / recolours / changes the icon. Expenses keep pointing at it. */
    suspend fun update(key: String, name: String, color: Long, icon: String) = lock.withLock {
        val all = dao.getAll().map { it.toDomain() }
        val old = all.firstOrNull { it.key == key } ?: throw CategoryException("This category no longer exists")
        CategoryRules.nameError(name, all, editingKey = key)?.let { throw CategoryException(it) }
        dao.upsert(old.copy(name = name.trim(), color = color, icon = icon).toEntity())
        reload()
    }

    /** Moves a category one place up (-1) or down (+1) in every list. */
    suspend fun move(key: String, delta: Int) = lock.withLock {
        val all = dao.getAll().map { it.toDomain() }
        dao.updateAll(CategoryRules.move(all, key, delta).map { it.toEntity() })
        reload()
    }

    /** Back to the built-in order and names; categories the user added stay, after them. */
    suspend fun restoreDefaults() = lock.withLock {
        val all = dao.getAll().map { it.toDomain() }
        val custom = all.filter { !it.isBuiltIn }
        val defaults = Category.DEFAULTS
        db.withTransaction {
            dao.insertIgnore(defaults.map { it.toEntity() })
            dao.updateAll(CategoryRules.renumber(defaults + custom).map { it.toEntity() })
        }
        reload()
    }

    suspend fun usage(key: String): CategoryUsage = CategoryUsage(
        expenses = dao.expenseCount(key),
        recurring = dao.recurringCount(key),
        budget = db.budgetDao().getAll().firstOrNull { it.key == key }?.amount
    )

    /**
     * Deletes [key]. Its expenses and recurring rules move to [moveTo]; its budget is removed.
     * All or nothing.
     */
    suspend fun delete(key: String, moveTo: String = Category.OTHER.key) {
        lock.withLock {
            if (key == Category.OTHER.key) throw CategoryException("\"Other\" can't be deleted")
            if (key == moveTo) throw CategoryException("Pick another category for its expenses")
            db.withTransaction {
                if (dao.getAll().none { it.key == moveTo }) throw CategoryException("That category no longer exists")
                dao.moveExpenses(key, moveTo)
                dao.moveRecurring(key, moveTo)
                dao.deleteBudget(key)
                dao.deleteBudgetAlerts(key)
                dao.delete(key)
            }
            reload()
        }
        onDataChanged()
    }

    /**
     * After a restore: makes sure "Other" exists and that every category used by an expense or
     * rule exists (a placeholder named after its key otherwise), so nothing points nowhere.
     */
    suspend fun repair() {
        val all = dao.getAll()
        val known = all.map { it.key }.toSet()
        val defaults = Category.DEFAULTS.associateBy { it.key }
        var order = (all.maxOfOrNull { it.sortOrder } ?: -1) + 1
        val missing = (dao.usedKeys() + Category.OTHER.key).distinct().filter { it !in known }.map { key ->
            defaults[key]?.copy(sortOrder = order++)?.toEntity()
                ?: CategoryEntity(key, key.removePrefix("c_").take(CategoryRules.MAX_NAME_LENGTH), 0xFF8A8F98, "category", order++)
        }
        if (missing.isNotEmpty()) dao.insertIgnore(missing)
        reload()
    }
}
