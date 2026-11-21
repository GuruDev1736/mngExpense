package com.guruprasad.mngexpense.data.budget

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.guruprasad.mngexpense.MainActivity
import com.guruprasad.mngexpense.R
import com.guruprasad.mngexpense.domain.BudgetAlert
import com.guruprasad.mngexpense.domain.BudgetAlerts
import com.guruprasad.mngexpense.util.money
import java.time.format.TextStyle
import java.util.Locale

/** "Food budget: 80% used" / "Food budget exceeded" notifications. */
object BudgetNotifier {
    private const val CHANNEL_ID = "budget_alerts"
    private const val NOTIF_ID_BASE = 300

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID, "Budget alerts", NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Tells you when a monthly budget is 80% used or exceeded" }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    @SuppressLint("MissingPermission") // checked with areNotificationsEnabled()
    fun notify(context: Context, alerts: List<BudgetAlert>) {
        val nm = NotificationManagerCompat.from(context)
        if (alerts.isEmpty() || !nm.areNotificationsEnabled()) return
        createChannel(context)
        val open = PendingIntent.getActivity(
            context, 1, MainActivity.budgetsIntent(context),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        for (alert in alerts) {
            val (title, text) = texts(alert)
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_budget)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setContentIntent(open)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .build()
            // One notification per budget: a later "exceeded" replaces that budget's "80% used"
            // One notification per budget: a stable id from its key (0 = the overall budget)
            val slot = alert.status.budget.category?.let { Math.floorMod(it.key.hashCode(), 99) + 1 } ?: 0
            nm.notify(NOTIF_ID_BASE + slot, notification)
        }
    }

    fun texts(alert: BudgetAlert): Pair<String, String> {
        val s = alert.status
        val monthName = s.month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
        return if (alert.percent >= BudgetAlerts.LIMIT_PERCENT) {
            val title = if (s.left == 0.0) "${s.budget.label} used up" else "${s.budget.label} exceeded"
            val over = if (s.left < 0) " · over by ${money(-s.left)}" else ""
            title to "${money(s.spent)} spent of ${money(s.budget.amount)} in $monthName$over"
        } else {
            "${s.budget.label}: ${s.percent}% used" to
                "${money(s.left)} left of ${money(s.budget.amount)} for $monthName"
        }
    }
}
