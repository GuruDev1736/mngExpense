package com.guruprasad.mngexpense.ui.screens.transactions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.guruprasad.mngexpense.domain.Category
import com.guruprasad.mngexpense.domain.DateFilter
import com.guruprasad.mngexpense.domain.Expense
import com.guruprasad.mngexpense.domain.TransactionFilter
import com.guruprasad.mngexpense.domain.TransactionKind
import com.guruprasad.mngexpense.domain.TransactionSort
import com.guruprasad.mngexpense.domain.groupTransactions
import com.guruprasad.mngexpense.ui.common.ExpenseListItem
import com.guruprasad.mngexpense.ui.common.color
import com.guruprasad.mngexpense.util.money
import com.guruprasad.mngexpense.util.currencySymbol
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val DAY_MS = 86_400_000L

@Composable
fun TransactionsScreen(
    initialCategory: Category?,
    initialStart: LocalDate?,
    initialEnd: LocalDate?,
    focusSearch: Boolean,
    onBack: () -> Unit,
    onOpenExpense: (Long) -> Unit,
    viewModel: TransactionsViewModel = viewModel(
        factory = TransactionsViewModel.factory(initialCategory, initialStart, initialEnd)
    )
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    TransactionsContent(
        state = state,
        filter = filter,
        focusSearch = focusSearch,
        onBack = onBack,
        onOpenExpense = onOpenExpense,
        onQueryChange = viewModel::setQuery,
        onDateChange = viewModel::setDate,
        onCategoriesChange = viewModel::setCategories,
        onAmountChange = viewModel::setAmountRange,
        onKindChange = viewModel::setKind,
        onSortChange = viewModel::setSort,
        onClearFilters = viewModel::clearFilters
    )
}

/** Which bottom sheet is open. */
private enum class Sheet { DATE, CATEGORY, AMOUNT, TYPE, SORT }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TransactionsContent(
    state: TransactionsState,
    filter: TransactionFilter,
    focusSearch: Boolean = false,
    onBack: () -> Unit = {},
    onOpenExpense: (Long) -> Unit = {},
    onQueryChange: (String) -> Unit = {},
    onDateChange: (DateFilter) -> Unit = {},
    onCategoriesChange: (Set<Category>) -> Unit = {},
    onAmountChange: (Double?, Double?) -> Unit = { _, _ -> },
    onKindChange: (TransactionKind) -> Unit = {},
    onSortChange: (TransactionSort) -> Unit = {},
    onClearFilters: () -> Unit = {},
    today: LocalDate = LocalDate.now()
) {
    var sheet by rememberSaveable { mutableStateOf<Sheet?>(null) }
    var pickRange by rememberSaveable { mutableStateOf(false) }
    val locale = LocalConfiguration.current.locales[0]
    val focus = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    // Only once per screen (not again after rotation or coming back from an expense)
    var focusedOnce by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (focusSearch && !focusedOnce) {
            focus.requestFocus()
            focusedOnce = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transactions") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())) {
            // ---- Search ----
            OutlinedTextField(
                value = filter.query,
                onValueChange = onQueryChange,
                placeholder = { Text("Search title or note") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (filter.query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.extraLarge,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .focusRequester(focus)
            )

            // ---- Filter chips ----
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DropChip(filter.date.label(locale), filter.date != DateFilter.AllTime) { sheet = Sheet.DATE }
                DropChip(categoryChipLabel(filter.categories), filter.categories.isNotEmpty()) { sheet = Sheet.CATEGORY }
                DropChip(amountChipLabel(filter), filter.minAmount != null || filter.maxAmount != null) { sheet = Sheet.AMOUNT }
                DropChip(
                    if (filter.kind == TransactionKind.ALL) "Type" else filter.kind.label,
                    filter.kind != TransactionKind.ALL
                ) { sheet = Sheet.TYPE }
                FilterChip(
                    selected = false,
                    onClick = { sheet = Sheet.SORT },
                    label = { Text(filter.sort.label) },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = null, Modifier.size(18.dp)) }
                )
                if (filter.activeFilterCount > 0) {
                    InputChip(
                        selected = false,
                        onClick = onClearFilters,
                        label = { Text("Clear filters") },
                        trailingIcon = { Icon(Icons.Filled.Close, contentDescription = null, Modifier.size(18.dp)) }
                    )
                }
            }

            // ---- Results ----
            when {
                state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.count == 0 -> NoResults(filter, onClearFilters = onClearFilters, onClearSearch = { onQueryChange("") })
                else -> LazyColumn(
                    contentPadding = PaddingValues(bottom = padding.calculateBottomPadding() + 16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item(key = "summary") {
                        Text(
                            "${state.count} ${if (state.count == 1) "transaction" else "transactions"} · ${money(state.total)}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                    for (group in state.groups) {
                        if (group.date != null) {
                            stickyHeader(key = "h${group.date}") { DayHeader(group.date, group.total, today, locale) }
                        }
                        items(group.expenses, key = { it.id }) { e ->
                            ExpenseListItem(
                                e = e,
                                onClick = { onOpenExpense(e.id) },
                                showDate = group.date == null, // the day header already shows it
                                highlight = filter.highlightTerms,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            HorizontalDivider(Modifier.padding(start = 38.dp, end = 16.dp))
                        }
                    }
                }
            }
        }
    }

    // ---- Sheets ----
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    when (sheet) {
        null -> Unit
        Sheet.DATE -> FilterSheet("Date", sheetState, onDismiss = { sheet = null }) {
            DateFilter.presets.forEach { d ->
                RadioRow(d.label(locale), selected = filter.date == d) { onDateChange(d); sheet = null }
            }
            RadioRow(
                if (filter.date is DateFilter.Custom) "Custom: ${filter.date.label(locale)}" else "Custom range…",
                selected = filter.date is DateFilter.Custom
            ) { sheet = null; pickRange = true }
        }
        Sheet.CATEGORY -> FilterSheet("Categories", sheetState, onDismiss = { sheet = null }) {
            Category.entries.forEach { c ->
                val checked = c in filter.categories
                Row(
                    Modifier
                        .fillMaxWidth()
                        .toggleable(checked, role = Role.Checkbox) {
                            onCategoriesChange(if (it) filter.categories + c else filter.categories - c)
                        }
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = checked, onCheckedChange = null)
                    Spacer(Modifier.width(12.dp))
                    Box(Modifier.size(10.dp).background(c.color, CircleShape))
                    Spacer(Modifier.width(10.dp))
                    Text(c.label, style = MaterialTheme.typography.bodyLarge)
                }
            }
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { onCategoriesChange(emptySet()) }, Modifier.weight(1f)) { Text("All categories") }
                Button(onClick = { sheet = null }, Modifier.weight(1f)) { Text("Done") }
            }
        }
        Sheet.AMOUNT -> FilterSheet("Amount", sheetState, onDismiss = { sheet = null }) {
            AmountRangeEditor(filter.minAmount, filter.maxAmount) { min, max -> onAmountChange(min, max); sheet = null }
        }
        Sheet.TYPE -> FilterSheet("Type", sheetState, onDismiss = { sheet = null }) {
            TransactionKind.entries.forEach { k ->
                RadioRow(k.label, selected = filter.kind == k) { onKindChange(k); sheet = null }
            }
        }
        Sheet.SORT -> FilterSheet("Sort by", sheetState, onDismiss = { sheet = null }) {
            TransactionSort.entries.forEach { s ->
                RadioRow(s.label, selected = filter.sort == s) { onSortChange(s); sheet = null }
            }
        }
    }

    if (pickRange) {
        DateRangeDialog(
            initial = filter.date as? DateFilter.Custom,
            today = today,
            onPick = { start, end -> onDateChange(DateFilter.Custom(start, end)) },
            onDismiss = { pickRange = false }
        )
    }
}

@Composable
private fun DropChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null, Modifier.size(18.dp)) }
    )
}

private fun categoryChipLabel(categories: Set<Category>): String = when (categories.size) {
    0 -> "Category"
    1 -> categories.first().label
    else -> {
        val first = Category.entries.first { it in categories }
        "${first.label} +${categories.size - 1}"
    }
}

private fun amountChipLabel(f: TransactionFilter): String = when {
    f.minAmount != null && f.maxAmount != null -> "${money(f.minAmount)} – ${money(f.maxAmount)}"
    f.minAmount != null -> "≥ ${money(f.minAmount)}"
    f.maxAmount != null -> "≤ ${money(f.maxAmount)}"
    else -> "Amount"
}

@Composable
private fun DayHeader(date: LocalDate, total: Double, today: LocalDate, locale: Locale) {
    val label = when (date) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        else -> date.format(
            DateTimeFormatter.ofPattern(if (date.year == today.year) "EEE, d MMM" else "EEE, d MMM yyyy", locale)
        )
    }
    Surface(color = MaterialTheme.colorScheme.surface) { // opaque, so rows don't show through when stuck
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            Text(
                money(total), style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NoResults(filter: TransactionFilter, onClearFilters: () -> Unit, onClearSearch: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            Icons.Filled.SearchOff, contentDescription = null, modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            when {
                filter.isDefault -> "No expenses yet"
                filter.query.isNotBlank() -> "No results for \"${filter.query.trim()}\""
                else -> "No transactions match these filters"
            },
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        if (!filter.isDefault) {
            Text(
                if (filter.query.isNotBlank()) "Try other words, or widen the date range and filters."
                else "Try a wider date range, or remove a filter.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (filter.query.isNotEmpty()) OutlinedButton(onClick = onClearSearch) { Text("Clear search") }
                if (filter.activeFilterCount > 0) Button(onClick = onClearFilters) { Text("Clear filters") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSheet(
    title: String,
    state: androidx.compose.material3.SheetState,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = state) {
        Column(Modifier.navigationBarsPadding().padding(bottom = 8.dp)) {
            Text(
                title, style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
            content()
        }
    }
}

@Composable
private fun RadioRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun AmountRangeEditor(min: Double?, max: Double?, onApply: (Double?, Double?) -> Unit) {
    var minText by rememberSaveable { mutableStateOf(min?.let(::formatAmount) ?: "") }
    var maxText by rememberSaveable { mutableStateOf(max?.let(::formatAmount) ?: "") }
    val minValue = minText.toDoubleOrNull()
    val maxValue = maxText.toDoubleOrNull()
    val error = if (minValue != null && maxValue != null && minValue > maxValue) "Minimum is more than maximum" else null
    val amountInput = Regex("""\d{0,9}(\.\d{0,2})?""")

    Column(Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = minText,
                onValueChange = { if (it.isEmpty() || amountInput.matches(it)) minText = it },
                label = { Text("Minimum") },
                prefix = { Text(currencySymbol() + " ") },
                singleLine = true,
                isError = error != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = maxText,
                onValueChange = { if (it.isEmpty() || amountInput.matches(it)) maxText = it },
                label = { Text("Maximum") },
                prefix = { Text(currencySymbol() + " ") },
                singleLine = true,
                isError = error != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                modifier = Modifier.weight(1f)
            )
        }
        Text(
            error ?: "Leave a field empty for no limit.",
            style = MaterialTheme.typography.bodySmall,
            color = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = { onApply(null, null) }, Modifier.weight(1f)) { Text("Any amount") }
            Button(onClick = { onApply(minValue, maxValue) }, enabled = error == null, modifier = Modifier.weight(1f)) {
                Text("Apply")
            }
        }
    }
}

/** Range picker limited to today and earlier (expenses can't be in the future). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangeDialog(
    initial: DateFilter.Custom?,
    today: LocalDate,
    onPick: (LocalDate, LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val todayMs = today.toEpochDay() * DAY_MS
    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initial?.start?.toEpochDay()?.times(DAY_MS),
        initialSelectedEndDateMillis = initial?.end?.toEpochDay()?.times(DAY_MS),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= todayMs
        }
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val start = state.selectedStartDateMillis ?: return@TextButton
                    // A single tapped day counts as a one-day range
                    val end = state.selectedEndDateMillis ?: start
                    onPick(LocalDate.ofEpochDay(start / DAY_MS), LocalDate.ofEpochDay(end / DAY_MS))
                    onDismiss()
                },
                enabled = state.selectedStartDateMillis != null
            ) { Text("Apply") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    ) {
        DateRangePicker(state, modifier = Modifier.height(500.dp))
    }
}

private fun formatAmount(v: Double): String =
    if (v % 1.0 == 0.0) v.toLong().toString() else "%.2f".format(Locale.ROOT, v)

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun TransactionsPreview() {
    val today = LocalDate.of(2026, 11, 18)
    val list = listOf(
        Expense(1, "Auto to office", 120.0, Category.TRANSPORT, today, note = "shared"),
        Expense(2, "Lunch", 320.0, Category.FOOD, today),
        Expense(3, "Rent", 15000.0, Category.BILLS, today.minusDays(1), recurringId = 1),
        Expense(4, "Groceries", 2400.0, Category.FOOD, today.minusDays(4))
    )
    val filter = TransactionFilter(query = "o", categories = setOf(Category.FOOD, Category.TRANSPORT))
    val results = filter.apply(list)
    MaterialTheme {
        TransactionsContent(
            TransactionsState(groupTransactions(results, filter.sort), results.size, results.sumOf { it.amount }, false),
            filter, today = today
        )
    }
}
