package com.guruprasad.mngexpense.data.export

import java.io.OutputStream
import java.time.LocalDate
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Minimal .xlsx (Office Open XML) writer — no third-party library needed.
 * Writes two sheets: "Expenses" (one row per expense + total) and "Summary" (per category).
 */
object XlsxWriter {

    // Cell style indexes — must match cellXfs in STYLES below
    private const val S_TEXT = 0
    private const val S_BOLD = 1
    private const val S_MONEY = 2
    private const val S_DATE = 3
    private const val S_BOLD_MONEY = 4

    fun write(report: ExpenseReport, out: OutputStream) {
        ZipOutputStream(out).use { zip ->
            fun put(name: String, xml: String) {
                zip.putNextEntry(ZipEntry(name))
                zip.write(xml.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
            put("[Content_Types].xml", CONTENT_TYPES)
            put("_rels/.rels", ROOT_RELS)
            put("xl/workbook.xml", WORKBOOK)
            put("xl/_rels/workbook.xml.rels", WORKBOOK_RELS)
            put("xl/styles.xml", STYLES)
            put("xl/worksheets/sheet1.xml", expensesSheet(report))
            put("xl/worksheets/sheet2.xml", summarySheet(report))
        }
    }

    private fun expensesSheet(r: ExpenseReport): String {
        val rows = StringBuilder()
        var n = 1
        rows.row(n++) { text("A", "${r.title} — ${r.periodLabel}", S_BOLD) }
        n++ // blank row
        val header = n
        rows.row(n++) {
            text("A", "Date", S_BOLD); text("B", "Title", S_BOLD); text("C", "Category", S_BOLD)
            text("D", "Amount (${r.currency})", S_BOLD); text("E", "Note", S_BOLD)
            if (r.hasForeign) { text("F", "Paid in", S_BOLD); text("G", "Amount paid", S_BOLD) }
        }
        r.expenses.forEach { e ->
            rows.row(n++) {
                date("A", e.date); text("B", e.title); text("C", e.category.label)
                number("D", e.amount, S_MONEY); text("E", e.note)
                if (e.isForeign) { text("F", e.currency!!); number("G", e.originalAmount!!, S_MONEY) }
            }
        }
        val last = n - 1
        rows.row(n) {
            text("C", "Total", S_BOLD)
            formula("D", if (last > header) "SUM(D${header + 1}:D$last)" else "0", r.total, S_BOLD_MONEY)
        }
        val widths = listOf(14.0, 32.0, 16.0, 14.0, 40.0) + if (r.hasForeign) listOf(10.0, 14.0) else emptyList()
        return sheet(rows, colWidths = widths)
    }

    private fun summarySheet(r: ExpenseReport): String {
        val rows = StringBuilder()
        var n = 1
        rows.row(n++) { text("A", "Category", S_BOLD); text("B", "Amount (${r.currency})", S_BOLD); text("C", "Share", S_BOLD) }
        r.byCategory.forEach { (c, amount) ->
            rows.row(n++) {
                text("A", c.label); number("B", amount, S_MONEY)
                text("C", if (r.total > 0) "${(amount / r.total * 100).toInt()}%" else "0%")
            }
        }
        rows.row(n) { text("A", "Total", S_BOLD); number("B", r.total, S_BOLD_MONEY) }
        return sheet(rows, colWidths = listOf(18.0, 14.0, 10.0))
    }

    // ---------------------------------------------------------------- cell helpers

    private class RowBuilder(private val row: Int, private val sb: StringBuilder) {
        fun text(col: String, v: String, style: Int = S_TEXT) {
            if (v.isEmpty()) return
            sb.append("""<c r="$col$row" t="inlineStr" s="$style"><is><t xml:space="preserve">${xml(v)}</t></is></c>""")
        }
        fun number(col: String, v: Double, style: Int) =
            sb.append("""<c r="$col$row" s="$style"><v>${num(v)}</v></c>""")
        fun formula(col: String, f: String, cached: Double, style: Int) =
            sb.append("""<c r="$col$row" s="$style"><f>$f</f><v>${num(cached)}</v></c>""")
        /** Excel stores dates as days since 1899-12-30; 1970-01-01 is day 25569. */
        fun date(col: String, d: LocalDate) =
            sb.append("""<c r="$col$row" s="$S_DATE"><v>${d.toEpochDay() + 25569}</v></c>""")
    }

    private fun StringBuilder.row(n: Int, block: RowBuilder.() -> Unit) {
        append("""<row r="$n">""")
        RowBuilder(n, this).block()
        append("</row>")
    }

    private fun sheet(rows: CharSequence, colWidths: List<Double>): String {
        val cols = colWidths.mapIndexed { i, w ->
            """<col min="${i + 1}" max="${i + 1}" width="$w" customWidth="1"/>"""
        }.joinToString("")
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><cols>$cols</cols><sheetData>$rows</sheetData></worksheet>"""
    }

    private fun num(v: Double) = String.format(Locale.ROOT, "%.2f", v)

    /** Escapes XML special characters and drops characters XML can't contain. */
    internal fun xml(s: String): String = buildString {
        s.forEach { ch ->
            when {
                ch == '&' -> append("&amp;")
                ch == '<' -> append("&lt;")
                ch == '>' -> append("&gt;")
                ch == '"' -> append("&quot;")
                ch == '\t' || ch == '\n' || ch == '\r' || ch >= ' ' -> append(ch)
                else -> Unit // control characters are invalid in XML 1.0
            }
        }
    }

    // ---------------------------------------------------------------- fixed package parts

    private const val CONTENT_TYPES = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/><Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/><Override PartName="/xl/worksheets/sheet2.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/><Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/></Types>"""

    private const val ROOT_RELS = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/></Relationships>"""

    private const val WORKBOOK = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><sheets><sheet name="Expenses" sheetId="1" r:id="rId1"/><sheet name="Summary" sheetId="2" r:id="rId2"/></sheets></workbook>"""

    private const val WORKBOOK_RELS = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/><Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet2.xml"/><Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/></Relationships>"""

    private const val STYLES = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><numFmts count="2"><numFmt numFmtId="164" formatCode="#,##0.00"/><numFmt numFmtId="165" formatCode="dd-mmm-yyyy"/></numFmts><fonts count="2"><font><sz val="11"/><name val="Calibri"/></font><font><b/><sz val="11"/><name val="Calibri"/></font></fonts><fills count="2"><fill><patternFill patternType="none"/></fill><fill><patternFill patternType="gray125"/></fill></fills><borders count="1"><border><left/><right/><top/><bottom/><diagonal/></border></borders><cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs><cellXfs count="5"><xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/><xf numFmtId="0" fontId="1" fillId="0" borderId="0" xfId="0" applyFont="1"/><xf numFmtId="164" fontId="0" fillId="0" borderId="0" xfId="0" applyNumberFormat="1"/><xf numFmtId="165" fontId="0" fillId="0" borderId="0" xfId="0" applyNumberFormat="1"/><xf numFmtId="164" fontId="1" fillId="0" borderId="0" xfId="0" applyNumberFormat="1" applyFont="1"/></cellXfs></styleSheet>"""
}
