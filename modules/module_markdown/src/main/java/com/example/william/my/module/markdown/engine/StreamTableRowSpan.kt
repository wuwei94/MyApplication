package com.example.william.my.module.markdown.engine

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.StaticLayout
import android.text.TextPaint
import android.text.style.ReplacementSpan
import android.widget.TextView
import io.noties.markwon.ext.tables.TableSpan
import io.noties.markwon.ext.tables.TableTheme
import io.noties.markwon.utils.LeadingMarginUtils
import io.noties.markwon.utils.SpanUtils
import java.lang.ref.WeakReference

/**
 * 专为流式 AI 打字机优化的 TableRowSpan (StreamTableRowSpan)
 *
 * 核心机制：
 * 1. 在 getSize 阶段直接获取宿主 TextView 的可用宽度并构建 StaticLayout；
 * 2. 一次性计算出单元格最大真实高度与内边距 (fm.ascent = -(maxHeight + 2*padding))；
 * 3. 测量期与绘制期高度严格一致，无需异步二次刷新，实现 0 闪烁、0 抖动、文字永不压底线。
 */
class StreamTableRowSpan(
    private val theme: TableTheme,
    private val cells: List<Cell>,
    private val header: Boolean,
    private val odd: Boolean,
    private val textViewRef: WeakReference<TextView>? = null
) : ReplacementSpan() {

    class Cell(val alignment: Layout.Alignment, val text: CharSequence)

    private val layouts = ArrayList<Layout>(cells.size)
    private val textPaint = TextPaint()
    private val rect = Rect()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var measuredWidth = 0
    private var measuredHeight = 0

    override fun getSize(
        p: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        val targetWidth = resolveTargetWidth(p)
        if (targetWidth > 0) {
            ensureLayouts(targetWidth, p)
        }

        if (fm != null && layouts.isNotEmpty()) {
            var max = 0
            for (layout in layouts) {
                val h = layout.height
                if (h > max) {
                    max = h
                }
            }
            measuredHeight = max
            val padding = theme.tableCellPadding() * 2
            val totalHeight = max + padding

            fm.ascent = -totalHeight
            fm.descent = 0
            fm.top = fm.ascent
            fm.bottom = 0
        }

        return measuredWidth.coerceAtLeast(targetWidth)
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        p: Paint
    ) {
        val canvasWidth = if (text != null) SpanUtils.width(canvas, text) else 0
        val targetWidth = if (canvasWidth > 0) canvasWidth else resolveTargetWidth(p)

        if (targetWidth > 0 && (layouts.isEmpty() || measuredWidth != targetWidth)) {
            ensureLayouts(targetWidth, p)
        }

        if (layouts.isEmpty()) return

        val size = layouts.size
        val w = cellWidth(size)
        val roundingDiff = w - (measuredWidth / size)
        val padding = theme.tableCellPadding()

        // 1. 绘制行背景
        if (header) {
            theme.applyTableHeaderRowStyle(paint)
        } else if (odd) {
            theme.applyTableOddRowStyle(paint)
        } else {
            theme.applyTableEvenRowStyle(paint)
        }

        if (paint.color != 0) {
            val save = canvas.save()
            try {
                rect.set(0, 0, measuredWidth, bottom - top)
                canvas.translate(x, top.toFloat())
                canvas.drawRect(rect, paint)
            } finally {
                canvas.restoreToCount(save)
            }
        }

        // 2. 绘制边框
        paint.set(p)
        theme.applyTableBorderStyle(paint)
        val borderWidth = theme.tableBorderWidth(paint)
        val drawBorder = borderWidth > 0

        val rowHeight = bottom - top
        val heightDiff = ((rowHeight - measuredHeight - padding * 2) / 2).coerceAtLeast(0)

        val isFirstTableRow: Boolean
        if (drawBorder && text is android.text.Spanned) {
            val spans = text.getSpans(start, end, TableSpan::class.java)
            val first = spans != null && spans.isNotEmpty() && LeadingMarginUtils.selfStart(start, text, spans[0])
            if (first) {
                rect.set(x.toInt(), top, measuredWidth, top + borderWidth)
                canvas.drawRect(rect, paint)
            }
            rect.set(x.toInt(), bottom - borderWidth, measuredWidth, bottom)
            canvas.drawRect(rect, paint)
            isFirstTableRow = first
        } else {
            if (drawBorder) {
                rect.set(x.toInt(), bottom - borderWidth, measuredWidth, bottom)
                canvas.drawRect(rect, paint)
            }
            isFirstTableRow = false
        }

        val borderWidthHalf = borderWidth / 2
        val borderTop = if (isFirstTableRow) borderWidth else 0
        val borderBottom = bottom - top - borderWidth

        // 3. 绘制每个单元格内容
        for (i in 0 until size) {
            val layout = layouts[i]
            val save = canvas.save()
            try {
                canvas.translate(x + (i * w), top.toFloat())

                if (drawBorder) {
                    if (i == 0) {
                        rect.set(0, borderTop, borderWidth, borderBottom)
                    } else {
                        rect.set(-borderWidthHalf, borderTop, borderWidthHalf, borderBottom)
                    }
                    canvas.drawRect(rect, paint)

                    if (i == size - 1) {
                        rect.set(w - borderWidth - roundingDiff, borderTop, w - roundingDiff, borderBottom)
                        canvas.drawRect(rect, paint)
                    }
                }

                canvas.translate(padding.toFloat(), (padding + heightDiff).toFloat())
                layout.draw(canvas)
            } finally {
                canvas.restoreToCount(save)
            }
        }
    }

    private fun resolveTargetWidth(p: Paint): Int {
        val tv = textViewRef?.get()
        if (tv != null) {
            val availableWidth = tv.width - tv.paddingLeft - tv.paddingRight
            if (availableWidth > 0) {
                return availableWidth
            }
        }
        return measuredWidth
    }

    private fun ensureLayouts(targetWidth: Int, p: Paint) {
        measuredWidth = targetWidth
        if (p is TextPaint) {
            textPaint.set(p)
        } else {
            textPaint.set(p)
        }
        textPaint.isFakeBoldText = header

        val columns = cells.size
        val padding = theme.tableCellPadding() * 2
        val cellW = (measuredWidth / columns) - padding

        layouts.clear()
        for (cell in cells) {
            val spannable = if (cell.text is Spannable) cell.text else SpannableString(cell.text)
            val layout = StaticLayout(
                spannable,
                textPaint,
                cellW.coerceAtLeast(1),
                cell.alignment,
                1.0f,
                0.0f,
                false
            )
            layouts.add(layout)
        }
    }

    private fun cellWidth(size: Int): Int {
        return (1.0f * measuredWidth / size + 0.5f).toInt()
    }
}
