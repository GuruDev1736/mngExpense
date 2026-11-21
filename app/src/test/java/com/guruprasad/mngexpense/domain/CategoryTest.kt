package com.guruprasad.mngexpense.domain

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryTest {

    private val pets = CategoryInfo("c_pets", "Pets", 0xFF6D4C41, "pets", 7)

    @After
    fun resetStore() = CategoryStore.set(Category.DEFAULTS)

    @Test
    fun builtInsKeepTheirOldKeysSoExistingDataStillMatches() {
        assertEquals(listOf("FOOD", "TRANSPORT", "SHOPPING", "BILLS", "ENTERTAINMENT", "HEALTH", "OTHER"), Category.entries.map { it.key })
        assertEquals("Food", Category.FOOD.label)
        assertTrue(CategoryStore.find("FOOD")!!.isBuiltIn)
        assertFalse(pets.isBuiltIn)
    }

    @Test
    fun customCategoryIsFoundAndSorted() {
        CategoryStore.set(Category.DEFAULTS + pets.copy(sortOrder = -1))
        assertEquals(Category("c_pets"), Category.entries.first())
        assertEquals("Pets", Category.fromName("c_pets").label)
        assertEquals(0, Category("c_pets").order)
    }

    @Test
    fun renameShowsEverywhereBecauseOnlyTheKeyIsStored() {
        val food = Category.FOOD // e.g. held by an expense on screen
        CategoryStore.set(Category.DEFAULTS.map { if (it.key == "FOOD") it.copy(name = "Groceries") else it })
        assertEquals("Groceries", food.label)
        assertEquals("Grocer…", food.shortLabel)
    }

    @Test
    fun unknownOrDeletedKeyFallsBackToOther() {
        assertEquals(Category.OTHER, Category.fromName("c_deleted"))
        assertEquals("Other", Category("c_deleted").label)
        assertEquals(Int.MAX_VALUE, Category("c_deleted").order)
    }

    @Test
    fun firstOrPicksAnExistingCategory() {
        assertEquals(Category.FOOD, Category.firstOr(Category.FOOD))
        CategoryStore.set(Category.DEFAULTS.filter { it.key != "FOOD" })
        assertEquals(Category.TRANSPORT, Category.firstOr(Category.FOOD))
    }

    @Test
    fun shortLabels() {
        assertEquals("Travel", Category.TRANSPORT.shortLabel)
        assertEquals("Fun", Category.ENTERTAINMENT.shortLabel)
        CategoryStore.set(Category.DEFAULTS + pets.copy(name = "Subscriptions"))
        assertEquals("Subscr…", Category("c_pets").shortLabel)
        // A renamed built-in uses its new name, not the old short label
        CategoryStore.set(Category.DEFAULTS.map { if (it.key == "TRANSPORT") it.copy(name = "Cab") else it })
        assertEquals("Cab", Category.TRANSPORT.shortLabel)
    }

    @Test
    fun otherCantBeDeleted() {
        assertFalse(CategoryStore.find("OTHER")!!.canDelete)
        assertTrue(CategoryStore.find("FOOD")!!.canDelete)
        assertTrue(pets.canDelete)
    }

    @Test
    fun emptyStoreKeepsBuiltIns() {
        CategoryStore.set(emptyList())
        assertEquals(7, Category.entries.size)
    }

    @Test
    fun nameValidation() {
        val all = Category.DEFAULTS + pets
        assertEquals("Enter a name", CategoryRules.nameError("   ", all))
        assertEquals("A category with this name already exists", CategoryRules.nameError(" food ", all))
        assertEquals("A category with this name already exists", CategoryRules.nameError("PETS", all))
        assertNotNull(CategoryRules.nameError("x".repeat(CategoryRules.MAX_NAME_LENGTH + 1), all))
        assertNull(CategoryRules.nameError("Gym", all))
        // Renaming a category to its own name (or another case of it) is fine
        assertNull(CategoryRules.nameError("pets", all, editingKey = "c_pets"))
    }

    @Test
    fun moveReordersAndRenumbers() {
        val all = Category.DEFAULTS + pets
        val up = CategoryRules.move(all, "c_pets", -1)
        assertEquals(listOf("c_pets", "OTHER"), up.takeLast(2).map { it.key })
        assertEquals((0..7).toList(), up.map { it.sortOrder })
        assertEquals("c_pets", up[6].key)
        // Can't move past either end
        assertEquals(all.map { it.key }, CategoryRules.move(all, "FOOD", -1).map { it.key })
        assertEquals(all.map { it.key }, CategoryRules.move(all, "c_pets", 1).map { it.key })
        assertEquals(all.map { it.key }, CategoryRules.move(all, "missing", 1).map { it.key })
    }
}
