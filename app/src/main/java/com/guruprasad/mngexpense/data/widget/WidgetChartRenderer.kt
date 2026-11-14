package com.guruprasad.mngexpense.data.widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.createBitmap
import com.guruprasad.mngexpense.domain.Category
import com.guruprasad.mngexpense.ui.common.color
import kotlin.math.min

/**
 * Widgets can't run Compose Canvas, so the charts are drawn into bitmaps
 * with android.graphics and shown as images in the Glance layout.
 */
object WidgetChartRenderer {

    /** Animated-free donut chart, same look as the in-app one. [size] is in pixels. */
    fun donut(totals: List<Pair<Category, Double>>, size: Int, density: Float): Bitmap {
        val bmp = createBitmap(size, size)
        val total = totals.sumOf { it.second }
        if (total <= 0) return bmp
        val canvas = Canvas(bmp)
        val stroke = size * 0.16f
        val pad = stroke / 2 + 1 * density
        val oval = RectF(pad, pad, size - pad, size - pad)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = stroke
        }
        val gap = if (totals.size > 1) 2f else 0f
        var start = -90f
        totals.forEach { (category, amount) ->
            val sweep = (amount / total * 360).toFloat()
            if (sweep > gap) {
                paint.color = category.color.toArgb()
                canvas.drawArc(oval, start + gap / 2, sweep - gap, false, paint)
            }
            start += sweep
        }
        return bmp
    }

    /**
     * Vertical bars only: one per category, equal-width slots across [width].
     * Values and labels are NOT drawn here: they're Glance Text views laid out in the
     * same slots, so they follow light/dark mode (a bitmap can't change colour with the theme).
     */
    fun bars(totals: List<Pair<Category, Double>>, width: Int, height: Int, density: Float): Bitmap {
        val bmp = createBitmap(width, height)
        if (totals.isEmpty()) return bmp
        val canvas = Canvas(bmp)
        val bar = Paint(Paint.ANTI_ALIAS_FLAG)
        val axis = Paint().apply { color = 0x55888888; strokeWidth = 1 * density }

        val bottom = height - 1 * density
        val max = totals.maxOf { it.second }.coerceAtLeast(1.0)
        val slot = width.toFloat() / totals.size
        val barW = min(slot * 0.55f, 34 * density)
        val radius = 5 * density

        totals.forEachIndexed { i, (category, amount) ->
            val cx = slot * i + slot / 2
            val h = (bottom * (amount / max)).toFloat().coerceAtLeast(2 * density)
            val top = bottom - h
            bar.color = category.color.toArgb()
            canvas.drawRoundRect(RectF(cx - barW / 2, top, cx + barW / 2, bottom), radius, radius, bar)
            // square off the bottom corners so bars sit flat on the axis
            canvas.drawRect(cx - barW / 2, bottom - minOf(radius, h), cx + barW / 2, bottom, bar)
        }
        canvas.drawLine(0f, bottom, width.toFloat(), bottom, axis)
        return bmp
    }
}
