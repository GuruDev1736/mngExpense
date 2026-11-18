package com.guruprasad.mngexpense.data.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.Button
import androidx.glance.ButtonDefaults
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.guruprasad.mngexpense.MainActivity
import com.guruprasad.mngexpense.MngExpenseApp
import com.guruprasad.mngexpense.domain.Category
import com.guruprasad.mngexpense.domain.Expense
import com.guruprasad.mngexpense.ui.common.color
import com.guruprasad.mngexpense.util.compactMoney
import com.guruprasad.mngexpense.util.money
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.min

/**
 * Home-screen widget: today's spending as a donut (Pie tab) or bar chart (Bar tab),
 * plus an "Add expense" button. The selected tab is remembered per widget instance.
 */
class TodayWidget : GlanceAppWidget() {

    // Re-render for the exact size the launcher gives us, so chart bitmaps are always sharp
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = (context.applicationContext as MngExpenseApp).expenseRepository
        val today = LocalDate.now()
        val todayFlow = repository.observeBetween(today, today.plusDays(1))
        val initial = todayFlow.first()

        provideContent {
            val expenses by todayFlow.collectAsState(initial)
            val tab = currentState(TAB_KEY) ?: TAB_PIE
            GlanceTheme {
                WidgetContent(today, expenses, tab)
            }
        }
    }

    companion object {
        val TAB_KEY = stringPreferencesKey("tab")
        const val TAB_PIE = "pie"
        const val TAB_BAR = "bar"
    }
}

/** Switches between the Pie and Bar tabs for one widget instance. */
class SelectTabAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val tab = parameters[TabParam] ?: return
        updateAppWidgetState(context, glanceId) { it[TodayWidget.TAB_KEY] = tab }
        TodayWidget().update(context, glanceId)
    }

    companion object {
        val TabParam = ActionParameters.Key<String>("tab")
    }
}

// ============================================================================
// Layout
// ============================================================================

@Composable
private fun WidgetContent(today: LocalDate, expenses: List<Expense>, tab: String) {
    val context = LocalContext.current
    val size = LocalSize.current
    val totals = remember(expenses) {
        expenses.groupBy { it.category }
            .map { (c, list) -> c to list.sumOf { it.amount } }
            .sortedByDescending { it.second }
    }
    val total = totals.sumOf { it.second }
    val openApp = actionStartActivity(Intent(context, MainActivity::class.java))

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .cornerRadius(24.dp)
            .padding(14.dp)
    ) {
        // ---- Header: today's total (tap → dashboard) ----
        Row(
            modifier = GlanceModifier.fillMaxWidth().clickable(openApp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(GlanceModifier.defaultWeight()) {
                Text(
                    "Spent today",
                    style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 12.sp)
                )
                Text(
                    money(total),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Text(
                today.format(DateTimeFormatter.ofPattern("EEE, d MMM")),
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 12.sp)
            )
        }

        Spacer(GlanceModifier.height(10.dp))

        // ---- Tabs ----
        Row(GlanceModifier.fillMaxWidth()) {
            TabChip("Pie", selected = tab == TodayWidget.TAB_PIE, value = TodayWidget.TAB_PIE,
                modifier = GlanceModifier.defaultWeight())
            Spacer(GlanceModifier.width(8.dp))
            TabChip("Bar", selected = tab == TodayWidget.TAB_BAR, value = TodayWidget.TAB_BAR,
                modifier = GlanceModifier.defaultWeight())
        }

        Spacer(GlanceModifier.height(10.dp))

        // ---- Chart area (tap → dashboard) ----
        Box(
            modifier = GlanceModifier.fillMaxWidth().defaultWeight().clickable(openApp),
            contentAlignment = Alignment.Center
        ) {
            // Space left for the chart after header, tabs, button and paddings
            val chartArea = DpSize(size.width - 28.dp, (size.height - 186.dp).coerceAtLeast(56.dp))
            when {
                totals.isEmpty() -> EmptyChart()
                tab == TodayWidget.TAB_BAR -> BarChart(totals, chartArea)
                else -> PieChart(totals, chartArea)
            }
        }

        Spacer(GlanceModifier.height(10.dp))

        // ---- Add expense ----
        Button(
            text = "+  Add expense",
            onClick = actionStartActivity(MainActivity.addExpenseIntent(context)),
            modifier = GlanceModifier.fillMaxWidth().height(44.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = GlanceTheme.colors.primary,
                contentColor = GlanceTheme.colors.onPrimary
            )
        )
    }
}

@Composable
private fun TabChip(label: String, selected: Boolean, value: String, modifier: GlanceModifier) {
    Box(
        modifier = modifier
            .height(34.dp)
            .cornerRadius(17.dp)
            .background(if (selected) GlanceTheme.colors.secondaryContainer else GlanceTheme.colors.surfaceVariant)
            .clickable(actionRunCallback<SelectTabAction>(actionParametersOf(SelectTabAction.TabParam to value))),
        contentAlignment = Alignment.Center
    ) {
        Text(
            (if (selected) "✓ " else "") + label,
            style = TextStyle(
                color = if (selected) GlanceTheme.colors.onSecondaryContainer else GlanceTheme.colors.onSurfaceVariant,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        )
    }
}

@Composable
private fun EmptyChart() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "No expenses yet today",
            style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        )
        Text(
            "Tap Add expense to log one",
            style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 12.sp)
        )
    }
}

@Composable
private fun PieChart(totals: List<Pair<Category, Double>>, area: DpSize) {
    val context = LocalContext.current
    val density = context.resources.displayMetrics.density
    val side = min(area.height.value, area.width.value * 0.45f).dp
    val bitmap = remember(totals, side) {
        WidgetChartRenderer.donut(totals, (side.value * density).toInt().coerceAtLeast(1), density)
    }
    val total = totals.sumOf { it.second }
    Row(GlanceModifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
        Image(
            provider = ImageProvider(bitmap),
            contentDescription = "Pie chart of today's spending by category",
            modifier = GlanceModifier.size(side)
        )
        Spacer(GlanceModifier.width(14.dp))
        // Legend: as many rows as fit (about 20dp each)
        val rows = (area.height.value / 20).toInt().coerceIn(1, totals.size)
        Column(GlanceModifier.defaultWeight()) {
            totals.take(rows).forEach { (c, amount) ->
                Row(
                    GlanceModifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(GlanceModifier.size(10.dp).cornerRadius(3.dp).background(ColorProvider(c.color))) {}
                    Spacer(GlanceModifier.width(6.dp))
                    Text(
                        c.label, maxLines = 1,
                        style = TextStyle(color = GlanceTheme.colors.onSurface, fontSize = 12.sp),
                        modifier = GlanceModifier.defaultWeight()
                    )
                    Text(
                        "${(amount / total * 100).toInt()}%",
                        style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    )
                }
            }
            if (totals.size > rows) {
                Text(
                    "+${totals.size - rows} more",
                    style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant, fontSize = 11.sp)
                )
            }
        }
    }
}

@Composable
private fun BarChart(totals: List<Pair<Category, Double>>, area: DpSize) {
    val context = LocalContext.current
    val density = context.resources.displayMetrics.density
    val textRow = 16.dp
    val barsHeight = (area.height - textRow * 2 - 4.dp).coerceAtLeast(24.dp)
    val bitmap = remember(totals, area) {
        WidgetChartRenderer.bars(
            totals,
            (area.width.value * density).toInt().coerceAtLeast(1),
            (barsHeight.value * density).toInt().coerceAtLeast(1),
            density
        )
    }
    Column(GlanceModifier.fillMaxWidth()) {
        // Values above, labels below: real Text, so colours follow light/dark mode.
        // Each sits in an equal-weight slot that lines up with its bar in the bitmap.
        SlotRow(totals.map { compactMoney(it.second) }, bold = true, height = textRow)
        Image(
            provider = ImageProvider(bitmap),
            contentDescription = "Bar chart of today's spending by category",
            modifier = GlanceModifier.size(area.width, barsHeight)
        )
        Spacer(GlanceModifier.height(4.dp))
        SlotRow(totals.map { it.first.shortLabel }, bold = false, height = textRow)
    }
}

@Composable
private fun SlotRow(texts: List<String>, bold: Boolean, height: androidx.compose.ui.unit.Dp) {
    Row(GlanceModifier.fillMaxWidth().height(height)) {
        texts.forEach { t ->
            Box(GlanceModifier.defaultWeight().fillMaxHeight(), contentAlignment = Alignment.Center) {
                Text(
                    t, maxLines = 1,
                    style = TextStyle(
                        color = if (bold) GlanceTheme.colors.onSurface else GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }
}
