package com.guruprasad.mngexpense.ui.screens.setting

import android.Manifest
import android.app.StatusBarManager
import android.content.ComponentName
import android.graphics.drawable.Icon as SystemIcon
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.guruprasad.mngexpense.R
import com.guruprasad.mngexpense.data.service.ShakeService
import com.guruprasad.mngexpense.data.tile.QuickAddTileService
import com.guruprasad.mngexpense.data.widget.TodayWidgetReceiver
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.guruprasad.mngexpense.util.ShakePrefs
import com.guruprasad.mngexpense.data.backup.BackupPrefs
import com.guruprasad.mngexpense.data.backup.BackupSchedule
import com.guruprasad.mngexpense.ui.screens.backup.formatBackupTime
import androidx.compose.foundation.clickable

/** Permission status, re-checked every time the user comes back from Settings. */
private data class PermState(
    val overlay: Boolean = false,
    val notifications: Boolean = true,
    val battery: Boolean = false,
    val fullScreen: Boolean = true
)

private fun readPerms(ctx: Context) = PermState(
    overlay = Settings.canDrawOverlays(ctx),
    notifications = Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED,
    battery = (ctx.getSystemService(Context.POWER_SERVICE) as PowerManager)
        .isIgnoringBatteryOptimizations(ctx.packageName),
    fullScreen = Build.VERSION.SDK_INT < 34 ||
            (ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).canUseFullScreenIntent()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShakeSettingsScreen(
    onBack: (() -> Unit)? = null,
    onOpenBackup: () -> Unit = {},
    onOpenCategories: () -> Unit = {}
) {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(ShakePrefs.isEnabled(context)) }
    var sensitivity by remember { mutableFloatStateOf(ShakePrefs.sensitivity(context).toFloat()) }
    var perms by remember { mutableStateOf(readPerms(context)) }

    // Refresh permission status whenever the screen resumes (e.g. back from Settings)
    LifecycleResumeEffect(Unit) {
        perms = readPerms(context)
        onPauseOrDispose { }
    }

    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        perms = readPerms(context)
    }

    fun openSettings(action: String, withPackage: Boolean = true) {
        val intent = if (withPackage) Intent(action, Uri.parse("package:${context.packageName}"))
        else Intent(action)
        try {
            context.startActivity(intent)
        } catch (e: android.content.ActivityNotFoundException) {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    // No arrow when shown as a tab of the bottom bar / side rail
                    if (onBack != null) IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("General", style = MaterialTheme.typography.titleMedium)

            // ---- Currency ----
            CurrencyCard()

            // ---- Categories ----
            CategoriesCard(onClick = onOpenCategories)

            // ---- Backup & restore ----
            BackupCard(onClick = onOpenBackup)

            // ---- Quick add: tile + home-screen widget ----
            Spacer(Modifier.height(4.dp))
            Text("Quick add", style = MaterialTheme.typography.titleMedium)
            QuickTileCard()
            WidgetCard()

            Spacer(Modifier.height(4.dp))
            Text("Shake to open", style = MaterialTheme.typography.titleMedium)

            // ---- Status + main switch ----
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (enabled && perms.overlay) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Open app when I shake the phone", style = MaterialTheme.typography.titleMedium)
                        Text(
                            when {
                                !enabled -> "Off"
                                perms.overlay -> "Ready: works when the phone is locked or unlocked"
                                else -> "Allow \"Display over other apps\" below so the app can open by itself"
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(
                        checked = enabled,
                        onCheckedChange = { on ->
                            enabled = on
                            ShakePrefs.setEnabled(context, on)
                            if (on) {
                                ShakeService.start(context)
                                if (!perms.overlay) {
                                    Toast.makeText(context, "Please allow \"Display over other apps\"", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                ShakeService.stop(context)
                            }
                        }
                    )
                }
            }

            // ---- Sensitivity ----
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Shake sensitivity", style = MaterialTheme.typography.titleSmall)
                    Slider(
                        value = sensitivity,
                        onValueChange = { sensitivity = it },
                        onValueChangeFinished = {
                            ShakePrefs.setSensitivity(context, sensitivity.toInt())
                            if (enabled) ShakeService.start(context) // service reloads the threshold
                        },
                        valueRange = 1f..5f,
                        steps = 3
                    )
                    Row {
                        Text("Hard shake", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                        Text("Light shake", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // ---- Permissions ----
            Spacer(Modifier.height(4.dp))
            Text("Setup", style = MaterialTheme.typography.titleMedium)
            Text(
                "Tap each item with ⚠ and allow it.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            PermissionRow(
                title = "Display over other apps",
                subtitle = "Required: lets the app open itself after a shake",
                granted = perms.overlay,
                onClick = { openSettings(Settings.ACTION_MANAGE_OVERLAY_PERMISSION) }
            )
            if (Build.VERSION.SDK_INT >= 33) {
                PermissionRow(
                    title = "Notifications",
                    subtitle = "Shows that shake detection is running",
                    granted = perms.notifications,
                    onClick = { notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
                )
            }
            PermissionRow(
                title = "Don't optimize battery",
                subtitle = "Stops Android from killing shake detection",
                granted = perms.battery,
                onClick = { openSettings(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS, withPackage = false) }
            )
            if (Build.VERSION.SDK_INT >= 34) {
                PermissionRow(
                    title = "Full-screen alerts",
                    subtitle = "Backup way to open over the lock screen",
                    granted = perms.fullScreen,
                    onClick = { openSettings(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT) }
                )
            }
            PermissionRow(
                title = "App settings (Autostart)",
                subtitle = "Xiaomi / Oppo / Vivo / Realme: turn on Autostart and set battery to No restrictions",
                granted = null,
                onClick = { openSettings(Settings.ACTION_APPLICATION_DETAILS_SETTINGS) }
            )
        }
    }
}

@Composable
private fun PermissionRow(title: String, subtitle: String, granted: Boolean?, onClick: () -> Unit) {
    OutlinedCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            when (granted) {
                true -> Icon(Icons.Filled.CheckCircle, "Allowed", tint = MaterialTheme.colorScheme.primary)
                false -> Icon(Icons.Filled.Warning, "Not allowed", tint = MaterialTheme.colorScheme.error)
                null -> Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** Explains the "Add expense" Quick Settings tile and, on Android 13+, adds it in one tap. */
@Composable
private fun QuickTileCard() {
    val context = LocalContext.current
    val tileLabel = stringResource(R.string.tile_add_expense) // configuration-aware
    var status by remember { mutableStateOf<String?>(null) }

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painterResource(R.drawable.ic_tile_add_expense), contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text("Quick Settings tile", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Add an expense straight from the notification shade. Shows today's total too.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Button(onClick = {
                    val statusBar = context.getSystemService(StatusBarManager::class.java)
                    statusBar.requestAddTileService(
                        ComponentName(context, QuickAddTileService::class.java),
                        tileLabel,
                        SystemIcon.createWithResource(context, R.drawable.ic_tile_add_expense),
                        context.mainExecutor
                    ) { result ->
                        status = when (result) {
                            StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ADDED -> "Tile added ✓"
                            StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED -> "The tile is already in your Quick Settings"
                            StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_NOT_ADDED -> null // user said no
                            else -> "Couldn't add automatically. Swipe down twice, tap ✎ and drag \"Add expense\" in."
                        }
                    }
                }) { Text("Add tile") }
                status?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Text(
                    "Swipe down twice from the top of the screen, tap ✎ (edit) and drag \"Add expense\" into your tiles.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}


/** Explains the "Today's spending" widget and offers to pin it (launchers that support it). */
@Composable
private fun WidgetCard() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pinSupported = remember {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            android.appwidget.AppWidgetManager.getInstance(context).isRequestPinAppWidgetSupported
    }

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.PieChart, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text("Home-screen widget", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Today's spending as a pie or bar chart, with an Add expense button.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            if (pinSupported) {
                Button(onClick = {
                    scope.launch {
                        GlanceAppWidgetManager(context)
                            .requestPinGlanceAppWidget(TodayWidgetReceiver::class.java)
                    }
                }) { Text("Add widget") }
            } else {
                Text(
                    "Long-press your home screen → Widgets → MngExpense, and drag \"Today's spending\" onto it.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/** Settings → Backup & restore: shows the schedule and last backup, opens the backup screen. */
@Composable
private fun BackupCard(onClick: () -> Unit) {
    val context = LocalContext.current
    var summary by remember { mutableStateOf("") }
    // Re-read when coming back from the backup screen
    LifecycleResumeEffect(Unit) {
        val schedule = BackupPrefs.schedule(context)
        val last = BackupPrefs.lastBackupAt(context)?.let { "last backup ${formatBackupTime(it)}" } ?: "no backup yet"
        summary = (if (schedule == BackupSchedule.OFF) "Scheduled backup off" else "${schedule.label} backup") + " · $last"
        onPauseOrDispose { }
    }
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Backup, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("Backup & restore", style = MaterialTheme.typography.titleSmall)
                Text(summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }
}

/** Settings → Categories: how many there are; opens the categories screen. */
@Composable
private fun CategoriesCard(onClick: () -> Unit) {
    val count = com.guruprasad.mngexpense.domain.CategoryStore.all.size // Compose state: updates by itself
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Category, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("Categories", style = MaterialTheme.typography.titleSmall)
                Text(
                    "$count categories · add your own, rename, recolour, reorder or delete",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }
}
