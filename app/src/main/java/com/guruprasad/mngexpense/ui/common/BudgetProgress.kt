package com.guruprasad.mngexpense.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.guruprasad.mngexpense.domain.BudgetLevel
import com.guruprasad.mngexpense.domain.BudgetStatus
import com.guruprasad.mngexpense.util.money

/** Green under 80 %, amber from 80 %, red when the budget is used up or exceeded. */
@Composable
fun budgetColor(level: BudgetLevel): Color = when (level) {
    BudgetLevel.ON_TRACK -> Color(0xFF1BAF7A)
    BudgetLevel.WARNING -> Color(0xFFEDA100)
    BudgetLevel.OVER -> MaterialTheme.colorScheme.error
}

@Composable
fun BudgetProgressBar(status: BudgetStatus, modifier: Modifier = Modifier, height: Int = 10) {
    LinearProgressIndicator(
        progress = { status.fraction.toFloat().coerceIn(0f, 1f) },
        modifier = modifier.fillMaxWidth().height(height.dp).clip(RoundedCornerShape(50)),
        color = budgetColor(status.level),
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
        strokeCap = StrokeCap.Round,
        gapSize = 0.dp,
        drawStopIndicator = {}
    )
}

/** "₹8,600 left · 12 days to go" / "Over by ₹1,200" / "₹2,000 under budget" (past month). */
fun budgetLeftText(status: BudgetStatus): String = when {
    status.left < 0 -> "Over by ${money(-status.left)}"
    status.left == 0.0 -> "Budget used up"
    !status.isCurrentMonth -> "${money(status.left)} under budget"
    status.daysLeft == 0 -> "${money(status.left)} left · last day of the month"
    status.daysLeft == 1 -> "${money(status.left)} left · 1 day to go"
    else -> "${money(status.left)} left · ${status.daysLeft} days to go"
}

/** Pace hint for the current month, or null when there is no projection yet. */
fun budgetPaceText(status: BudgetStatus): String? {
    val projected = status.projected ?: return null
    if (status.left < 0) return null // already over: the pace adds nothing
    val diff = projected - status.budget.amount
    return if (diff > 0) "At this pace: about ${money(projected, rounded = true)} by month end (${money(diff, rounded = true)} over)"
    else "On track: about ${money(projected, rounded = true)} by month end"
}
