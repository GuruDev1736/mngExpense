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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.adaptive.navigationsuite.rememberNavigationSuiteScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.guruprasad.mngexpense.data.service.ShakeService
import com.guruprasad.mngexpense.domain.Category
import com.guruprasad.mngexpense.ui.screens.backup.BackupScreen
import com.guruprasad.mngexpense.ui.screens.backup.RestorePrompt
import com.guruprasad.mngexpense.ui.screens.budget.BudgetScreen
import com.guruprasad.mngexpense.ui.screens.category.CategoriesScreen
import com.guruprasad.mngexpense.ui.screens.dashboard.DashboardScreen
import com.guruprasad.mngexpense.ui.screens.expense.ExpenseEditScreen
import com.guruprasad.mngexpense.ui.screens.recurring.RecurringEditScreen
import com.guruprasad.mngexpense.ui.screens.recurring.RecurringListScreen
import com.guruprasad.mngexpense.ui.screens.setting.ShakeSettingsScreen
import com.guruprasad.mngexpense.ui.screens.transactions.TransactionsScreen
import com.guruprasad.mngexpense.ui.navigation.TopLevelTab
import com.guruprasad.mngexpense.ui.theme.MngExpenseTheme
import com.guruprasad.mngexpense.util.ShakePrefs
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainActivity : ComponentActivity() {

    /** Screen that a tile, shortcut or notification asked to open (see [NavRequest]). */
    private var navRequest by mutableStateOf<NavRequest?>(null)
    private var navRequestCount = 0

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
                AppNavigation(navRequest = navRequest)
                // First launch after a reinstall: "Restore your backup?"
                RestorePrompt()
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
        val route = when (intent?.action) {
            ACTION_ADD_EXPENSE -> {
                // Tells the launcher this action is used, so it ranks the shortcut higher
                ShortcutManagerCompat.reportShortcutUsed(this, SHORTCUT_ADD_EXPENSE)
                ExpenseRoute()
            }
            ACTION_OPEN_RECURRING -> RecurringListRoute
            ACTION_OPEN_BUDGETS -> BudgetsRoute
            ACTION_OPEN_BACKUP -> BackupRoute
            else -> return
        }
        navRequest = NavRequest(route, ++navRequestCount)
    }

    companion object {
        const val ACTION_ADD_EXPENSE = "com.guruprasad.mngexpense.action.ADD_EXPENSE"
        const val ACTION_OPEN_RECURRING = "com.guruprasad.mngexpense.action.OPEN_RECURRING"
        const val ACTION_OPEN_BUDGETS = "com.guruprasad.mngexpense.action.OPEN_BUDGETS"
        const val ACTION_OPEN_BACKUP = "com.guruprasad.mngexpense.action.OPEN_BACKUP"

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

        /** Opens the app on the "Budgets" screen. */
        fun budgetsIntent(context: Context): Intent =
            Intent(context, MainActivity::class.java)
                .setAction(ACTION_OPEN_BUDGETS)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

        /** Opens the app on the "Backup & restore" screen. */
        fun backupIntent(context: Context): Intent =
            Intent(context, MainActivity::class.java)
                .setAction(ACTION_OPEN_BACKUP)
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

/** Ask [AppNavigation] to open [route]; [id] makes a repeated request for the same screen distinct. */
data class NavRequest(val route: NavKey, val id: Int)

@Composable
fun AppNavigation(navRequest: NavRequest? = null) {
    // The back stack: a list of routes. The last one is shown on screen.
    // It always starts with the dashboard, so Back from any tab goes Home first.
    val backStack = rememberNavBackStack(DashboardRoute)
    val current = backStack.lastOrNull()
    val currentTab = TopLevelTab.of(current)

    /** Opens [tab] fresh: Home, or Home + that tab's screen. */
    fun selectTab(tab: TopLevelTab) {
        if (current == tab.root) return
        while (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
        if (backStack.firstOrNull() != DashboardRoute) {
            backStack.clear()
            backStack.add(DashboardRoute)
        }
        if (tab != TopLevelTab.HOME) backStack.add(tab.root)
    }

    fun open(route: NavKey) {
        val tab = TopLevelTab.entries.firstOrNull { it.root == route }
        if (tab != null) selectTab(tab) else if (current != route) backStack.add(route)
    }

    fun goBack() {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }

    // Tile / shortcut / notification → open that screen
    LaunchedEffect(navRequest) {
        navRequest?.route?.let(::open)
    }

    // Bottom bar on phones; side rail on tablets, foldables and in landscape
    val suiteState = rememberNavigationSuiteScaffoldState()
    val showNavigation = TopLevelTab.showsNavigation(current)
    LaunchedEffect(showNavigation) {
        if (showNavigation) suiteState.show() else suiteState.hide()
    }

    // The library's default keeps a bottom bar on a phone in landscape, where it eats a third of
    // the short screen: use the side rail whenever the window is wide enough
    val wide = LocalConfiguration.current.screenWidthDp >= 600
    NavigationSuiteScaffold(
        state = suiteState,
        layoutType = if (wide) NavigationSuiteType.NavigationRail else NavigationSuiteType.NavigationBar,
        navigationSuiteItems = {
            TopLevelTab.entries.forEach { tab ->
                val selected = tab == currentTab
                item(
                    selected = selected,
                    onClick = { selectTab(tab) },
                    icon = { Icon(if (selected) tab.selectedIcon else tab.icon, contentDescription = null) },
                    label = { Text(tab.label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                )
            }
        }
    ) {
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
                        onOpenRecurring = { selectTab(TopLevelTab.RECURRING) },
                        onOpenBudgets = { selectTab(TopLevelTab.BUDGETS) },
                        onOpenTransactions = { route -> backStack.add(route) }
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
                        onAdd = { backStack.add(RecurringRoute()) },
                        onOpen = { id -> backStack.add(RecurringRoute(id)) }
                    )
                }
                entry<TransactionsRoute> { route ->
                    TransactionsScreen(
                        initialCategory = route.category?.let { name -> Category.entries.firstOrNull { it.key == name } },
                        initialStart = route.startDay?.let(LocalDate::ofEpochDay),
                        initialEnd = route.endDay?.let(LocalDate::ofEpochDay),
                        focusSearch = route.focusSearch,
                        // The plain tab has no arrow; a search or chart drill-down from Home does
                        onBack = if (route == TopLevelTab.TRANSACTIONS.root) null else ({ goBack() }),
                        onOpenExpense = { id -> backStack.add(ExpenseRoute(id)) }
                    )
                }
                entry<BudgetsRoute> {
                    BudgetScreen()
                }
                entry<RecurringRoute> { route ->
                    RecurringEditScreen(ruleId = route.id, onClose = { goBack() })
                }
                entry<SettingsRoute> {
                    ShakeSettingsScreen(
                        onOpenBackup = { backStack.add(BackupRoute) },
                        onOpenCategories = { backStack.add(CategoriesRoute) }
                    )
                }
                entry<CategoriesRoute> {
                    CategoriesScreen(onBack = { goBack() })
                }
                entry<BackupRoute> {
                    BackupScreen(onBack = { goBack() })
                }
            }
        )
    }
}
