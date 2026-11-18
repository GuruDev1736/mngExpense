package com.guruprasad.mngexpense.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.guruprasad.mngexpense.domain.Expense
import com.guruprasad.mngexpense.util.AppCurrency
import com.guruprasad.mngexpense.util.money
import java.time.format.DateTimeFormatter

/**
 * One expense in a list: colour dot, title (+ 🔁 when added by a recurring rule),
 * "Category · date" and amount. [highlight] words are marked in the title and note,
 * and the note is shown when a search word was found there.
 */
@Composable
fun ExpenseListItem(
    e: Expense,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDate: Boolean = true,
    highlight: List<String> = emptyList()
) {
    val mark = SpanStyle(
        fontWeight = FontWeight.Bold,
        background = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
    )
    val noteMatches = highlight.isNotEmpty() && e.note.isNotBlank() &&
        highlight.any { it in e.note.lowercase() }
    Row(
        modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(e.category.color))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    highlighted(e.title, highlight, mark), style = MaterialTheme.typography.bodyMedium, maxLines = 1,
                    overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false)
                )
                if (e.recurringId != null) {
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        Icons.Filled.EventRepeat, contentDescription = "Recurring",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                if (showDate) "${e.category.label} · ${e.date.format(DateTimeFormatter.ofPattern("d MMM"))}"
                else e.category.label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (noteMatches) {
                Text(
                    highlighted(e.note, highlight, mark),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(money(e.amount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            // Paid in another currency: show what was actually paid
            if (e.isForeign && e.currency != AppCurrency.code) {
                Text(
                    money(e.originalAmount!!, e.currency!!),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** [text] with every case-insensitive occurrence of [terms] styled with [style]. */
fun highlighted(text: String, terms: List<String>, style: SpanStyle): AnnotatedString {
    if (terms.isEmpty()) return AnnotatedString(text)
    val lower = text.lowercase()
    // Mark matched characters, then merge into runs (handles overlapping terms)
    val marked = BooleanArray(text.length)
    for (term in terms) {
        var i = lower.indexOf(term)
        while (i >= 0) {
            for (k in i until minOf(i + term.length, text.length)) marked[k] = true
            i = lower.indexOf(term, i + 1)
        }
    }
    return buildAnnotatedString {
        append(text)
        var start = -1
        for (k in 0..text.length) {
            val on = k < text.length && marked[k]
            if (on && start < 0) start = k
            if (!on && start >= 0) {
                addStyle(style, start, k)
                start = -1
            }
        }
    }
}
