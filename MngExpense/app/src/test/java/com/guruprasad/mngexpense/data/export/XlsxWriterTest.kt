package com.guruprasad.mngexpense.data.export

import com.guruprasad.mngexpense.domain.Category
import com.guruprasad.mngexpense.domain.Expense
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory

class XlsxWriterTest {

    private val report = ExpenseReport(
        "Expense report", "November 2026",
        listOf(
            Expense(1, "Tea & <snacks> \"x\"", 120.5, Category.FOOD, LocalDate.of(2026, 11, 1), "note\u0001"),
            Expense(2, "Bus", 40.0, Category.TRANSPORT, LocalDate.of(2026, 11, 2))
        )
    )

    private fun unzip(bytes: ByteArray): Map<String, String> {
        val out = mutableMapOf<String, String>()
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            while (true) {
                val e = zip.nextEntry ?: break
                out[e.name] = zip.readBytes().toString(Charsets.UTF_8)
            }
        }
        return out
    }

    @Test fun producesAllPartsAsWellFormedXml() {
        val buf = ByteArrayOutputStream()
        XlsxWriter.write(report, buf)
        val parts = unzip(buf.toByteArray())

        assertEquals(
            setOf(
                "[Content_Types].xml", "_rels/.rels", "xl/workbook.xml", "xl/_rels/workbook.xml.rels",
                "xl/styles.xml", "xl/worksheets/sheet1.xml", "xl/worksheets/sheet2.xml"
            ),
            parts.keys
        )
        val parser = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        parts.values.forEach { parser.parse(ByteArrayInputStream(it.toByteArray())) } // throws if malformed

        val sheet1 = parts.getValue("xl/worksheets/sheet1.xml")
        assertTrue(sheet1.contains("Tea &amp; &lt;snacks&gt; &quot;x&quot;"))
        assertTrue(sheet1.contains("<v>46327</v>"))          // 1 Nov 2026 as an Excel date serial
        assertTrue(sheet1.contains("<f>SUM(D4:D5)</f><v>160.50</v>"))
    }
}
