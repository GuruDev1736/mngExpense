package com.guruprasad.mngexpense.ui.screens.recurring

import androidx.compose.foundation.background
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.guruprasad.mngexpense.data.local.RecurringHistory
import com.guruprasad.mngexpense.domain.Category
import com.guruprasad.mngexpense.domain.Frequency
import com.guruprasad.mngexpense.ui.common.DatePickerModal
import com.guruprasad.mngexpense.ui.common.color
import com.guruprasad.mngexpense.util.rupees
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy")

@Composable
fun RecurringEditScreen(
    ruleId: Long?,
    onClose: () -> Unit,
    viewModel: RecurringEditViewModel = viewModel(factory = RecurringEditViewModel.factory(ruleId))
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    LaunchedEffect(state.finished) { if (state.finished) onClose() }

    RecurringEditContent(
        state = state,
        preview = viewModel.preview(state),
        history = history,
        onBack = onClose,
        onTitleChange = viewModel::onTitleChange,
        onAmountChange = viewModel::onAmountChange,
        onCategoryChange = viewModel::onCategoryChange,
        onFrequencyChange = viewModel::onFrequencyChange,
        onEveryChange = viewModel::onEveryChange,
        onStartDateChange = viewModel::onStartDateChange,
        onHasEndDateChange = viewModel::onHasEndDateChange,
        onEndDateChange = viewModel::onEndDateChange,
        onNoteChange = viewModel::onNoteChange,
        onSave = viewModel::save,
        onDelete = viewModel::delete
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RecurringEditContent(
    state: RecurringFormState,
    preview: RecurringPreview,
    history: RecurringHistory? = null,
    onBack: () -> Unit = {},
    onTitleChange: (String) -> Unit = {},
    onAmountChange: (String) -> Unit = {},
    onCategoryChange: (Category) -> Unit = {},
    onFrequencyChange: (Frequency) -> Unit = {},
    onEveryChange: (Int) -> Unit = {},
    onStartDateChange: (LocalDate) -> Unit = {},
    onHasEndDateChange: (Boolean) -> Unit = {},
    onEndDateChange: (LocalDate) -> Unit = {},
    onNoteChange: (String) -> Unit = {},
    onSave: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    var pickStart by rememberSaveable { mutableStateOf(false) }
    var pickEnd by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Edit recurring" else "New recurring expense") },
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
                placeholder = { Text("e.g. Rent, Netflix, EMI") },
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
                        leadingIcon = { Box(Modifier.size(10.dp).background(c.color, CircleShape)) }
                    )
                }
            }

            // ---- Schedule ----
            Text("Repeats", style = MaterialTheme.typography.titleSmall)
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                Frequency.entries.forEachIndexed { i, f ->
                    SegmentedButton(
                        selected = f == state.frequency,
                        onClick = { onFrequencyChange(f) },
                        shape = SegmentedButtonDefaults.itemShape(i, Frequency.entries.size),
                        label = { Text(f.label) }
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Every", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.width(12.dp))
                FilledTonalIconButton(onClick = { onEveryChange(state.every - 1) }, enabled = state.every > 1) {
                    Icon(Icons.Filled.Remove, contentDescription = "Less often")
                }
                Text(
                    state.every.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                FilledTonalIconButton(
                    onClick = { onEveryChange(state.every + 1) },
                    enabled = state.every < RecurringEditViewModel.MAX_EVERY
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "More apart")
                }
                Spacer(Modifier.width(12.dp))
                Text(state.frequency.unit(state.every), style = MaterialTheme.typography.bodyLarge)
            }

            DateField(
                label = if (state.isEditing) "Next payment on" else "First payment on",
                date = state.startDate,
                onClick = { pickStart = true },
                error = state.startDateError
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .toggleable(value = state.hasEndDate, role = Role.Switch, onValueChange = onHasEndDateChange)
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Ends", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        if (state.hasEndDate) "Stops after the end date (e.g. a loan EMI)" else "Never — repeats until you delete it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = state.hasEndDate, onCheckedChange = null) // the whole row toggles
            }
            if (state.hasEndDate) {
                DateField(
                    label = "Last payment on or before",
                    date = state.endDate,
                    onClick = { pickEnd = true },
                    error = state.endDateError
                )
            }

            OutlinedTextField(
                value = state.note,
                onValueChange = onNoteChange,
                label = { Text("Note (optional)") },
                minLines = 2,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth()
            )

            PreviewCard(state, preview, history)

            Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
                Text(if (state.isEditing) "Save changes" else "Save recurring expense")
            }
        }
    }

    if (pickStart) {
        DatePickerModal(
            initial = state.startDate,
            onPick = onStartDateChange,
            onDismiss = { pickStart = false },
            minDate = state.minStartDate // never on or before a payment that was already added
        )
    }
    if (pickEnd) {
        DatePickerModal(
            initial = state.endDate,
            onPick = onEndDateChange,
            onDismiss = { pickEnd = false },
            minDate = state.startDate
        )
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete recurring expense?") },
            text = {
                Text(
                    "\"${state.title}\" will no longer be added automatically. " +
                        "Expenses it already added stay in your history."
                )
            },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete() }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun DateField(label: String, date: LocalDate, onClick: () -> Unit, error: String? = null) {
    OutlinedTextField(
        value = date.format(DATE_FORMAT),
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        trailingIcon = {
            IconButton(onClick = onClick) {
                Icon(Icons.Filled.DateRange, contentDescription = "Pick date")
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

/** Live summary: schedule, next date, what gets added right away, history. */
@Composable
private fun PreviewCard(state: RecurringFormState, preview: RecurringPreview, history: RecurringHistory?) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(Modifier.padding(16.dp)) {
            Icon(Icons.Filled.EventRepeat, contentDescription = null, modifier = Modifier.padding(top = 2.dp))
            Spacer(Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(preview.scheduleText, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    when {
                        state.paused -> "Paused — resume it from the list to continue"
                        preview.nextDate == null -> "No more payments: the end date has passed"
                        else -> "Next: ${preview.nextDate.format(DATE_FORMAT)}"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                if (preview.pastCount > 0) {
                    Text(
                        if (preview.pastCount == 1) "1 expense for a past date will be added now"
                        else "${preview.pastCount} expenses for past dates will be added now",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                preview.monthlyEquivalent?.let {
                    Text("≈ ${rupees(it)} per month", style = MaterialTheme.typography.bodySmall)
                }
                if (history != null && history.count > 0) {
                    Text(
                        "Added ${history.count} ${if (history.count == 1) "time" else "times"} so far · ${rupees(history.total)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (state.isEditing) {
                    Text(
                        "Changes apply to future payments. Expenses already added are not changed.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun RecurringEditPreview() {
    MaterialTheme {
        RecurringEditContent(
            state = RecurringFormState(title = "Rent", amount = "15000", startDate = LocalDate.of(2026, 12, 1)),
            preview = RecurringPreview("Every month on the 1st", 0, LocalDate.of(2026, 12, 1), null)
        )
    }
}
