package com.guruprasad.mngexpense.domain

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Everything about a category that the user can change.
 *
 * @param key stored with every expense, recurring rule and budget. Never changes, so renaming a
 *   category renames it everywhere. Built-in categories use their old enum names ("FOOD").
 * @param color ARGB, e.g. 0xFF2A78D6
 * @param icon a key of the icon set in ui/common/CategoryIcons.kt
 */
data class CategoryInfo(
    val key: String,
    val name: String,
    val color: Long,
    val icon: String,
    val sortOrder: Int = 0
) {
    val isBuiltIn: Boolean get() = Category.DEFAULTS.any { it.key == key }

    /** "Other" catches expenses of deleted categories, so it can't be deleted itself. */
    val canDelete: Boolean get() = key != Category.OTHER.key
}

/**
 * An expense category: just its [key]. Name, colour and icon are looked up in [CategoryStore],
 * so a rename shows everywhere at once (screens redraw by themselves: the store is Compose state).
 */
@Immutable
data class Category(val key: String) {
    val info: CategoryInfo get() = CategoryStore.find(key) ?: CategoryStore.fallback(key)
    val label: String get() = info.name

    /** For narrow chart labels: "Travel", "Fun", or the name cut short. */
    val shortLabel: String
        get() {
            val name = label
            DEFAULTS.firstOrNull { it.key == key && it.name == name }?.let { return SHORT_LABELS[key] ?: name }
            return if (name.length <= 7) name else name.take(6) + "…"
        }

    /** Position in the user's order (for sorting); unknown categories go last. */
    val order: Int get() = CategoryStore.all.indexOfFirst { it.key == key }.let { if (it < 0) Int.MAX_VALUE else it }

    override fun toString() = key

    companion object {
        val FOOD = Category("FOOD")
        val TRANSPORT = Category("TRANSPORT")
        val SHOPPING = Category("SHOPPING")
        val BILLS = Category("BILLS")
        val ENTERTAINMENT = Category("ENTERTAINMENT")
        val HEALTH = Category("HEALTH")
        val OTHER = Category("OTHER")

        /** The categories a new install starts with (also what older backups know). */
        val DEFAULTS: List<CategoryInfo> = listOf(
            CategoryInfo("FOOD", "Food", 0xFF2A78D6, "restaurant", 0),
            CategoryInfo("TRANSPORT", "Transport", 0xFFEB6834, "car", 1),
            CategoryInfo("SHOPPING", "Shopping", 0xFF1BAF7A, "shopping", 2),
            CategoryInfo("BILLS", "Bills", 0xFFEDA100, "receipt", 3),
            CategoryInfo("ENTERTAINMENT", "Entertainment", 0xFFE87BA4, "movie", 4),
            CategoryInfo("HEALTH", "Health", 0xFF008300, "health", 5),
            CategoryInfo("OTHER", "Other", 0xFF8A8F98, "category", 6)
        )

        private val SHORT_LABELS = mapOf("TRANSPORT" to "Travel", "SHOPPING" to "Shop", "ENTERTAINMENT" to "Fun")

        /** All categories, in the user's order. */
        val entries: List<Category> get() = CategoryStore.all.map { Category(it.key) }

        /** The category stored as [key]; [OTHER] if it no longer exists. */
        fun fromName(key: String): Category = if (CategoryStore.find(key) != null) Category(key) else OTHER

        /** For a new expense: the first category, as long as it still exists. */
        fun firstOr(preferred: Category): Category =
            if (CategoryStore.find(preferred.key) != null) preferred else entries.firstOrNull() ?: OTHER
    }
}

/**
 * The categories currently in the database, kept in memory so any code (UI, widget, export,
 * notifications) can look one up without waiting. Loaded when the app starts and refreshed after
 * every change by CategoryRepository.
 */
object CategoryStore {
    var all: List<CategoryInfo> by mutableStateOf(Category.DEFAULTS)
        private set

    fun set(categories: List<CategoryInfo>) {
        // Never empty: if the table is somehow empty, keep the built-in ones on screen
        all = categories.sortedBy { it.sortOrder }.ifEmpty { Category.DEFAULTS }
    }

    fun find(key: String): CategoryInfo? = all.firstOrNull { it.key == key }

    /** For a key that isn't (or no longer) a category: shown like "Other". */
    fun fallback(key: String): CategoryInfo =
        (find(Category.OTHER.key) ?: Category.DEFAULTS.last()).copy(key = key)
}

/** Rules for category names and order (pure, so they're unit-tested). */
object CategoryRules {
    const val MAX_NAME_LENGTH = 24

    /** Why [name] can't be used, or null if it's fine. [editingKey] is the category being renamed. */
    fun nameError(name: String, existing: List<CategoryInfo>, editingKey: String? = null): String? {
        val trimmed = name.trim()
        return when {
            trimmed.isEmpty() -> "Enter a name"
            trimmed.length > MAX_NAME_LENGTH -> "At most $MAX_NAME_LENGTH characters"
            existing.any { it.key != editingKey && it.name.trim().equals(trimmed, ignoreCase = true) } ->
                "A category with this name already exists"
            else -> null
        }
    }

    /** [list] with [key] moved one place up (delta = -1) or down (+1), renumbered 0, 1, 2… */
    fun move(list: List<CategoryInfo>, key: String, delta: Int): List<CategoryInfo> {
        val from = list.indexOfFirst { it.key == key }
        val to = from + delta
        if (from < 0 || to !in list.indices) return renumber(list)
        val moved = list.toMutableList().apply { add(to, removeAt(from)) }
        return renumber(moved)
    }

    fun renumber(list: List<CategoryInfo>) = list.mapIndexed { i, c -> c.copy(sortOrder = i) }
}
