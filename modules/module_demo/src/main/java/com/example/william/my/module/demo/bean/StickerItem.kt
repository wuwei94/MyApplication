package com.example.william.my.module.demo.bean

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.graphics.withMatrix

/**
 * 贴纸与文字模型
 */
data class StickerItem(
    val bitmap: Bitmap?, // 如果是图片贴纸
    var text: String?,   // 如果是文字贴纸
    val isText: Boolean,
    var matrix: Matrix = Matrix(),
    var rect: RectF = RectF(),
    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
) {
    init {
        val w = bitmap?.width?.toFloat() ?: 300f
        val h = bitmap?.height?.toFloat() ?: 100f
        rect = RectF(0f, 0f, w, h)

        if (isText) {
            paint.color = Color.WHITE
            paint.textSize = 60f
            paint.style = Paint.Style.FILL
            paint.setShadowLayer(5f, 2f, 2f, Color.BLACK) // 文字阴影，增加可读性
        }
    }

    fun draw(canvas: Canvas) {
        canvas.withMatrix(matrix) {
            if (isText && text != null) {
                // 简单的文字居中绘制逻辑
                val fontMetrics = paint.fontMetrics
                val yOffset = (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent
                drawText(text!!, 0f, rect.height() / 2 + yOffset, paint)

                // 绘制文字边框（可选，表示选中状态）
                // canvas.drawRect(rect, Paint().apply { style = Paint.Style.STROKE; color = Color.RED })
            } else if (bitmap != null) {
                drawBitmap(bitmap, null, rect, null)
            }

        }
    }

    // 判断触摸点是否在贴纸内
    fun contains(x: Float, y: Float): Boolean {
        // 将触摸点通过 Matrix 的逆矩阵映射回贴纸的原始坐标系
        val inverse = Matrix()
        matrix.invert(inverse)
        val dst = floatArrayOf(x, y)
        inverse.mapPoints(dst)
        return rect.contains(dst[0], dst[1])
    }
}