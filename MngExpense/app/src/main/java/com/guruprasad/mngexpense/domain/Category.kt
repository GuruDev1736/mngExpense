package com.guruprasad.mngexpense.domain

/** Expense categories. The enum name is what gets stored in the database. */
enum class Category(val label: String, val shortLabel: String) {
    FOOD("Food", "Food"),
    TRANSPORT("Transport", "Travel"),
    SHOPPING("Shopping", "Shop"),
    BILLS("Bills", "Bills"),
    ENTERTAINMENT("Entertainment", "Fun"),
    HEALTH("Health", "Health"),
    OTHER("Other", "Other");

    companion object {
        fun fromName(name: String): Category = entries.firstOrNull { it.name == name } ?: OTHER
    }
}
