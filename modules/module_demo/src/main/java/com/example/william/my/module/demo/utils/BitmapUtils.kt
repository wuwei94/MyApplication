package com.example.william.my.module.demo.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import androidx.core.graphics.createBitmap

/**
 * 图像处理工具类
 */
object BitmapUtils {

    // 2. 调节亮度 (Brightness) 和 对比度 (Contrast)
    // brightness: -255 to 255 (0 is default)
    // contrast: 0.0 to 2.0 (1.0 is default)
    fun adjustBitmap(src: Bitmap, brightness: Float, contrast: Float): Bitmap {
        val dest = createBitmap(src.width, src.height, src.config ?: Bitmap.Config.ARGB_8888)
        val canvas = Canvas(dest)
        val paint = Paint()

        val colorMatrix = ColorMatrix()

        // 调整对比度 (Scale)
        val scaleMatrix = ColorMatrix()
        scaleMatrix.set(floatArrayOf(
            contrast, 0f, 0f, 0f, 0f,
            0f, contrast, 0f, 0f, 0f,
            0f, 0f, contrast, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ))

        // 调整亮度 (Translate)
        val translateMatrix = ColorMatrix()
        translateMatrix.set(floatArrayOf(
            1f, 0f, 0f, 0f, brightness,
            0f, 1f, 0f, 0f, brightness,
            0f, 0f, 1f, 0f, brightness,
            0f, 0f, 0f, 1f, 0f
        ))

        colorMatrix.postConcat(scaleMatrix)
        colorMatrix.postConcat(translateMatrix)

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(src, 0f, 0f, paint)
        return dest
    }

    // 1. 旋转与裁剪
    fun rotate(src: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
    }

    fun crop(src: Bitmap, cropRect: Rect): Bitmap {
        return Bitmap.createBitmap(src, cropRect.left, cropRect.top, cropRect.width(), cropRect.height())
    }

    // 4. 简单的快速模糊算法 (StackBlur 的简化版，用于演示背景虚化)
    // 注意：生产环境建议使用 RenderScript 或 OpenGL
    fun blur(src: Bitmap, radius: Int): Bitmap {
        // 为了性能，先缩小图片进行模糊，再放大
        val scale = 0.4f
        val smallBitmap = Bitmap.createScaledBitmap(src, (src.width * scale).toInt(), (src.height * scale).toInt(), false)

        // 这里为了代码简洁，使用简单的缩放模糊模拟虚化 (实际SDK应引入 FastBlur 算法)
        // 真正的虚化通常需要处理复杂的像素卷积
        // 模拟：极度缩小再放大 = 模糊
        val blurred = Bitmap.createScaledBitmap(smallBitmap, src.width, src.height, true)
        smallBitmap.recycle()
        return blurred
    }
}