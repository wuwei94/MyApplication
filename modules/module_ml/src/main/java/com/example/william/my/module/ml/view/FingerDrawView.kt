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
 * 4. 导出时整图拉伸到 28×28，与官方样例的预处理完全一致
 */
class FingerDrawView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val density = resources.displayMetrics.density

    /** 笔迹粗细：以 dp 为基准换算成像素，保证不同 dpi 设备上视觉粗细一致 */
    private val strokeWidthPx = 18f * density

    /** 相邻触摸点位移超过该像素阈值才追加一段，避免高频抖动点淹没路径 */
    private val minSampleDistancePx = 2f * density

    private val drawPath = Path()
    private val drawPaint = Paint().apply {
        color = Color.WHITE
        isAntiAlias = true
        strokeWidth = strokeWidthPx
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
                if (dx >= minSampleDistancePx || dy >= minSampleDistancePx) {
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
                // 自绘 View 需自行上报点击语义，否则无障碍服务无法感知这次触摸
                performClick()
                onStrokeFinishedListener?.invoke()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    fun clear() {
        drawPath.reset()
        drawCanvas?.drawColor(Color.BLACK)
        isCanvasEmpty = true
        invalidate()
    }

    fun isCanvasEmpty(): Boolean = isCanvasEmpty

    /**
     * 导出 [targetSize]×[targetSize] 的位图供模型推理
     *
     * 整图直接拉伸，等价于官方样例的 `Bitmap.createScaledBitmap(bitmap, 28, 28, true)`，
     * 预处理与官方 sample 保持一致。
     *
     * 返回的必定是**新对象**：画布位图由 `Bitmap.createBitmap(w, h, config)` 创建，
     * 始终可变，因此 `createScaledBitmap` 不会原样返回画布本体，调用方可以安全回收。
     * 画布尚未就绪时返回 null。
     */
    fun exportScaledBitmap(targetSize: Int = SCALED_SIZE): Bitmap? {
        val src = canvasBitmap ?: return null
        if (src.isRecycled) return null
        return Bitmap.createScaledBitmap(src, targetSize, targetSize, true)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        canvasBitmap?.recycle()
        canvasBitmap = null
        drawCanvas = null
    }

    private companion object {
        /** MNIST 模型输入边长 */
        const val SCALED_SIZE = 28
    }
}
