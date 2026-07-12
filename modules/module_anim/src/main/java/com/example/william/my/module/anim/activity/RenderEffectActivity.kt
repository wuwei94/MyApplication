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
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import com.alibaba.android.arouter.facade.annotation.Route
import com.example.william.my.basic.basic_shared.R
import com.example.william.my.basic.basic_shared.router.path.RouterPath
import com.example.william.my.lib.activity.BaseVBActivity
import com.example.william.my.module.anim.databinding.AnimActivityRenderEffectBinding
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
class RenderEffectActivity : BaseVBActivity<AnimActivityRenderEffectBinding>() {

    private lateinit var originalBitmap: Bitmap
    private var isBlurred = false
    private var currentMethod = ""

    override fun getViewBinding(): AnimActivityRenderEffectBinding {
        return AnimActivityRenderEffectBinding.inflate(layoutInflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        val drawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_launcher, null)!!
        originalBitmap = drawable.toBitmap()

        mBinding.renderEffectOriginal.setImageBitmap(originalBitmap)

        mBinding.renderEffectViewBlur.setOnClickListener {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                Toast.makeText(this, "需要 Android 12+（API 31）", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (isBlurred && currentMethod == "view") {
                clearBlur()
            } else {
                applyViewBlur()
            }
        }

        mBinding.renderEffectBitmapBlur.setOnClickListener {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                Toast.makeText(this, "需要 Android 12+（API 31）", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (isBlurred && currentMethod == "bitmap") {
                clearBlur()
            } else {
                applyBitmapBlur()
            }
        }
    }

    /**
     * 方案一：View.setRenderEffect()
     * 直接对 View 设置 RenderEffect，GPU 加速，代码最简
     */
    @RequiresApi(Build.VERSION_CODES.S)
    private fun applyViewBlur() {
        mBinding.renderEffectBlur.setImageBitmap(originalBitmap)
        mBinding.renderEffectBlur.setRenderEffect(
            RenderEffect.createBlurEffect(20f, 20f, Shader.TileMode.CLAMP)
        )
        isBlurred = true
        currentMethod = "view"
        mBinding.renderEffectStatus.text = "View.setRenderEffect 模糊已开启"
    }

    /**
     * 方案二：HardwareRenderer 离屏渲染
     * 将 Bitmap 渲染到 RenderNode，应用模糊，输出为新 Bitmap
     * 适合需要保存/分享模糊图的场景
     */
    @RequiresApi(Build.VERSION_CODES.S)
    private fun applyBitmapBlur() {
        val blurred = blurWithHardwareRenderer(originalBitmap, 20f)
        mBinding.renderEffectBlur.setImageBitmap(blurred)
        isBlurred = true
        currentMethod = "bitmap"
        mBinding.renderEffectStatus.text = "HardwareRenderer 模糊已开启"
    }

    private fun clearBlur() {
        mBinding.renderEffectBlur.setRenderEffect(null)
        mBinding.renderEffectBlur.setImageBitmap(null)
        isBlurred = false
        currentMethod = ""
        mBinding.renderEffectStatus.text = "模糊已关闭"
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun blurWithHardwareRenderer(bitmap: Bitmap, radius: Float): Bitmap {
        val clampedRadius = max(0.1f, min(25.0f, radius))

        val imageReader = ImageReader.newInstance(
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

        val hardwareRenderer = HardwareRenderer().apply {
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
    }
}
