package com.guruprasad.mngexpense

import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.guruprasad.mngexpense.data.service.ShakeService
import com.guruprasad.mngexpense.ui.screens.dashboard.DashboardScreen
import com.guruprasad.mngexpense.ui.screens.expense.ExpenseEditScreen
import com.guruprasad.mngexpense.ui.screens.setting.ShakeSettingsScreen
import com.guruprasad.mngexpense.ui.theme.MngExpenseTheme
import com.guruprasad.mngexpense.util.ShakePrefs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showOverLockScreen()
        enableEdgeToEdge()
        ShakeService.createChannels(this)
        handleShakeIntent(intent)
        setContent {
            MngExpenseTheme {
                AppNavigation()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleShakeIntent(intent)
    }

    // Make sure detection is running whenever the app is opened
    override fun onResume() {
        super.onResume()
        if (ShakePrefs.isEnabled(this)) ShakeService.start(this)
    }

    private fun showOverLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }

    private fun handleShakeIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(ShakeService.EXTRA_FROM_SHAKE, false) != true) return

        // Remove the fallback "Shake detected" notification if it was shown
        getSystemService(NotificationManager::class.java).cancel(ShakeService.NOTIF_ID_LAUNCH)

        // Ask the user to unlock (PIN / fingerprint) right away.
        // Remove this block if you want the app to just show over the lock screen.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val km = getSystemService(KeyguardManager::class.java)
            if (km.isKeyguardLocked) km.requestDismissKeyguard(this, null)
        }
    }
}

@Composable
fun AppNavigation() {
    // The back stack: a list of routes. The last one is shown on screen.
    val backStack = rememberNavBackStack(DashboardRoute)

    fun goBack() {
        if (backStack.size > 1) backStack.removeLastOrNull()
    }

    NavDisplay(
        backStack = backStack,
        onBack = { goBack() },   // system Back button / gesture
        // Give each screen its own saved state and ViewModels (cleared when the screen is popped)
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<DashboardRoute> {
                DashboardScreen(
                    onAddExpense = { backStack.add(ExpenseRoute()) },
                    onExpenseClick = { id -> backStack.add(ExpenseRoute(id)) },
                    onOpenSettings = { backStack.add(SettingsRoute) }
                )
            }
            entry<ExpenseRoute> { route ->
                ExpenseEditScreen(expenseId = route.id, onClose = { goBack() })
            }
            entry<SettingsRoute> {
                ShakeSettingsScreen(onBack = { goBack() })
            }
        }
    )
}
