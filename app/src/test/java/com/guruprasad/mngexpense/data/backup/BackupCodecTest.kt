package com.guruprasad.mngexpense.data.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class BackupCodecTest {

    private val sample = BackupFile(
        createdAt = 1_795_000_000_000,
        expenses = listOf(
            BackupExpense(1, "Lunch", 250.0, "FOOD", 20_400, "with team"),
            BackupExpense(2, "Netflix", 649.0, "ENTERTAINMENT", 20_401, recurringId = 7),
            BackupExpense(3, "Coffee", 415.0, "FOOD", 20_402, currency = "USD", originalAmount = 5.0)
        ),
        recurring = listOf(BackupRecurring(7, "Netflix", 649.0, "ENTERTAINMENT", "", "MONTHLY", 1, 20_000, null, 3, false)),
        budgets = listOf(BackupBudget("OVERALL", 30_000.0), BackupBudget("FOOD", 8_000.0)),
        budgetAlerts = listOf(BackupBudgetAlert("2026-11", "FOOD", 80)),
        categories = listOf(BackupCategory("FOOD", "Food", 0xFF2A78D6, "restaurant", 0), BackupCategory("c_pets", "Pets", 0xFF6D4C41, "pets", 1)),
        settings = BackupSettings(homeCurrency = "INR", exchangeRates = mapOf("USD_INR" to 83.0), budgetAlerts = false, backupSchedule = "WEEKLY")
    )

    @Test
    fun roundTripKeepsEverything() {
        assertEquals(sample, BackupCodec.decode(BackupCodec.encode(sample)))
    }

    @Test
    fun entityConversionKeepsIdsSoRecurringLinksSurvive() {
        val expense = sample.expenses[1]
        assertEquals(expense, expense.toEntity().toBackup())
        val rule = sample.recurring[0]
        assertEquals(rule, rule.toEntity().toBackup())
    }

    @Test
    fun unknownFieldsFromNewerVersionsAreIgnored() {
        val json = """{"app":"mngexpense","version":1,"createdAt":5,"futureField":[1,2],
            "expenses":[{"id":1,"title":"Tea","amount":20.0,"category":"FOOD","epochDay":3,"tags":["x"]}]}"""
        val backup = BackupCodec.decode(json)
        assertEquals("Tea", backup.expenses.single().title)
        assertEquals(BackupSettings(), backup.settings)
    }

    @Test
    fun rejectsOtherFiles() {
        assertThrows(InvalidBackupException::class.java) { BackupCodec.decode("not json") }
        assertThrows(InvalidBackupException::class.java) { BackupCodec.decode("""{"name":"something else"}""") }
        assertThrows(InvalidBackupException::class.java) {
            BackupCodec.decode("""{"app":"otherapp","version":1,"createdAt":1}""")
        }
    }

    @Test
    fun rejectsBackupsFromNewerFormat() {
        val e = assertThrows(InvalidBackupException::class.java) {
            BackupCodec.decode("""{"app":"mngexpense","version":99,"createdAt":1}""")
        }
        assert(e.message!!.contains("newer version"))
    }
}
