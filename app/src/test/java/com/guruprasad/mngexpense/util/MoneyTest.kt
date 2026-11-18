package com.guruprasad.mngexpense.util

import com.guruprasad.mngexpense.domain.Category
import com.guruprasad.mngexpense.domain.Currencies
import com.guruprasad.mngexpense.domain.Expense
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class MoneyTest {

    @After fun reset() { AppCurrency.code = AppCurrency.DEFAULT }

    @Test
    fun formatsEachCurrencyWithItsSymbolAndDecimals() {
        assertEquals("₹12,400", money(12400.0, "INR"))
        assertEquals("₹120.50", money(120.5, "INR"))
        assertEquals("$4.50", money(4.5, "USD"))
        assertEquals("$1,234", money(1234.0, "USD"))
        assertEquals("€80", money(80.0, "EUR"))
        assertEquals("£9.99", money(9.99, "GBP"))
        assertEquals("AED 250", money(250.0, "AED")) // letters get a space
        assertEquals("¥1,200", money(1200.4, "JPY")) // yen has no decimals
        assertEquals("-$20", money(-20.0, "USD"))
    }

    @Test
    fun defaultsToTheHomeCurrency() {
        assertEquals("₹500", money(500.0))
        AppCurrency.code = "USD"
        assertEquals("$500", money(500.0))
        assertEquals("$", currencySymbol())
    }

    @Test
    fun compactLabels() {
        assertEquals("₹850", compactMoney(850.0, "INR"))
        assertEquals("₹3.5k", compactMoney(3500.0, "INR"))
        assertEquals("₹1.2L", compactMoney(120000.0, "INR"))
        assertEquals("₹2Cr", compactMoney(20_000_000.0, "INR"))
        assertEquals("$1.2M", compactMoney(1_200_000.0, "USD"))
        assertEquals("$950", compactMoney(950.0, "USD"))
    }

    @Test
    fun unknownCodeFallsBackToDefault() {
        assertEquals("₹5", money(5.0, "NOPE"))
    }

    @Test
    fun conversionRoundsToTwoDecimals() {
        assertEquals(8320.0, Currencies.convert(100.0, 83.2), 0.0)
        assertEquals(1153.53, Currencies.convert(12.99, 88.8015), 0.0)
        assertEquals(0.33, Currencies.convert(1.0, 0.3333), 0.0)
    }

    @Test
    fun foreignExpenseKnowsItsRate() {
        val e = Expense(1, "Hotel", 8320.0, Category.OTHER, LocalDate.of(2026, 11, 2), currency = "USD", originalAmount = 100.0)
        assertTrue(e.isForeign)
        assertEquals(83.2, e.exchangeRate!!, 1e-9)
        val home = Expense(2, "Tea", 20.0, Category.FOOD, LocalDate.of(2026, 11, 2))
        assertFalse(home.isForeign)
        assertNull(home.exchangeRate)
    }

    @Test
    fun currencyListIsCleanAndHasPopularOnes() {
        val all = Currencies.all()
        assertTrue(all.containsAll(Currencies.popular))
        assertTrue(all.none { it.startsWith("X") })
        assertEquals(all.sorted(), all)
    }
}
