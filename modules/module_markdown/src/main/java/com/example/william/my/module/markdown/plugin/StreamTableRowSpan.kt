package com.example.william.my.module.markdown.plugin

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.StaticLayout
import android.text.TextPaint
import android.text.style.ReplacementSpan
import io.noties.markwon.ext.tables.TableTheme

/**
 * 高性能流式 GFM 表格行 Span (StreamTableRowSpan)
 *
 * 核心机制：
 * 1. 在 getSize() 首轮测量期即通过 [widthProvider] 获取宿主可用宽度并即时构建 StaticLayout；
 * 2. 测量期精准上报真实的 fm.ascent = -(measuredHeight + 2 * padding)，让 Android 系统在第 1 帧就分配充足行高；
 * 3. draw() 阶段按标准绘制边框与背景，内容严格上下左右留白 cellPadding，彻底杜绝内容压线与行间重叠。
 */
class StreamTableRowSpan(
    private val theme: TableTheme,
    private val cells: List<Cell>,
    private val isHeader: Boolean,
    private val isOdd: Boolean,
    private val widthProvider: () -> Int,
) : ReplacementSpan() {

    class Cell(val alignment: Layout.Alignment, val text: CharSequence)

    private val layouts = ArrayList<Layout>(cells.size)
    private val textPaint = TextPaint()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = Rect()

    private var measuredWidth = 0
    private var measuredHeight = 0

    override fun getSize(
        p: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?,
    ): Int {
        val targetWidth = widthProvider().coerceAtLeast(100)
        ensureLayouts(targetWidth, p)

        if (fm != null) {
            val cellPadding = theme.tableCellPadding()
            val totalHeight = measuredHeight + (cellPadding * 2)
            fm.ascent = -totalHeight
            fm.descent = 0
            fm.top = fm.ascent
            fm.bottom = 0
        }

        return measuredWidth
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
        p: Paint,
    ) {
        val targetWidth = widthProvider().coerceAtLeast(100)
        if (layouts.isEmpty() || measuredWidth != targetWidth) {
            ensureLayouts(targetWidth, p)
        }
        if (layouts.isEmpty()) return

        val size = layouts.size
        val rowHeight = bottom - top
        val cellPadding = theme.tableCellPadding()
        val cellW = measuredWidth / size

        // 1. 绘制整行背景
        if (isHeader) {
            theme.applyTableHeaderRowStyle(paint)
        } else if (isOdd) {
            theme.applyTableOddRowStyle(paint)
        } else {
            theme.applyTableEvenRowStyle(paint)
        }

        if (paint.color != 0) {
            paint.style = Paint.Style.FILL
            rect.set(x.toInt(), top, (x + measuredWidth).toInt(), bottom)
            canvas.drawRect(rect, paint)
        }

        // 2. 绘制表格边框
        paint.set(p)
        theme.applyTableBorderStyle(paint)
        val borderWidth = theme.tableBorderWidth(paint).coerceAtLeast(1)
        paint.strokeWidth = borderWidth.toFloat()
        paint.style = Paint.Style.STROKE

        val halfBorder = borderWidth / 2f
        // 行底边线
        canvas.drawLine(x, bottom - halfBorder, x + measuredWidth, bottom - halfBorder, paint)
        // 表头顶边线
        if (isHeader) {
            canvas.drawLine(x, top + halfBorder, x + measuredWidth, top + halfBorder, paint)
        }
        // 左外边框与右外边框
        canvas.drawLine(x + halfBorder, top.toFloat(), x + halfBorder, bottom.toFloat(), paint)
        canvas.drawLine(x + measuredWidth - halfBorder, top.toFloat(), x + measuredWidth - halfBorder, bottom.toFloat(), paint)

        // 3. 绘制各个单元格内容及列分割竖线
        for (i in 0 until size) {
            val layout = layouts[i]
            val cellLeft = x + (i * cellW)
            val cellRight = if (i == size - 1) x + measuredWidth else cellLeft + cellW

            // 列分割竖线
            if (i > 0) {
                canvas.drawLine(cellLeft, top.toFloat(), cellLeft, bottom.toFloat(), paint)
            }

            // 垂直留白：确保文字上方始终保留 cellPadding，绝不压线
            val verticalOffset = top + cellPadding + ((measuredHeight - layout.height) / 2).coerceAtLeast(0)

            val saveCount = canvas.save()
            try {
                // 裁剪保护：防止超长单词越过单元格边界压线
                canvas.clipRect(
                    cellLeft + halfBorder,
                    top + halfBorder,
                    cellRight - halfBorder,
                    bottom - halfBorder,
                )
                canvas.translate(cellLeft + cellPadding, verticalOffset.toFloat())
                layout.draw(canvas)
            } finally {
                canvas.restoreToCount(saveCount)
            }
        }
    }

    private fun ensureLayouts(targetWidth: Int, p: Paint) {
        measuredWidth = targetWidth
        textPaint.set(p)
        textPaint.isFakeBoldText = isHeader

        val columns = cells.size
        if (columns <= 0) {
            measuredHeight = 0
            layouts.clear()
            return
        }
        val cellPadding2 = theme.tableCellPadding() * 2
        val cellW = ((measuredWidth / columns) - cellPadding2).coerceAtLeast(1)

        layouts.clear()
        var maxH = 0
        for (cell in cells) {
            val spannable = if (cell.text is Spannable) cell.text else SpannableString(cell.text)
            val layout = StaticLayout(
                spannable,
                textPaint,
                cellW,
                cell.alignment,
                1.0f,
                0.0f,
                false,
            )
            layouts.add(layout)
            if (layout.height > maxH) {
                maxH = layout.height
            }
        }
        measuredHeight = maxH
    }
}
