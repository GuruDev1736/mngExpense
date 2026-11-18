package com.guruprasad.mngexpense.ui.screens.budget

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.guruprasad.mngexpense.domain.Budget
import com.guruprasad.mngexpense.domain.BudgetAlerts
import com.guruprasad.mngexpense.domain.BudgetStatus
import com.guruprasad.mngexpense.domain.Category
import com.guruprasad.mngexpense.ui.common.BudgetProgressBar
import com.guruprasad.mngexpense.ui.common.budgetLeftText
import com.guruprasad.mngexpense.ui.common.budgetPaceText
import com.guruprasad.mngexpense.ui.common.color
import com.guruprasad.mngexpense.util.money
import com.guruprasad.mngexpense.util.currencySymbol
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun BudgetScreen(
    onBack: () -> Unit,
    viewModel: BudgetViewModel = viewModel(factory = BudgetViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val alertsEnabled by viewModel.alertsEnabled.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Re-check notification permission whenever the screen comes back (e.g. from system settings)
    var notificationsAllowed by remember { mutableStateOf(true) }
    LifecycleResumeEffect(Unit) {
        notificationsAllowed = NotificationManagerCompat.from(context).areNotificationsEnabled()
        onPauseOrDispose { }
    }
    fun openNotificationSettings() = context.startActivity(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        } else {
            // Android 7 has no notification-settings screen per app: open App info instead
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:${context.packageName}".toUri())
        }
    )
    val askPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        notificationsAllowed = granted
        // After two refusals Android stops showing the dialog — send the user to settings instead
        if (!granted) openNotificationSettings()
    }

    BudgetContent(
        state = state,
        alertsEnabled = alertsEnabled,
        notificationsAllowed = notificationsAllowed,
        onBack = onBack,
        onAlertsChange = viewModel::setAlertsEnabled,
        onAllowNotifications = {
            // The permission dialog exists on Android 13+; before that notifications are on by default
            // and can only be turned back on in system settings
            if (Build.VERSION.SDK_INT >= 33) askPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            else openNotificationSettings()
        },
        onSave = viewModel::saveBudget,
        onRemove = viewModel::removeBudget
    )
}

/** Which budget the amount dialog is editing. */
private data class EditTarget(val category: Category?, val current: Double?)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetContent(
    state: BudgetScreenState,
    alertsEnabled: Boolean = true,
    notificationsAllowed: Boolean = true,
    onBack: () -> Unit = {},
    onAlertsChange: (Boolean) -> Unit = {},
    onAllowNotifications: () -> Unit = {},
    onSave: (Category?, Double) -> Unit = { _, _ -> },
    onRemove: (Category?) -> Unit = {}
) {
    // Saved across rotation as plain values (Category is an enum, so its name is enough)
    var editing by rememberSaveable { mutableStateOf(false) }
    var editKey by rememberSaveable { mutableStateOf(Budget.OVERALL_KEY) }
    val locale = LocalConfiguration.current.locales[0] // recomposes if the language changes
    val monthLabel = state.month.format(DateTimeFormatter.ofPattern("MMMM yyyy", locale))

    fun edit(category: Category?) {
        editKey = Budget.keyOf(category)
        editing = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budgets") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 4.dp,
                bottom = padding.calculateBottomPadding() + 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    monthLabel, style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item { OverallCard(state, onEdit = { edit(null) }) }

            val sum = state.categoryBudgetSum
            val overall = state.overall
            if (overall != null && sum > overall.budget.amount) {
                item {
                    HintRow(
                        "Your category budgets add up to ${money(sum)}, more than the " +
                            "${money(overall.budget.amount)} monthly budget."
                    )
                }
            }

            item {
                Text(
                    "Category budgets", style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(state.categories, key = { it.category.name }) { row ->
                CategoryRow(row, onClick = { edit(row.category) })
            }

            item {
                AlertsCard(
                    enabled = alertsEnabled,
                    notificationsAllowed = notificationsAllowed,
                    onChange = onAlertsChange,
                    onAllowNotifications = onAllowNotifications
                )
            }
        }
    }

    if (editing) {
        val category = Budget.categoryOf(editKey)
        val current = if (category == null) state.overall?.budget?.amount
        else state.categories.firstOrNull { it.category == category }?.status?.budget?.amount
        BudgetAmountDialog(
            target = EditTarget(category, current),
            onSave = { onSave(category, it); editing = false },
            onRemove = { onRemove(category); editing = false },
            onDismiss = { editing = false }
        )
    }
}

@Composable
private fun OverallCard(state: BudgetScreenState, onEdit: () -> Unit) {
    val status = state.overall
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onEdit),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Monthly budget", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                if (status != null) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit monthly budget", modifier = Modifier.size(18.dp))
                }
            }
            if (status == null) {
                Text(
                    "Set one limit for all your spending. You'll see how much is left each month " +
                        "and get an alert at ${BudgetAlerts.WARNING_PERCENT}% and when you go over.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "${money(state.totalSpent)} spent so far this month",
                    style = MaterialTheme.typography.bodySmall
                )
                Button(onClick = onEdit, modifier = Modifier.padding(top = 4.dp)) { Text("Set monthly budget") }
            } else {
                Text(
                    "${money(status.spent)} of ${money(status.budget.amount)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                BudgetProgressBar(status, height = 12)
                Text(budgetLeftText(status), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                budgetPaceText(status)?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            }
        }
    }
}

@Composable
private fun CategoryRow(row: CategoryBudgetRow, onClick: () -> Unit) {
    val status = row.status
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).background(row.category.color, CircleShape))
                Spacer(Modifier.width(10.dp))
                Text(row.category.label, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                if (status == null) {
                    Text("Set budget", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                } else {
                    Text(
                        "${status.percent}%", style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            if (status == null) {
                Text(
                    "${money(row.spent)} spent this month · no budget",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                BudgetProgressBar(status, height = 8)
                Text(
                    "${money(status.spent)} of ${money(status.budget.amount)} · ${budgetLeftText(status)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AlertsCard(
    enabled: Boolean,
    notificationsAllowed: Boolean,
    onChange: (Boolean) -> Unit,
    onAllowNotifications: () -> Unit
) {
    Card(
        Modifier.fillMaxWidth().padding(top = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column {
            Row(
                Modifier
                    .fillMaxWidth()
                    .toggleable(value = enabled, role = Role.Switch, onValueChange = onChange)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text("Budget alerts", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Notify me when a budget is ${BudgetAlerts.WARNING_PERCENT}% used and when it's exceeded " +
                            "(once each per month)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = enabled, onCheckedChange = null) // the whole row toggles
            }
            if (enabled && !notificationsAllowed) {
                Row(
                    Modifier.padding(start = 16.dp, end = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Warning, contentDescription = null,
                        tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Notifications are off for this app, so alerts can't be shown.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onAllowNotifications) { Text("Allow") }
                }
            }
        }
    }
}

@Composable
private fun HintRow(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
        Icon(Icons.Filled.Warning, contentDescription = null, tint = Amber, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall)
    }
}

/** Amount entry for one budget, with "Remove" when it already exists. */
@Composable
private fun BudgetAmountDialog(
    target: EditTarget,
    onSave: (Double) -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit
) {
    var text by rememberSaveable { mutableStateOf(target.current?.let(::formatAmount) ?: "") }
    val amount = text.toDoubleOrNull()
    val valid = amount != null && amount > 0
    val focus = remember { FocusRequester() }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.Savings, contentDescription = null) },
        title = { Text(BudgetViewModel.labelOf(target.category)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    if (target.category == null) "How much do you want to spend in total each month?"
                    else "How much do you want to spend on ${target.category.label} each month?",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { v -> if (v.isEmpty() || AMOUNT_INPUT.matches(v)) text = v },
                    prefix = { Text(currencySymbol() + " ") },
                    label = { Text("Amount per month") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().focusRequester(focus)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { amount?.let(onSave) }, enabled = valid) { Text("Save") }
        },
        dismissButton = {
            Row {
                if (target.current != null) {
                    TextButton(onClick = onRemove) {
                        Text("Remove", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
    LaunchedEffect(Unit) { focus.requestFocus() }
}

private val AMOUNT_INPUT = Regex("""\d{0,9}(\.\d{0,2})?""")

private fun formatAmount(v: Double): String =
    if (v % 1.0 == 0.0) v.toLong().toString() else "%.2f".format(Locale.ROOT, v)

private val Amber = androidx.compose.ui.graphics.Color(0xFFEDA100)

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun BudgetPreview() {
    val today = LocalDate.of(2026, 11, 18)
    val month = YearMonth.of(2026, 11)
    fun s(c: Category?, amount: Double, spent: Double) = BudgetStatus(Budget(c, amount), spent, month, today)
    MaterialTheme {
        BudgetContent(
            BudgetScreenState(
                month = month,
                overall = s(null, 30000.0, 21400.0),
                totalSpent = 21400.0,
                categories = listOf(
                    CategoryBudgetRow(Category.FOOD, 7300.0, s(Category.FOOD, 8000.0, 7300.0)),
                    CategoryBudgetRow(Category.ENTERTAINMENT, 2450.0, s(Category.ENTERTAINMENT, 2000.0, 2450.0)),
                    CategoryBudgetRow(Category.TRANSPORT, 900.0, null)
                ),
                loading = false
            ),
            notificationsAllowed = false
        )
    }
}
