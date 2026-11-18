package com.guruprasad.mngexpense.ui.screens.setting

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.glance.appwidget.updateAll
import com.guruprasad.mngexpense.data.widget.TodayWidget
import com.guruprasad.mngexpense.ui.common.CurrencyPickerDialog
import com.guruprasad.mngexpense.util.AppCurrency
import com.guruprasad.mngexpense.util.CurrencyPrefs
import com.guruprasad.mngexpense.util.currencyName
import com.guruprasad.mngexpense.util.currencySymbol
import kotlinx.coroutines.launch

/** Settings → Currency: the home currency used for every total, budget and report. */
@Composable
fun CurrencyCard() {
    val context = LocalContext.current
    val locale = LocalConfiguration.current.locales[0]
    val scope = rememberCoroutineScope()
    var picking by rememberSaveable { mutableStateOf(false) }
    var pending by rememberSaveable { mutableStateOf<String?>(null) }
    val home = AppCurrency.code

    Card(
        Modifier.fillMaxWidth().clickable { picking = true },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.CurrencyExchange, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("Home currency", style = MaterialTheme.typography.titleSmall)
                Text(
                    "${currencyName(home, locale)} (${currencySymbol(home)}) · used for totals, budgets and reports. " +
                        "Expenses in other currencies can be added from the amount field.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }

    if (picking) {
        CurrencyPickerDialog(
            title = "Home currency",
            selected = home,
            onPick = { code ->
                picking = false
                if (code != home) pending = code
            },
            onDismiss = { picking = false }
        )
    }

    // Amounts are stored as plain numbers: switching only changes how they're labelled
    pending?.let { code ->
        AlertDialog(
            onDismissRequest = { pending = null },
            icon = { Icon(Icons.Filled.CurrencyExchange, contentDescription = null) },
            title = { Text("Switch to ${currencyName(code, locale)}?") },
            text = {
                Text(
                    "Amounts you have already entered are not converted: ${currencySymbol(home)}1,000 " +
                        "will show as ${currencySymbol(code)}1,000. Budgets and recurring expenses keep their " +
                        "numbers too.\n\nThis is meant for choosing the currency you live in, not for a trip — " +
                        "for that, pick the currency on each expense instead."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    setHomeCurrency(context, code, scope)
                    pending = null
                }) { Text("Switch") }
            },
            dismissButton = { TextButton(onClick = { pending = null }) { Text("Cancel") } }
        )
    }
}

private fun setHomeCurrency(context: Context, code: String, scope: kotlinx.coroutines.CoroutineScope) {
    CurrencyPrefs.setHomeCurrency(context, code)
    // Widgets aren't Compose screens: redraw them with the new symbol
    scope.launch { runCatching { TodayWidget().updateAll(context.applicationContext) } }
}
