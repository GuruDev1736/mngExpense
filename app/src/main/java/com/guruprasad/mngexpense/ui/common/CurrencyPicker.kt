package com.guruprasad.mngexpense.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.guruprasad.mngexpense.domain.Currencies
import com.guruprasad.mngexpense.util.currencyName
import com.guruprasad.mngexpense.util.currencySymbol

/** Full-screen currency list with search; popular currencies first. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyPickerDialog(
    title: String,
    selected: String,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val locale = LocalConfiguration.current.locales[0]
    var query by rememberSaveable { mutableStateOf("") }
    val all = remember { Currencies.all() }
    val q = query.trim().lowercase()
    fun matches(code: String) = q.isEmpty() ||
        code.lowercase().contains(q) || currencyName(code, locale).lowercase().contains(q)
    val popular = Currencies.popular.filter(::matches)
    val others = all.filter { it !in Currencies.popular && matches(it) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, contentDescription = "Close") }
                    }
                )
            }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search currency or code") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                )
                LazyColumn(Modifier.fillMaxSize()) {
                    if (popular.isNotEmpty()) {
                        item { SectionLabel("Popular") }
                        items(popular, key = { "p$it" }) { CurrencyRow(it, it == selected, locale) { onPick(it) } }
                    }
                    if (others.isNotEmpty()) {
                        item { SectionLabel("All currencies") }
                        items(others, key = { "a$it" }) { CurrencyRow(it, it == selected, locale) { onPick(it) } }
                    }
                    if (popular.isEmpty() && others.isEmpty()) {
                        item {
                            Text(
                                "No currency matches \"$query\"",
                                modifier = Modifier.padding(24.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
    )
}

@Composable
private fun CurrencyRow(code: String, selected: Boolean, locale: java.util.Locale, onClick: () -> Unit) {
    Column {
        Row(
            Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(code, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(52.dp))
            Text(currencyName(code, locale), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            Text(currencySymbol(code), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (selected) {
                Spacer(Modifier.width(12.dp))
                Icon(Icons.Filled.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
            }
        }
        HorizontalDivider(Modifier.padding(start = 16.dp))
    }
}
