package com.guruprasad.mngexpense.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.guruprasad.mngexpense.MngExpenseApp
import com.guruprasad.mngexpense.domain.Category
import com.guruprasad.mngexpense.domain.CategoryRules
import com.guruprasad.mngexpense.domain.CategoryStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * Create or edit a category: name, colour and icon, with a live preview.
 *
 * @param editingKey the category being edited (null = a new one), so its own name isn't a duplicate
 * @param onSave returns an error message to show, or null when saved (the dialog then closes itself
 *   through [onDismiss] from the caller)
 */
@Composable
fun CategoryEditorDialog(
    title: String,
    initialName: String = "",
    initialColor: Long = CategoryPalette.first(),
    initialIcon: String = "category",
    editingKey: String? = null,
    saving: Boolean = false,
    serverError: String? = null,
    onSave: (name: String, color: Long, icon: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    var color by rememberSaveable { mutableLongStateOf(initialColor) }
    var icon by rememberSaveable { mutableStateOf(initialIcon) }
    var triedSave by rememberSaveable { mutableStateOf(false) }
    val nameError = CategoryRules.nameError(name, CategoryStore.all, editingKey)
    val shownError = serverError ?: nameError?.takeIf { triedSave || name.isNotEmpty() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                // Top padding: the field's floating label sits above its border and was clipped
                Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    CategoryBadge(categoryIcon(icon), Color(color), size = 48.dp)
                    Spacer(Modifier.width(12.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { if (it.length <= CategoryRules.MAX_NAME_LENGTH) name = it },
                        label = { Text("Name") },
                        placeholder = { Text("e.g. Groceries") },
                        isError = shownError != null,
                        supportingText = { Text(shownError ?: "${name.length}/${CategoryRules.MAX_NAME_LENGTH}") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(8.dp))
                Text("Colour", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                EvenGrid(CategoryPalette, minCell = 40.dp, maxCell = 52.dp) { c, cell ->
                        val selected = c == color
                        Box(
                            Modifier
                                .size(cell)
                                .clip(CircleShape)
                                .background(Color(c))
                                .then(
                                    if (selected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                    else Modifier
                                )
                                .clickable { color = c }
                                .semantics { contentDescription = "Colour"; this.selected = selected },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selected) Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(cell * 0.45f))
                        }
                }

                Spacer(Modifier.height(16.dp))
                Text("Icon", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                EvenGrid(CategoryIcons, minCell = 40.dp, maxCell = 52.dp) { (key, vector), cell ->
                        val selected = key == icon
                        Box(
                            Modifier
                                .size(cell)
                                .clip(CircleShape)
                                .background(if (selected) Color(color) else MaterialTheme.colorScheme.surfaceContainerHighest)
                                .clickable { icon = key }
                                .semantics { contentDescription = key; this.selected = selected },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                vector, contentDescription = null, modifier = Modifier.size(cell * 0.55f),
                                tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !saving,
                onClick = {
                    triedSave = true
                    if (nameError == null) onSave(name.trim(), color, icon)
                }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

/**
 * "New category" dialog that saves by itself; [onCreated] gets the new category (e.g. to select it
 * on the expense being added).
 */
@Composable
fun NewCategoryDialog(onCreated: (Category) -> Unit, onDismiss: () -> Unit) {
    val app = LocalContext.current.applicationContext as MngExpenseApp
    val scope = rememberCoroutineScope()
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    // A colour not used yet, so the new category stands out in charts
    val freeColor = remember {
        val used = CategoryStore.all.map { it.color }.toSet()
        CategoryPalette.firstOrNull { it !in used } ?: CategoryPalette.first()
    }
    CategoryEditorDialog(
        title = "New category",
        initialColor = freeColor,
        saving = saving,
        serverError = error,
        onSave = { name, color, icon ->
            saving = true
            error = null
            scope.launch {
                try {
                    onCreated(app.categoryRepository.add(name, color, icon))
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    error = e.message ?: "Couldn't save the category"
                } finally {
                    saving = false
                }
            }
        },
        onDismiss = onDismiss
    )
}

/** Category chips for the add / edit forms, with a "New" chip at the end. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryChips(selected: Category, onSelect: (Category) -> Unit, modifier: Modifier = Modifier) {
    var creating by rememberSaveable { mutableStateOf(false) }
    FlowRow(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Category.entries.forEach { c ->
            FilterChip(
                selected = c == selected,
                onClick = { onSelect(c) },
                label = { Text(c.label) },
                leadingIcon = { Icon(c.icon, contentDescription = null, tint = c.color, modifier = Modifier.size(18.dp)) }
            )
        }
        AssistChip(
            onClick = { creating = true },
            label = { Text("New") },
            leadingIcon = { Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp)) }
        )
    }
    if (creating) {
        NewCategoryDialog(
            onCreated = {
                creating = false
                onSelect(it)
            },
            onDismiss = { creating = false }
        )
    }
}

/**
 * Grid of square cells that spans the full width: the outer columns touch both edges and the gaps
 * are even. Picks the most columns that fit [minCell], preferring a count that divides the items
 * evenly so the last row is full; cells then grow (up to [maxCell]) to use the width.
 */
@Composable
private fun <T> EvenGrid(
    items: List<T>,
    minCell: Dp,
    maxCell: Dp,
    minGap: Dp = 8.dp,
    cellContent: @Composable (item: T, cell: Dp) -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val width = maxWidth
        val fit = ((width + minGap) / (minCell + minGap)).toInt().coerceAtLeast(1)
        val columns = (fit downTo (fit - 2).coerceAtLeast(1)).firstOrNull { items.size % it == 0 } ?: fit
        val cell = ((width - minGap * (columns - 1)) / columns).coerceAtMost(maxCell)
        Column(verticalArrangement = Arrangement.spacedBy(minGap)) {
            items.chunked(columns).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    row.forEach { cellContent(it, cell) }
                    // Keep a short last row aligned to the columns above
                    repeat(columns - row.size) { Spacer(Modifier.size(cell)) }
                }
            }
        }
    }
}
