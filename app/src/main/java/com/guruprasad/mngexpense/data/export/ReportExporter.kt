package com.guruprasad.mngexpense.data.export

import com.guruprasad.mngexpense.util.AppCurrency
import android.content.Context
import android.net.Uri
import com.guruprasad.mngexpense.data.repository.ExpenseRepository
import com.guruprasad.mngexpense.domain.Period
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.IOException

enum class ExportFormat(val mimeType: String, val extension: String) {
    EXCEL("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx"),
    PDF("application/pdf", "pdf")
}

class ReportExporter(
    private val context: Context,
    private val repository: ExpenseRepository
) {
    /** Suggested file name, e.g. "Expenses_November_2026.pdf". */
    fun fileName(period: Period, format: ExportFormat): String =
        "Expenses_" + period.label().replace(Regex("[^A-Za-z0-9]+"), "_").trim('_') + "." + format.extension

    /** Writes the report for [period] to [uri] (from the system "Save as" picker). */
    suspend fun export(period: Period, format: ExportFormat, uri: Uri) = withContext(Dispatchers.IO) {
        val expenses = repository.observeBetween(period.start, period.endExclusive).first()
            .sortedWith(compareBy({ it.date }, { it.id }))
        val report = ExpenseReport("Expense report", period.label(), expenses, AppCurrency.code)
        val out = context.contentResolver.openOutputStream(uri, "wt")
            ?: throw IOException("Could not open $uri")
        out.use {
            when (format) {
                ExportFormat.EXCEL -> XlsxWriter.write(report, it)
                ExportFormat.PDF -> PdfWriter.write(report, it)
            }
        }
    }
}
