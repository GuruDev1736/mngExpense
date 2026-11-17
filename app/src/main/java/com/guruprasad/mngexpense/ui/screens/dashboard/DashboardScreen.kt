package com.guruprasad.mngexpense.ui.screens.dashboard

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.guruprasad.mngexpense.data.export.ExportFormat
import com.guruprasad.mngexpense.domain.Bucket
import com.guruprasad.mngexpense.domain.Category
import com.guruprasad.mngexpense.domain.Expense
import com.guruprasad.mngexpense.domain.Period
import com.guruprasad.mngexpense.domain.RecurringExpense
import com.guruprasad.mngexpense.domain.TimeFilter
import com.guruprasad.mngexpense.ui.common.color
import com.guruprasad.mngexpense.ui.common.relativeDay
import com.guruprasad.mngexpense.util.compactRupees
import com.guruprasad.mngexpense.util.rupees
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.min

// ============================================================================
// Stateful entry point (wired to the ViewModel)
// ============================================================================

@Composable
fun DashboardScreen(
    onAddExpense: () -> Unit = {},
    onExpenseClick: (Long) -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenRecurring: () -> Unit = {},
    viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val upcoming by viewModel.upcoming.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }

    // System "Save as" pickers — one per file type
    val saveExcel = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(ExportFormat.EXCEL.mimeType)
    ) { uri -> uri?.let { viewModel.export(ExportFormat.EXCEL, it) } }
    val savePdf = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(ExportFormat.PDF.mimeType)
    ) { uri -> uri?.let { viewModel.export(ExportFormat.PDF, it) } }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is DashboardEvent.ExportDone -> {
                    // With an action, Material 3 defaults to Indefinite — that would cover the export buttons
                    val result = snackbar.showSnackbar(
                        "Report saved", actionLabel = "Open", withDismissAction = true,
                        duration = SnackbarDuration.Long
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        val view = Intent(Intent.ACTION_VIEW)
                            .setDataAndType(event.uri, event.format.mimeType)
                            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        try {
                            context.startActivity(view)
                        } catch (_: ActivityNotFoundException) {
                            snackbar.showSnackbar("No app installed to open this file")
                        }
                    }
                }
                is DashboardEvent.ExportFailed -> snackbar.showSnackbar("Export failed: ${event.message}")
            }
        }
    }

    DashboardContent(
        state = state,
        snackbarHostState = snackbar,
        onFilterChange = viewModel::setFilter,
        onPreviousPeriod = viewModel::previousPeriod,
        onNextPeriod = viewModel::nextPeriod,
        onAddExpense = onAddExpense,
        onExpenseClick = onExpenseClick,
        onOpenSettings = onOpenSettings,
        upcoming = upcoming,
        onOpenRecurring = onOpenRecurring,
        onExport = { format ->
            val name = viewModel.exportFileName(format)
            when (format) {
                ExportFormat.EXCEL -> saveExcel.launch(name)
                ExportFormat.PDF -> savePdf.launch(name)
            }
        }
    )
}

// ============================================================================
// Stateless screen
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    state: DashboardUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onFilterChange: (TimeFilter) -> Unit = {},
    onPreviousPeriod: () -> Unit = {},
    onNextPeriod: () -> Unit = {},
    onAddExpense: () -> Unit = {},
    onExpenseClick: (Long) -> Unit = {},
    onOpenSettings: () -> Unit = {},
    upcoming: List<RecurringExpense> = emptyList(),
    onOpenRecurring: () -> Unit = {},
    onExport: (ExportFormat) -> Unit = {}
) {
    val filter = state.period.filter
    val periodLabel = state.period.label()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense Dashboard") },
                actions = {
                    IconButton(onClick = onOpenRecurring) {
                        Icon(Icons.Filled.EventRepeat, contentDescription = "Recurring expenses")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddExpense,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Add expense") }
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 4.dp,
                bottom = padding.calculateBottomPadding() + 88.dp // room for the FAB
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ---- Filter: Day / Week / Month / Year ----
            item {
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    TimeFilter.entries.forEachIndexed { i, f ->
                        SegmentedButton(
                            selected = f == filter,
                            onClick = { onFilterChange(f) },
                            shape = SegmentedButtonDefaults.itemShape(i, TimeFilter.entries.size),
                            label = { Text(f.title) }
                        )
                    }
                }
            }

            // ---- Period navigator ----
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPreviousPeriod) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous")
                    }
                    Text(
                        periodLabel,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(onClick = onNextPeriod, enabled = state.canGoNext) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next")
                    }
                }
            }

            // ---- Summary tiles ----
            item {
                val change = state.changePercent?.let {
                    val arrow = if (it >= 0) "▲" else "▼"
                    "$arrow ${abs(it).toInt()}% vs last ${filter.title.lowercase()}"
                } ?: "No data for last ${filter.title.lowercase()}"
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        "Total spent", rupees(state.total), change,
                        MaterialTheme.colorScheme.primaryContainer, Modifier.weight(1f)
                    )
                    StatCard(
                        "Transactions", state.count.toString(), "Avg ${rupees(state.averagePerDay)} / day",
                        MaterialTheme.colorScheme.surfaceContainerHigh, Modifier.weight(1f)
                    )
                }
            }

            // ---- Upcoming recurring expenses (next 7 days) ----
            if (upcoming.isNotEmpty()) {
                item { UpcomingCard(upcoming, onOpenRecurring) }
            }

            if (!state.loading && state.count == 0) {
                item { EmptyState(periodLabel, onAddExpense) }
            } else if (state.count > 0) {
                // ---- Donut chart ----
                item {
                    SectionCard("Spending by category") {
                        Box(
                            Modifier
                                .size(200.dp)
                                .align(Alignment.CenterHorizontally),
                            contentAlignment = Alignment.Center
                        ) {
                            DonutChart(state.categories, Modifier.size(200.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "Total", style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    rupees(state.total), style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        state.categories.forEach { c -> LegendRow(c, state.total) }
                    }
                }

                // ---- Bar chart ----
                item {
                    val title = when (filter) {
                        TimeFilter.DAY -> "Today by category"
                        TimeFilter.WEEK -> "Daily spending"
                        TimeFilter.MONTH -> "Weekly spending"
                        TimeFilter.YEAR -> "Monthly spending"
                    }
                    SectionCard(title) {
                        BarChart(state.bars, barColor = MaterialTheme.colorScheme.primary)
                    }
                }

                // ---- Recent expenses ----
                item {
                    SectionCard("Recent") {
                        state.recent.forEachIndexed { i, e ->
                            if (i > 0) HorizontalDivider()
                            ExpenseRow(e, onClick = { onExpenseClick(e.id) })
                        }
                    }
                }

                // ---- Generate report ----
                item {
                    SectionCard("Generate report") {
                        Text(
                            "Export $periodLabel as a spreadsheet or a printable document.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = { onExport(ExportFormat.EXCEL) },
                                modifier = Modifier.weight(1f)
                            ) { Text("Excel (.xlsx)") }
                            Button(
                                onClick = { onExport(ExportFormat.PDF) },
                                modifier = Modifier.weight(1f)
                            ) { Text("PDF") }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// Reusable pieces
// ============================================================================

@Composable
private fun EmptyState(periodLabel: String, onAddExpense: () -> Unit) {
    SectionCard("No expenses yet") {
        Text(
            "Nothing recorded for $periodLabel. Add your first expense to see charts and reports.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        Button(onClick = onAddExpense) { Text("Add expense") }
    }
}

@Composable
private fun StatCard(label: String, value: String, caption: String, container: Color, modifier: Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = container)) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            Text(
                value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Text(caption, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun LegendRow(c: CategoryTotal, total: Double) {
    val percent = if (total > 0) (c.amount / total * 100).toInt() else 0
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(c.category.color))
        Spacer(Modifier.width(12.dp))
        Text(c.category.label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(
            "$percent%", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier.width(40.dp) // fixed column so amounts line up
        )
        Spacer(Modifier.width(16.dp))
        Text(rupees(c.amount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ExpenseRow(e: Expense, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(e.category.color))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    e.title, style = MaterialTheme.typography.bodyMedium, maxLines = 1,
                    overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false)
                )
                if (e.recurringId != null) {
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        Icons.Filled.EventRepeat, contentDescription = "Recurring",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                "${e.category.label} · ${e.date.format(DateTimeFormatter.ofPattern("d MMM"))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(rupees(e.amount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun UpcomingCard(upcoming: List<RecurringExpense>, onSeeAll: () -> Unit) {
    SectionCard("Upcoming this week") {
        upcoming.take(4).forEachIndexed { i, rule ->
            if (i > 0) HorizontalDivider()
            Row(
                Modifier.fillMaxWidth().clickable(onClick = onSeeAll).padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(rule.category.color))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(rule.title, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        relativeDay(rule.nextDate!!),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(rupees(rule.amount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            }
        }
        TextButton(onClick = onSeeAll, modifier = Modifier.align(Alignment.End)) {
            Text(if (upcoming.size > 4) "See all (${upcoming.size})" else "Manage recurring")
        }
    }
}

/** Animated donut (pie) chart. */
@Composable
private fun DonutChart(data: List<CategoryTotal>, modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(data) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
    }
    val total = data.sumOf { it.amount }
    Canvas(modifier) {
        if (total <= 0) return@Canvas
        val stroke = 28.dp.toPx()
        val diameter = min(size.width, size.height) - stroke
        val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
        val gap = if (data.size > 1) 1.5f else 0f // degrees between slices
        var start = -90f
        data.forEach { item ->
            val sweep = (item.amount / total * 360).toFloat() * progress.value
            if (sweep > gap) {
                drawArc(
                    color = item.category.color,
                    startAngle = start + gap / 2,
                    sweepAngle = sweep - gap,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                    style = Stroke(width = stroke)
                )
            }
            start += sweep
        }
    }
}

/** Animated vertical bar chart built from plain Compose layouts. */
@Composable
private fun BarChart(data: List<Bucket>, barColor: Color, chartHeight: Int = 180) {
    if (data.isEmpty()) return
    val progress = remember { Animatable(0f) }
    LaunchedEffect(data) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
    }
    val max = data.maxOf { it.amount }.coerceAtLeast(1.0)
    val showValues = data.size <= 7 // values above bars only when they fit

    Row(
        Modifier.fillMaxWidth().height((chartHeight + 40).dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { bar ->
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                if (showValues && bar.amount > 0) {
                    Text(
                        compactRupees(bar.amount), style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1
                    )
                    Spacer(Modifier.height(4.dp))
                }
                Box(
                    Modifier
                        .fillMaxWidth(0.6f)
                        .height((chartHeight * (bar.amount / max) * progress.value).dp)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(barColor)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    bar.label, style = MaterialTheme.typography.labelSmall, maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ============================================================================
// Preview
// ============================================================================

@Preview(showBackground = true, heightDp = 1600)
@Composable
private fun DashboardPreview() {
    val today = LocalDate.of(2026, 11, 13)
    val period = Period.of(TimeFilter.MONTH, today)
    val expenses = listOf(
        Expense(1, "Groceries", 2400.0, Category.FOOD, today),
        Expense(2, "Metro card", 800.0, Category.TRANSPORT, today.minusDays(2)),
        Expense(3, "Shoes", 3200.0, Category.SHOPPING, today.minusDays(5)),
        Expense(4, "Electricity", 1900.0, Category.BILLS, today.minusDays(9))
    )
    MaterialTheme {
        DashboardContent(
            DashboardUiState(
                period = period,
                total = expenses.sumOf { it.amount },
                count = expenses.size,
                averagePerDay = expenses.sumOf { it.amount } / 13,
                changePercent = 8.0,
                categories = expenses.map { CategoryTotal(it.category, it.amount) },
                bars = period.buckets(expenses),
                recent = expenses,
                loading = false
            )
        )
    }
}
