package com.guruprasad.mngexpense.data.recurring

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
import com.guruprasad.mngexpense.domain.Expense
import com.guruprasad.mngexpense.util.money

/** "Added: Rent ₹15,000" — tells the user what was added automatically in the background. */
object RecurringNotifier {
    private const val CHANNEL_ID = "recurring_expenses"
    private const val NOTIF_ID = 201

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID, "Recurring expenses", NotificationManager.IMPORTANCE_LOW
        ).apply { description = "Shows recurring expenses that were added automatically" }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    @SuppressLint("MissingPermission") // checked with areNotificationsEnabled()
    fun notifyAdded(context: Context, added: List<Expense>) {
        val nm = NotificationManagerCompat.from(context)
        if (added.isEmpty() || !nm.areNotificationsEnabled()) return
        createChannel(context)

        val open = PendingIntent.getActivity(
            context, 0, MainActivity.recurringListIntent(context),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val total = added.sumOf { it.amount }
        val title = if (added.size == 1) "Added: ${added[0].title}"
        else "Added ${added.size} recurring expenses"
        val text = if (added.size == 1) "${money(total)} · ${added[0].category.label}"
        else "${money(total)} in total"

        val inbox = NotificationCompat.InboxStyle()
        added.take(6).forEach { inbox.addLine("${it.title}  ${money(it.amount)}") }
        if (added.size > 6) inbox.addLine("+${added.size - 6} more")

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_recurring)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(if (added.size > 1) inbox else null)
            .setContentIntent(open)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        nm.notify(NOTIF_ID, notification)
    }
}
