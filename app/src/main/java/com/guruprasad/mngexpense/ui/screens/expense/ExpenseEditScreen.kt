package com.guruprasad.mngexpense.ui.screens.expense

import androidx.compose.foundation.background
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Switch
import com.guruprasad.mngexpense.domain.Frequency
import com.guruprasad.mngexpense.domain.RecurringExpense
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.guruprasad.mngexpense.domain.Category
import com.guruprasad.mngexpense.ui.common.color
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val DAY_MS = 86_400_000L

@Composable
fun ExpenseEditScreen(
    expenseId: Long?,
    onClose: () -> Unit,
    onOpenRecurring: (Long) -> Unit = {},
    viewModel: ExpenseEditViewModel = viewModel(factory = ExpenseEditViewModel.factory(expenseId))
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.finished) { if (state.finished) onClose() }

    ExpenseEditContent(
        state = state,
        onBack = onClose,
        onTitleChange = viewModel::onTitleChange,
        onAmountChange = viewModel::onAmountChange,
        onCategoryChange = viewModel::onCategoryChange,
        onDateChange = viewModel::onDateChange,
        onNoteChange = viewModel::onNoteChange,
        onRepeatChange = viewModel::onRepeatChange,
        onRepeatFrequencyChange = viewModel::onRepeatFrequencyChange,
        onOpenRecurring = onOpenRecurring,
        onSave = viewModel::save,
        onDelete = viewModel::delete
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExpenseEditContent(
    state: ExpenseFormState,
    onBack: () -> Unit = {},
    onTitleChange: (String) -> Unit = {},
    onAmountChange: (String) -> Unit = {},
    onCategoryChange: (Category) -> Unit = {},
    onDateChange: (LocalDate) -> Unit = {},
    onNoteChange: (String) -> Unit = {},
    onRepeatChange: (Boolean) -> Unit = {},
    onRepeatFrequencyChange: (Frequency) -> Unit = {},
    onOpenRecurring: (Long) -> Unit = {},
    onSave: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Edit expense" else "Add expense") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.isEditing) {
                        IconButton(onClick = { confirmDelete = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (state.loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = state.amount,
                onValueChange = onAmountChange,
                label = { Text("Amount") },
                prefix = { Text("₹ ") },
                isError = state.amountError != null,
                supportingText = state.amountError?.let { { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                textStyle = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.title,
                onValueChange = onTitleChange,
                label = { Text("Title") },
                placeholder = { Text("e.g. Groceries") },
                isError = state.titleError != null,
                supportingText = state.titleError?.let { { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Text("Category", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Category.entries.forEach { c ->
                    FilterChip(
                        selected = c == state.category,
                        onClick = { onCategoryChange(c) },
                        label = { Text(c.label) },
                        leadingIcon = {
                            Box(Modifier.size(10.dp).background(c.color, CircleShape))
                        }
                    )
                }
            }

            OutlinedTextField(
                value = state.date.format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy")),
                onValueChange = {},
                readOnly = true,
                label = { Text("Date") },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Filled.DateRange, contentDescription = "Pick date")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // ---- Recurring ----
            if (!state.isEditing) {
                RepeatSection(state, onRepeatChange, onRepeatFrequencyChange)
            } else if (state.recurringId != null) {
                AutoAddedCard(onManage = { onOpenRecurring(state.recurringId) })
            }

            OutlinedTextField(
                value = state.note,
                onValueChange = onNoteChange,
                label = { Text("Note (optional)") },
                minLines = 2,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth()
            )

            Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
                Text(
                    when {
                        state.isEditing -> "Save changes"
                        state.repeat -> "Add recurring expense"
                        else -> "Add expense"
                    }
                )
            }
        }
    }

    if (showDatePicker) {
        val todayMs = LocalDate.now().toEpochDay() * DAY_MS
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.date.toEpochDay() * DAY_MS,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= todayMs
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    // DatePicker reports midnight UTC, so dividing by a day gives the epoch day
                    pickerState.selectedDateMillis?.let { onDateChange(LocalDate.ofEpochDay(it / DAY_MS)) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(pickerState) }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete expense?") },
            text = { Text("\"${state.title}\" will be removed permanently.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete() }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RepeatSection(
    state: ExpenseFormState,
    onRepeatChange: (Boolean) -> Unit,
    onFrequencyChange: (Frequency) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .toggleable(value = state.repeat, role = Role.Switch, onValueChange = onRepeatChange)
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.EventRepeat, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Repeat", style = MaterialTheme.typography.bodyLarge)
                Text(
                    if (state.repeat) {
                        val preview = RecurringExpense(
                            title = "", amount = 0.0, category = state.category,
                            frequency = state.repeatFrequency, startDate = state.date
                        )
                        "Added automatically ${preview.scheduleText().replaceFirstChar { it.lowercase() }}"
                    } else "For rent, bills, subscriptions…",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = state.repeat, onCheckedChange = null) // the whole row toggles
        }
        if (state.repeat) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Frequency.entries.forEach { f ->
                    FilterChip(
                        selected = f == state.repeatFrequency,
                        onClick = { onFrequencyChange(f) },
                        label = { Text(f.label) }
                    )
                }
            }
        }
    }
}

/** Shown when editing an expense that a recurring rule added. */
@Composable
private fun AutoAddedCard(onManage: () -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.EventRepeat, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text(
                "Added automatically by a recurring expense. Edits here change only this one.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onManage) { Text("Manage") }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpenseEditPreview() {
    MaterialTheme {
        ExpenseEditContent(ExpenseFormState(title = "Groceries", amount = "2400", date = LocalDate.of(2026, 11, 13)))
    }
}
