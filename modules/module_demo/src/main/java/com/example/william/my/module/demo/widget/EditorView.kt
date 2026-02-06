package com.example.william.my.module.demo.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.william.my.module.demo.bean.StickerItem
import com.example.william.my.module.demo.utils.BitmapUtils
import kotlin.math.atan2
import kotlin.math.sqrt

class EditorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var sourceBitmap: Bitmap? = null   // 原始图片
    private var displayBitmap: Bitmap? = null  // 当前显示的底图（经过滤镜处理的）

    private val stickers = mutableListOf<StickerItem>()
    private var currentSticker: StickerItem? = null // 当前选中的贴纸

    // 触摸控制变量
    private var lastX = 0f
    private var lastY = 0f
    private var mode = 0 // 0=NONE, 1=DRAG, 2=ZOOM/ROTATE
    private var initialDistance = 1f
    private var initialRotation = 0f

    // 功能开关
    var brightness = 0f
    var contrast = 1f
    var isBlurActive = false

    fun setImage(bitmap: Bitmap) {
        this.sourceBitmap = bitmap
        applyEffects() // 初始化显示
    }

    // --- 核心功能调用接口 ---

    fun addSticker(bitmap: Bitmap) {
        val sticker = StickerItem(bitmap, null, false)
        // 默认放在中心
        sticker.matrix.postTranslate(
            (width - sticker.rect.width()) / 2,
            (height - sticker.rect.height()) / 2
        )
        stickers.add(sticker)
        currentSticker = sticker
        invalidate()
    }

    fun addText(text: String) {
        val sticker = StickerItem(null, text, true)
        sticker.matrix.postTranslate(width / 2f, height / 2f)
        stickers.add(sticker)
        currentSticker = sticker
        invalidate()
    }

    fun applyEffects() {
        sourceBitmap?.let { src ->
            // 1. 处理模糊
            var temp = if (isBlurActive) {
                BitmapUtils.blur(src, 25)
            } else {
                src.copy(src.config ?: Bitmap.Config.ARGB_8888, true)
            }

            // 2. 处理亮度对比度
            if (brightness != 0f || contrast != 1f) {
                temp = BitmapUtils.adjustBitmap(temp, brightness, contrast)
            }

            displayBitmap = temp
            invalidate()
        }
    }

    fun deleteCurrentSticker() {
        currentSticker?.let {
            stickers.remove(it)
            currentSticker = null
            invalidate()
        }
    }

    fun rotateImage90() {
        sourceBitmap?.let {
            sourceBitmap = BitmapUtils.rotate(it, 90f)
            applyEffects()
        }
    }

    // --- 绘制流程 ---

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // 1. 绘制底图
        displayBitmap?.let {
            // 简单居中绘制，实际应用应用 Matrix 进行 FitCenter
            val left = (width - it.width) / 2f
            val top = (height - it.height) / 2f
            canvas.drawBitmap(it, left, top, null)
        }

        // 2. 绘制所有贴纸
        for (sticker in stickers) {
            sticker.draw(canvas)
        }
    }

    // --- 触摸与手势逻辑 (移动/缩放/旋转) ---

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                // 查找点击了哪个贴纸 (倒序查找，优先选中最上面的)
                currentSticker = stickers.findLast { it.contains(event.x, event.y) }
                if (currentSticker != null) {
                    mode = 1 // DRAG
                    lastX = event.x
                    lastY = event.y
                }
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                if (currentSticker != null && event.pointerCount == 2) {
                    mode = 2 // ZOOM & ROTATE
                    initialDistance = calculateDistance(event)
                    initialRotation = calculateRotation(event)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (currentSticker != null) {
                    if (mode == 1) { // 移动
                        val dx = event.x - lastX
                        val dy = event.y - lastY
                        currentSticker!!.matrix.postTranslate(dx, dy)
                        lastX = event.x
                        lastY = event.y
                    } else if (mode == 2 && event.pointerCount == 2) { // 旋转与缩放
                        val newDistance = calculateDistance(event)
                        val newRotation = calculateRotation(event)

                        val scale = newDistance / initialDistance
                        val rotate = newRotation - initialRotation

                        // 获取贴纸中心点
                        val values = FloatArray(9)
                        currentSticker!!.matrix.getValues(values)
                        val globalX = values[Matrix.MTRANS_X] + currentSticker!!.rect.width() / 2
                        val globalY = values[Matrix.MTRANS_Y] + currentSticker!!.rect.height() / 2

                        currentSticker!!.matrix.postScale(scale, scale, globalX, globalY)
                        currentSticker!!.matrix.postRotate(rotate, globalX, globalY)

                        // 更新基准值，防止抖动
                        // 简单的实现可以不重置 initial，这里为了平滑可以不做
                    }
                    invalidate()
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                mode = 0
            }
        }
        return true
    }

    // 计算两指距离
    private fun calculateDistance(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return sqrt(x * x + y * y)
    }

    // 计算两指角度
    private fun calculateRotation(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return Math.toDegrees(atan2(y.toDouble(), x.toDouble())).toFloat()
    }
}