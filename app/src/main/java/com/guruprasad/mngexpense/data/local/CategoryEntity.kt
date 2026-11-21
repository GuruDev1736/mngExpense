package com.guruprasad.mngexpense.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.guruprasad.mngexpense.domain.CategoryInfo

/** One row per category (built-in and user-made). Expenses store [key] in their `category` column. */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey @ColumnInfo(name = "category_key") val key: String,
    val name: String,
    /** ARGB */
    val color: Long,
    val icon: String,
    @ColumnInfo(name = "sort_order") val sortOrder: Int
)

fun CategoryEntity.toDomain() = CategoryInfo(key, name, color, icon, sortOrder)
fun CategoryInfo.toEntity() = CategoryEntity(key, name, color, icon, sortOrder)

/** How much a category is used: shown before deleting it. */
data class CategoryUsageRow(val category: String, val count: Int)
