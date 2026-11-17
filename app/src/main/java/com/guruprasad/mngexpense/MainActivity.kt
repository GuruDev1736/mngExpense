package com.guruprasad.mngexpense

import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.guruprasad.mngexpense.data.service.ShakeService
import com.guruprasad.mngexpense.ui.screens.dashboard.DashboardScreen
import com.guruprasad.mngexpense.ui.screens.expense.ExpenseEditScreen
import com.guruprasad.mngexpense.ui.screens.recurring.RecurringEditScreen
import com.guruprasad.mngexpense.ui.screens.recurring.RecurringListScreen
import com.guruprasad.mngexpense.ui.screens.setting.ShakeSettingsScreen
import com.guruprasad.mngexpense.ui.theme.MngExpenseTheme
import com.guruprasad.mngexpense.util.ShakePrefs
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    /** Bumped each time something (Quick Settings tile, app shortcut) asks to open "Add expense". */
    private var addExpenseRequests by mutableIntStateOf(0)

    /** Bumped when a "recurring expense added" notification is tapped. */
    private var recurringRequests by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showOverLockScreen()
        enableEdgeToEdge()
        ShakeService.createChannels(this)
        handleShakeIntent(intent)
        // Only on a fresh start — after rotation the back stack is already restored
        if (savedInstanceState == null) handleNavigationIntent(intent)
        setContent {
            MngExpenseTheme {
                AppNavigation(addExpenseRequests = addExpenseRequests, recurringRequests = recurringRequests)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleShakeIntent(intent)
        handleNavigationIntent(intent)
    }

    private fun handleNavigationIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_ADD_EXPENSE -> {
                addExpenseRequests++
                // Tells the launcher this action is used, so it ranks the shortcut higher
                ShortcutManagerCompat.reportShortcutUsed(this, SHORTCUT_ADD_EXPENSE)
            }
            ACTION_OPEN_RECURRING -> recurringRequests++
        }
    }

    companion object {
        const val ACTION_ADD_EXPENSE = "com.guruprasad.mngexpense.action.ADD_EXPENSE"
        const val ACTION_OPEN_RECURRING = "com.guruprasad.mngexpense.action.OPEN_RECURRING"

        /** Must match android:shortcutId in res/xml/shortcuts.xml */
        const val SHORTCUT_ADD_EXPENSE = "add_expense"

        /** Opens the app straight on the "Add expense" screen. */
        fun addExpenseIntent(context: Context): Intent =
            Intent(context, MainActivity::class.java)
                .setAction(ACTION_ADD_EXPENSE)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

        /** Opens the app on the "Recurring expenses" list. */
        fun recurringListIntent(context: Context): Intent =
            Intent(context, MainActivity::class.java)
                .setAction(ACTION_OPEN_RECURRING)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }

    // Make sure detection is running whenever the app is opened
    override fun onResume() {
        super.onResume()
        if (ShakePrefs.isEnabled(this)) ShakeService.start(this)
        // The app may have stayed open past midnight: add any recurring expenses now due
        val app = application as MngExpenseApp
        app.appScope.launch {
            try {
                app.recurringRepository.processDue()
            } catch (e: Exception) {
                Log.e("MainActivity", "Recurring catch-up failed", e)
            }
        }
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
fun AppNavigation(addExpenseRequests: Int = 0, recurringRequests: Int = 0) {
    // The back stack: a list of routes. The last one is shown on screen.
    val backStack = rememberNavBackStack(DashboardRoute)

    // Quick Settings tile / shortcut → open "Add expense" (unless it is already showing)
    LaunchedEffect(addExpenseRequests) {
        if (addExpenseRequests > 0 && backStack.lastOrNull() != ExpenseRoute()) {
            backStack.add(ExpenseRoute())
        }
    }

    // "Recurring expense added" notification → open the recurring list
    LaunchedEffect(recurringRequests) {
        if (recurringRequests > 0 && backStack.lastOrNull() != RecurringListRoute) {
            backStack.add(RecurringListRoute)
        }
    }

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
                    onOpenSettings = { backStack.add(SettingsRoute) },
                    onOpenRecurring = { backStack.add(RecurringListRoute) }
                )
            }
            entry<ExpenseRoute> { route ->
                ExpenseEditScreen(
                    expenseId = route.id,
                    onClose = { goBack() },
                    onOpenRecurring = { id -> backStack.add(RecurringRoute(id)) }
                )
            }
            entry<RecurringListRoute> {
                RecurringListScreen(
                    onBack = { goBack() },
                    onAdd = { backStack.add(RecurringRoute()) },
                    onOpen = { id -> backStack.add(RecurringRoute(id)) }
                )
            }
            entry<RecurringRoute> { route ->
                RecurringEditScreen(ruleId = route.id, onClose = { goBack() })
            }
            entry<SettingsRoute> {
                ShakeSettingsScreen(onBack = { goBack() })
            }
        }
    )
}
