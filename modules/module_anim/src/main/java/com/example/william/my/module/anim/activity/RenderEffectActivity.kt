package com.example.william.my.module.anim.activity

import android.graphics.Bitmap
import android.graphics.HardwareRenderer
import android.graphics.PixelFormat
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.Shader
import android.hardware.HardwareBuffer
import android.media.ImageReader
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.R
import com.example.william.my.basic.basic_shared.activity.BasicImageActivity
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import kotlin.math.max
import kotlin.math.min

/**
 * RenderEffect 模糊 — Android 12+（API 31）
 *
 * 两种方案：
 * 1. View.setRenderEffect() — 直接对 View 应用模糊，代码最简
 * 2. HardwareRenderer 离屏渲染 — 输出为 Bitmap，可保存/分享
 *
 * RenderEffect.createBlurEffect(radius, radius, TileMode)
 * - radius: 模糊半径，范围 0.1 ~ 25.0
 * - TileMode: CLAMP（边缘自然过渡，推荐）/ REPEAT / MIRROR
 */
@Route(path = RouterPath.Anim.RenderEffect)
class RenderEffectActivity : BasicImageActivity() {

    private lateinit var originalBitmap: Bitmap
    private var isBlurred = false
    private var currentMethod = ""

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        val drawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_launcher, null)!!
        originalBitmap = drawable.toBitmap()
        showImage(originalBitmap)
    }

    override fun buildList(): ArrayList<String> {
        return arrayListOf(
            "View.setRenderEffect（推荐）",
            "HardwareRenderer 离屏渲染"
        )
    }

    override fun onRecyclerClick(position: Int, string: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            Toast.makeText(this, "需要 Android 12+（API 31）", Toast.LENGTH_SHORT).show()
            return
        }

        when (position) {
            0 -> {
                if (isBlurred && currentMethod == "view") {
                    clearBlur()
                } else {
                    applyViewBlur()
                }
            }
            1 -> {
                if (isBlurred && currentMethod == "bitmap") {
                    clearBlur()
                } else {
                    applyBitmapBlur()
                }
            }
        }
    }

    /**
     * 方案一：View.setRenderEffect()
     * 直接对 View 设置 RenderEffect，GPU 加速，代码最简
     */
    @RequiresApi(Build.VERSION_CODES.S)
    private fun applyViewBlur() {
        mBinding.basicsImage.setRenderEffect(
            RenderEffect.createBlurEffect(20f, 20f, Shader.TileMode.CLAMP)
        )
        isBlurred = true
        currentMethod = "view"
    }

    /**
     * 方案二：HardwareRenderer 离屏渲染
     * 将 Bitmap 渲染到 RenderNode，应用模糊，输出为新 Bitmap
     * 适合需要保存/分享模糊图的场景
     */
    @RequiresApi(Build.VERSION_CODES.S)
    private fun applyBitmapBlur() {
        val blurred = blurWithHardwareRenderer(originalBitmap, 20f)
        showImage(blurred)
        isBlurred = true
        currentMethod = "bitmap"
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun clearBlur() {
        mBinding.basicsImage.setRenderEffect(null)
        showImage(originalBitmap)
        isBlurred = false
        currentMethod = ""
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun blurWithHardwareRenderer(bitmap: Bitmap, radius: Float): Bitmap {
        val clampedRadius = max(0.1f, min(25.0f, radius))

        var imageReader: ImageReader? = null
        var hardwareRenderer: HardwareRenderer? = null

        try {
            imageReader = ImageReader.newInstance(
                bitmap.width, bitmap.height,
                PixelFormat.RGBA_8888, 1,
                HardwareBuffer.USAGE_GPU_SAMPLED_IMAGE or HardwareBuffer.USAGE_GPU_COLOR_OUTPUT
            )

            val renderNode = RenderNode("BlurEffect").apply {
                setPosition(0, 0, imageReader.width, imageReader.height)
                setRenderEffect(
                    RenderEffect.createBlurEffect(clampedRadius, clampedRadius, Shader.TileMode.CLAMP)
                )
            }

            hardwareRenderer = HardwareRenderer().apply {
                setSurface(imageReader.surface)
                setContentRoot(renderNode)
            }

            val canvas = renderNode.beginRecording()
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            renderNode.endRecording()

            hardwareRenderer.createRenderRequest().setWaitForPresent(true).syncAndDraw()

            val image = imageReader.acquireNextImage() ?: throw RuntimeException("No Image")
            val hwBuffer = image.hardwareBuffer ?: throw RuntimeException("No HardwareBuffer")
            val result = Bitmap.wrapHardwareBuffer(hwBuffer, null) ?: throw RuntimeException("Failed")
            image.close()

            return result
        } finally {
            hardwareRenderer?.destroy()
            imageReader?.close()
        }
    }
}
