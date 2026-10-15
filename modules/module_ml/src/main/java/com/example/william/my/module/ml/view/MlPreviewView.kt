package com.example.william.my.module.ml.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

/**
 * 端侧模型结果可视化预览 View
 *
 * 1. 以 fitCenter 等比绘制输入位图，位图显示矩形即归一化坐标 [0,1] 的映射基准
 * 2. 支持语义分割半透明色块叠加绘制
 * 3. 仅持有位图引用不做回收，位图生命周期由宿主 Activity 统一管理
 */
class MlPreviewView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private var image: Bitmap? = null
    private var overlay: Bitmap? = null

    private val imageRect = RectF()

    private val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        alpha = 150
    }

    /**
     * 设置待绘制位图并同时清空上一张图遗留的叠加结果
     *
     * 换图时若不清空，旧分割色块会短暂停留在新图上，因此清空动作内联在此，
     * 不再单独暴露清空接口，调用方也无从漏调。
     */
    fun setImage(bitmap: Bitmap) {
        image = bitmap
        overlay = null
        invalidate()
    }

    /**
     * 设置语义分割结果：原图 + 同尺寸的 ARGB 色块掩码，按 fitCenter 对齐后半透明叠加
     */
    fun setSegmentation(bitmap: Bitmap, mask: Bitmap) {
        image = bitmap
        overlay = mask
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bitmap = image ?: return
        computeImageRect(bitmap)
        canvas.drawBitmap(bitmap, null, imageRect, null)
        overlay?.let { canvas.drawBitmap(it, null, imageRect, overlayPaint) }
    }

    /**
     * 计算 fitCenter 下位图的实际显示矩形，作为归一化坐标换算基准
     */
    private fun computeImageRect(bitmap: Bitmap) {
        val viewRatio = width.toFloat() / height
        val imageRatio = bitmap.width.toFloat() / bitmap.height
        val drawWidth: Float
        val drawHeight: Float
        if (imageRatio > viewRatio) {
            drawWidth = width.toFloat()
            drawHeight = drawWidth / imageRatio
        } else {
            drawHeight = height.toFloat()
            drawWidth = drawHeight * imageRatio
        }
        val left = (width - drawWidth) / 2f
        val top = (height - drawHeight) / 2f
        imageRect.set(left, top, left + drawWidth, top + drawHeight)
    }
}
