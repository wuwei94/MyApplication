package com.example.william.my.core.ninepatch

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.view.View
import com.example.william.my.core.ninepatch.ninepatchchunk.NinePatchChunk
import java.net.URL
import java.util.concurrent.Executors
import kotlin.math.min

object NinePatchHelper {

    /**
     * https://github.com/xesam/InfiniteImageView
     */
    private val mainHandler = Handler(Looper.getMainLooper())
    private val executor = Executors.newSingleThreadExecutor()

    fun ninePatchChunk(context: Context?, view: View, url: String?) {
        val ctx = context?.applicationContext ?: return
        val imageUrl = url ?: return
        executor.execute {
            try {
                val reqWidth = view.measuredWidth.takeIf { it > 0 } ?: 0
                val reqHeight = view.measuredHeight.takeIf { it > 0 } ?: 0
                val bitmap = decodeSampledBitmap(imageUrl, reqWidth, reqHeight) ?: return@execute

                mainHandler.post {
                    if (view.isAttachedToWindow) {
                        val drawable = NinePatchChunk.create9PatchDrawable(ctx, bitmap, imageUrl)
                        view.background = drawable
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 从 URL 下载图片并降采样解码
     * 第一遍读尺寸，第二遍解码像素
     */
    private fun decodeSampledBitmap(imageUrl: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        // 第一遍：只读尺寸，不加载像素
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        URL(imageUrl).openStream().use { BitmapFactory.decodeStream(it, null, options) }

        // 计算降采样率
        if (reqWidth > 0 || reqHeight > 0) {
            options.inSampleSize = calculateInSampleSize(
                options.outWidth, options.outHeight, reqWidth, reqHeight
            )
        }
        options.inJustDecodeBounds = false

        // 第二遍：用降采样率解码
        return URL(imageUrl).openStream().use {
            BitmapFactory.decodeStream(it, null, options)
        }
    }

    /**
     * 计算 inSampleSize
     * 与 Glide 逻辑一致：找到最大的 2 的幂次，使解码尺寸 ≥ 目标尺寸
     */
    private fun calculateInSampleSize(
        width: Int, height: Int,
        reqWidth: Int, reqHeight: Int
    ): Int {
        if (reqWidth == 0 && reqHeight == 0) return 1

        val halfWidth = if (reqWidth > 0) width / reqWidth else width
        val halfHeight = if (reqHeight > 0) height / reqHeight else height

        var inSampleSize = 1
        if (halfHeight > inSampleSize || halfWidth > inSampleSize) {
            val halfCalculated = if (reqHeight > 0 && reqWidth > 0) {
                min(halfWidth, halfHeight)
            } else if (reqHeight > 0) {
                halfHeight
            } else {
                halfWidth
            }
            inSampleSize = 1 shl (floorLog2(halfCalculated))
        }
        return inSampleSize
    }

    private fun floorLog2(value: Int): Int {
        return 31 - Integer.numberOfLeadingZeros(value)
    }
}
