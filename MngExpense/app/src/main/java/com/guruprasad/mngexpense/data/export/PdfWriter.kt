package com.guruprasad.mngexpense.data.export

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.TextPaint
import android.text.TextUtils
import com.guruprasad.mngexpense.util.rupees
import java.io.OutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** Renders an [ExpenseReport] as a multi-page A4 PDF using Android's built-in PdfDocument. */
object PdfWriter {
    private const val PAGE_W = 595 // A4 in PostScript points
    private const val PAGE_H = 842
    private const val MARGIN = 40f
    private const val ROW_H = 20f

    private val dateFmt = DateTimeFormatter.ofPattern("dd MMM yyyy")

    fun write(report: ExpenseReport, out: OutputStream) {
        val doc = PdfDocument()
        val title = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 20f; typeface = Typeface.DEFAULT_BOLD }
        val sub = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 11f; color = Color.DKGRAY }
        val body = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 10f }
        val bold = TextPaint(body).apply { typeface = Typeface.DEFAULT_BOLD }
        val line = Paint().apply { color = Color.LTGRAY; strokeWidth = 0.5f }

        // Column x positions: Date | Title | Category | Amount (right aligned)
        val xDate = MARGIN
        val xTitle = MARGIN + 80
        val xCat = MARGIN + 300
        val xAmountRight = PAGE_W - MARGIN

        var pageNo = 0
        lateinit var page: PdfDocument.Page
        var y = 0f

        fun newPage() {
            if (pageNo > 0) doc.finishPage(page)
            pageNo++
            page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, pageNo).create())
            y = MARGIN
            page.canvas.drawText("Page $pageNo", PAGE_W - MARGIN - 40, PAGE_H - 20f, sub)
        }

        fun ensureSpace(h: Float) { if (y + h > PAGE_H - MARGIN) newPage() }

        fun tableHeader() {
            val c = page.canvas
            c.drawText("Date", xDate, y, bold)
            c.drawText("Title", xTitle, y, bold)
            c.drawText("Category", xCat, y, bold)
            bold.textAlign = Paint.Align.RIGHT
            c.drawText("Amount", xAmountRight, y, bold)
            bold.textAlign = Paint.Align.LEFT
            c.drawLine(MARGIN, y + 6, PAGE_W - MARGIN, y + 6, line)
            y += ROW_H
        }

        newPage()
        val c0 = page.canvas
        y += 10
        c0.drawText(report.title, MARGIN, y, title); y += 20
        c0.drawText(report.periodLabel, MARGIN, y, sub); y += 14
        c0.drawText("Generated ${LocalDate.now().format(dateFmt)}", MARGIN, y, sub); y += 28

        // ---- Summary ----
        c0.drawText("Total spent: ${rupees(report.total)}   ·   ${report.expenses.size} transactions", MARGIN, y, bold)
        y += ROW_H
        report.byCategory.forEach { (cat, amount) ->
            ensureSpace(ROW_H)
            val pct = if (report.total > 0) (amount / report.total * 100).toInt() else 0
            page.canvas.drawText("${cat.label}  ($pct%)", MARGIN + 10, y, body)
            body.textAlign = Paint.Align.RIGHT
            page.canvas.drawText(rupees(amount), MARGIN + 260, y, body)
            body.textAlign = Paint.Align.LEFT
            y += 16
        }
        y += 20

        // ---- Table ----
        ensureSpace(ROW_H * 3)
        tableHeader()
        report.expenses.forEach { e ->
            if (y + ROW_H > PAGE_H - MARGIN) { newPage(); tableHeader() }
            val c = page.canvas
            c.drawText(e.date.format(dateFmt), xDate, y, body)
            val t = TextUtils.ellipsize(e.title, body, xCat - xTitle - 10, TextUtils.TruncateAt.END).toString()
            c.drawText(t, xTitle, y, body)
            c.drawText(e.category.label, xCat, y, body)
            body.textAlign = Paint.Align.RIGHT
            c.drawText(rupees(e.amount), xAmountRight, y, body)
            body.textAlign = Paint.Align.LEFT
            y += ROW_H
        }
        ensureSpace(ROW_H)
        page.canvas.drawLine(MARGIN, y - 12, PAGE_W - MARGIN, y - 12, line)
        page.canvas.drawText("Total", xCat, y, bold)
        bold.textAlign = Paint.Align.RIGHT
        page.canvas.drawText(rupees(report.total), xAmountRight, y, bold)

        doc.finishPage(page)
        try {
            doc.writeTo(out)
        } finally {
            doc.close()
        }
    }
}
