package com.guruprasad.mngexpense.ui.screens.category

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.guruprasad.mngexpense.domain.Category
import com.guruprasad.mngexpense.ui.common.CategoryBadge
import com.guruprasad.mngexpense.ui.common.CategoryEditorDialog
import com.guruprasad.mngexpense.ui.common.CategoryPalette
import com.guruprasad.mngexpense.ui.common.categoryIcon
import com.guruprasad.mngexpense.util.money

/** Settings → Categories: add, edit, reorder and delete categories. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(onBack: () -> Unit, vm: CategoriesViewModel = viewModel(factory = CategoriesViewModel.Factory)) {
    val rows by vm.rows.collectAsStateWithLifecycle()
    val ui by vm.ui.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var menuOpen by remember { mutableStateOf(false) }
    var confirmRestore by remember { mutableStateOf(false) }

    LaunchedEffect(ui.message) {
        ui.message?.let {
            snackbar.showSnackbar(it)
            vm.messageShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    Box {
                        IconButton(onClick = { menuOpen = true }) { Icon(Icons.Filled.MoreVert, contentDescription = "More options") }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(
                                text = { Text("Restore built-in categories") },
                                leadingIcon = { Icon(Icons.Filled.Restore, contentDescription = null) },
                                onClick = {
                                    menuOpen = false
                                    confirmRestore = true
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = vm::startCreate,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("New category") }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        val list = rows
        if (list == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        LazyColumn(
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + 88.dp // room for the button
            )
        ) {
            item {
                Text(
                    "Used for expenses, budgets, charts and reports. Tap a category to rename it or change " +
                        "its colour and icon. The order here is the order everywhere.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            itemsIndexed(list, key = { _, row -> row.info.key }) { index, row ->
                CategoryListRow(
                    row = row,
                    isFirst = index == 0,
                    isLast = index == list.lastIndex,
                    onEdit = { vm.startEdit(row.info) },
                    onMove = { delta -> vm.move(row.info, delta) },
                    onDelete = { vm.requestDelete(row.info) },
                    modifier = Modifier.animateItem()
                )
                HorizontalDivider(Modifier.padding(start = 72.dp))
            }
        }
    }

    ui.editor?.let { editor ->
        val editing = editor.editing
        CategoryEditorDialog(
            title = if (editing == null) "New category" else "Edit category",
            initialName = editing?.name ?: "",
            initialColor = editing?.color ?: freeColor(rows),
            initialIcon = editing?.icon ?: "category",
            editingKey = editing?.key,
            saving = editor.saving,
            serverError = editor.error,
            onSave = vm::save,
            onDismiss = vm::closeEditor
        )
    }

    ui.deleting?.let { request ->
        DeleteCategoryDialog(request, onMoveToChange = vm::setMoveTo, onConfirm = vm::confirmDelete, onDismiss = vm::cancelDelete)
    }

    if (confirmRestore) {
        AlertDialog(
            onDismissRequest = { confirmRestore = false },
            icon = { Icon(Icons.Filled.Restore, contentDescription = null) },
            title = { Text("Restore built-in categories?") },
            text = {
                Text(
                    "Brings back built-in categories you deleted and resets the names, colours and order of the " +
                        "built-in ones. Categories you created stay, after them. No expense is changed."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmRestore = false
                    vm.restoreDefaults()
                }) { Text("Restore") }
            },
            dismissButton = { TextButton(onClick = { confirmRestore = false }) { Text("Cancel") } }
        )
    }
}

/** A colour no category uses yet. */
private fun freeColor(rows: List<CategoryRow>?): Long {
    val used = rows.orEmpty().map { it.info.color }.toSet()
    return CategoryPalette.firstOrNull { it !in used } ?: CategoryPalette.first()
}

@Composable
private fun CategoryListRow(
    row: CategoryRow,
    isFirst: Boolean,
    isLast: Boolean,
    onEdit: () -> Unit,
    onMove: (Int) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menu by remember { mutableStateOf(false) }
    val info = row.info
    ListItem(
        modifier = modifier.clickable(onClick = onEdit),
        leadingContent = { CategoryBadge(categoryIcon(info.icon), Color(info.color)) },
        headlineContent = { Text(info.name) },
        supportingContent = {
            Text(
                when (row.expenses) {
                    0 -> "No expenses"
                    1 -> "1 expense"
                    else -> "${row.expenses} expenses"
                } + if (info.isBuiltIn) " · Built-in" else ""
            )
        },
        trailingContent = {
            Box {
                IconButton(onClick = { menu = true }) { Icon(Icons.Filled.MoreVert, contentDescription = "Options for ${info.name}") }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        leadingIcon = { Icon(Icons.Filled.Edit, null) },
                        onClick = { menu = false; onEdit() }
                    )
                    DropdownMenuItem(
                        text = { Text("Move up") },
                        leadingIcon = { Icon(Icons.Filled.ArrowUpward, null) },
                        enabled = !isFirst,
                        onClick = { menu = false; onMove(-1) }
                    )
                    DropdownMenuItem(
                        text = { Text("Move down") },
                        leadingIcon = { Icon(Icons.Filled.ArrowDownward, null) },
                        enabled = !isLast,
                        onClick = { menu = false; onMove(1) }
                    )
                    DropdownMenuItem(
                        text = { Text(if (info.canDelete) "Delete" else "Delete (Other is always kept)") },
                        leadingIcon = { Icon(Icons.Filled.Delete, null) },
                        enabled = info.canDelete,
                        onClick = { menu = false; onDelete() }
                    )
                }
            }
        }
    )
}

@Composable
private fun DeleteCategoryDialog(
    request: DeleteRequest,
    onMoveToChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val usage = request.usage
    val name = request.category.name
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.Delete, contentDescription = null) },
        title = { Text("Delete \"$name\"?") },
        text = {
            Column {
                if (!usage.isUsed) {
                    Text("No expense, recurring expense or budget uses it.")
                    return@Column
                }
                val parts = buildList {
                    if (usage.expenses > 0) add(if (usage.expenses == 1) "1 expense" else "${usage.expenses} expenses")
                    if (usage.recurring > 0) add(if (usage.recurring == 1) "1 recurring expense" else "${usage.recurring} recurring expenses")
                }
                if (parts.isNotEmpty()) {
                    Text("${parts.joinToString(" and ")} will move to:")
                    Spacer(Modifier.height(8.dp))
                    MoveToPicker(selected = request.moveTo, exclude = request.category.key, onSelect = onMoveToChange)
                }
                usage.budget?.let {
                    Spacer(Modifier.height(12.dp))
                    Text("Its monthly budget of ${money(it)} is removed.")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Delete", color = MaterialTheme.colorScheme.error) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

/** Where the expenses of a deleted category go. */
@Composable
private fun MoveToPicker(selected: String, exclude: String, onSelect: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    val current = Category(selected)
    Box {
        OutlinedButton(onClick = { open = true }, modifier = Modifier.fillMaxWidth()) {
            CategoryBadge(current, size = 24.dp)
            Spacer(Modifier.width(8.dp))
            Text(current.label, modifier = Modifier.weight(1f))
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            Category.entries.filter { it.key != exclude }.forEach { c ->
                DropdownMenuItem(
                    text = { Text(c.label) },
                    leadingIcon = { CategoryBadge(c, size = 24.dp) },
                    onClick = {
                        open = false
                        onSelect(c.key)
                    }
                )
            }
        }
    }
}
