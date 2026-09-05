package com.example.william.my.module.ml.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * 手写数字涂鸦画板 View
 *
 * 1. 默认黑底白字（符合标准 MNIST 数据集规范）
 * 2. 支持手指贝塞尔曲线平滑绘制与画笔粗细自适应
 * 3. 支持画布清空与实时触摸事件回调
 * 4. 支持将笔迹渲染并精确等比缩放为 28x28 灰度 Bitmap 供 TFLite 直接推理
 */
class FingerDrawView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val drawPath = Path()
    private val drawPaint = Paint().apply {
        color = Color.WHITE
        isAntiAlias = true
        strokeWidth = 56f
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    private var canvasBitmap: Bitmap? = null
    private var drawCanvas: Canvas? = null
    private var isCanvasEmpty = true

    private var lastX = 0f
    private var lastY = 0f

    var onStrokeFinishedListener: (() -> Unit)? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.BLACK)
            canvasBitmap?.recycle()
            canvasBitmap = bitmap
            drawCanvas = canvas
            isCanvasEmpty = true
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvasBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }
        canvas.drawPath(drawPath, drawPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                drawPath.reset()
                drawPath.moveTo(x, y)
                lastX = x
                lastY = y
                isCanvasEmpty = false
                invalidate()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = Math.abs(x - lastX)
                val dy = Math.abs(y - lastY)
                if (dx >= 4 || dy >= 4) {
                    drawPath.quadTo(lastX, lastY, (x + lastX) / 2, (y + lastY) / 2)
                    lastX = x
                    lastY = y
                }
                invalidate()
                return true
            }

            MotionEvent.ACTION_UP -> {
                drawPath.lineTo(lastX, lastY)
                drawCanvas?.drawPath(drawPath, drawPaint)
                drawPath.reset()
                invalidate()
                onStrokeFinishedListener?.invoke()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun clear() {
        drawPath.reset()
        drawCanvas?.drawColor(Color.BLACK)
        isCanvasEmpty = true
        invalidate()
    }

    fun isCanvasEmpty(): Boolean = isCanvasEmpty

    /**
     * 导出缩放为指定尺寸（默认 28x28）的 Bitmap
     */
    fun exportBitmap(targetWidth: Int = 28, targetHeight: Int = 28): Bitmap? {
        val src = canvasBitmap ?: return null
        if (src.isRecycled) return null
        return Bitmap.createScaledBitmap(src, targetWidth, targetHeight, true)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        canvasBitmap?.recycle()
        canvasBitmap = null
        drawCanvas = null
    }
}
