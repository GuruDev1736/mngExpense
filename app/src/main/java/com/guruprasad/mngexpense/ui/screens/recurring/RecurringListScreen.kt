package com.guruprasad.mngexpense.ui.screens.recurring

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.guruprasad.mngexpense.domain.Category
import com.guruprasad.mngexpense.domain.Frequency
import com.guruprasad.mngexpense.domain.RecurringExpense
import com.guruprasad.mngexpense.ui.common.color
import com.guruprasad.mngexpense.ui.common.relativeDay
import com.guruprasad.mngexpense.util.money
import java.time.LocalDate

@Composable
fun RecurringListScreen(
    onBack: (() -> Unit)? = null,
    onAdd: () -> Unit,
    onOpen: (Long) -> Unit,
    viewModel: RecurringListViewModel = viewModel(factory = RecurringListViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    RecurringListContent(
        state = state,
        onBack = onBack,
        onAdd = onAdd,
        onOpen = onOpen,
        onPausedChange = viewModel::setPaused
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringListContent(
    state: RecurringListState,
    onBack: (() -> Unit)? = null,
    onAdd: () -> Unit = {},
    onOpen: (Long) -> Unit = {},
    onPausedChange: (RecurringExpense, Boolean) -> Unit = { _, _ -> },
    today: LocalDate = LocalDate.now()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recurring expenses") },
                navigationIcon = {
                    // No arrow when shown as a tab of the bottom bar / side rail
                    if (onBack != null) IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.rules.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = onAdd,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Add recurring") }
                )
            }
        }
    ) { padding ->
        if (state.loading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 4.dp,
                bottom = padding.calculateBottomPadding() + 88.dp // room for the FAB
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.rules.isEmpty()) {
                item { EmptyRecurring(onAdd) }
                return@LazyColumn
            }
            item { SummaryCard(state) }
            items(state.rules, key = { it.id }) { rule ->
                RecurringRow(
                    rule = rule,
                    today = today,
                    onClick = { onOpen(rule.id) },
                    onPausedChange = { onPausedChange(rule, it) }
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(state: RecurringListState) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Fixed costs per month", style = MaterialTheme.typography.labelLarge)
            Text(
                money(state.monthlyTotal, rounded = true),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Text(
                when (state.activeCount) {
                    0 -> "No active recurring expenses"
                    1 -> "From 1 active recurring expense"
                    else -> "From ${state.activeCount} active recurring expenses"
                },
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun RecurringRow(
    rule: RecurringExpense,
    today: LocalDate,
    onClick: () -> Unit,
    onPausedChange: (Boolean) -> Unit
) {
    val status = when {
        rule.hasEnded -> "Ended"
        rule.paused -> "Paused"
        else -> "Next: ${relativeDay(rule.nextDate!!, today)}"
    }
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(
                Modifier.weight(1f).alpha(if (rule.isActive) 1f else 0.6f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(10.dp).background(rule.category.color, CircleShape))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        rule.title, style = MaterialTheme.typography.titleSmall,
                        maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(money(rule.amount), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    rule.scheduleText(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    status,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = if (rule.isActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(12.dp))
            if (rule.hasEnded) {
                Spacer(Modifier.width(52.dp)) // same width as a Switch, keeps amounts aligned
            } else {
                Switch(
                    checked = !rule.paused,
                    onCheckedChange = { active -> onPausedChange(!active) },
                    modifier = Modifier.semantics {
                        contentDescription = if (rule.paused) "Resume ${rule.title}" else "Pause ${rule.title}"
                    }
                )
            }
        }
    }
}

@Composable
private fun EmptyRecurring(onAdd: () -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Filled.EventRepeat, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text("No recurring expenses yet", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                "Add costs that repeat — rent, EMIs, phone recharge, Netflix — once, and MngExpense " +
                    "will add them for you automatically on the right day.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onAdd) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add recurring expense")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RecurringListPreview() {
    val today = LocalDate.of(2026, 11, 18)
    val rules = listOf(
        RecurringExpense(1, "Rent", 15000.0, Category.BILLS, startDate = LocalDate.of(2026, 1, 1), nextIndex = 11),
        RecurringExpense(2, "Netflix", 499.0, Category.ENTERTAINMENT, startDate = LocalDate.of(2026, 3, 20), nextIndex = 8),
        RecurringExpense(3, "Gym", 1200.0, Category.HEALTH, frequency = Frequency.MONTHLY, startDate = LocalDate.of(2026, 6, 5), nextIndex = 6, paused = true)
    )
    MaterialTheme {
        RecurringListContent(
            RecurringListState(rules, rules.filter { it.isActive }.sumOf { it.monthlyEquivalent }, 2, false),
            today = today
        )
    }
}
