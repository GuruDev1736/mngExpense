package com.guruprasad.mngexpense.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.guruprasad.mngexpense.domain.Category

@Database(
    entities = [
        ExpenseEntity::class, RecurringExpenseEntity::class,
        BudgetEntity::class, BudgetAlertEntity::class, CategoryEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun recurringExpenseDao(): RecurringExpenseDao
    abstract fun budgetDao(): BudgetDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        /** v2: recurring expenses. Existing expenses are kept. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `expenses` ADD COLUMN `recurring_id` INTEGER")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `recurring_expenses` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`title` TEXT NOT NULL, `amount` REAL NOT NULL, `category` TEXT NOT NULL, " +
                        "`note` TEXT NOT NULL, `frequency` TEXT NOT NULL, `repeat_every` INTEGER NOT NULL, " +
                        "`start_day` INTEGER NOT NULL, `end_day` INTEGER, `next_index` INTEGER NOT NULL, " +
                        "`paused` INTEGER NOT NULL)"
                )
            }
        }

        /** v3: monthly budgets and the record of sent budget alerts. */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `budgets` (" +
                        "`budget_key` TEXT NOT NULL, `amount` REAL NOT NULL, PRIMARY KEY(`budget_key`))"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `budget_alerts` (" +
                        "`month` TEXT NOT NULL, `budget_key` TEXT NOT NULL, `level` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`month`, `budget_key`, `level`))"
                )
            }
        }

        /** v4: expenses paid in another currency keep their original currency and amount. */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `expenses` ADD COLUMN `currency` TEXT")
                db.execSQL("ALTER TABLE `expenses` ADD COLUMN `original_amount` REAL")
            }
        }

        /** v5: categories the user can add, rename, recolour, reorder and delete. Starts with the built-in ones. */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                createCategoriesTable(db)
                seedDefaultCategories(db)
            }
        }

        private fun createCategoriesTable(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `categories` (" +
                    "`category_key` TEXT NOT NULL, `name` TEXT NOT NULL, `color` INTEGER NOT NULL, " +
                    "`icon` TEXT NOT NULL, `sort_order` INTEGER NOT NULL, PRIMARY KEY(`category_key`))"
            )
        }

        /** Inserts the built-in categories that are missing (never overwrites the user's changes). */
        fun seedDefaultCategories(db: SupportSQLiteDatabase) {
            Category.DEFAULTS.forEach { c ->
                db.execSQL(
                    "INSERT OR IGNORE INTO `categories` (`category_key`, `name`, `color`, `icon`, `sort_order`) VALUES (?, ?, ?, ?, ?)",
                    arrayOf<Any>(c.key, c.name, c.color, c.icon, c.sortOrder)
                )
            }
        }

        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "mngexpense.db")
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .addCallback(object : Callback() {
                    // A new install: Room created the tables, add the built-in categories
                    override fun onCreate(db: SupportSQLiteDatabase) = seedDefaultCategories(db)
                })
                .build()
    }
}
