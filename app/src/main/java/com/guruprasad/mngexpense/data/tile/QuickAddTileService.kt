package com.guruprasad.mngexpense.data.tile

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.guruprasad.mngexpense.MainActivity
import com.guruprasad.mngexpense.MngExpenseApp
import com.guruprasad.mngexpense.R
import com.guruprasad.mngexpense.util.rupees
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Quick Settings tile: "Add expense".
 * Tap → opens the Add Expense screen (the system asks to unlock first if the phone is locked).
 * While the panel is open the tile shows today's total as its subtitle (Android 10+).
 */
class QuickAddTileService : TileService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var refreshJob: Job? = null

    override fun onStartListening() {
        super.onStartListening()
        updateTile(subtitle = null)
        refreshJob?.cancel()
        refreshJob = scope.launch {
            val today = LocalDate.now()
            val total = (application as MngExpenseApp).expenseRepository
                .observeBetween(today, today.plusDays(1))
                .first()
                .sumOf { it.amount }
            updateTile(subtitle = "Today: ${rupees(total)}")
        }
    }

    override fun onStopListening() {
        refreshJob?.cancel()
        super.onStopListening()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onClick() {
        super.onClick()
        // No unlockAndRun here: startActivityAndCollapse already asks for PIN/fingerprint
        // when the phone is locked (MainActivity isn't declared showWhenLocked in the
        // manifest), then opens the screen. Wrapping it in unlockAndRun races with the
        // keyguard dismiss animation and the launch gets dropped.
        openAddExpense()
    }

    private fun updateTile(subtitle: String?) {
        val tile = qsTile ?: return
        tile.label = getString(R.string.tile_add_expense)
        tile.icon = Icon.createWithResource(this, R.drawable.ic_tile_add_expense)
        // Not an on/off toggle: INACTIVE renders it as a plain action button
        tile.state = Tile.STATE_INACTIVE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = subtitle
        }
        tile.updateTile()
    }

    @SuppressLint("StartActivityAndCollapseDeprecated")
    private fun openAddExpense() {
        val intent = MainActivity.addExpenseIntent(this)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 14+: only the PendingIntent overload is allowed
            val pending = PendingIntent.getActivity(
                this, REQUEST_ADD, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            startActivityAndCollapse(pending)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }

    private companion object {
        const val REQUEST_ADD = 200
    }
}
